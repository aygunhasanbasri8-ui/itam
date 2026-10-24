-- Silinen (is_deleted) sunucunun hostname'i ve iş istasyonunun seri numarası aynı değerle
-- yeni kayıt açılmasını engellemesin. is_deleted base_asset'te duruyor, tekil indeksler alt
-- tablolarda; kısmi indeks başka tabloya bakamadığı için bayrağın bir kopyası alt tablolarda
-- tutuluyor ve base_asset'teki değişikliği tetikleyici taşıyor. Kopya kolonlar entity'lere
-- maplenmiyor, uygulama kodu yalnızca base_asset.is_deleted'ı bilir.

ALTER TABLE server_asset      ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE workstation_asset ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE server_asset s
   SET is_deleted = b.is_deleted
  FROM base_asset b
 WHERE b.id = s.asset_id;

UPDATE workstation_asset w
   SET is_deleted = b.is_deleted
  FROM base_asset b
 WHERE b.id = w.asset_id;

CREATE FUNCTION sync_asset_deleted_flag() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    UPDATE server_asset      SET is_deleted = NEW.is_deleted WHERE asset_id = NEW.id;
    UPDATE workstation_asset SET is_deleted = NEW.is_deleted WHERE asset_id = NEW.id;
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_base_asset_deleted_flag
    AFTER UPDATE OF is_deleted ON base_asset
    FOR EACH ROW
    WHEN (OLD.is_deleted IS DISTINCT FROM NEW.is_deleted)
    EXECUTE FUNCTION sync_asset_deleted_flag();

DROP INDEX uq_server_hostname;
CREATE UNIQUE INDEX uq_server_hostname ON server_asset (lower(hostname)) WHERE NOT is_deleted;

DROP INDEX uq_workstation_serial;
CREATE UNIQUE INDEX uq_workstation_serial ON workstation_asset (upper(serial_number)) WHERE NOT is_deleted;
