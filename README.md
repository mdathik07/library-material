# Reading Material Library

A Spring Boot backend service for managing a versioned library of reading materials for staff.

The system supports:

- PDF
- Article
- Book
- Slide Deck

The application supports immutable material versions, `DRAFT` / `LIVE` / `ARCHIVED` lifecycle, rollback to older versions, reader-specific progress, version-aware continuation, catalog search, database-side PDF page-count filtering, and pagination.

---

# 1. Technology Stack

- Java 21
- Spring Boot 4.1.0
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Maven
- Testcontainers
- JUnit 5
- Mockito
- SpringDoc OpenAPI / Swagger UI
- Docker / Docker Compose

---
# 2 final run command:
# 3 java "-Duser.timezone=UTC" -jar target\reading-material-library-0.0.1-SNAPSHOT.jar



# 2. Architecture

The application follows a layered architecture:

```text
                    HTTP / REST
                        │
                        ▼
                 ┌─────────────┐
                 │ Controllers │
                 └──────┬──────┘
                        │
                        ▼
                 ┌─────────────┐
                 │  Services   │
                 └──────┬──────┘
                        │
          ┌─────────────┼─────────────┐
          │             │             │
          ▼             ▼             ▼
    Validation      Kind-specific  Repositories
                   Strategies
                                       │
                                       ▼
                                ┌─────────────┐
                                │ PostgreSQL  │
                                └─────────────┘
```

### Main responsibilities

**Controllers**

Handle HTTP requests, headers, path variables, query parameters, request validation, and HTTP responses.

**Services**

Contain business rules such as:

- material creation
- draft version creation
- draft editing
- publishing
- rollback
- reader progress
- completion calculation
- reader continuation
- catalog search
- pagination

**Repositories**

Provide database access through Spring Data JPA, including database-backed filtering, pagination, and projections.

**Validation and kind-specific strategies**

Kind-specific behavior is separated into validator, creator, copier, updater, and reader-position components.

This keeps the system extensible when a new material kind is introduced.

---

# 3. Database Design

The logical material, immutable versions, kind-specific details, and reader progress are stored separately.

```text
materials
    │
    │ 1 : N
    ▼
material_versions
    │
    ├─────────────── 1 : 0..1 ────────► pdf_details
    │
    ├─────────────── 1 : 0..1 ────────► article_details
    │
    ├─────────────── 1 : 0..1 ────────► book_details
    │
    └─────────────── 1 : 0..1 ────────► slide_deck_details

reader_progress
    │
    └─────────────── N : 1 ──────────► material_versions
```

Each version is associated with the kind-specific details matching the material kind.
This invariant is enforced by the application layer and covered by automated tests.

## 3.1 `materials`

Stores the logical material.

| Column | Description |
|---|---|
| `id` | Primary key |
| `title` | Material title |
| `kind` | `PDF`, `ARTICLE`, `BOOK`, or `SLIDE_DECK` |
| `created_at` | Creation timestamp |

## 3.2 `material_versions`

Stores every version of a material.

| Column | Description |
|---|---|
| `id` | Primary key |
| `material_id` | Foreign key → `materials.id` |
| `version_number` | Version number within the material |
| `status` | `DRAFT`, `LIVE`, or `ARCHIVED` |
| `created_at` | Version creation timestamp |
| `published_at` | Publication timestamp |

Versions are never deleted.

## 3.3 `pdf_details`

| Column | Description |
|---|---|
| `version_id` | Primary/foreign key → `material_versions.id` |
| `page_count` | Total number of pages |
| `file_url` | PDF location |

## 3.4 `article_details`

| Column | Description |
|---|---|
| `version_id` | Primary/foreign key → `material_versions.id` |
| `word_count` | Total word count |

## 3.5 `book_details`

| Column | Description |
|---|---|
| `version_id` | Primary/foreign key → `material_versions.id` |
| `author` | Book author |
| `chapters` | Chapter list stored as PostgreSQL `JSONB` |

