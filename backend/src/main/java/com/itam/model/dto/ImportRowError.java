package com.itam.model.dto;

/**
 * İçe aktarılan dosyadaki tek bir hata.
 *
 * @param row    Excel'deki satır numarası (1'den başlar, kullanıcının gördüğü numara)
 * @param column hatanın ait olduğu kolon başlığı; satırın tamamıyla ilgiliyse null
 */
public record ImportRowError(int row, String column, String message) {
}
