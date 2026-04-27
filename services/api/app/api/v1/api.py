from fastapi import APIRouter
from app.api.v1.endpoints import health, auth, users, links, materials, assignments, quizzes

api_router = APIRouter()
api_router.include_router(health.router, prefix="/health", tags=["health"])
api_router.include_router(auth.router, prefix="/auth", tags=["auth"])
api_router.include_router(users.router, prefix="/users", tags=["users"])
api_router.include_router(links.router, prefix="/links", tags=["links"])
api_router.include_router(materials.router, prefix="/materials", tags=["materials"])
api_router.include_router(assignments.router, prefix="/assignments", tags=["assignments"])
api_router.include_router(quizzes.router, prefix="/quizzes", tags=["quizzes"])
from fastapi import APIRouter

from app.api.v1.endpoints import (
    assignments,
    auth,
    health,
    links,
    materials,
    policies,
    quizzes,
    usage,
    users,
)

api_router = APIRouter()

api_router.include_router(health.router, prefix="/health", tags=["health"])
api_router.include_router(auth.router, prefix="/auth", tags=["auth"])
api_router.include_router(users.router, prefix="/users", tags=["users"])
api_router.include_router(links.router, prefix="/links", tags=["links"])
api_router.include_router(materials.router, prefix="/materials", tags=["materials"])
api_router.include_router(assignments.router, prefix="/assignments", tags=["assignments"])
api_router.include_router(quizzes.router, prefix="/quizzes", tags=["quizzes"])

# focus and usage endpoints
api_router.include_router(policies.router, prefix="/policies", tags=["policies"])
api_router.include_router(usage.router, prefix="/usage", tags=["usage"])