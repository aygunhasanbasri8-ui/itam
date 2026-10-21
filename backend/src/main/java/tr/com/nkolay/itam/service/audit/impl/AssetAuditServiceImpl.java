package tr.com.nkolay.itam.service.audit.impl;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.exception.ResourceNotFoundException;
import tr.com.nkolay.itam.model.dto.AssetRevisionResponse;
import tr.com.nkolay.itam.model.dto.FieldChangeDto;
import tr.com.nkolay.itam.model.entity.ApplicationAsset;
import tr.com.nkolay.itam.model.entity.BaseAsset;
import tr.com.nkolay.itam.model.entity.BusinessServiceAsset;
import tr.com.nkolay.itam.model.entity.CloudAsset;
import tr.com.nkolay.itam.model.entity.ItamRevisionEntity;
import tr.com.nkolay.itam.model.entity.ServerAsset;
import tr.com.nkolay.itam.model.entity.WorkstationAsset;
import tr.com.nkolay.itam.repository.BaseAssetRepository;
import tr.com.nkolay.itam.service.audit.AssetAuditService;
import tr.com.nkolay.itam.service.export.ApplicationExportColumn;
import tr.com.nkolay.itam.service.export.AssetColumn;
import tr.com.nkolay.itam.service.export.BusinessServiceExportColumn;
import tr.com.nkolay.itam.service.export.CloudExportColumn;
import tr.com.nkolay.itam.service.export.ServerExportColumn;
import tr.com.nkolay.itam.service.export.WorkstationExportColumn;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Envers'ın her yazmada tuttuğu revizyonları okunabilir bir değişiklik
 * geçmişine çevirir.
 * <p>
 * Ortak alanlar, tipe özel alanlar (Excel kolon kataloğu üzerinden) ve özel
 * alanlar anahtar bazında karşılaştırılıyor.
 */
@Service
@Transactional(readOnly = true)
public class AssetAuditServiceImpl implements AssetAuditService {

    private static final Set<String> COMMON_COLUMN_CODES =
            Set.of("id", "assetName", "status", "notes", "responsibilityOwner", "createdAt", "updatedAt");

    private final BaseAssetRepository baseAssetRepository;

    // @PersistenceContext yalnızca alan/metot hedefler, constructor parametresinde
    // kullanılamıyor (framework kısıtı) — bu yüzden burada alan enjeksiyonuna geçildi.
    @PersistenceContext
    private EntityManager entityManager;

    public AssetAuditServiceImpl(BaseAssetRepository baseAssetRepository) {
        this.baseAssetRepository = baseAssetRepository;
    }

    @Override
    public List<AssetRevisionResponse> getHistory(Long assetId) {
        if (!baseAssetRepository.existsById(assetId)) {
            throw new ResourceNotFoundException("Asset", assetId);
        }

        // AuditReader, Hibernate'in birinci seviye önbelleğini görmeden doğrudan
        // veritabanına sorgu atıyor; aynı transaction içinde az önce yazılmış bir
        // revizyon flush edilmeden bu sorguya yansımaz.
        entityManager.flush();
        AuditReader reader = AuditReaderFactory.get(entityManager);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = reader.createQuery()
                .forRevisionsOfEntity(BaseAsset.class, false, true)
                .add(AuditEntity.id().eq(assetId))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();

        List<AssetRevisionResponse> history = new ArrayList<>();
        BaseAsset previous = null;

        for (Object[] row : rows) {
            BaseAsset snapshot = (BaseAsset) row[0];
            ItamRevisionEntity revision = (ItamRevisionEntity) row[1];
            RevisionType revisionType = (RevisionType) row[2];

            history.add(new AssetRevisionResponse(
                    revision.getId(),
                    toOffsetDateTime(revision.getTimestamp()),
                    revision.getUsername(),
                    revisionType.name(),
                    diff(previous, snapshot)));

            previous = snapshot;
        }

        return history;
    }

