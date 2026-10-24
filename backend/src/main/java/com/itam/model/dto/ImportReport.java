package com.itam.model.dto;

import java.util.List;

/**
 * İçe aktarma sonucu. dryRun ise hiçbir şey yazılmamıştır; değilse ya tüm
 * satırlar yazılmıştır (created = validRows) ya da hiçbiri (errors dolu).
 */
public record ImportReport(boolean dryRun, int totalRows, int validRows, int created,
                           List<ImportRowError> errors) {
}
