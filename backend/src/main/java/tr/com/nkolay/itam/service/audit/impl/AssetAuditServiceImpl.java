package tr.com.nkolay.itam.service.audit.impl;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import tr.com.nkolay.itam.model.entity.BaseAsset;
import tr.com.nkolay.itam.model.entity.ItamRevisionEntity;
import tr.com.nkolay.itam.repository.BaseAssetRepository;
import tr.com.nkolay.itam.service.audit.AssetAuditService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Envers'ın her yazmada tuttuğu revizyonları okunabilir bir değişiklik
 * geçmişine çevirir.
 * <p>
 * Yalnızca BaseAsset'in ortak alanları (assetName, status, notes, deleted,
 * responsibilityOwner, assetType) karşılaştırılıyor; sunucu/uygulama gibi
 * tiplere özel alanların (hostname, os, ...) revizyon bazlı karşılaştırması
 * kapsam dışı bırakıldı, ileride ihtiyaç olursa asıl alt tip sınıfı üzerinden
 * (ör. ServerAsset.class) aynı desenle eklenebilir.
 */
@Service
@Transactional(readOnly = true)
public class AssetAuditServiceImpl implements AssetAuditService {

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

        addIfChanged(changes, "assetName", value(previous, BaseAsset::getAssetName), current.getAssetName());
        addIfChanged(changes, "status",
                value(previous, p -> p.getStatus() == null ? null : p.getStatus().name()),
                current.getStatus() == null ? null : current.getStatus().name());
        addIfChanged(changes, "notes", value(previous, BaseAsset::getNotes), current.getNotes());
        addIfChanged(changes, "deleted",
                value(previous, p -> String.valueOf(p.isDeleted())),
                String.valueOf(current.isDeleted()));
        addIfChanged(changes, "responsibilityOwnerId",
                value(previous, p -> ownerId(p)), ownerId(current));
        addIfChanged(changes, "assetType",
                value(previous, p -> p.getAssetType() == null ? null : p.getAssetType().getCode()),
                current.getAssetType() == null ? null : current.getAssetType().getCode());

        return changes;
    }

    private String ownerId(BaseAsset asset) {
        return asset.getResponsibilityOwner() == null ? null
                : String.valueOf(asset.getResponsibilityOwner().getId());
    }

    private String value(BaseAsset asset, Function<BaseAsset, String> extractor) {
        return asset == null ? null : extractor.apply(asset);
    }

    private void addIfChanged(List<FieldChangeDto> changes, String field, String oldValue, String newValue) {
        if (!Objects.equals(oldValue, newValue)) {
            changes.add(new FieldChangeDto(field, oldValue, newValue));
        }
    }

    private OffsetDateTime toOffsetDateTime(long epochMillis) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }
}