## 3.6 `slide_deck_details`

| Column | Description |
|---|---|
| `version_id` | Primary/foreign key → `material_versions.id` |
| `slide_count` | Total number of slides |
| `file_url` | Slide deck location |

## 3.7 `reader_progress`

| Column | Description |
|---|---|
| `id` | Primary key |
| `reader_id` | Reader identifier |
| `material_version_id` | Foreign key → `material_versions.id` |
| `position` | Kind-specific reading position |
| `completed` | Server-calculated completion state |
| `created_at` | Creation timestamp |
| `updated_at` | Last update timestamp |

---

# 4. Important Database Constraints

## 4.1 Unique version numbers

A material cannot have duplicate version numbers:

```sql
UNIQUE (material_id, version_number)
```

For example:

```text
material 1 → version 1
material 1 → version 2
material 1 → version 3
```

but not two version 2 rows.

## 4.2 Exactly one LIVE version

The database enforces the most important versioning invariant:

```sql
CREATE UNIQUE INDEX ux_one_live_version_per_material
ON material_versions(material_id)
WHERE status = 'LIVE';
```

Therefore the database itself prevents two LIVE versions from existing for the same material.

## 4.3 Unique reader progress

Reader progress is unique per reader and version:

```sql
UNIQUE (reader_id, material_version_id)
```

Therefore a reader has at most one progress record for a particular material version.

---

# 5. Material Version Lifecycle

A material follows this lifecycle:

```text
Create
  │
  ▼
DRAFT
  │
  │ Publish
  ▼
LIVE
  │
  │ New version published
  ▼
ARCHIVED
```

### Editing rules

- `DRAFT` versions can be edited.
- `LIVE` versions cannot be edited.
- `ARCHIVED` versions cannot be edited.
- `DRAFT` versions can be published.
- `ARCHIVED` versions can be published again for rollback.
- Versions are never deleted.

### Creating a new version

A new version is created by copying the current LIVE version:

```text
LIVE v1
   │
   │ copy
   ▼
DRAFT v2
   │
   │ edit
   ▼
publish
   │
   ├── v1 → ARCHIVED
   └── v2 → LIVE
```

---

# 6. Publishing and Concurrency

Publishing is transactional.

The service locks the material row using a pessimistic write lock:

```text
Material row
     │
     ▼
PESSIMISTIC_WRITE
     │
     ├── archive current LIVE version
     ├── flush
     ├── make target version LIVE
     └── flush
```

This serializes concurrent publication attempts for the same material.

The PostgreSQL partial unique index remains the final database-level safety guarantee.

Therefore two concurrent admins cannot successfully leave two LIVE versions for the same material.

---

# 7. Rollback

Rollback does not create a new synthetic version.

Instead, an existing ARCHIVED version can be published again:

```text
Before:

v1 → ARCHIVED
v2 → LIVE

Rollback v1:

v1 → LIVE
v2 → ARCHIVED
```

The original contents of v1 remain unchanged.

---

# 8. Reader Identity

Authentication is intentionally outside the assignment scope.

For development and testing, the reader is identified using:

```http
X-Reader-Id: 101
```

Example:

```http
GET /api/materials/1
X-Reader-Id: 101
```

In a real production deployment, this should be replaced with authenticated user identity.

---

# 9. Reader Progress Model

Reader progress is stored against the exact version:

```text
reader_id + material_version_id
```

For example:

```text
Reader 101
Material 1
Version 1
Position 20
```

If version 2 is later published, the saved progress remains attached to version 1.

It is not silently converted to version 2 because the meaning of a position may change between versions.

---

# 10. Position Semantics

The meaning of `position` depends on the material kind.

| Kind | Position |
|---|---|
| PDF | Page number |
| Article | Percentage |
| Book | Chapter number |
| Slide Deck | Slide number |

The backend validates the position against the exact version being read.

---

# 11. Completion Rules

