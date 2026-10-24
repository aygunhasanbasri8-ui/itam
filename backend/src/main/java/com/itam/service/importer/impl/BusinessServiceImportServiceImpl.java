package com.itam.service.importer.impl;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.itam.enums.ServiceCriticality;
import com.itam.model.dto.BusinessServiceAssetRequest;
import com.itam.repository.DepartmentRepository;
import com.itam.repository.PersonRepository;
import com.itam.service.asset.BusinessServiceAssetService;
import com.itam.service.export.BusinessServiceExportColumn;

import jakarta.validation.Validator;

@Service
public class BusinessServiceImportServiceImpl
        extends AbstractAssetImportService<BusinessServiceExportColumn, BusinessServiceAssetRequest> {

    private final BusinessServiceAssetService businessServiceAssetService;

    public BusinessServiceImportServiceImpl(BusinessServiceAssetService businessServiceAssetService,
                                            PersonRepository personRepository,
                                            DepartmentRepository departmentRepository, Validator validator) {
        super(BusinessServiceExportColumn.class, validator, personRepository, departmentRepository);
        this.businessServiceAssetService = businessServiceAssetService;
    }

    @Override
    protected Set<BusinessServiceExportColumn> requiredColumns() {
        return EnumSet.of(BusinessServiceExportColumn.ASSET_NAME);
    }

    @Override
    protected String sheetName() {
        return "İş Servisleri";
    }

    @Override
    protected String exampleName() {
        return "Online ödeme";
    }

    @Override
    protected BusinessServiceAssetRequest toRequest(ImportRow<BusinessServiceExportColumn> row) {
        return new BusinessServiceAssetRequest(
                row.text(BusinessServiceExportColumn.ASSET_NAME),
                row.text(BusinessServiceExportColumn.NOTES),
                row.person(BusinessServiceExportColumn.RESPONSIBILITY_OWNER),
                row.enumValue(BusinessServiceExportColumn.CRITICALITY, ServiceCriticality.class, "kritiklik"),
                row.text(BusinessServiceExportColumn.SLA),
                row.person(BusinessServiceExportColumn.SERVICE_OWNER),
                null);
    }

    @Override
    protected void create(BusinessServiceAssetRequest request) {
        businessServiceAssetService.create(request);
    }
}
