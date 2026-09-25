# Kurumsal IT Asset Management (ITAM) Sistemi

Kurum bünyesindeki tüm donanım, yazılım ve iş servisi varlıklarının tek merkezden yönetildiği, denetim standartlarına uygun envanter yönetim sistemi.

**Durum:** Backend Faz 0-6 tamamlandı (CRUD, kimlik doğrulama, bağımlılık haritası, denetim izi, custom field, dashboard). Frontend Faz 2'de: iskelet + kimlik doğrulama + sunucu CRUD ekranı hazır, kalan 4 asset tipi ve özel ekranlar (bağımlılık haritası, denetim izi, custom field) sırada.

---

## Yığın

| Katman | Teknoloji |
|---|---|
| Backend | Java 21, Spring Boot 3.5.16 |
| Veritabanı | PostgreSQL 16 |
| Şema yönetimi | Flyway |
| Denetim izi | Hibernate Envers |
| API dokümantasyonu | springdoc-openapi (Swagger UI) |
| Frontend | React 19 + TypeScript + Vite, TanStack Query, Tailwind CSS |

---

## Yerel Kurulum

### Gereksinimler

- JDK 21
- Maven 3.9+
- Docker ve Docker Compose

### 1. Veritabanını başlat

```bash
docker compose up -d
```

Hazır olduğunu doğrula:

```bash
docker compose ps
```

`STATUS` sütununda `healthy` görünmeli.

### 2. Uygulamayı çalıştır

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

İlk açılışta Flyway `V1__init_schema.sql` dosyasını uygular ve 13 tabloyu oluşturur.

### 3. Doğrula

| Adres | Beklenen |
|---|---|
| http://localhost:8080/actuator/health | `{"status":"UP"}` |
| http://localhost:8080/swagger-ui.html | Swagger arayüzü |
| http://localhost:8080/v3/api-docs | OpenAPI JSON |

Veritabanını doğrudan kontrol etmek için:

```bash
docker exec -it itam-postgres psql -U itam -d itam -c "\dt"
```

---

## Test

```bash
cd backend
mvn verify
```

Integration test'ler Testcontainers ile kendi PostgreSQL konteynerini ayağa kaldırır; `docker compose` çalışıyor olmasa da testler geçer. Docker daemon'ın erişilebilir olması yeterlidir.

`*IT.java` ile biten sınıflar failsafe tarafından `verify` aşamasında, `*Test.java` ile bitenler surefire tarafından `test` aşamasında çalışır.

### 4. Frontend'i çalıştır

```bash
cd frontend
npm install
npm run dev
```

`http://localhost:5173` — giriş: `admin` / `admin123`. Dev sunucusu `/api` isteklerini
otomatik olarak backend'e (8080) proxy'ler, ayrıca CORS ayarı gerekmez. Ayrıntılar için
`frontend/README.md`.

---

## Proje Yapısı

```
itam/
├── backend/          Spring Boot uygulaması
├── frontend/         React uygulaması
├── docker/           docker-compose.yml
└── docs/             Tasarım raporu ve yol haritası
```

### Paket düzeni

Paketleme **katman önce**. "Sistemde hangi uçlar var, hangi iş kuralları var,
hangi çevrimler var" sorusu tek klasöre bakarak yanıtlanır.

```
tr/com/nkolay/itam/
├── config/          açılış yapılandırması, Envers revizyon dinleyicisi
├── controller/      REST uçları, @Valid, @PreAuthorize
├── service/
│   └── <modül>/     asset, person, export, importer ...
│       ├── XxxService          arayüz — dışarısı yalnızca bunu görür
│       └── impl/XxxServiceImpl @Service, gerçekleştirim
├── repository/      Spring Data arayüzleri
│   └── spec/        filtreleri Specification'a çeviren yardımcılar
├── mapper/          entity ↔ DTO çevrimi
├── model/
│   ├── entity/      JPA sınıfları
│   └── dto/         request / response record'ları
├── enums/           AssetStatus, AssetKind, UserRole ...
├── exception/       GlobalExceptionHandler + özel exception'lar
├── validation/      özel constraint'ler ve ValidationCollector
└── security/        JWT, UserDetails, SecurityConfig (Faz 4)
```

