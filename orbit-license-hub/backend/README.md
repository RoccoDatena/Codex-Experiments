# Orbit License Hub - Backend

Spring Boot API for multi-tenant tenant/application/license/user management with JWT and RBAC.

## Stack

- Java 21+
- Spring Boot 3.5
- MySQL
- Flyway

## Run

1. Configure MySQL credentials in `src/main/resources/application.properties`.
2. Start MySQL and create/connect to database `orbit_license_hub` (auto-create enabled).
3. Start app:

```bash
.\\mvnw.cmd spring-boot:run
```

## Seeded users

All seeded users have password `ChangeMe123!`:

- `superadmin`
- `tenantadmin`
- `licenseadmin`
- `viewer`

## Main endpoints

- `POST /api/auth/login`
- `GET/POST/PUT /api/tenants`
- `GET/POST/PUT /api/applications` + `/enable` `/disable` `/credentials`
- `GET/POST /api/licenses` + `/deassign` `/release`
- `GET/POST /api/users`
