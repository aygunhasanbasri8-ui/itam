package tr.com.nkolay.itam.model.dto;

/** Excel'e aktarılabilecek bir kolon; code ?columns= parametresine, header ekranda seçime gider. */
public record ExportColumnResponse(String code, String header) {
}
