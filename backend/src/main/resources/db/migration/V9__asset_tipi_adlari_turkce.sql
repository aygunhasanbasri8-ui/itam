-- V1'deki tohum kayıtlar Türkçe karakter olmadan yazılmıştı; Asset Tipleri
-- ekranında kullanıcıya bu adlar gösterileceği için düzeltiliyor.
UPDATE asset_type_def SET display_name = 'İş İstasyonu'        WHERE code = 'WORKSTATION'      AND display_name = 'Is Istasyonu';
UPDATE asset_type_def SET display_name = 'Cloud Varlığı'       WHERE code = 'CLOUD'            AND display_name = 'Cloud Varligi';
UPDATE asset_type_def SET display_name = 'Uygulama & Yazılım'  WHERE code = 'APPLICATION'      AND display_name = 'Uygulama & Yazilim';
UPDATE asset_type_def SET display_name = 'İş Servisi'          WHERE code = 'BUSINESS_SERVICE' AND display_name = 'Is Servisi';
