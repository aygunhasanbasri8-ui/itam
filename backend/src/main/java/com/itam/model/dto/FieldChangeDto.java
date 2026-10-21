package com.itam.model.dto;

/**
 * Bir denetim revizyonunda tek bir alanın önceki/yeni değeri; alan değişmediyse listeye hiç girmez.
 *
 * @param label alanın ekranda görünen adı; istemci her tipin alan adlarını ayrıca bilmek zorunda kalmasın
 */
public record FieldChangeDto(String field, String label, String oldValue, String newValue) {
}
