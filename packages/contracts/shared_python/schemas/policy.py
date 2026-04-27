from datetime import datetime
from typing import Any
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field

from ..enums import PolicyRuleType


class PolicyBase(BaseModel):
    # type of rule, like daily limit or time window
    rule_type: PolicyRuleType

    # rule details are saved as json
    # example: {"package_name": "com.instagram.android", "daily_limit_minutes": 30}
    config_json: dict[str, Any]


class PolicyCreate(PolicyBase):
    # the parent creates the rule for this student
    student_id: UUID


class PolicyResponse(PolicyBase):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    parent_id: UUID
    student_id: UUID


class UsageEventCreate(BaseModel):
    # app package name from android
    package_name: str = Field(..., min_length=1)

    # usage time in seconds
    duration_sec: int = Field(..., ge=0)

    # time of the usage record
    timestamp: datetime = Field(default_factory=datetime.utcnow)


class UsageEventResponse(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    student_id: UUID
    package_name: str
    duration_sec: int
    timestamp: datetime


class UsageSyncRequest(BaseModel):
    # android sends a list of usage records
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