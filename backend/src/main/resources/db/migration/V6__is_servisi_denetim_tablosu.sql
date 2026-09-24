-- business_service_asset @Audited, aynı V2/V3/V4/V5 deseni.

CREATE TABLE business_service_asset_aud (
    rev               INTEGER              NOT NULL,
    asset_id          BIGINT               NOT NULL,
    criticality       service_criticality,
    sla               VARCHAR(120),
    service_owner_id  BIGINT,
    PRIMARY KEY (rev, asset_id),
    CONSTRAINT fk_business_service_asset_aud_base
        FOREIGN KEY (rev, asset_id) REFERENCES base_asset_aud (rev, id)
);
