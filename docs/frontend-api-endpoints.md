    # Frontend API Endpoints Index

Bu dokuman, mevcut backend kodunu baz alarak frontend tarafi icin kullanilabilir endpointleri ozetler.

## Genel Bilgi

- Varsayilan API host (local): `http://localhost:8080`
- Canli ornek host: `https://techstack-insight-backend.onrender.com`
- Ortak hata formati: `ApiErrorResponse`
- OpenAPI JSON (aciksa): `/api-docs`
- Swagger UI (aciksa): `/swagger-ui` veya `/swagger-ui/index.html`

## Endpoint Ozet Tablosu

| Alan | Method | Path | Aciklama |
|---|---|---|---|
| Company | GET | `/api/v1/companies/{companyId}` | Sirket detayi getirir |
| Job | POST | `/api/jobs` | Yeni job kaydi olusturur |
| Job | GET | `/api/jobs` | Tum job kayitlarini listeler |
| Job | GET | `/api/jobs/{id}` | Tek job kaydini getirir |
| Skill | POST | `/api/skills` | Skill olusturur veya mevcut kaydi dondurur |
| Skill | GET | `/api/skills` | Tum skill kayitlarini listeler |
| Skill | GET | `/api/skills/{id}` | Tek skill kaydini getirir |
| Analytics | GET | `/api/analytics/top-skills` | En cok gecen skill listesi |
| Analytics | GET | `/api/analytics/location-trends` | Lokasyon bazli job trendleri |
| Analytics | GET | `/api/analytics/role-skill-distribution` | Rol + skill dagilimi |
| Ingestion Admin | POST | `/api/admin/ingestion/run` | Tum aktif kaynaklar icin manuel ingestion |
| Ingestion Admin | POST | `/api/admin/ingestion/run-source?type=...&token=...` | Tek kaynagi manuel ingest eder |
| Ops | GET | `/actuator/health` | Health check endpoint |

> Not: Path versiyonlamasi su anda karisik: `Company` endpointi `/api/v1/...`, digerleri `/api/...`.

## Endpoint Detaylari

### 1) Company

#### GET `/api/v1/companies/{companyId}`
- Path param:
  - `companyId` (UUID)
- 200 Response (`CompanyResponseDto`):
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Vercel",
  "externalSource": "GREENHOUSE",
  "externalCompanyId": "vercel",
  "createdAt": "2026-04-23T10:20:30Z",
  "updatedAt": "2026-04-23T10:20:30Z"
}
```
- Olasi hata:
  - `404 Not Found` (kayit yoksa)

---

### 2) Job

#### POST `/api/jobs`
- Body (`CreateJobRequestDto`):
```json
{
  "externalId": "12345",
  "source": "GREENHOUSE",
  "title": "Backend Engineer",
  "location": "Remote",
  "description": "Java ve Spring Boot...",
  "applyUrl": "https://example.com/apply",
  "postedAt": "2026-04-23",
  "companyId": "3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```
- Validasyon:
  - `externalId`, `source`, `title`, `location`, `description`, `applyUrl`: bos olamaz
  - `postedAt`, `companyId`: null olamaz
- 201 Response (`JobResponseDto`)

#### GET `/api/jobs`
- 200 Response: `JobResponseDto[]`

#### GET `/api/jobs/{id}`
- Path param:
  - `id` (Long)
- 200 Response: `JobResponseDto`
- Olasi hata:
  - `404 Not Found`

`JobResponseDto` alanlari:
- `id`, `externalId`, `source`, `title`
- `normalizedTitle`, `softwareRelevant`, `roleFamily`, `roleSubfamily`
- `location`, `locationNormalized`, `country`, `remote`, `hybrid`
- `description`, `applyUrl`, `postedAt`
- `companyId`, `companyName`

---

### 3) Skill

#### POST `/api/skills`
- Body (`CreateSkillRequestDto`):
```json
{
  "name": "Spring Boot"
}
```
- Validasyon:
  - `name`: bos olamaz
  - `name`: max 255
- 201 Response (`SkillResponseDto`)

#### GET `/api/skills`
- 200 Response: `SkillResponseDto[]`

#### GET `/api/skills/{id}`
- Path param:
  - `id` (Long)
- 200 Response: `SkillResponseDto`
- Olasi hata:
  - `404 Not Found`

`SkillResponseDto` alanlari:
- `id`, `name`, `createdAt`, `updatedAt`

---

### 4) Analytics (Read-only)

#### GET `/api/analytics/top-skills`
- Query param (opsiyonel):
  - `limit` (Integer)
- 200 Response (`TopSkillResponseDto[]`):
```json
[
  { "skillName": "Java", "jobCount": 120 },
  { "skillName": "Spring Boot", "jobCount": 95 }
]
```

#### GET `/api/analytics/location-trends`
- 200 Response (`LocationTrendResponseDto[]`):
```json
[
  { "location": "Remote", "jobCount": 320 },
  { "location": "Istanbul", "jobCount": 70 }
]
```

#### GET `/api/analytics/role-skill-distribution`
- 200 Response (`RoleSkillDistributionResponseDto[]`):
```json
[
  { "jobTitle": "Backend Engineer", "skillName": "Java", "jobCount": 80 }
]
```

---

### 5) Ingestion Admin (Frontend panelde admin ekran varsa)

#### POST `/api/admin/ingestion/run`
- Body: yok
- 200 Response: `IngestionRunStatsDto[]`

#### POST `/api/admin/ingestion/run-source?type=GREENHOUSE&token=vercel`
- Query param:
  - `type` (`GREENHOUSE` veya `LEVER`)
  - `token` (kaynak token)
- 200 Response: `IngestionRunStatsDto`

`IngestionRunStatsDto` alanlari:
- `source`, `token`
- `fetchedCount`, `insertedCount`, `skippedCount`
- `softwareRelevantCount`, `extractedSkillsCount`
- `failureCount`, `companyReusedAfterDuplicateCount`
- `runDurationMs`, `status`

---

### 6) Ops

#### GET `/actuator/health`
- Render health check icin kullanilir.

## Ortak Hata Formati

Tum endpointlerde ortak hata response tipi (`ApiErrorResponse`):

```json
{
  "timestamp": "2026-04-23T10:20:30Z",
  "status": 404,
  "error": "Not Found",
  "message": "Resource not found",
  "path": "/api/skills/999"
}
```

## Frontend Icın Kisa Entegrasyon Notlari

- En hizli baslangic icin once su endpointleri bagla:
  1. `GET /api/analytics/top-skills`
  2. `GET /api/analytics/location-trends`
  3. `GET /api/analytics/role-skill-distribution`
  4. `GET /api/jobs`
  5. `GET /api/skills`
- Admin panel varsa, ingestion endpointlerini ayri bir admin UI altinda kullan.
- Swagger prod ortaminda kapali olabilir; dokuman icin bu dosya + `/api-docs` (aciksa) birlikte kullanilabilir.

