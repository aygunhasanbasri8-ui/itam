-- cloud_asset @Audited, aynı V2/V3 deseni.

CREATE TABLE cloud_asset_aud (
    rev             INTEGER        NOT NULL,
    asset_id        BIGINT         NOT NULL,
    cloud_provider  cloud_provider,
    instance_type   VARCHAR(80),
    region          VARCHAR(60),
    account_id      VARCHAR(120),
    PRIMARY KEY (rev, asset_id),
    CONSTRAINT fk_cloud_asset_aud_base
        FOREIGN KEY (rev, asset_id) REFERENCES base_asset_aud (rev, id)
);
