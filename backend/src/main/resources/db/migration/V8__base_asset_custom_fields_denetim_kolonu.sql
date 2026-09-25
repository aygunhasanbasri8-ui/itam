-- base_asset.custom_fields Faz 5'e kadar Java tarafında hiç map edilmemişti,
-- bu yüzden V2'deki base_asset_aud tablosuna dahil edilmemişti. Şimdi entity
-- @Audited olarak bu alanı da taşıdığı için denetim tablosunda karşılığı olmalı.
ALTER TABLE base_asset_aud ADD COLUMN custom_fields JSONB;
