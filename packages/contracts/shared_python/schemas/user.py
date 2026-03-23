from pydantic import BaseModel, EmailStr
from datetime import datetime
from uuid import UUID
from ..enums import Role

class UserBase(BaseModel):
    email: EmailStr
    role: Role

class UserCreate(UserBase):
    password: str

class UserResponse(UserBase):
    id: UUID
    created_at: datetime
    
    class Config:
        from_attributes = True

class ParentStudentLinkBase(BaseModel):
    parent_id: UUID
    student_id: UUID
