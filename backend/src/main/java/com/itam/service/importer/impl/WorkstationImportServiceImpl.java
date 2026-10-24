package com.itam.service.importer.impl;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.itam.model.dto.WorkstationAssetRequest;
import com.itam.repository.DepartmentRepository;
import com.itam.repository.PersonRepository;
import com.itam.repository.WorkstationAssetRepository;
import com.itam.service.asset.WorkstationAssetService;
import com.itam.service.export.WorkstationExportColumn;

import jakarta.validation.Validator;

@Service
public class WorkstationImportServiceImpl
        extends AbstractAssetImportService<WorkstationExportColumn, WorkstationAssetRequest> {

    private final WorkstationAssetService workstationAssetService;
    private final WorkstationAssetRepository workstationAssetRepository;

    public WorkstationImportServiceImpl(WorkstationAssetService workstationAssetService,
                                        WorkstationAssetRepository workstationAssetRepository,
                                        PersonRepository personRepository, DepartmentRepository departmentRepository,
                                        Validator validator) {
        super(WorkstationExportColumn.class, validator, personRepository, departmentRepository);
        this.workstationAssetService = workstationAssetService;
        this.workstationAssetRepository = workstationAssetRepository;
    }

    @Override
    protected Set<WorkstationExportColumn> requiredColumns() {
        return EnumSet.of(WorkstationExportColumn.ASSET_NAME, WorkstationExportColumn.SERIAL_NUMBER);
    }

    @Override
    protected String sheetName() {
        return "İş İstasyonları";
    }

    @Override
    protected String exampleName() {
        return "Muhasebe dizüstü 03";
    }

    @Override
    protected UniqueKey<WorkstationExportColumn> uniqueKey() {
        // İndeks upper(serial_number) üzerinde (V1, V10)
        return new UniqueKey<>(WorkstationExportColumn.SERIAL_NUMBER, "seri no", s -> s.toUpperCase(Locale.ROOT),
                workstationAssetRepository::findActiveSerialNumbersUpperCase,
                "Bu seri numarasıyla kayıtlı bir iş istasyonu zaten var");
    }

    @Override
    protected WorkstationAssetRequest toRequest(ImportRow<WorkstationExportColumn> row) {
        return new WorkstationAssetRequest(
                row.text(WorkstationExportColumn.ASSET_NAME),
                row.text(WorkstationExportColumn.NOTES),
                row.person(WorkstationExportColumn.RESPONSIBILITY_OWNER),
                row.text(WorkstationExportColumn.SERIAL_NUMBER),
                row.text(WorkstationExportColumn.MODEL),
                row.text(WorkstationExportColumn.CPU),
                row.integer(WorkstationExportColumn.RAM_GB),
                row.integer(WorkstationExportColumn.DISK_GB),
                row.person(WorkstationExportColumn.ASSIGNED_PERSON),
                row.department(WorkstationExportColumn.DEPARTMENT),
                null);
    }

    @Override
    protected void create(WorkstationAssetRequest request) {
        workstationAssetService.create(request);
    }
}
