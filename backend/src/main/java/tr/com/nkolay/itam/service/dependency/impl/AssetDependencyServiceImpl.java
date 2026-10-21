package tr.com.nkolay.itam.service.dependency.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tr.com.nkolay.itam.enums.DependencyType;
import tr.com.nkolay.itam.exception.ResourceNotFoundException;
import tr.com.nkolay.itam.mapper.AssetDependencyMapper;
import tr.com.nkolay.itam.model.dto.AssetDependencyRequest;
import tr.com.nkolay.itam.model.dto.AssetDependencyResponse;
import tr.com.nkolay.itam.model.dto.DependencyTreeNode;
import tr.com.nkolay.itam.model.entity.AssetDependency;
import tr.com.nkolay.itam.model.entity.BaseAsset;
import tr.com.nkolay.itam.repository.AssetDependencyRepository;
import tr.com.nkolay.itam.repository.BaseAssetRepository;
import tr.com.nkolay.itam.service.dependency.AssetDependencyService;
import tr.com.nkolay.itam.validation.ValidationCollector;

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

        Map<Long, List<AssetDependency>> childrenByParentId = new HashMap<>();
        for (AssetDependency edge : dependencyRepository.findAll()) {
            childrenByParentId
                    .computeIfAbsent(edge.getParentAsset().getId(), key -> new ArrayList<>())
                    .add(edge);
        }

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
        Map<Long, List<Long>> forward = new HashMap<>();
        for (AssetDependency edge : dependencyRepository.findAll()) {
            forward.computeIfAbsent(edge.getParentAsset().getId(), key -> new ArrayList<>())
                    .add(edge.getChildAsset().getId());
        }

        Set<Long> visited = new HashSet<>();
        ArrayDeque<Long> queue = new ArrayDeque<>();
        queue.add(childId);

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (current.equals(parentId)) {
                return true;
            }
            if (!visited.add(current)) {
                continue;
            }
            queue.addAll(forward.getOrDefault(current, List.of()));
        }
        return false;
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