The client does not submit the `completed` field.

Completion is calculated by the backend.

| Kind | Completed when |
|---|---|
| PDF | Last page is reached |
| Article | Position reaches 100% |
| Book | Last chapter is reached |
| Slide Deck | Last slide is reached |

Examples:

```text
PDF:
page 20 / 50  → incomplete
page 50 / 50  → complete

Article:
98% → incomplete
100% → complete

Book:
chapter 3 / 4 → incomplete
chapter 4 / 4 → complete
```

For books, the chapter list itself determines the final chapter.

---

# 12. Reader APIs

## 12.1 Open current LIVE material

```http
GET /api/materials/{materialId}
X-Reader-Id: 101
```

Returns the current LIVE version.

If the material has no LIVE version, the reader cannot open it.

## 12.2 Save reader progress

```http
PUT /api/materials/{materialId}/versions/{versionId}/progress
X-Reader-Id: 101
Content-Type: application/json
```

Example:

```json
{
  "position": 20
}
```

The backend validates the position and calculates completion.

## 12.3 Reader's own material list

```http
GET /api/me/materials
X-Reader-Id: 101
```

Supports:

```text
status=IN_PROGRESS
status=COMPLETED
page
size
```

Example:

```http
GET /api/me/materials?status=IN_PROGRESS&page=0&size=20
X-Reader-Id: 101
```

The list remains version-aware.

## 12.4 Continue reading

```http
GET /api/me/materials/{materialId}/reading
X-Reader-Id: 101
```

This returns the reader's saved version and position.

This is intentionally different from:

```text
GET /api/materials/{materialId}
    → current LIVE version

GET /api/me/materials/{materialId}/reading
    → reader's saved version and position
```

Example:

```text
Reader saved v1 / page 20

v2 becomes LIVE

Open material
    → v2

Continue reading
    → v1 / page 20
```

---

# 13. Admin APIs

## 13.1 Create material

```http
POST /api/admin/materials
Content-Type: application/json
```

Example:

```json
{
  "title": "Intro to SQL",
  "kind": "PDF",
  "details": {
    "pageCount": 50,
    "fileUrl": "https://example.com/sql.pdf"
  }
}
```

Creates version 1 as `DRAFT`.

## 13.2 Create draft version

```http
POST /api/admin/materials/{materialId}/versions
```

Copies the current LIVE version into a new DRAFT.

## 13.3 Edit draft version

```http
PUT /api/admin/materials/{materialId}/versions/{versionId}
Content-Type: application/json
```

Only DRAFT versions can be edited.

## 13.4 Publish version

```http
POST /api/admin/materials/{materialId}/versions/{versionId}/publish
```

A DRAFT or ARCHIVED version can be published.

---

# 14. Catalog APIs

## 14.1 List published materials

```http
GET /api/materials
```

Supports:

```text
page
size
kind
minPages
```

Example:

```http
GET /api/materials?page=0&size=20
```

## 14.2 Filter by kind

```http
GET /api/materials?kind=PDF
```

## 14.3 PDF page-count search

```http
GET /api/materials?kind=PDF&minPages=100
```

The page-count filter is executed in PostgreSQL.

The application does not load the entire catalog into Java and filter it in memory.

## 14.4 Pagination

Pagination is database-backed using `Pageable`.

Example:

```http
GET /api/materials?page=0&size=20
```

Response includes pagination metadata:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

The reader's own list uses the same database-backed pagination approach.

---

# 15. Material Kind Extensibility

Kind-specific details are kept in separate tables:

```text
materials
    │
    ▼
material_versions
    │
    ├── pdf_details
    ├── article_details
    ├── book_details
    └── slide_deck_details
```

Adding a fifth kind such as VIDEO would involve:

```text
video_details
    version_id
    duration_minutes
```

plus the corresponding Java components for:

- validation
- creation
- copying
- updating
- reader-position/completion logic
- response mapping

