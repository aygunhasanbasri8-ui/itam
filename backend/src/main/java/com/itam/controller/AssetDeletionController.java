package com.itam.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.itam.service.asset.AssetDeletionService;

@RestController
public class AssetDeletionController {

    private final AssetDeletionService assetDeletionService;

    public AssetDeletionController(AssetDeletionService assetDeletionService) {
        this.assetDeletionService = assetDeletionService;
    }

    @DeleteMapping("/api/v1/assets/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        assetDeletionService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
