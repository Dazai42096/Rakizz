---
trigger: always_on
---

Security requirements:
- never commit secrets
- maintain .env.example files
- validate all backend input
- enforce role checks on sensitive endpoints
- document Android permission limitations explicitly
- do not claim unsupported Android guarantees
- destructive terminal commands require review
- do not fabricate device-level enforcement behavior
- log security-relevant actions where appropriate