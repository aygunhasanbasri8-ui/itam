package com.itam.service.importer.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;

import com.itam.model.dto.ImportRowError;
import com.itam.service.export.AssetColumn;

/**
 * Dosyadaki tek bir satır. Okuma yardımcıları çevrilemeyen değer için hatayı
 * satıra yazıp null döner; böylece bir satırdaki bütün sorunlar tek seferde raporlanıyor.
 */
final class ImportRow<C extends Enum<C> & AssetColumn<?>> {

    private static final DateTimeFormatter TR_DATE = DateTimeFormatter.ofPattern("d.M.uuuu");

    private final int number;
    private final Map<C, String> text;
    private final Map<C, Cell> cells;
    private final ImportLookups lookups;
    private final List<ImportRowError> errors;

    ImportRow(int number, Map<C, String> text, Map<C, Cell> cells, ImportLookups lookups,
              List<ImportRowError> errors) {
        this.number = number;
        this.text = text;
        this.cells = cells;
        this.lookups = lookups;
        this.errors = errors;
    }

    int number() {
        return number;
    }

    boolean isEmpty() {
        return text.values().stream().allMatch(String::isEmpty);
    }

    String raw(C column) {
        return text.getOrDefault(column, "");
    }

    /** Boş hücre null. */
    String text(C column) {
        String value = raw(column);
        return value.isBlank() ? null : value.trim();
    }

    void error(C column, String message) {
        errors.add(new ImportRowError(number, column.header(), message));
    }

    Long person(C column) {
        return lookup(column, true);
    }

    Long department(C column) {
        return lookup(column, false);
    }

    private Long lookup(C column, boolean person) {
        String value = text(column);
        if (value == null) {
            return null;
        }
        try {
            return person ? lookups.person(value) : lookups.department(value);
        } catch (ImportLookups.LookupException e) {
            error(column, e.getMessage());
            return null;
        }
    }

    /** Büyük/küçük harf fark etmez; {@code label} hata mesajında görünen ad ("ortam"). */
    <E extends Enum<E>> E enumValue(C column, Class<E> type, String label) {
        String value = text(column);
        if (value == null) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            String allowed = Arrays.stream(type.getEnumConstants()).map(Enum::name).collect(Collectors.joining(", "));
            error(column, "Geçersiz " + label + ": " + value + " (" + allowed + ")");
            return null;
        }
    }

    /** Boş hücre "Hayır" sayılıyor; tanınmayan değerde hata ve false. */
    boolean bool(C column) {
        return switch (raw(column).toLowerCase(ImportLookups.TR)) {
            case "", "hayır", "hayir", "false", "0", "no" -> false;
            case "evet", "true", "1", "yes" -> true;
            default -> {
                error(column, "Evet ya da Hayır olmalı");
                yield false;
            }
        };
    }

    Integer integer(C column) {
        String value = text(column);
        if (value == null) {
            return null;
        }
        try {
            // Excel'de sayı hücresi "16" gelir, elle yazılmış metin "16.0" da olabilir
            return new BigDecimal(value).intValueExact();
        } catch (NumberFormatException | ArithmeticException e) {
            error(column, "Tam sayı olmalı: " + value);
            return null;
        }
    }

    /** Excel tarih hücresi ya da 2027-01-31 / 31.01.2027 biçiminde metin. */
    LocalDate date(C column) {
        Cell cell = cells.get(column);
        if (cell != null && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        String value = text(column);
        if (value == null) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException iso) {
            try {
                return LocalDate.parse(value, TR_DATE);
            } catch (DateTimeParseException tr) {
                error(column, "Tarih 2027-01-31 ya da 31.01.2027 biçiminde olmalı: " + value);
                return null;
            }
        }
    }
}
