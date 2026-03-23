from pydantic import BaseModel
from uuid import UUID
from typing import Dict, Any
from datetime import datetime
from ..enums import PolicyRuleType

class PolicyBase(BaseModel):
    rule_type: PolicyRuleType
    config_json: Dict[str, Any]

class PolicyCreate(PolicyBase):
    student_id: UUID

class PolicyResponse(PolicyBase):
    id: UUID
    parent_id: UUID
    student_id: UUID

    class Config:
        from_attributes = True

class UsageEventBase(BaseModel):
    package_name: str
    duration_sec: int
    timestamp: datetime

class UsageEventCreate(UsageEventBase):
    student_id: UUID
    
class UsageEventResponse(UsageEventBase):
    id: UUID
    student_id: UUID

    class Config:
        from_attributes = True
