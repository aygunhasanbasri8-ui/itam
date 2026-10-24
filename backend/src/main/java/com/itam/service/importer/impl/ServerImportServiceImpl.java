package com.itam.service.importer.impl;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.itam.enums.ServerEnvironment;
import com.itam.model.dto.ServerAssetRequest;
import com.itam.repository.DepartmentRepository;
import com.itam.repository.PersonRepository;
import com.itam.repository.ServerAssetRepository;
import com.itam.service.asset.ServerAssetService;
import com.itam.service.export.ServerExportColumn;

import jakarta.validation.Validator;

@Service
public class ServerImportServiceImpl extends AbstractAssetImportService<ServerExportColumn, ServerAssetRequest> {

    private final ServerAssetService serverAssetService;
    private final ServerAssetRepository serverAssetRepository;

    public ServerImportServiceImpl(ServerAssetService serverAssetService, ServerAssetRepository serverAssetRepository,
                                   PersonRepository personRepository, DepartmentRepository departmentRepository,
                                   Validator validator) {
        super(ServerExportColumn.class, validator, personRepository, departmentRepository);
        this.serverAssetService = serverAssetService;
        this.serverAssetRepository = serverAssetRepository;
    }

    @Override
    protected Set<ServerExportColumn> requiredColumns() {
        return EnumSet.of(ServerExportColumn.ASSET_NAME, ServerExportColumn.HOSTNAME, ServerExportColumn.ENVIRONMENT);
    }

    @Override
    protected String sheetName() {
        return "Sunucular";
    }

    @Override
    protected String exampleName() {
        return "Web sunucusu 01";
    }

    @Override
    protected UniqueKey<ServerExportColumn> uniqueKey() {
        return new UniqueKey<>(ServerExportColumn.HOSTNAME, "hostname", h -> h.toLowerCase(Locale.ROOT),
                serverAssetRepository::findActiveHostnamesLowerCase, "Bu hostname'le kayıtlı bir sunucu zaten var");
    }

    @Override
    protected ServerAssetRequest toRequest(ImportRow<ServerExportColumn> row) {
        return new ServerAssetRequest(
                row.text(ServerExportColumn.ASSET_NAME),
                row.text(ServerExportColumn.NOTES),
                row.person(ServerExportColumn.RESPONSIBILITY_OWNER),
                row.text(ServerExportColumn.DOMAIN),
                row.text(ServerExportColumn.HOSTNAME),
                row.text(ServerExportColumn.IP_ADDRESS),
                row.enumValue(ServerExportColumn.ENVIRONMENT, ServerEnvironment.class, "ortam"),
                row.text(ServerExportColumn.SERVER_CLASS),
                row.text(ServerExportColumn.GROUP_NAME),
                null,
                null,
                row.text(ServerExportColumn.INFRASTRUCTURE_TYPE),
                row.text(ServerExportColumn.OS),
                row.bool(ServerExportColumn.VIRTUAL),
                row.person(ServerExportColumn.SERVICE_OWNER),
                row.person(ServerExportColumn.APP_RESPONSIBLE),
                row.person(ServerExportColumn.OS_RESPONSIBLE),
                null);
    }

    @Override
    protected void create(ServerAssetRequest request) {
        serverAssetService.create(request);
    }
}
