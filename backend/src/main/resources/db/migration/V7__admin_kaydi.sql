-- UserRole.java'daki K5 kararı: iki rol (ADMIN/USER) yeterli, PDF tasarım
-- raporundaki üç seviyeli ADMIN/EDITOR/VIEWER matrisi burada uygulanmadı —
-- bu, dokümana değil kodun kendi (zaten verilmiş) kararına uyuyor.
--
-- Kimlik 1: app_user_seq 51'den başladığı için çakışma olmaz (person/FIRMA
-- kaydıyla aynı desen).
INSERT INTO app_user (id, username, password_hash, role, person_id, is_active)
VALUES (1, 'admin', '$2a$10$JX7aMnFLsm/.LX34g43DnOl9aIeS47A/4yI7aAdh/KNuLtu4yc05G', 'ADMIN', 1, TRUE);
