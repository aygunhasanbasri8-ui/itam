# Enterprise IT Asset Management (ITAM) System

English | [Türkçe](README.tr.md)

An inventory management system that tracks all of an organization's hardware, software and business-service assets in one place, built with auditability in mind.

**Status:** Backend phases 0-6 are complete (CRUD, authentication, user management, dependency map, audit trail, custom fields, dashboard). The frontend already has list/form screens for the 5 asset types (bulk status update, Excel export, custom fields, dependency and change-history panels), person/department/user management and custom field definitions. Excel import (phase 7) is not implemented yet.

---

## Features

- **Five core asset types:** server, workstation, cloud resource, application/software and business service. Admins can also define new types from the UI.
- **Dependency map:** dependencies between assets and a tree view ("what depends on this asset").
- **Audit trail:** the change history of every asset is kept with Hibernate Envers; the record's contents are also stored when it is deleted.
- **Custom fields:** fields defined by an admin show up in forms, lists and exports.
- **Bulk status update**, search and filtering (owner, license expiry, etc.).
- **Excel export:** per asset type, with column selection.
- **Dashboard:** counts by type, status and environment; licenses that are expired or about to expire; recently added assets.
- **Person, department and user management:** JWT login with two roles: `ADMIN` has full access, `USER` can only view, filter and export.
- **Soft delete** and mandatory owner assignment (see [Data Model Notes](#data-model-notes)).
- **Production guard:** the app refuses to start in production with the default JWT secret or the default admin password.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.5.16 |
| Database | PostgreSQL 16 |
| Schema management | Flyway |
| Audit trail | Hibernate Envers |
| API docs | springdoc-openapi (Swagger UI) |
| Frontend | React 19 + TypeScript + Vite, TanStack Query, Tailwind CSS |

---

## Getting Started

### Requirements

- JDK 21
- Maven 3.9+
- Docker and Docker Compose
- Node.js and npm (for the frontend)

### 1. Start the database

```bash
docker compose up -d
```

Check that it is ready:

```bash
docker compose ps
```

The `STATUS` column should show `healthy`.

### 2. Run the backend

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

On first start Flyway applies all migrations from `V1` to `V9`: 13 core tables plus the Envers audit tables (`*_aud`).

The `dev` profile uses the default JWT secret from the repo and the seeded `admin` / `admin123`
user as they are; both are meant only for local development and tests.

### 3. Verify

| URL | Expected |
|---|---|
| http://localhost:8080/actuator/health | `{"status":"UP"}` |
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/v3/api-docs | OpenAPI JSON |

To inspect the database directly:

```bash
docker exec -it itam-postgres psql -U itam -d itam -c "\dt"
```

### 4. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

`http://localhost:5173`. Locally (`dev` profile) log in with `admin` / `admin123`. The dev server proxies `/api`
requests to the backend (8080), so no CORS setup is needed. See `frontend/README.md` for details.

Frontend commands:

```bash
npm run build   # tsc -b && vite build
npm run lint    # oxlint
```

---

## Configuration

Backend settings live in `backend/src/main/resources/application.yml` and can be overridden with these environment variables:

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/itam` | JDBC URL |
| `DB_USER` | `itam` | Database user |
| `DB_PASSWORD` | `itam` | Database password |
| `JWT_SECRET` | development key only | At least 32 bytes for HS256 |
| `JWT_EXPIRATION_MINUTES` | `480` | Token lifetime in minutes |
| `ADMIN_INITIAL_PASSWORD` | none | See below |

### Running in production

When neither the `dev` nor the `test` profile is active, the app checks the following at startup and refuses to start if they are missing:

| Variable | Description |
|---|---|
| `JWT_SECRET` | At least 32 random bytes (e.g. `openssl rand -base64 48`). It cannot be empty or equal to the development key. |
| `ADMIN_INITIAL_PASSWORD` | Required while the `admin` user's password is still `admin123` (at least 8 characters). The password is replaced with this value on first start; it is not needed on later starts. |

---

## Testing

```bash
cd backend
mvn verify
```

Integration tests start their own PostgreSQL container with Testcontainers, so they pass even when `docker compose` is not running. The Docker daemon only needs to be reachable.

Classes ending in `IT.java` are run by failsafe in the `verify` phase, classes ending in `Test.java` by surefire in the `test` phase.

There is currently no CI workflow and no frontend test in the repo.

---

## Project Structure

```
itam/
├── backend/          Spring Boot application
├── frontend/         React application
├── docker-compose.yml
└── docs/             Documentation notes
```

### Package layout

Packaging is **layer first**. The questions "which endpoints exist, which business rules exist,
which mappings exist" can each be answered by looking at a single folder.

```
com/itam/
├── config/          startup configuration, Envers revision listener
├── controller/      REST endpoints, @Valid, @PreAuthorize
├── service/
│   └── <module>/    asset, person, export ...
│       ├── XxxService          interface; the outside sees only this
│       └── impl/XxxServiceImpl @Service, implementation
├── repository/      Spring Data interfaces
│   └── spec/        helpers that turn filters into Specifications
├── mapper/          entity ↔ DTO mapping
├── model/
│   ├── entity/      JPA classes
│   └── dto/         request / response records
├── enums/           AssetStatus, AssetKind, UserRole ...
├── exception/       GlobalExceptionHandler + custom exceptions
├── validation/      custom constraints and ValidationCollector
└── security/        JWT, UserDetails, SecurityConfig (phase 4)
```

The price of layer-first packaging is that no class can stay package-private:
`ServerService` and `ServerRepository` live in different packages, so the repository
has to be `public` and the compiler cannot control who reaches for it.

That is why the dependency chain is enforced by `ArchitectureTest`. It reads the import
lines of the source files and needs no extra dependency. The rule is: **a rule that
cannot be measured is not a rule.**

| Rule | Reason |
|---|---|
| Only services and security touch repositories | A table's read rules should not live in two places |
| Controllers talk only to service interfaces | Changing a service's internals should not force controller changes |
| Services do not see web types | A service can be called from outside HTTP as well |
| Mappers do not reach into data or business rules | Mapping stays pure |
| DTOs do not depend on entities | A schema change must not silently break the API contract |
| No DDL in Java sources | The schema belongs to Flyway alone |

The last rule also forbids the `static from()` pattern that builds a DTO from an entity;
mapping is done in the `mapper/` layer.

---

## Development Rules

### Schema changes

Hibernate does **not** touch the schema (`ddl-auto: none`). Every change is added as a new Flyway migration file:

```
V10__descriptive_name.sql
V11__another_change.sql
```

An applied migration file is **never edited**: Flyway verifies checksums and the app would not start.

When adding a value to a PostgreSQL enum type, put this line at the top of the migration file:

```sql
-- flyway:executeInTransaction=false
```

A value added with `ALTER TYPE ... ADD VALUE` cannot be used inside the same transaction.

### ID generation

All sequences are defined with `INCREMENT BY 50`. On the entity side `allocationSize` **must also be 50**:

```java
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "base_asset_seq")
@SequenceGenerator(name = "base_asset_seq", sequenceName = "base_asset_seq",
                   allocationSize = 50)
```

`GenerationType.IDENTITY` is not used: it disables JDBC batch inserts and would slow down the planned Excel import (phase 7).

### Branches and commits

- `main` represents a healthy state (build and tests green)
- `feature/<topic>` is a separate branch for each task; when green it is merged into `main` with `--no-ff`

Commit messages are written in Turkish, as a single line, in the imperative style of the repo's history.

```
sunucu listeleme endpointi eklendi
```

(Example: "server listing endpoint added".)

---

## Data Model Notes

**`asset_kind` vs `asset_type`.** `asset_kind` is the JPA inheritance discriminator column and a closed enum set. `asset_type` is the type the user sees and references the `asset_type_def` table; it is dynamic because an admin can add new types from the UI. For the five core types both columns carry the same value; for admin-defined types `asset_kind = 'GENERIC'`.

**An owner is mandatory.** `base_asset.responsibility_owner_id` is NOT NULL. If the user does not pick an owner, the service layer assigns the system record with `code = 'FIRMA'`. That record cannot be deleted.

**Deletion is logical.** The `is_deleted` flag is used; `status = 'RETIRED'` is a different concept (out of service but still in the inventory).

---

## License

MIT. See [`LICENSE`](LICENSE).
