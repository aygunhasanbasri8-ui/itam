package com.itam.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itam.model.dto.AssetTypeResponse;
import com.itam.model.dto.AssetTypeUpdateRequest;
import com.itam.service.assettype.AssetTypeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/asset-types")
public class AssetTypeController {

    private final AssetTypeService assetTypeService;

    public AssetTypeController(AssetTypeService assetTypeService) {
        this.assetTypeService = assetTypeService;
    }

    @GetMapping
    public List<AssetTypeResponse> list() {
        return assetTypeService.list();
    }

    @PutMapping("/{code}")
    public AssetTypeResponse update(@PathVariable String code, @Valid @RequestBody AssetTypeUpdateRequest request) {
        return assetTypeService.update(code, request);
    }
}
