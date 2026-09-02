CREATE TABLE base_asset_aud (
                                rev                      INTEGER      NOT NULL,
                                revtype                  SMALLINT,
                                id                       BIGINT       NOT NULL,
                                asset_kind               VARCHAR(31)  NOT NULL,
                                asset_name               VARCHAR(200),
                                asset_type               VARCHAR(40),
                                responsibility_owner_id  BIGINT,
                                status                   asset_status,
                                notes                    TEXT,
                                is_deleted               BOOLEAN,
                                PRIMARY KEY (rev, id),
                                CONSTRAINT fk_base_asset_aud_rev FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE INDEX ix_base_asset_aud_id ON base_asset_aud (id);

CREATE TABLE server_asset_aud (
                                  rev                  INTEGER NOT NULL,
                                  asset_id             BIGINT  NOT NULL,
                                  domain               VARCHAR(120),
                                  hostname             VARCHAR(150),
                                  ip_address           VARCHAR(45),
                                  environment          server_environment,
                                  server_class         VARCHAR(80),
                                  group_name           VARCHAR(120),
                                  group_info           VARCHAR(255),
                                  group_desc           TEXT,
                                  infrastructure_type  VARCHAR(80),
                                  os                   VARCHAR(120),
                                  is_virtual           BOOLEAN,
                                  service_owner_id     BIGINT,
                                  app_responsible_id   BIGINT,
                                  os_responsible_id    BIGINT,
                                  PRIMARY KEY (rev, asset_id),
                                  CONSTRAINT fk_server_asset_aud_base
                                      FOREIGN KEY (rev, asset_id) REFERENCES base_asset_aud (rev, id)
);