package com.itam.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itam.model.dto.AssetDependencyRequest;
import com.itam.model.dto.AssetDependencyResponse;
import com.itam.model.dto.DependencyTreeNode;
import com.itam.service.dependency.AssetDependencyService;

import jakarta.validation.Valid;

@RestController
public class AssetDependencyController {

    private final AssetDependencyService dependencyService;

    public AssetDependencyController(AssetDependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    @PostMapping("/api/v1/asset-dependencies")
    public ResponseEntity<AssetDependencyResponse> create(@Valid @RequestBody AssetDependencyRequest request) {
        AssetDependencyResponse created = dependencyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/api/v1/asset-dependencies/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dependencyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/assets/{assetId}/dependencies")
    public List<AssetDependencyResponse> listByAsset(@PathVariable Long assetId) {
        return dependencyService.listByAsset(assetId);
    }

    @GetMapping("/api/v1/assets/{assetId}/dependency-tree")
    public DependencyTreeNode dependencyTree(@PathVariable Long assetId) {
        return dependencyService.getDependencyTree(assetId);
    }
}
