from datetime import datetime
from typing import Optional
from uuid import UUID

from pydantic import BaseModel


class UnlockCheckRequest(BaseModel):
    # package name like com.instagram.android
    package_name: str

    # used when android accessibility service already caught the blocked app
    force_blocked: bool = False


class UnlockCheckResponse(BaseModel):
    blocked: bool
    package_name: str
    app_name: Optional[str] = None
    message: str

    # newest material is used for the unlock quiz
    material_id: Optional[UUID] = None

    # if already unlocked, backend returns when it expires
    unlocked_until: Optional[datetime] = None


class UnlockGrantRequest(BaseModel):
    package_name: str
    quiz_attempt_id: UUID
    granted_minutes: int = 15


class UnlockGrantResponse(BaseModel):
    status: str
    package_name: str
    granted_minutes: int
    expires_at: datetime