package tr.com.nkolay.itam.model.dto;

/** Bir denetim revizyonunda tek bir alanın önceki/yeni değeri; alan değişmediyse listeye hiç girmez. */
public record FieldChangeDto(String field, String oldValue, String newValue) {
}
