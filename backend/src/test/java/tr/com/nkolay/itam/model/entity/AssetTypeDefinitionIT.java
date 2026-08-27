package tr.com.nkolay.itam.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import tr.com.nkolay.itam.AbstractIntegrationTest;
import tr.com.nkolay.itam.enums.AssetKind;
import tr.com.nkolay.itam.repository.AssetTypeDefinitionRepository;

/** Faz 1 kabul testi: native enum eşlemesi ve zaman damgası altyapısı. */
@DisplayName("Asset tipi eşlemesi")
class AssetTypeDefinitionIT extends AbstractIntegrationTest {

	@Autowired
	AssetTypeDefinitionRepository repository;

	@Test
	@DisplayName("native enum kolonu Java enum'una eşleniyor")
	void enumEslemesiCalisiyor() {
		AssetTypeDefinition sunucu = repository.findById("SERVER").orElseThrow();

		assertThat(sunucu.getKind()).isEqualTo(AssetKind.SERVER);
		assertThat(sunucu.isSystem()).isTrue();
	}

	@Test
	@DisplayName("dinamik tip GENERIC olarak kaydedilebiliyor")
	void dinamikTipEklenebiliyor() {
		AssetTypeDefinition agCihazi = new AssetTypeDefinition();
		agCihazi.setCode("NETWORK_DEVICE");
		agCihazi.setDisplayName("Ağ Cihazı");
		agCihazi.setKind(AssetKind.GENERIC);
		agCihazi.setIcon("fa-network-wired");

		repository.saveAndFlush(agCihazi);

		AssetTypeDefinition kayitli = repository.findById("NETWORK_DEVICE").orElseThrow();
		assertThat(kayitli.getKind()).isEqualTo(AssetKind.GENERIC);
		assertThat(kayitli.getCreatedAt()).isNotNull();
		assertThat(kayitli.getUpdatedAt()).isNotNull();
	}

	@Test
	@DisplayName("aktif tipler sıralı dönüyor")
	void aktifTiplerSirali() {
		assertThat(repository.findByActiveTrueOrderBySortOrderAsc())
				.extracting(AssetTypeDefinition::getCode)
				.startsWith("SERVER", "WORKSTATION", "CLOUD");
	}
}
