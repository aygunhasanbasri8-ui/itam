package com.itam.service.export;

import com.itam.model.entity.BaseAsset;

/**
 * Bir asset tipinin alan kataloğu: kod, ekranda görünen başlık ve değerin nasıl okunacağı.
 * Excel'e aktarım ve değişiklik geçmişi aynı kataloğu kullanıyor; yeni bir alan bir yerde
 * tanımlanınca ikisine de yansısın diye.
 */
public interface AssetColumn<T extends BaseAsset> {

    String code();

    String header();

    Object valueOf(T asset);
}
