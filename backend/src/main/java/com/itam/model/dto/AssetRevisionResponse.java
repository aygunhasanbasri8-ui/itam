package com.itam.model.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @param revisionType Envers RevisionType adı: ADD, MOD veya DEL
 * @param changes      önceki revizyona göre değişen alanlar; ADD'de tüm alanlar
 *                     "önceki değer null" şeklinde ilk halini gösterir
 */
public record AssetRevisionResponse(
        int revision,
        OffsetDateTime timestamp,
        String username,
        String revisionType,
        List<FieldChangeDto> changes) {
}
