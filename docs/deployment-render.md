# Render Deployment (Supabase PostgreSQL)

## Runtime profile
- `SPRING_PROFILES_ACTIVE=prod`

## Required environment variables
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

## Optional environment variables
- `SPRINGDOC_ENABLED` (default: `false` in `prod`)
- `APP_INGESTION_RUN_ON_STARTUP` (default: `true` in `prod`)
- `APP_INGESTION_SCHEDULER_ENABLED` (default: `true` in `prod`)
- `APP_INGESTION_SCHEDULER_CRON` (default: `0 0 3 * * *`)

## Render commands
- Build command:
  - `./gradlew clean build -x test`
- Start command:
  - `java -Dspring.profiles.active=prod -jar build/libs/*.jar`

## Health check
- Path: `/actuator/health`

## Scheduler behavior in prod
- Startup full ingestion: enabled by default (`APP_INGESTION_RUN_ON_STARTUP=true`)
- Daily scheduler: enabled by default (`APP_INGESTION_SCHEDULER_ENABLED=true`)
- Default cron: once per day at 03:00 (`0 0 3 * * *`)

## Notes
- Render provides `PORT`; app uses `server.port=${PORT:8080}`.
- Supabase credentials stay in Render environment variables, never in repository files.

