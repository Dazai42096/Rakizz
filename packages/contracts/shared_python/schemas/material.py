from pydantic import BaseModel, Field
from datetime import datetime
from uuid import UUID


class MaterialCreate(BaseModel):
    title: str = Field(..., min_length=1)
    source_url: str = Field(..., min_length=1)


class MaterialResponse(BaseModel):
    id: UUID
    owner_id: UUID
    title: str
    source_url: str
    created_at: datetime

    class Config:
        from_attributes = True