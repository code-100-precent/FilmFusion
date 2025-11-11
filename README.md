# Project Template: Spring Boot + Vue Admin + uni-app

This repository is a clean starter template. We removed all domain-specific business logic and retained only core, reusable modules and configurations.

## Modules
- Backend: Spring Boot (Java 17), MyBatis-Plus, Redis, JWT, Actuator
- Admin Web: Vue 3 + Vite + Element Plus + Pinia + Axios
- Mini Program: uni-app (Vue 3), Pinia

## What’s Included
- Auth/user basics on both admin and mini program sides (login flow placeholders)
- Core backend utilities: global exception handler, file upload, admin auth endpoints
- Clean configuration with environment-variable placeholders (no secrets committed)
- Dockerfile, docker-compose baseline, Kubernetes manifests (base/dev/prod)

## What’s Removed
- Domain features such as alumni, POI, routes, stories, tags, visitors, feedback and their admin UIs and mobile pages
- Corresponding REST controllers and mapper XML files

## Quick Start
1) Backend
- Java 17 and Maven required
- Copy .env or export env vars for DB/Redis/JWT
- Build and run:
  mvn clean package -DskipTests
  java -jar target/*.jar

2) Admin
- cd admin
- npm install
- npm run dev

3) Mini Program
- cd miniprogram
- npm install
- Open with HBuilderX or run via CLI targeting your platform

## Configuration
- See src/main/resources/application.yml for placeholders
- Override via environment variables in dev/prod

## Docker
- docker build -t app-template .
- docker compose up -d

## Notes
- Add your new domain modules (entities, mappers, services, controllers) under your own package
- Keep auth and common modules reusable
