package tr.com.nkolay.itam.model.dto;

import java.util.List;

import tr.com.nkolay.itam.enums.DependencyType;

/**
 * Bağımlılık ağacında tek bir düğüm.
 *
 * @param dependencyType bu düğümün üst (parent) düğüme bağımlılık türü;
 *                        ağacın kök düğümünde her zaman null'dur
 * @param children       bu asset'in bağımlı olduğu (child) asset'ler
 */
public record DependencyTreeNode(
        Long assetId,
        String assetName,
        String assetTypeCode,
        DependencyType dependencyType,
        List<DependencyTreeNode> children) {
}
