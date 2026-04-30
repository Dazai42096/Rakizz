from datetime import datetime
from typing import Any
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field

from ..enums import PolicyRuleType


class PolicyBase(BaseModel):
    # type of rule like daily_limit or time_window
    rule_type: PolicyRuleType

    # rule details are saved as json
    config_json: dict[str, Any]


class PolicyCreate(PolicyBase):
    # parent creates the rule for this student
    student_id: UUID


class PolicyResponse(PolicyBase):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    parent_id: UUID
    student_id: UUID


class UsageEventCreate(BaseModel):
    # android package name
    package_name: str = Field(..., min_length=1)

    # usage time in seconds
    duration_sec: int = Field(..., ge=0)

    timestamp: datetime = Field(default_factory=datetime.utcnow)


class UsageEventResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    student_id: UUID
    package_name: str
    duration_sec: int
    timestamp: datetime


class UsageSyncRequest(BaseModel):
    # android sends more than one usage item
    events: list[UsageEventCreate] = Field(..., min_length=1, max_length=200)


class UsageSyncResponse(BaseModel):
    saved_count: int


class UsagePackageSummary(BaseModel):
    package_name: str
    duration_sec: int


class UsageSummaryResponse(BaseModel):
    student_id: UUID
    days: int
    total_duration_sec: int
    packages: list[UsagePackageSummary]


class InstalledAppCreate(BaseModel):
    # app package from student phone
    package_name: str = Field(..., min_length=1)

    # readable app name
    app_name: str = Field(..., min_length=1)

    category: str | None = None


class InstalledAppsSyncRequest(BaseModel):
    # student phone sends all installed apps here
    apps: list[InstalledAppCreate] = Field(..., min_length=1, max_length=500)


class InstalledAppResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    student_id: UUID
    package_name: str
    app_name: str
    category: str | None = None
    updated_at: datetime


class InstalledAppsSyncResponse(BaseModel):
    saved_count: int


class StudentAppCatalogResponse(BaseModel):
    student_id: UUID
    apps: list[InstalledAppResponse]