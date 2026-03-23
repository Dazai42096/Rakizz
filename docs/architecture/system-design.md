# System Design & Architecture

## 1. Architectural Principles

1. **Contracts-First Design**: API definitions (`OpenAPI`) and shared interfaces reside in `packages/contracts`. Both frontend and backend conform strictly to these contracts, eliminating circular dependencies.
2. **Backend Owns Business Logic**: All validation, role handling, quiz generation algorithms, and core assignment state mechanisms live in the FastAPI backend (`services/api`).
3. **Android Owns Device Enforcement Logic**: The local Android background service defines how an app is physically blocked on screen. The FastAPI backend does not construct views or dictate OS-level API usage; it strictly transmits policy data.
4. **Web-Admin Stays Enforcement-Agnostic**: The Next.js dashboard is a pure interface for reading usage logs and mutating configuration policies and never implements blocking rules itself.

## 2. Component Diagram
```
[ Web Dashboard ] (Next.js)      [ Android Client ] (Kotlin/Compose)
        |                                   |
        | (Reads metrics, edits rules)      | (Syncs rules, sends metrics)
        v                                   v
+-------------------------------------------------------------+
|                     API Gateway / REST                      |
|                      [services/api]                         |
+-------------------------------------------------------------+
| - IAM & Roles     - Dashboard APIs     - AI Quiz Generation |
| - Policy Engine   - Assignment APIs    - Metric Aggregation |
+-------------------------------------------------------------+
                        |
                        v
                 +--------------+
                 | PostgreSQL   |
                 | (Core DB)    |
                 +--------------+
```

## 3. Communication Patterns

- **API Protocols**: Standard REST/JSON via HTTPS. Authentication managed via JWTs passed in headers.
- **Android Offline Behavior**: The Android client acts as local authoritative source for blocking rules to handle offline scenarios. The client pulls JSON policies from the backend and maintains a local SQLite or DataStore shadow copy.
- **Analytics Sync**: Android pushes usage statistics in batched JSON payloads to a dedicated REST endpoint sequentially.