Existing tables and existing rows do not need to be changed.

---

# 16. Validation

Kind-specific details are validated before creation and update.

### PDF

- `pageCount` is required.
- `pageCount` must be positive.
- `fileUrl` is required.

### Article

- `wordCount` is required.
- `wordCount` must be an integer.
- `wordCount` cannot be negative.

### Book

- `author` is required.
- `chapters` must be a non-empty array.
- Each chapter must contain a non-empty name.

### Slide Deck

- `slideCount` is required.
- `slideCount` must be positive.
- `fileUrl` is required.

Reader positions are also validated against the exact version's details.

---

# 17. Error Handling

The API uses a consistent error response:

```json
{
  "timestamp": "2026-08-14T05:00:00Z",
  "status": 409,
  "code": "VERSION_ALREADY_LIVE",
  "message": "Version 2 is already LIVE.",
  "path": "/api/admin/materials/1/versions/2/publish"
}
```

Common HTTP statuses:

```text
400 → invalid request / validation error
404 → resource not found
409 → business-state conflict
```

Examples of stable error codes:

```text
MATERIAL_NOT_FOUND
VERSION_NOT_FOUND
VERSION_WRONG_MATERIAL
MATERIAL_NO_LIVE_VERSION

VERSION_ALREADY_LIVE
VERSION_NOT_EDITABLE
VERSION_NOT_PUBLISHABLE

INVALID_PDF_DETAILS
INVALID_ARTICLE_DETAILS
INVALID_BOOK_DETAILS
INVALID_SLIDE_DECK_DETAILS
INVALID_READER_POSITION
```

---

# 18. Seed Data

Seed data is available through the `seed` Spring profile.

PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="seed"
mvn spring-boot:run
```

It creates one published example of each supported type:

```text
PDF
Article
Book
Slide Deck
```

Seed data is not created during normal application startup.

---

# 19. Configuration

The application currently connects to local PostgreSQL using:

```text
Host: localhost
Port: 5433
Database: reading_library
Username: reading_library
```

The application uses:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Hibernate validates the schema rather than creating or altering tables.

Flyway is responsible for database schema migrations.

---

# 20. Docker / PostgreSQL

Start PostgreSQL from the project root:

```powershell
docker compose up -d
```

Verify the container:

```powershell
docker ps
```

The local application expects PostgreSQL on port `5433`.

If port `5433` is already occupied, adjust the Docker Compose mapping and the application configuration together.

---

# 21. Flyway Migrations

Migration files are stored under:

```text
src/main/resources/db/migration/
```

The current migration sequence includes:

```text
V1__...
V2__create_material_versions.sql
V3__optimize_indexes.sql
```

Migration versions must be unique.

Already-applied migrations should not be modified. New schema changes should be introduced through a new Flyway migration.

---

# 22. Important Indexes

The database uses indexes for important access paths:

```text
UNIQUE(material_id, version_number)

Partial unique index:
ux_one_live_version_per_material

materials(kind)

pdf_details(page_count)

reader_progress(reader_id, completed, updated_at)

UNIQUE(reader_id, material_version_id)
```

These support:

- version uniqueness
- single LIVE version enforcement
- catalog kind filtering
- PDF page-count search
- reader list filtering and pagination
- duplicate progress prevention

---

# 23. Scaling Considerations

For approximately 50,000 materials and 100,000 readers, the design avoids loading the complete dataset into application memory.

Important characteristics:

- catalog pagination is database-backed
- reader-list pagination is database-backed
- PDF page-count filtering is database-backed
- reader progress is indexed by reader/version
- duplicate reader progress is database-constrained
- exactly one LIVE version is database-constrained

For a larger production deployment, additional work may include:

- connection-pool tuning
- load testing
- caching where justified
- read replicas for read-heavy workloads
- query-plan monitoring
- more detailed observability
- backup/restore procedures

These should be based on measured workload rather than introduced prematurely.

---

# 24. API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

Swagger is useful for local manual testing and review.

For production, Swagger can be disabled using:

```properties
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

