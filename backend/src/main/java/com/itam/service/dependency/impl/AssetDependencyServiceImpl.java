package com.itam.service.dependency.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itam.enums.DependencyType;
import com.itam.exception.ResourceNotFoundException;
import com.itam.mapper.AssetDependencyMapper;
import com.itam.model.dto.AssetDependencyRequest;
import com.itam.model.dto.AssetDependencyResponse;
import com.itam.model.dto.DependencyTreeNode;
import com.itam.model.entity.AssetDependency;
import com.itam.model.entity.BaseAsset;
import com.itam.repository.AssetDependencyRepository;
import com.itam.repository.BaseAssetRepository;
import com.itam.service.dependency.AssetDependencyService;
import com.itam.validation.ValidationCollector;

@Service
@Transactional
public class AssetDependencyServiceImpl implements AssetDependencyService {

    private final AssetDependencyRepository dependencyRepository;
    private final BaseAssetRepository baseAssetRepository;
    private final AssetDependencyMapper mapper;

    public AssetDependencyServiceImpl(AssetDependencyRepository dependencyRepository,
            BaseAssetRepository baseAssetRepository, AssetDependencyMapper mapper) {
        this.dependencyRepository = dependencyRepository;
        this.baseAssetRepository = baseAssetRepository;
        this.mapper = mapper;
    }

    @Override
    public AssetDependencyResponse create(AssetDependencyRequest request) {
        ValidationCollector errors = ValidationCollector.create();

        errors.check(!request.parentAssetId().equals(request.childAssetId()),
                "childAssetId", "kendineBagimlilik",
                "Bir asset kendisine bağımlı olamaz", request.childAssetId());

        BaseAsset parent = resolveAsset(request.parentAssetId(), "parentAssetId", errors);
        BaseAsset child = resolveAsset(request.childAssetId(), "childAssetId", errors);
        errors.throwIfInvalid();

        errors.check(!dependencyRepository.existsByParentAssetIdAndChildAssetId(
                        request.parentAssetId(), request.childAssetId()),
                "childAssetId", "bagimlilikZatenVar",
                "Bu iki asset arasında bağımlılık zaten tanımlı", request.childAssetId());

        errors.check(!wouldCreateCycle(request.parentAssetId(), request.childAssetId()),
                "childAssetId", "donguselBagimlilik",
                "Bu bağımlılık, mevcut zincirle birlikte döngü oluşturuyor", request.childAssetId());
        errors.throwIfInvalid();

        AssetDependency dependency = new AssetDependency();
        dependency.setParentAsset(parent);
        dependency.setChildAsset(child);
        dependency.setDependencyType(request.dependencyType());
        dependency.setNotes(request.notes());

        return mapper.toResponse(dependencyRepository.save(dependency));
    }

    @Override
    public void delete(Long id) {
        if (!dependencyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Bağımlılık", id);
        }
        dependencyRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AssetDependencyResponse> listByAsset(Long assetId) {
        return dependencyRepository.findEdgesTouchingAsset(assetId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DependencyTreeNode getDependencyTree(Long rootAssetId) {
        if (!baseAssetRepository.existsById(rootAssetId)) {
            throw new ResourceNotFoundException("Asset", rootAssetId);
        }

        Map<Long, List<AssetDependency>> childrenByParentId = loadReachableEdges(rootAssetId);

        Set<Long> assetIds = new HashSet<>();
        DependencyTreeNode idOnlyTree = buildNode(rootAssetId, null, childrenByParentId,
                new HashSet<>(), assetIds);
        assetIds.add(rootAssetId);

        Map<Long, BaseAsset> assetsById = new HashMap<>();
        baseAssetRepository.findAllById(assetIds)
                .forEach(asset -> assetsById.put(asset.getId(), asset));

        return withDetails(idOnlyTree, assetsById);
    }

    /** Döngüye düşmemek için ziyaret edilen asset'leri izler; kaçak bir döngü varsa orada durur. */
    private DependencyTreeNode buildNode(Long assetId, DependencyType incomingType,
            Map<Long, List<AssetDependency>> childrenByParentId, Set<Long> visited, Set<Long> collected) {
        collected.add(assetId);
        if (!visited.add(assetId)) {
            return new DependencyTreeNode(assetId, null, null, incomingType, List.of());
        }

        List<DependencyTreeNode> children = childrenByParentId
                .getOrDefault(assetId, List.of()).stream()
                .map(edge -> buildNode(edge.getChildAsset().getId(), edge.getDependencyType(),
                        childrenByParentId, visited, collected))
                .toList();

        return new DependencyTreeNode(assetId, null, null, incomingType, children);
    }

    private DependencyTreeNode withDetails(DependencyTreeNode node, Map<Long, BaseAsset> assetsById) {
        BaseAsset asset = assetsById.get(node.assetId());
        List<DependencyTreeNode> children = node.children().stream()
                .map(child -> withDetails(child, assetsById))
                .toList();
        return new DependencyTreeNode(node.assetId(),
                asset == null ? null : asset.getAssetName(),
                asset == null ? null : asset.getAssetType().getCode(),
                node.dependencyType(), children);
    }

    /** parentId -> childId kenarı eklendiğinde, childId'den parentId'ye zaten bir yol varsa döngü oluşur. */
    private boolean wouldCreateCycle(Long parentId, Long childId) {
        if (parentId.equals(childId)) {
            return true;
        }
        return loadReachableEdges(childId).values().stream()
                .flatMap(List::stream)
                .anyMatch(edge -> edge.getChildAsset().getId().equals(parentId));
    }

    /**
     * Kökten ulaşılabilen kenarları seviye seviye çeker: her seviye için
     * ix_dependency_parent indeksini kullanan tek bir sorgu. Eskiden tüm tablo
     * okunuyordu; envanter büyüdükçe her ağaç isteği bütün kenarları taşıyordu.
     */
    private Map<Long, List<AssetDependency>> loadReachableEdges(Long rootAssetId) {
        Map<Long, List<AssetDependency>> childrenByParentId = new HashMap<>();
        Set<Long> visited = new HashSet<>(Set.of(rootAssetId));
        Set<Long> frontier = Set.of(rootAssetId);

        while (!frontier.isEmpty()) {
            Set<Long> next = new HashSet<>();
            for (AssetDependency edge : dependencyRepository.findByParentAssetIdIn(frontier)) {
                childrenByParentId
                        .computeIfAbsent(edge.getParentAsset().getId(), key -> new ArrayList<>())
                        .add(edge);
                Long childId = edge.getChildAsset().getId();
                if (visited.add(childId)) {
                    next.add(childId);
                }
            }
            frontier = next;
        }
        return childrenByParentId;
    }

    private BaseAsset resolveAsset(Long id, String field, ValidationCollector errors) {
        BaseAsset asset = baseAssetRepository.findById(id).orElse(null);
        if (asset == null) {
            errors.add(field, "assetBulunamadi", "Asset bulunamadı", id);
            return null;
        }
        if (asset.isDeleted()) {
            errors.add(field, "silinmisAsset", "Silinmiş asset bağımlılığa dahil edilemez", id);
            return null;
        }
        return asset;
    }
}
