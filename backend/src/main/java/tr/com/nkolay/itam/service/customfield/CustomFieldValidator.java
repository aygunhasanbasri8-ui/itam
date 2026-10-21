package tr.com.nkolay.itam.service.customfield;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import tr.com.nkolay.itam.enums.CustomFieldType;
import tr.com.nkolay.itam.model.entity.CustomFieldDefinition;
import tr.com.nkolay.itam.repository.CustomFieldDefinitionRepository;
import tr.com.nkolay.itam.validation.ValidationCollector;

/**
 * Bir asset kaydedilirken customFields map'indeki değerleri, o asset tipi için
 * tanımlı custom_field_def kayıtlarına göre doğrular.
 * <p>
 * V1'de tüm alanlar opsiyonel (ck_custom_field_req), bu yüzden eksik alan hatası
 * üretilmiyor — yalnızca tanımsız anahtar ve yanlış biçim kontrol ediliyor.
 * <p>
 * Pasife alınmış tanımlar da geçerli sayılıyor: pasif alan formda gösterilmiyor
 * ama eski asset'lerde değeri duruyor. Onu reddetseydik, alanı pasife almak o
 * asset'lerin bir daha kaydedilememesi demek olurdu.
 */
@Component
public class CustomFieldValidator {

    private final CustomFieldDefinitionRepository definitionRepository;

    public CustomFieldValidator(CustomFieldDefinitionRepository definitionRepository) {
        this.definitionRepository = definitionRepository;
    }

    public void validate(String assetTypeCode, Map<String, String> customFields, ValidationCollector errors) {
        if (customFields == null || customFields.isEmpty()) {
            return;
        }

        Map<String, CustomFieldDefinition> defsByKey = definitionRepository
                .findByAssetTypeCodeOrderBySortOrder(assetTypeCode).stream()
                .collect(java.util.stream.Collectors.toMap(CustomFieldDefinition::getFieldKey, d -> d));

        for (Map.Entry<String, String> entry : customFields.entrySet()) {
            CustomFieldDefinition definition = defsByKey.get(entry.getKey());
            if (definition == null) {
                errors.add("customFields." + entry.getKey(), "tanimsizOzelAlan",
                        "Bu asset tipi için tanımlı olmayan özel alan", entry.getKey());
                continue;
            }

            String value = entry.getValue();
            if (value != null && !matchesType(definition.getDataType(), value)) {
                errors.add("customFields." + entry.getKey(), "gecersizOzelAlanDegeri",
                        "'%s' alanı %s tipine uygun bir değer değil"
                                .formatted(definition.getLabel(), definition.getDataType()),
                        value);
            }
        }
    }

    private boolean matchesType(CustomFieldType type, String value) {
        Predicate<String> check = switch (type) {
            case TEXT -> v -> true;
            case NUMBER -> this::isNumber;
            case DATE -> this::isIsoDate;
            case BOOLEAN -> v -> v.equalsIgnoreCase("true") || v.equalsIgnoreCase("false");
        };
        return check.test(value);
    }

    private boolean isNumber(String value) {
        try {
            Double.parseDouble(value);
            return true;
        }
        catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isIsoDate(String value) {
        try {
            LocalDate.parse(value);
            return true;
        }
        catch (DateTimeParseException ex) {
            return false;
        }
    }
}
