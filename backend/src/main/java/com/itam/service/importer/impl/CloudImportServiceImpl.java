package com.itam.service.importer.impl;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.itam.enums.CloudProvider;
import com.itam.model.dto.CloudAssetRequest;
import com.itam.repository.DepartmentRepository;
import com.itam.repository.PersonRepository;
import com.itam.service.asset.CloudAssetService;
import com.itam.service.export.CloudExportColumn;

import jakarta.validation.Validator;

@Service
public class CloudImportServiceImpl extends AbstractAssetImportService<CloudExportColumn, CloudAssetRequest> {

    private final CloudAssetService cloudAssetService;

    public CloudImportServiceImpl(CloudAssetService cloudAssetService, PersonRepository personRepository,
                                  DepartmentRepository departmentRepository, Validator validator) {
        super(CloudExportColumn.class, validator, personRepository, departmentRepository);
        this.cloudAssetService = cloudAssetService;
    }

    @Override
    protected Set<CloudExportColumn> requiredColumns() {
        return EnumSet.of(CloudExportColumn.ASSET_NAME, CloudExportColumn.CLOUD_PROVIDER);
    }

    @Override
    protected String sheetName() {
        return "Bulut Kaynakları";
    }

    @Override
    protected String exampleName() {
        return "Raporlama VM";
    }

    @Override
    protected CloudAssetRequest toRequest(ImportRow<CloudExportColumn> row) {
        return new CloudAssetRequest(
                row.text(CloudExportColumn.ASSET_NAME),
                row.text(CloudExportColumn.NOTES),
                row.person(CloudExportColumn.RESPONSIBILITY_OWNER),
                row.enumValue(CloudExportColumn.CLOUD_PROVIDER, CloudProvider.class, "sağlayıcı"),
                row.text(CloudExportColumn.INSTANCE_TYPE),
                row.text(CloudExportColumn.REGION),
                row.text(CloudExportColumn.ACCOUNT_ID),
                null);
    }

    @Override
    protected void create(CloudAssetRequest request) {
        cloudAssetService.create(request);
    }
}
