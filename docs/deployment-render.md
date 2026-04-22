# Render Deployment (Docker + Supabase PostgreSQL)

This project is deployed to Render as a **Docker Web Service** using the repository-root `Dockerfile`.

## Render service setup
- Environment: `Docker`
- Dockerfile path: `./Dockerfile`
- Health check path: `/actuator/health`

## Required environment variables
- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

## Optional environment variables
- `SPRINGDOC_ENABLED` (default: `false` in `prod`)
- `APP_INGESTION_RUN_ON_STARTUP` (default: `true` in `prod`)
- `APP_INGESTION_SCHEDULER_ENABLED` (default: `true` in `prod`)
- `APP_INGESTION_SCHEDULER_CRON` (default: `0 0 3 * * *`)

## Port and runtime notes
- Render provides `PORT`; app binds with `server.port=${PORT:8080}`.
- Container image does not contain DB secrets; Supabase credentials are injected via Render environment variables.

## Scheduler behavior in prod
- Startup full ingestion: enabled by default (`APP_INGESTION_RUN_ON_STARTUP=true`)
- Daily scheduler: enabled by default (`APP_INGESTION_SCHEDULER_ENABLED=true`)
- Default cron: once per day at 03:00 (`0 0 3 * * *`)