Katman-önce düzenin bedeli, hiçbir sınıfın package-private kalamamasıdır:
`ServerService` ile `ServerRepository` farklı paketlerde olduğu için repository
`public` olmak zorunda ve derleyici kimin ona uzandığını denetleyemez.

Bu yüzden zincir `ArchitectureTest` ile korunur. Kaynak dosyaların import
satırlarını okur, ek bağımlılık gerektirmez. Kural şudur: **ölçülemeyen kural
kural değildir.**

| Kural | Gerekçe |
|---|---|
| Repository'ye yalnızca servis ve güvenlik dokunur | Bir tablonun okunma kuralı iki yerde yaşamasın |
| Controller yalnızca servis arayüzüne konuşur | Servisin içi değişince controller açılmasın |
| Servis web tiplerini görmez | Servis HTTP dışından da çağrılabilsin |
| Mapper veriye ve iş kuralına uzanmaz | Çevrim saf kalsın |
| DTO entity'ye bağımlı olmaz | Şema değişikliği API sözleşmesini habersiz kırmasın |
| Java kaynağında DDL geçmez | Şema yalnızca Flyway'in |

Son kural, entity'den DTO üreten `static from()` desenini de yasaklar; çevrim
`mapper/` katmanında yapılır.

---

## Geliştirme Kuralları

### Şema değişikliği

Şemaya Hibernate **dokunmaz** (`ddl-auto: validate`). Her değişiklik yeni bir Flyway migration dosyası olarak eklenir:

```
V2__aciklayici_isim.sql
V3__baska_bir_degisiklik.sql
```

Uygulanmış bir migration dosyası **asla düzenlenmez** — Flyway checksum doğrulaması yapar ve uygulama açılmaz.

PostgreSQL enum tipine değer eklerken migration dosyasının başına şu satır konulmalıdır:

```sql
-- flyway:executeInTransaction=false
```

`ALTER TYPE ... ADD VALUE` ile eklenen değer aynı transaction içinde kullanılamaz.

### Kimlik üretimi

Tüm sequence'ler `INCREMENT BY 50` ile tanımlıdır. Entity tarafında `allocationSize` da **50 olmalıdır**:

```java
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "base_asset_seq")
@SequenceGenerator(name = "base_asset_seq", sequenceName = "base_asset_seq",
                   allocationSize = 50)
```

`GenerationType.IDENTITY` kullanılmaz — JDBC batch insert'i devre dışı bırakır ve Faz 7'deki Excel içe aktarımını yavaşlatır.

### Branch ve commit

- `main` — sağlam durumu temsil eder (derleme + testler yeşil)
- `feature/<konu>` — her iş için ayrı dal, yeşilse `main`'e `--no-ff` merge edilir

Commit mesajı: tek satır, Türkçe, emir kipi.

```
sunucu listeleme endpointi eklendi
```

---

## Veri Modeli Notları

**`asset_kind` ve `asset_type` ayrımı.** `asset_kind` JPA kalıtım ayırt edici kolonudur ve kapalı bir enum kümesidir. `asset_type` ise kullanıcının gördüğü tiptir ve `asset_type_def` tablosuna referans verir — yönetici arayüzden yeni tip ekleyebildiği için dinamiktir. Çekirdek beş tipte iki kolon aynı değeri taşır; yönetici tanımlı tiplerde `asset_kind = 'GENERIC'` olur.

**Sorumlu ataması zorunludur.** `base_asset.responsibility_owner_id` NOT NULL'dır. Kullanıcı sorumlu seçmezse servis katmanı `code = 'FIRMA'` olan sistem kaydını atar. Bu kayıt silinemez.

**Silme mantıksaldır.** `is_deleted` bayrağı kullanılır; `status = 'RETIRED'` ise farklı bir kavramdır (hizmet dışı ama envanterde).

Ayrıntılar için `docs/ITAM_Tasarim_Raporu.pdf`.
