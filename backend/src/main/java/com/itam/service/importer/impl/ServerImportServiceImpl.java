package com.itam.service.importer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itam.enums.ServerEnvironment;
import com.itam.model.dto.ImportReport;
import com.itam.model.dto.ImportRowError;
import com.itam.model.dto.ServerAssetRequest;
import com.itam.model.entity.Person;
import com.itam.repository.PersonRepository;
import com.itam.repository.ServerAssetRepository;
import com.itam.service.asset.ServerAssetService;
import com.itam.service.export.ServerExportColumn;
import com.itam.service.importer.ServerImportService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Service
public class ServerImportServiceImpl implements ServerImportService {

    static final int MAX_ROWS = 5000;
    private static final Locale TR = Locale.forLanguageTag("tr");
    /** Export dosyasının üstünde başlık bloğu var; başlık satırını ilk satırlarda arıyoruz. */
    private static final int HEADER_SEARCH_ROWS = 20;

    /** Export'ta olan ama elle girilmeyen kolonlar; dosyada bulunursa sessizce atlanır. */
    private static final Set<ServerExportColumn> IGNORED = EnumSet.of(
            ServerExportColumn.ID, ServerExportColumn.STATUS,
            ServerExportColumn.CREATED_AT, ServerExportColumn.UPDATED_AT);

    private static final Set<ServerExportColumn> REQUIRED = EnumSet.of(
            ServerExportColumn.ASSET_NAME, ServerExportColumn.HOSTNAME, ServerExportColumn.ENVIRONMENT);

    private final ServerAssetService serverAssetService;
    private final ServerAssetRepository serverAssetRepository;
    private final PersonRepository personRepository;
    private final Validator validator;

    public ServerImportServiceImpl(ServerAssetService serverAssetService, ServerAssetRepository serverAssetRepository,
                                   PersonRepository personRepository, Validator validator) {
        this.serverAssetService = serverAssetService;
        this.serverAssetRepository = serverAssetRepository;
        this.personRepository = personRepository;
        this.validator = validator;
    }

    @Override
    @Transactional
    public ImportReport importXlsx(InputStream xlsx, boolean dryRun) {
        List<ImportRowError> errors = new ArrayList<>();
        List<ServerAssetRequest> requests = new ArrayList<>();
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
        requests.forEach(serverAssetService::create);
        return new ImportReport(false, total, requests.size(), requests.size(), List.of());
    }

    /** Satırları isteklere çevirir; hataları toplar, dolu satır sayısını döner. */
    private int parse(Sheet sheet, List<ServerAssetRequest> requests, List<ImportRowError> errors) {
        DataFormatter formatter = new DataFormatter(Locale.ROOT);
        int headerIndex = findHeaderRow(sheet, formatter);
        if (headerIndex < 0) {
            errors.add(new ImportRowError(1, null,
                    "Başlık satırı bulunamadı; en az Ad, Hostname ve Ortam kolonları olmalı"));
            return 0;
        }
        Map<ServerExportColumn, Integer> columns = mapColumns(sheet.getRow(headerIndex), formatter, errors);
        if (!errors.isEmpty()) {
            return 0;
        }

        PersonLookup people = new PersonLookup(personRepository.findAll());
        Set<String> existingHostnames = new HashSet<>(serverAssetRepository.findActiveHostnamesLowerCase());
        Map<String, Integer> hostnamesInFile = new HashMap<>();

        int total = 0;
        for (int r = headerIndex + 1; r <= sheet.getLastRowNum(); r++) {
            Map<ServerExportColumn, String> values = read(sheet.getRow(r), columns, formatter);
            if (values.values().stream().allMatch(String::isEmpty)) {
                continue;   // boş satırlar (Excel sonda bırakabiliyor)
            }
            int excelRow = r + 1;
            if (++total > MAX_ROWS) {
                errors.add(new ImportRowError(excelRow, null,
                        "Tek seferde en fazla " + MAX_ROWS + " satır içe aktarılabilir"));
                break;
            }
            int before = errors.size();
            ServerAssetRequest request = toRequest(values, people, excelRow, errors);
            for (ConstraintViolation<ServerAssetRequest> v : validator.validate(request)) {
                errors.add(new ImportRowError(excelRow, headerOf(v.getPropertyPath().toString()), v.getMessage()));
            }
            String host = request.hostname() == null ? "" : request.hostname().toLowerCase(Locale.ROOT);
            if (!host.isEmpty()) {
                if (existingHostnames.contains(host)) {
                    errors.add(new ImportRowError(excelRow, ServerExportColumn.HOSTNAME.header(),
                            "Bu hostname'le kayıtlı bir sunucu zaten var"));
                }
                Integer firstRow = hostnamesInFile.putIfAbsent(host, excelRow);
                if (firstRow != null) {
                    errors.add(new ImportRowError(excelRow, ServerExportColumn.HOSTNAME.header(),
                            "Aynı hostname dosyada " + firstRow + ". satırda da var"));
                }
            }
            if (errors.size() == before) {
                requests.add(request);
            }
        }
        return total;
    }

