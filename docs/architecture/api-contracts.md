# API Contracts Summary

All official OpenAPI specifications reside in `packages/contracts/`. The FastAPI backend (`services/api`) serves these automatically under `/docs`. The MVP relies on the following major endpoints.

## 1. Authentication
- `POST /api/v1/auth/register`: Register new `User` (Parent or Student).
- `POST /api/v1/auth/login`: Issue JSON Web Token (JWT) identifying Role.

## 2. Materials
- `POST /api/v1/materials/upload`: Upload document or file linking to S3/GCS. Returns `material_id`.
- `GET /api/v1/materials/student/{student_id}`: List available materials for a targeted student.

## 3. Quizzes
- `POST /api/v1/quizzes/generate`: (Auth: Student). Payload: `material_id`. Evaluates the material text. Returns a 5-question JSON array.
- `POST /api/v1/quizzes/{quiz_id}/submit`: Submits the student's answers. Calculates score and returns boolean evaluation to unlock policy.

## 4. Assignments
- `POST /api/v1/assignments/`: Create a new assignment for a `student_id`.
- `PATCH /api/v1/assignments/{id}/status`: Toggle `status` between `Completed` or `Pending`.
- `GET /api/v1/assignments/student/{student_id}`: Returns all tracked assignments.

## 5. Policies
- `POST /api/v1/policies/`: (Auth: Parent). Registers a blocking rule against a specific target app/package.
- `GET /api/v1/policies/active`: (Auth: Student). The Jetpack Compose Android client polls this endpoint to cache its behavior logic locally for immediate background-service execution.

## 6. Usage Logs
- `POST /api/v1/usage/sync`: (Auth: Student Android App). Unidirectionally posts aggregated telemetry from `UsageStatsManager` in a structured batch object.
- `GET /api/v1/reports/dashboard`: (Auth: Parent/Admin). Returns summary data joining assignment completion rates, quiz scores, and app usage histories.
