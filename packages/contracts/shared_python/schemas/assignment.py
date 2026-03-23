from pydantic import BaseModel, Field
from datetime import datetime
from uuid import UUID
from shared_python.enums import AssignmentStatus

class AssignmentCreate(BaseModel):
    student_id: UUID
    description: str = Field(..., min_length=1)
    due_date: datetime

class AssignmentUpdateStatus(BaseModel):
    status: AssignmentStatus

class AssignmentUpdateMetadata(BaseModel):
    description: str | None = None
    due_date: datetime | None = None

class AssignmentResponse(BaseModel):
    id: UUID
    student_id: UUID
    description: str
    due_date: datetime
    status: AssignmentStatus

    class Config:
        from_attributes = True