    @Override
    public void writeTemplate(OutputStream out) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sunucular");
            Row header = sheet.createRow(0);
            int i = 0;
            for (ServerExportColumn column : ServerExportColumn.values()) {
                if (!IGNORED.contains(column)) {
                    header.createCell(i).setCellValue(column.header());
                    sheet.setColumnWidth(i++, 20 * 256);
                }
            }
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("Örnek: Web sunucusu 01");
            workbook.write(out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static int findHeaderRow(Sheet sheet, DataFormatter formatter) {
        int last = Math.min(sheet.getLastRowNum(), HEADER_SEARCH_ROWS);
        for (int r = 0; r <= last; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
            Set<ServerExportColumn> found = EnumSet.noneOf(ServerExportColumn.class);
            for (Cell cell : row) {
                ServerExportColumn column = columnFor(formatter.formatCellValue(cell));
                if (column != null) {
                    found.add(column);
                }
            }
            if (found.containsAll(REQUIRED)) {
                return r;
            }
        }
        return -1;
    }

    private static Map<ServerExportColumn, Integer> mapColumns(Row header, DataFormatter formatter,
                                                               List<ImportRowError> errors) {
        Map<ServerExportColumn, Integer> columns = new EnumMap<>(ServerExportColumn.class);
        for (Cell cell : header) {
            String text = formatter.formatCellValue(cell).trim();
            if (text.isEmpty()) {
                continue;
            }
            ServerExportColumn column = columnFor(text);
            if (column == null) {
                errors.add(new ImportRowError(header.getRowNum() + 1, text, "Tanınmayan kolon"));
            } else if (!IGNORED.contains(column) && columns.putIfAbsent(column, cell.getColumnIndex()) != null) {
                errors.add(new ImportRowError(header.getRowNum() + 1, text, "Kolon iki kez var"));
            }
        }
        return columns;
    }

    /** Kolon export başlığıyla ("İşletim Sistemi") ya da koduyla ("os") tanınır. */
    private static ServerExportColumn columnFor(String text) {
        String t = text.trim();
        for (ServerExportColumn column : ServerExportColumn.values()) {
            if (column.header().equalsIgnoreCase(t) || column.code().equalsIgnoreCase(t)) {
                return column;
            }
        }
        return null;
    }

    private static Map<ServerExportColumn, String> read(Row row, Map<ServerExportColumn, Integer> columns,
                                                        DataFormatter formatter) {
        Map<ServerExportColumn, String> values = new EnumMap<>(ServerExportColumn.class);
        columns.forEach((column, index) -> {
            Cell cell = row == null ? null : row.getCell(index);
            values.put(column, cell == null ? "" : formatter.formatCellValue(cell).trim());
        });
        return values;
    }

    private static ServerAssetRequest toRequest(Map<ServerExportColumn, String> v, PersonLookup people, int row,
                                                List<ImportRowError> errors) {
        ServerEnvironment environment = null;
        String env = v.getOrDefault(ServerExportColumn.ENVIRONMENT, "");
        if (!env.isEmpty()) {
            try {
                environment = ServerEnvironment.valueOf(env.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                errors.add(new ImportRowError(row, ServerExportColumn.ENVIRONMENT.header(),
                        "Geçersiz ortam: " + env + " (DEV, TEST, UAT, PROD)"));
            }
        }
        Boolean virtual = parseBoolean(v.getOrDefault(ServerExportColumn.VIRTUAL, ""));
        if (virtual == null) {
            errors.add(new ImportRowError(row, ServerExportColumn.VIRTUAL.header(), "Evet ya da Hayır olmalı"));
            virtual = false;
        }
        Long owner = people.resolve(v, ServerExportColumn.RESPONSIBILITY_OWNER, row, errors);
        Long serviceOwner = people.resolve(v, ServerExportColumn.SERVICE_OWNER, row, errors);
        Long appResponsible = people.resolve(v, ServerExportColumn.APP_RESPONSIBLE, row, errors);
        Long osResponsible = people.resolve(v, ServerExportColumn.OS_RESPONSIBLE, row, errors);

        return new ServerAssetRequest(
                emptyToNull(v.get(ServerExportColumn.ASSET_NAME)),
                emptyToNull(v.get(ServerExportColumn.NOTES)),
                owner,
                emptyToNull(v.get(ServerExportColumn.DOMAIN)),
                emptyToNull(v.get(ServerExportColumn.HOSTNAME)),
                emptyToNull(v.get(ServerExportColumn.IP_ADDRESS)),
                environment,
                emptyToNull(v.get(ServerExportColumn.SERVER_CLASS)),
                emptyToNull(v.get(ServerExportColumn.GROUP_NAME)),
                null,
                null,
                emptyToNull(v.get(ServerExportColumn.INFRASTRUCTURE_TYPE)),
                emptyToNull(v.get(ServerExportColumn.OS)),
                virtual,
                serviceOwner,
                appResponsible,
                osResponsible,
                null);
    }

    private static Boolean parseBoolean(String text) {
        return switch (text.toLowerCase(TR)) {
            case "", "hayır", "hayir", "false", "0", "no" -> false;
            case "evet", "true", "1", "yes" -> true;
            default -> null;
        };
    }

    private static String emptyToNull(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }

    /** Doğrulama hatasındaki alan adını kullanıcının dosyada gördüğü başlığa çevirir. */
    private static String headerOf(String property) {
        for (ServerExportColumn column : ServerExportColumn.values()) {
            if (column.code().equals(property)) {
                return column.header();
            }
        }
        return property;
    }

    /**
     * Kişi kolonları sicil kodu, e-posta ya da tam adla doldurulabilir (export tam ad
     * yazıyor). Aynı adda iki kişi varsa tahmin yürütmek yerine hata veriyoruz.
     */
    private static final class PersonLookup {
        private final Map<String, Long> byCode = new HashMap<>();
        private final Map<String, Long> byEmail = new HashMap<>();
        private final Map<String, List<Long>> byName = new HashMap<>();

        PersonLookup(List<Person> people) {
            for (Person p : people) {
                if (!p.isActive()) {
                    continue;
                }
                if (p.getCode() != null) {
                    byCode.put(p.getCode().toLowerCase(Locale.ROOT), p.getId());
                }
                if (p.getEmail() != null) {
                    byEmail.put(p.getEmail().toLowerCase(Locale.ROOT), p.getId());
                }
                if (p.getFullName() != null) {
                    byName.computeIfAbsent(p.getFullName().trim().toLowerCase(TR), k -> new ArrayList<>())
                            .add(p.getId());
                }
            }
        }

        Long resolve(Map<ServerExportColumn, String> values, ServerExportColumn column, int row,
                     List<ImportRowError> errors) {
            String text = values.getOrDefault(column, "");
            if (text.isEmpty()) {
                return null;
            }
            String key = text.toLowerCase(Locale.ROOT);
            Long id = byCode.get(key);
            if (id == null) {
                id = byEmail.get(key);
            }
            if (id != null) {
                return id;
            }
            List<Long> named = byName.getOrDefault(text.toLowerCase(TR), List.of());
            if (named.size() == 1) {
                return named.get(0);
            }
            errors.add(new ImportRowError(row, column.header(), named.isEmpty()
                    ? "Kişi bulunamadı: " + text
                    : "Bu adda birden fazla kişi var, sicil kodu ya da e-posta yazın: " + text));
            return null;
        }
    }
}
