package com.itam.service.importer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.transaction.annotation.Transactional;

import com.itam.model.dto.ImportReport;
import com.itam.model.dto.ImportRowError;
import com.itam.repository.DepartmentRepository;
import com.itam.repository.PersonRepository;
import com.itam.service.export.AssetColumn;
import com.itam.service.importer.AssetImportService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Dosya okuma, başlık eşleme, doğrulama ve tekillik kontrolü bütün tipler için burada.
 * Bir tip yalnızca kolon kataloğunu (export enum'u), zorunlu kolonları ve satırın
 * oluşturma isteğine nasıl çevrileceğini veriyor.
 */
abstract class AbstractAssetImportService<C extends Enum<C> & AssetColumn<?>, R> implements AssetImportService<R> {

    static final int MAX_ROWS = 5000;
    /** Export dosyasının üstünde başlık bloğu var; başlık satırını ilk satırlarda arıyoruz. */
    private static final int HEADER_SEARCH_ROWS = 20;
    /** Export'ta olan ama elle girilmeyen kolonlar; dosyada bulunursa sessizce atlanır. */
    private static final Set<String> IGNORED_CODES = Set.of("id", "status", "createdAt", "updatedAt");

    /**
     * Dosyada ve veritabanında tekrar edemeyen kolon (sunucuda hostname). {@code label}
     * dosya içi çakışma mesajında görünüyor, {@code existsMessage} veritabanında varsa.
     */
    protected record UniqueKey<K>(K column, String label, Function<String, String> normalize,
                                  Supplier<Collection<String>> existing, String existsMessage) {
    }

    private final Class<C> columnType;
    private final Validator validator;
    private final PersonRepository personRepository;
    private final DepartmentRepository departmentRepository;

    protected AbstractAssetImportService(Class<C> columnType, Validator validator,
                                         PersonRepository personRepository, DepartmentRepository departmentRepository) {
        this.columnType = columnType;
        this.validator = validator;
        this.personRepository = personRepository;
        this.departmentRepository = departmentRepository;
    }

    /** Başlık satırını tanımak için de kullanılıyor; dosyada hepsi olmalı. */
    protected abstract Set<C> requiredColumns();

    protected abstract String sheetName();

    /** Şablonun ikinci satırında Ad kolonuna yazılan örnek. */
    protected abstract String exampleName();

    protected abstract R toRequest(ImportRow<C> row);

    protected abstract void create(R request);

    protected UniqueKey<C> uniqueKey() {
        return null;
    }

    @Override
    @Transactional
    public ImportReport importXlsx(InputStream xlsx, boolean dryRun) {
        List<ImportRowError> errors = new ArrayList<>();
        List<R> requests = new ArrayList<>();
        int total;
        try (Workbook workbook = WorkbookFactory.create(xlsx)) {
            total = parse(workbook.getSheetAt(0), requests, errors);
        } catch (IOException | RuntimeException e) {
            // POI bozuk/şifreli/xlsx olmayan dosyada çeşitli RuntimeException'lar fırlatıyor;
            // bu aşamada henüz veritabanına bir şey yazılmadı
            errors.add(new ImportRowError(1, null, "Dosya okunamadı; .xlsx biçiminde bir Excel dosyası yükleyin"));
            return new ImportReport(dryRun, 0, 0, 0, errors);
        }

        if (dryRun || !errors.isEmpty()) {
            return new ImportReport(dryRun, total, requests.size(), 0, errors);
        }
        // Tek transaction: bir kayıt beklenmedik biçimde reddedilirse hepsi geri alınıyor
        requests.forEach(this::create);
        return new ImportReport(false, total, requests.size(), requests.size(), List.of());
    }

    /** Satırları isteklere çevirir; hataları toplar, dolu satır sayısını döner. */
    private int parse(Sheet sheet, List<R> requests, List<ImportRowError> errors) {
        DataFormatter formatter = new DataFormatter(Locale.ROOT);
        int headerIndex = findHeaderRow(sheet, formatter);
        if (headerIndex < 0) {
            errors.add(new ImportRowError(1, null, "Başlık satırı bulunamadı; " + requiredText()));
            return 0;
        }
        Map<C, Integer> columns = mapColumns(sheet.getRow(headerIndex), formatter, errors);
        if (!errors.isEmpty()) {
            return 0;
        }

        ImportLookups lookups = new ImportLookups(personRepository::findAll, departmentRepository::findAll);
        UniqueKey<C> unique = uniqueKey();
        Set<String> existing = unique == null ? Set.of() : new HashSet<>(unique.existing().get());
        Map<String, Integer> seenInFile = new HashMap<>();

        int total = 0;
        for (int r = headerIndex + 1; r <= sheet.getLastRowNum(); r++) {
            ImportRow<C> row = read(sheet.getRow(r), r + 1, columns, formatter, lookups, errors);
            if (row.isEmpty()) {
                continue;   // boş satırlar (Excel sonda bırakabiliyor)
            }
            if (++total > MAX_ROWS) {
                errors.add(new ImportRowError(row.number(), null,
                        "Tek seferde en fazla " + MAX_ROWS + " satır içe aktarılabilir"));
                break;
            }
            int before = errors.size();
            R request = toRequest(row);
            for (ConstraintViolation<R> v : validator.validate(request)) {
                errors.add(new ImportRowError(row.number(), headerOf(v.getPropertyPath().toString()), v.getMessage()));
            }
            if (unique != null) {
                checkUnique(row, unique, existing, seenInFile);
            }
            if (errors.size() == before) {
                requests.add(request);
            }
        }
        return total;
    }

    private static <C extends Enum<C> & AssetColumn<?>> void checkUnique(ImportRow<C> row, UniqueKey<C> unique,
                                                                       Set<String> existing,
                                                                       Map<String, Integer> seenInFile) {
        String value = row.text(unique.column());
        if (value == null) {
            return;
        }
        String key = unique.normalize().apply(value);
        if (existing.contains(key)) {
            row.error(unique.column(), unique.existsMessage());
        }
        Integer firstRow = seenInFile.putIfAbsent(key, row.number());
        if (firstRow != null) {
            row.error(unique.column(), "Aynı " + unique.label() + " dosyada " + firstRow + ". satırda da var");
        }
    }

    @Override
    public void writeTemplate(OutputStream out) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName());
            Row header = sheet.createRow(0);
            int i = 0;
            for (C column : columnType.getEnumConstants()) {
                if (!isIgnored(column)) {
                    header.createCell(i).setCellValue(column.header());
                    sheet.setColumnWidth(i++, 20 * 256);
                }
            }
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("Örnek: " + exampleName());
            workbook.write(out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static boolean isIgnored(AssetColumn<?> column) {
        return IGNORED_CODES.contains(column.code());
    }

    /** "en az Ad, Hostname ve Ortam kolonları olmalı" */
    private String requiredText() {
        List<String> headers = requiredColumns().stream().map(AssetColumn::header).toList();
        if (headers.size() == 1) {
            return "en az " + headers.get(0) + " kolonu olmalı";
        }
        return "en az " + String.join(", ", headers.subList(0, headers.size() - 1))
                + " ve " + headers.get(headers.size() - 1) + " kolonları olmalı";
    }

    private int findHeaderRow(Sheet sheet, DataFormatter formatter) {
        int last = Math.min(sheet.getLastRowNum(), HEADER_SEARCH_ROWS);
        for (int r = 0; r <= last; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            Set<C> found = EnumSet.noneOf(columnType);
            for (Cell cell : row) {
                C column = columnFor(formatter.formatCellValue(cell));
                if (column != null) {
                    found.add(column);
                }
            }
            if (found.containsAll(requiredColumns())) {
                return r;
            }
        }
        return -1;
    }

    private Map<C, Integer> mapColumns(Row header, DataFormatter formatter, List<ImportRowError> errors) {
        Map<C, Integer> columns = new EnumMap<>(columnType);
        for (Cell cell : header) {
            String text = formatter.formatCellValue(cell).trim();
            if (text.isEmpty()) {
                continue;
            }
            C column = columnFor(text);
            if (column == null) {
                errors.add(new ImportRowError(header.getRowNum() + 1, text, "Tanınmayan kolon"));
            } else if (!isIgnored(column) && columns.putIfAbsent(column, cell.getColumnIndex()) != null) {
                errors.add(new ImportRowError(header.getRowNum() + 1, text, "Kolon iki kez var"));
            }
        }
        return columns;
    }

    /** Kolon export başlığıyla ("İşletim Sistemi") ya da koduyla ("os") tanınır. */
    private C columnFor(String text) {
        String t = text.trim();
        for (C column : columnType.getEnumConstants()) {
            if (column.header().equalsIgnoreCase(t) || column.code().equalsIgnoreCase(t)) {
                return column;
            }
        }
        return null;
    }

    private ImportRow<C> read(Row row, int number, Map<C, Integer> columns, DataFormatter formatter,
                              ImportLookups lookups, List<ImportRowError> errors) {
        Map<C, String> text = new EnumMap<>(columnType);
        Map<C, Cell> cells = new EnumMap<>(columnType);
        columns.forEach((column, index) -> {
            Cell cell = row == null ? null : row.getCell(index);
            text.put(column, cell == null ? "" : formatter.formatCellValue(cell).trim());
            if (cell != null) {
                cells.put(column, cell);
            }
        });
        return new ImportRow<>(number, text, cells, lookups, errors);
    }

    /** Doğrulama hatasındaki alan adını ("responsibilityOwnerId") dosyadaki başlığa çevirir. */
    private String headerOf(String property) {
        String code = property.endsWith("Id") ? property.substring(0, property.length() - 2) : property;
        for (C column : columnType.getEnumConstants()) {
            if (column.code().equals(property) || column.code().equals(code)) {
                return column.header();
            }
        }
        return property;
    }
}
