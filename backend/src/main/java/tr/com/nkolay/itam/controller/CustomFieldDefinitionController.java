package tr.com.nkolay.itam.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tr.com.nkolay.itam.model.dto.CustomFieldDefinitionRequest;
import tr.com.nkolay.itam.model.dto.CustomFieldDefinitionResponse;
import tr.com.nkolay.itam.model.dto.CustomFieldDefinitionUpdateRequest;
import tr.com.nkolay.itam.service.customfield.CustomFieldDefinitionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/custom-field-definitions")
public class CustomFieldDefinitionController {

    private final CustomFieldDefinitionService definitionService;

    public CustomFieldDefinitionController(CustomFieldDefinitionService definitionService) {
        this.definitionService = definitionService;
    }

    @GetMapping
    public List<CustomFieldDefinitionResponse> listByAssetType(@RequestParam String assetTypeCode) {
        return definitionService.listByAssetType(assetTypeCode);
    }

    @PostMapping
    public ResponseEntity<CustomFieldDefinitionResponse> create(
            @Valid @RequestBody CustomFieldDefinitionRequest request) {
        CustomFieldDefinitionResponse created = definitionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public CustomFieldDefinitionResponse update(@PathVariable Long id,
            @Valid @RequestBody CustomFieldDefinitionUpdateRequest request) {
        return definitionService.update(id, request);
    }
}