    private List<FieldChangeDto> diff(BaseAsset previous, BaseAsset current) {
        List<FieldChangeDto> changes = new ArrayList<>();

        addIfChanged(changes, "assetName", "Asset adı", value(previous, BaseAsset::getAssetName), current.getAssetName());
        addIfChanged(changes, "status", "Durum",
                value(previous, p -> p.getStatus() == null ? null : p.getStatus().name()),
                current.getStatus() == null ? null : current.getStatus().name());
        addIfChanged(changes, "notes", "Notlar", value(previous, BaseAsset::getNotes), current.getNotes());
        addIfChanged(changes, "deleted", "Silindi",
                value(previous, p -> String.valueOf(p.isDeleted())),
                String.valueOf(current.isDeleted()));
        addIfChanged(changes, "responsibilityOwnerId", "Sorumlu kişi (id)",
                value(previous, p -> ownerId(p)), ownerId(current));
        addIfChanged(changes, "assetType", "Asset tipi",
                value(previous, p -> p.getAssetType() == null ? null : p.getAssetType().getCode()),
                current.getAssetType() == null ? null : current.getAssetType().getCode());
        addSubtypeChanges(changes, previous, current);
        addCustomFieldChanges(changes, previous == null ? null : previous.getCustomFields(),
                current.getCustomFields());

        return changes;
    }

    /**
     * Envers BaseAsset revizyonlarını gerçek alt sınıf olarak döndürüyor (ServerAsset vb.);
     * hostname, IP, lisans tarihi gibi tipe özel alanlar da Excel kataloğu üzerinden karşılaştırılıyor.
     * Ortak alanlar yukarıda zaten var, tarih damgaları ise her revizyonda değiştiği için atlanıyor.
     */
    @SuppressWarnings("unchecked")
    private void addSubtypeChanges(List<FieldChangeDto> changes, BaseAsset previous, BaseAsset current) {
        for (AssetColumn<?> raw : columnsOf(current)) {
            if (COMMON_COLUMN_CODES.contains(raw.code())) {
                continue;
            }
            AssetColumn<BaseAsset> column = (AssetColumn<BaseAsset>) raw;
            addIfChanged(changes, column.code(), column.header(),
                    previous == null ? null : asText(column.valueOf(previous)),
                    asText(column.valueOf(current)));
        }
    }

    private List<? extends AssetColumn<?>> columnsOf(BaseAsset asset) {
        return switch (asset) {
            case ServerAsset ignored -> List.of(ServerExportColumn.values());
            case WorkstationAsset ignored -> List.of(WorkstationExportColumn.values());
            case CloudAsset ignored -> List.of(CloudExportColumn.values());
            case ApplicationAsset ignored -> List.of(ApplicationExportColumn.values());
            case BusinessServiceAsset ignored -> List.of(BusinessServiceExportColumn.values());
            default -> List.of();
        };
    }

    private String asText(Object value) {
        return value == null ? null : value.toString();
    }

    // Özel alanlar tek JSONB kolonda duruyor; kolonu bütün olarak göstermek yerine
    // anahtar bazında "customFields.kabinNo" gibi ayrı satırlar üretiyoruz.
    private void addCustomFieldChanges(List<FieldChangeDto> changes, Map<String, String> before,
            Map<String, String> after) {
        Map<String, String> oldValues = before == null ? Map.of() : before;
        Map<String, String> newValues = after == null ? Map.of() : after;

        Set<String> keys = new TreeSet<>(oldValues.keySet());
        keys.addAll(newValues.keySet());
        for (String key : keys) {
            addIfChanged(changes, "customFields." + key, "Özel alan: " + key, oldValues.get(key), newValues.get(key));
        }
    }

    private String ownerId(BaseAsset asset) {
        return asset.getResponsibilityOwner() == null ? null
                : String.valueOf(asset.getResponsibilityOwner().getId());
    }

    private String value(BaseAsset asset, Function<BaseAsset, String> extractor) {
        return asset == null ? null : extractor.apply(asset);
    }

    private void addIfChanged(List<FieldChangeDto> changes, String field, String label, String oldValue,
            String newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            changes.add(new FieldChangeDto(field, label, oldValue, newValue));
        }
    }

    private OffsetDateTime toOffsetDateTime(long epochMillis) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }
}
