package com.itam.service.importer.impl;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.itam.model.dto.ApplicationAssetRequest;
import com.itam.repository.DepartmentRepository;
import com.itam.repository.PersonRepository;
import com.itam.service.asset.ApplicationAssetService;
import com.itam.service.export.ApplicationExportColumn;

import jakarta.validation.Validator;

@Service
public class ApplicationImportServiceImpl
        extends AbstractAssetImportService<ApplicationExportColumn, ApplicationAssetRequest> {

    private final ApplicationAssetService applicationAssetService;

    public ApplicationImportServiceImpl(ApplicationAssetService applicationAssetService,
                                        PersonRepository personRepository, DepartmentRepository departmentRepository,
                                        Validator validator) {
        super(ApplicationExportColumn.class, validator, personRepository, departmentRepository);
        this.applicationAssetService = applicationAssetService;
    }

    @Override
    protected Set<ApplicationExportColumn> requiredColumns() {
        return EnumSet.of(ApplicationExportColumn.ASSET_NAME);
    }

    @Override
    protected String sheetName() {
        return "Uygulamalar";
    }

    @Override
    protected String exampleName() {
        return "Muhasebe yazılımı";
    }

    // Aynı uygulamanın farklı sürümleri ayrı kayıt olabildiği için tekillik kontrolü yok

    @Override
    protected ApplicationAssetRequest toRequest(ImportRow<ApplicationExportColumn> row) {
        return new ApplicationAssetRequest(
                row.text(ApplicationExportColumn.ASSET_NAME),
                row.text(ApplicationExportColumn.NOTES),
                row.person(ApplicationExportColumn.RESPONSIBILITY_OWNER),
                row.text(ApplicationExportColumn.VERSION),
                row.text(ApplicationExportColumn.LICENSE_TYPE),
                row.integer(ApplicationExportColumn.LICENSE_COUNT),
                row.text(ApplicationExportColumn.VENDOR),
                row.date(ApplicationExportColumn.EXPIRY_DATE),
                row.text(ApplicationExportColumn.INSTALL_PATH),
                null);
    }

    @Override
    protected void create(ApplicationAssetRequest request) {
        applicationAssetService.create(request);
    }
}
