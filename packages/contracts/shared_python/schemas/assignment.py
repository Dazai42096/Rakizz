from datetime import datetime
from uuid import UUID

from pydantic import AliasChoices, BaseModel, ConfigDict, Field

from shared_python.enums import AssignmentStatus


class AssignmentCreate(BaseModel):
    model_config = ConfigDict(
        populate_by_name=True,
        extra="ignore",
    )

    title: str = Field(..., min_length=1)
    description: str = Field(..., min_length=1)
    due_at: datetime = Field(
        ...,
        validation_alias=AliasChoices("due_at", "due_date"),
        serialization_alias="due_at",
    )
    student_id: UUID | None = None

    @property
    def due_date(self) -> datetime:
        return self.due_at


class AssignmentUpdateStatus(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    status: AssignmentStatus


class AssignmentUpdateMetadata(BaseModel):
    model_config = ConfigDict(
        populate_by_name=True,
        extra="ignore",
    )

    title: str | None = Field(default=None, min_length=1)
    description: str | None = Field(default=None, min_length=1)
    due_at: datetime | None = Field(
        default=None,
        validation_alias=AliasChoices("due_at", "due_date"),
        serialization_alias="due_at",
    )

    @property
    def due_date(self) -> datetime | None:
        return self.due_at


class AssignmentResponse(BaseModel):
    model_config = ConfigDict(
        from_attributes=True,
        populate_by_name=True,
    )

    id: UUID
    student_id: UUID
    title: str
    description: str
    due_at: datetime = Field(
        ...,
        validation_alias=AliasChoices("due_at", "due_date"),
        serialization_alias="due_at",
    )
    status: AssignmentStatus
    reminder_warning: str | None = None

    @property
    def due_date(self) -> datetime:
        return self.due_at