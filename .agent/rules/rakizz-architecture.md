---
trigger: always_on
---

Use a monorepo with:
- apps/android = Kotlin + Jetpack Compose
- apps/web-admin = Next.js
- services/api = FastAPI
- packages/contracts = shared contracts

Architecture principles:
- contracts-first
- no circular dependencies
- backend owns business logic
- android owns device-level enforcement logic
- web-admin never implements enforcement logic
- keep module boundaries clear
- prefer feature-based structure over dumping logic into utils