---

# 25. Health Check

```http
GET /actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

The application exposes the `health` and `info` Actuator endpoints.

---

# 26. SQL Logging

SQL logging can be enabled when debugging database behavior.

It can be used to verify that:

- pagination is performed by PostgreSQL
- PDF page-count filtering is executed by PostgreSQL
- expected joins and predicates are used

SQL logging should be disabled or appropriately restricted in production.

---

# 27. Testing

Run all tests:

```powershell
mvn clean test
```

The current project contains **79 automated tests**.

The test suite uses real PostgreSQL through Testcontainers.

Important scenarios covered include:

- material creation
- kind-specific validation
- draft version creation
- draft editing
- publishing
- rollback
- single-LIVE-version invariant
- concurrent publication
- reader progress
- completion rules
- reader isolation
- historical version progress
- reader continuation
- reader lists
- catalog filtering
- pagination
- API error responses

---

# 28. Manual End-to-End Verification

The core flow has been manually verified through the running REST API.

The verified flow includes:

```text
1. Create PDF v1 as DRAFT
2. Confirm DRAFT is not visible to readers
3. Publish v1
4. Open v1 as a reader
5. Save progress at page 20
6. Create v2
7. Edit v2
8. Publish v2
9. Confirm v1 becomes ARCHIVED and v2 becomes LIVE
10. Confirm the reader opens v2
11. Confirm v1/page-20 progress remains attached to v1
12. Roll back to v1
13. Confirm v1 content is unchanged
14. Confirm the reader's v1/page-20 progress remains available
15. Create another PDF
16. Verify `minPages` catalog search
```

---

# 29. Production Considerations and Limitations

This is a production-oriented take-home implementation, but authentication and authorization are intentionally outside the assignment scope.

Before exposing the service to real users, I would additionally implement:

- authenticated user identity
- admin authorization
- structured application logging
- application metrics
- distributed tracing
- security hardening
- rate limiting where appropriate
- CI/CD
- backup and restore procedures
- load testing
- production connection-pool tuning
- caching/read replicas where justified

The service does not perform actual PDF or slide-file storage or parsing. It stores the corresponding file URLs as required by the model.

---

# 30. Clean Run

From a clean checkout:

```powershell
docker compose up -d
mvn clean test
mvn clean package
mvn spring-boot:run
```

Then verify:

```text
http://localhost:8080/actuator/health
http://localhost:8080/swagger-ui.html
```

---

# 31. Submission Checklist

Before submission, verify:

```text
✅ README.md present at project root
✅ Design document included
✅ Docker Compose included
✅ Flyway migrations included
✅ Source code included
✅ Tests included
✅ Seed profile included
✅ Swagger/OpenAPI available
✅ Actuator health available
✅ No duplicate Flyway migration versions
✅ `mvn clean test` passes
✅ `mvn clean package` passes
✅ No real production secrets committed
✅ `target/` and IDE files excluded from submission
```

---

# 32. Project Status

The implementation currently provides:

```text
✅ PDF, Article, Book, and Slide Deck support
✅ Kind-specific detail tables
✅ Immutable material version history
✅ DRAFT / LIVE / ARCHIVED lifecycle
✅ Database-enforced single LIVE version
✅ Concurrent publishing protection
✅ Rollback to older versions
✅ Version-specific reader progress
✅ Continue-reading endpoint
✅ Reader-specific material list
✅ Catalog search
✅ Database-side PDF page-count search
✅ Database-backed pagination
✅ Structured API error responses
✅ Flyway migrations
✅ PostgreSQL integration testing
✅ Testcontainers
✅ Swagger/OpenAPI
✅ Actuator health endpoint
✅ Seed data profile
✅ 79 automated tests
✅ Manual end-to-end verification
```

The primary production limitation is authentication and authorization, which are intentionally outside the assignment scope.