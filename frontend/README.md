# ITAM Frontend

React 19 + TypeScript + Vite. Backend'in `/api/v1` uçlarını tüketir.

**Durum:** Faz 2 — iskelet + kimlik doğrulama + sunucu CRUD ekranı (diğer 4 asset tipi,
bağımlılık haritası, denetim izi ve custom field ekranları henüz eklenmedi).

## Yığın

- React Router (sayfa yönlendirme)
- TanStack Query (sunucu verisi/cache yönetimi)
- Axios (HTTP istemcisi, JWT interceptor'ı ile)
- Tailwind CSS v4 (`@tailwindcss/vite`)

UI kütüphanesi seçimi tasarım dokümanında onaya bağlıydı; düşük değişim maliyeti
nedeniyle Tailwind ile başlandı (bir component library'ye geçiş, tam tersine göre
çok daha ucuz).

## Yerel çalıştırma

```bash
npm install
npm run dev
```

Vite dev sunucusu `/api` isteklerini `http://localhost:8080`'e proxy'ler (bkz.
`vite.config.ts`), backend'in ayrıca çalışıyor olması yeterli — CORS ayarı gerekmez.

## Klasör yapısı

```
src/
├── api/        axios istemcisi + her kaynak için istek fonksiyonları
├── auth/       AuthContext (JWT/localStorage) + RequireAuth route guard
├── layout/     AppLayout (sidebar + topbar)
├── pages/      route bileşenleri (asset tipi başına bir alt klasör)
└── types/      backend DTO'larıyla birebir eşleşen TS arayüzleri
```

## Build & Lint

```bash
npm run build   # tsc -b && vite build
npm run lint    # oxlint
```
