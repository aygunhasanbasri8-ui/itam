-- application_asset @Audited, aynı V2/V3/V4 deseni.

CREATE TABLE application_asset_aud (
    rev            INTEGER      NOT NULL,
    asset_id       BIGINT       NOT NULL,
    version        VARCHAR(60),
    license_type   VARCHAR(80),
    license_count  INTEGER,
    vendor         VARCHAR(150),
    expiry_date    DATE,
    install_path   VARCHAR(400),
    PRIMARY KEY (rev, asset_id),
    CONSTRAINT fk_application_asset_aud_base
        FOREIGN KEY (rev, asset_id) REFERENCES base_asset_aud (rev, id)
);
