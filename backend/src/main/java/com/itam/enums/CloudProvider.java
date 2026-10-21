package com.itam.enums;

/**
 * Cloud varlığının sağlayıcısı.
 * <p>
 * Değer adları PostgreSQL enum tipiyle birebir aynıdır; Hibernate NAMED_ENUM
 * eşlemesi isim üzerinden çalışır.
 */
public enum CloudProvider {
	AWS, AZURE, GCP, OTHER
}
