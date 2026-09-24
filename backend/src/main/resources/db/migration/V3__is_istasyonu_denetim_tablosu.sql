-- workstation_asset @Audited oldugu icin Envers'in ihtiyac duydugu aud tablosu.
-- server_asset_aud ile ayni desen (V2'ye bak): rev+asset_id birlesik anahtar,
-- base_asset_aud'a FK.

CREATE TABLE workstation_asset_aud (
    rev                 INTEGER      NOT NULL,
    asset_id            BIGINT       NOT NULL,
    serial_number       VARCHAR(100),
    model               VARCHAR(150),
    cpu                 VARCHAR(150),
    ram_gb              INTEGER,
    disk_gb             INTEGER,
    assigned_person_id  BIGINT,
    department_id       BIGINT,
    PRIMARY KEY (rev, asset_id),
    CONSTRAINT fk_workstation_asset_aud_base
        FOREIGN KEY (rev, asset_id) REFERENCES base_asset_aud (rev, id)
);
