-- =============================================================
-- ITAM - Baslangic semasi
-- PostgreSQL 16 / Flyway V1
-- =============================================================
-- Not: Sequence'ler INCREMENT BY 50 ve START WITH 51 olarak
-- tanimlanmistir. Hibernate tarafinda allocationSize degeri de 50
-- olmalidir; aksi halde kimlik cakismasi olusur.
-- START WITH 51 secimi, Hibernate'in varsayilan "pooled" optimizer'i
-- ilk nextval degerini araligin ust siniri kabul ettigi icindir.
-- =============================================================

-- ============================================================
-- 0. ENUM TIPLERI
-- ============================================================
-- Bu kumeler koda da gomulu oldugundan native enum olarak tanimlandi.
-- base_asset.asset_type BILEREK enum degildir: yonetici arayuzden yeni
-- tip ekleyebildiginden asset_type_def tablosuna yabanci anahtardir.

CREATE TYPE asset_status        AS ENUM ('ACTIVE', 'RETIRED', 'MAINTENANCE');
CREATE TYPE asset_kind          AS ENUM ('SERVER', 'WORKSTATION', 'CLOUD',
                                         'APPLICATION', 'BUSINESS_SERVICE', 'GENERIC');
CREATE TYPE server_environment  AS ENUM ('DEV', 'TEST', 'UAT', 'PROD');
CREATE TYPE cloud_provider      AS ENUM ('AWS', 'AZURE', 'GCP', 'OTHER');
CREATE TYPE service_criticality AS ENUM ('HIGH', 'MID', 'LOW');
CREATE TYPE user_role           AS ENUM ('ADMIN', 'USER');
CREATE TYPE custom_field_type   AS ENUM ('TEXT', 'NUMBER', 'DATE', 'BOOLEAN');
CREATE TYPE dependency_type     AS ENUM ('RUNS_ON', 'USES', 'DEPENDS_ON');

-- ============================================================
-- 1. SEQUENCE'LER
-- ============================================================

CREATE SEQUENCE department_seq       START WITH 51 INCREMENT BY 50;
CREATE SEQUENCE person_seq           START WITH 51 INCREMENT BY 50;
CREATE SEQUENCE app_user_seq         START WITH 51 INCREMENT BY 50;
CREATE SEQUENCE custom_field_def_seq START WITH 51 INCREMENT BY 50;
CREATE SEQUENCE base_asset_seq       START WITH 51 INCREMENT BY 50;
CREATE SEQUENCE asset_dependency_seq START WITH 51 INCREMENT BY 50;

-- ============================================================
-- 2. ORGANIZASYON
-- ============================================================

CREATE TABLE department (
    id          BIGINT       PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    code        VARCHAR(30)  NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_department_code UNIQUE (code),
    CONSTRAINT uq_department_name UNIQUE (name)
);

-- is_system: silinemeyen sistem kayitlari.
-- Genel sorumlu secilmeyen varliklar FIRMA kaydina zimmetlenir.
CREATE TABLE person (
    id             BIGINT       PRIMARY KEY,
    code           VARCHAR(40),
    full_name      VARCHAR(150) NOT NULL,
    email          VARCHAR(180),
    title          VARCHAR(120),
    phone          VARCHAR(30),
    department_id  BIGINT       REFERENCES department (id),
    is_system      BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_person_email UNIQUE (email),
    CONSTRAINT uq_person_code  UNIQUE (code)
);

CREATE INDEX ix_person_department ON person (department_id);
CREATE INDEX ix_person_active     ON person (is_active);

-- Kimlik 1: sequence 51'den basladigi icin cakisma olusmaz.
INSERT INTO person (id, code, full_name, is_system, is_active)
VALUES (1, 'FIRMA', 'Firma', TRUE, TRUE);

CREATE TABLE app_user (
    id             BIGINT       PRIMARY KEY,
    username       VARCHAR(80)  NOT NULL,
    password_hash  VARCHAR(120) NOT NULL,
    role           user_role    NOT NULL DEFAULT 'USER',
    person_id      BIGINT       REFERENCES person (id),
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    last_login_at  TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_app_user_username UNIQUE (username)
);

-- ============================================================
-- 3. ASSET TIPI VE ALAN SOZLUGU
-- ============================================================

