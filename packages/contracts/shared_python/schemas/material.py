from pydantic import BaseModel
from datetime import datetime
from uuid import UUID

class MaterialCreate(BaseModel):
    title: str
    source_url: str

class MaterialResponse(BaseModel):
    id: UUID
    owner_id: UUID
    title: str
    source_url: str
    created_at: datetime
    
    class Config:
        from_attributes = True
