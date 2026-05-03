from fastapi import APIRouter

from app.api.v1.endpoints import (
    app_catalog,
    assignments,
    auth,
    health,
    links,
    materials,
    policies,
    quizzes,
    unlock,
    usage,
    users,
)

api_router = APIRouter()

api_router.include_router(
    health.router,
    prefix="/health",
    tags=["health"],
)

api_router.include_router(
    auth.router,
    prefix="/auth",
    tags=["auth"],
)

api_router.include_router(
    users.router,
    prefix="/users",
    tags=["users"],
)

api_router.include_router(
    links.router,
    prefix="/links",
    tags=["links"],
)

api_router.include_router(
    materials.router,
    prefix="/materials",
    tags=["materials"],
)

api_router.include_router(
    assignments.router,
    prefix="/assignments",
    tags=["assignments"],
)

api_router.include_router(
    quizzes.router,
    prefix="/quizzes",
    tags=["quizzes"],
)

# focus rules made by the parent
api_router.include_router(
    policies.router,
    prefix="/policies",
    tags=["policies"],
)

# usage and installed apps
api_router.include_router(
    usage.router,
    prefix="/usage",
    tags=["usage"],
)

api_router.include_router(
    app_catalog.router,
    prefix="/app-catalog",
    tags=["app-catalog"],
)

# this is used when a blocked app opens
# android calls /api/v1/unlock/check and /api/v1/unlock/grant
api_router.include_router(
    unlock.router,
    prefix="/unlock",
    tags=["unlock"],
)