-- kind: hangi ekranin ve entity'nin kullanilacagini belirler.
-- GENERIC tiplerin detay tablosu yoktur; alanlari custom_field_def
-- sozlugunden uretilir, degerleri base_asset.custom_fields icinde durur.
CREATE TABLE asset_type_def (
    code          VARCHAR(40)  PRIMARY KEY,
    display_name  VARCHAR(100) NOT NULL,
    kind          asset_kind   NOT NULL,
    icon          VARCHAR(60)  NOT NULL DEFAULT 'fa-cube',
    description   VARCHAR(255),
    is_system     BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    sort_order    INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

INSERT INTO asset_type_def (code, display_name, kind, icon, is_system, sort_order) VALUES
    ('SERVER',           'Sunucu',             'SERVER',           'fa-server',  TRUE, 10),
    ('WORKSTATION',      'Is Istasyonu',       'WORKSTATION',      'fa-desktop', TRUE, 20),
    ('CLOUD',            'Cloud Varligi',      'CLOUD',            'fa-cloud',   TRUE, 30),
    ('APPLICATION',      'Uygulama & Yazilim', 'APPLICATION',      'fa-cubes',   TRUE, 40),
    ('BUSINESS_SERVICE', 'Is Servisi',         'BUSINESS_SERVICE', 'fa-sitemap', TRUE, 50);

-- Ilk surumde tum custom field'lar opsiyoneldir (ck_custom_field_req).
-- SELECT, PERSON_REF ve ASSET_REF tipleri sonraki faz kapsamindadir.
CREATE TABLE custom_field_def (
    id               BIGINT            PRIMARY KEY,
    asset_type_code  VARCHAR(40)       NOT NULL REFERENCES asset_type_def (code),
    field_key        VARCHAR(60)       NOT NULL,
    label            VARCHAR(120)      NOT NULL,
    data_type        custom_field_type NOT NULL,
    is_required      BOOLEAN           NOT NULL DEFAULT FALSE,
    show_in_list     BOOLEAN           NOT NULL DEFAULT FALSE,
    is_active        BOOLEAN           NOT NULL DEFAULT TRUE,
    sort_order       INTEGER           NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ       NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ       NOT NULL DEFAULT now(),
    CONSTRAINT uq_custom_field      UNIQUE (asset_type_code, field_key),
    CONSTRAINT ck_custom_field_req  CHECK (is_required = FALSE)
);

CREATE INDEX ix_custom_field_type ON custom_field_def (asset_type_code, sort_order);

-- ============================================================
-- 4. BASE ASSET
-- ============================================================

-- asset_kind : JPA kalitim ayirt edici kolonu (kapali kume)
-- asset_type : kullanicinin gordugu tip (dinamik, FK)
-- Cekirdek tiplerde iki kolon ayni degeri tasir; yonetici tanimli
-- tiplerde asset_kind = 'GENERIC' olur.
CREATE TABLE base_asset (
    id                       BIGINT       PRIMARY KEY,
    asset_name               VARCHAR(200) NOT NULL,
    asset_kind               asset_kind   NOT NULL,
    asset_type               VARCHAR(40)  NOT NULL REFERENCES asset_type_def (code),
    responsibility_owner_id  BIGINT       NOT NULL REFERENCES person (id),
    status                   asset_status NOT NULL DEFAULT 'ACTIVE',
    notes                    TEXT,
    custom_fields            JSONB        NOT NULL DEFAULT '{}'::jsonb,
    is_deleted               BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_by               VARCHAR(80),
    updated_by               VARCHAR(80)
);

CREATE INDEX ix_asset_type_status ON base_asset (asset_type, status) WHERE is_deleted = FALSE;
CREATE INDEX ix_asset_kind        ON base_asset (asset_kind)         WHERE is_deleted = FALSE;
CREATE INDEX ix_asset_owner       ON base_asset (responsibility_owner_id);
CREATE INDEX ix_asset_name        ON base_asset (lower(asset_name));

-- NOT: custom_fields uzerinde GIN indeks OLUSTURULMADI. Ilk surumde
-- custom field bazli filtreleme kapsam disi; kullanilmayan bir GIN
-- indeksi yalnizca yazma maliyeti yaratir. Filtreleme talep edildiginde
-- tek satirlik bir migration ile eklenebilir.

-- ============================================================
-- 5. ASSET TIPI DETAY TABLOLARI (JOINED INHERITANCE)
-- ============================================================

CREATE TABLE server_asset (
    asset_id             BIGINT             PRIMARY KEY REFERENCES base_asset (id) ON DELETE CASCADE,
    domain               VARCHAR(120),
    hostname             VARCHAR(150)       NOT NULL,
    ip_address           VARCHAR(45),
    environment          server_environment NOT NULL,
    server_class         VARCHAR(80),
    group_name           VARCHAR(120),
    group_info           VARCHAR(255),
    group_desc           TEXT,
    infrastructure_type  VARCHAR(80),
    os                   VARCHAR(120),
    is_virtual           BOOLEAN            NOT NULL DEFAULT FALSE,
    service_owner_id     BIGINT             REFERENCES person (id),
    app_responsible_id   BIGINT             REFERENCES person (id),
    os_responsible_id    BIGINT             REFERENCES person (id)
);

CREATE UNIQUE INDEX uq_server_hostname ON server_asset (lower(hostname));
CREATE INDEX ix_server_environment     ON server_asset (environment);
CREATE INDEX ix_server_ip              ON server_asset (ip_address);

CREATE TABLE workstation_asset (
    asset_id            BIGINT       PRIMARY KEY REFERENCES base_asset (id) ON DELETE CASCADE,
    serial_number       VARCHAR(100) NOT NULL,
    model               VARCHAR(150),
    cpu                 VARCHAR(150),
    ram_gb              INTEGER,
    disk_gb             INTEGER,
    assigned_person_id  BIGINT       REFERENCES person (id),
    department_id       BIGINT       REFERENCES department (id)
);

CREATE UNIQUE INDEX uq_workstation_serial ON workstation_asset (upper(serial_number));
CREATE INDEX ix_workstation_person        ON workstation_asset (assigned_person_id);

CREATE TABLE cloud_asset (
    asset_id        BIGINT         PRIMARY KEY REFERENCES base_asset (id) ON DELETE CASCADE,
    cloud_provider  cloud_provider NOT NULL,
    instance_type   VARCHAR(80),
    region          VARCHAR(60),
    account_id      VARCHAR(120)
);

CREATE INDEX ix_cloud_provider ON cloud_asset (cloud_provider);

CREATE TABLE application_asset (
    asset_id       BIGINT PRIMARY KEY REFERENCES base_asset (id) ON DELETE CASCADE,
    version        VARCHAR(60),
    license_type   VARCHAR(80),
    license_count  INTEGER,
    vendor         VARCHAR(150),
    expiry_date    DATE,
    install_path   VARCHAR(400)
);

CREATE INDEX ix_application_expiry ON application_asset (expiry_date);
CREATE INDEX ix_application_vendor ON application_asset (vendor);

CREATE TABLE business_service_asset (
    asset_id          BIGINT              PRIMARY KEY REFERENCES base_asset (id) ON DELETE CASCADE,
    criticality       service_criticality NOT NULL DEFAULT 'MID',
    sla               VARCHAR(120),
    service_owner_id  BIGINT              REFERENCES person (id)
);

CREATE INDEX ix_service_criticality ON business_service_asset (criticality);

-- ============================================================
-- 6. BAGIMLILIK HARITASI
-- ============================================================

CREATE TABLE asset_dependency (
    id               BIGINT          PRIMARY KEY,
    parent_asset_id  BIGINT          NOT NULL REFERENCES base_asset (id) ON DELETE CASCADE,
    child_asset_id   BIGINT          NOT NULL REFERENCES base_asset (id) ON DELETE CASCADE,
    dependency_type  dependency_type NOT NULL DEFAULT 'RUNS_ON',
    notes            VARCHAR(255),
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT now(),
    created_by       VARCHAR(80),
    CONSTRAINT uq_asset_dependency UNIQUE (parent_asset_id, child_asset_id),
    CONSTRAINT ck_dependency_self  CHECK (parent_asset_id <> child_asset_id)
);

CREATE INDEX ix_dependency_parent ON asset_dependency (parent_asset_id);
CREATE INDEX ix_dependency_child  ON asset_dependency (child_asset_id);

-- ============================================================
-- 7. DENETIM IZI (Hibernate Envers)
-- ============================================================
-- *_aud tablolari Envers tarafindan uretilecek; uretilen DDL
-- dogrulandiktan sonra V2 migration'a sabitlenecektir.

CREATE TABLE revinfo (
    rev       INTEGER     PRIMARY KEY,
    revtstmp  BIGINT,
    username  VARCHAR(80),
    ip_addr   VARCHAR(45)
);

CREATE SEQUENCE revinfo_seq START WITH 1 INCREMENT BY 50;
