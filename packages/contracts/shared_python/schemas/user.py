from pydantic import BaseModel, EmailStr
from datetime import datetime
from uuid import UUID
from typing import Optional

from ..enums import Role


class UserBase(BaseModel):
    email: EmailStr
    role: Role


class UserCreate(UserBase):
    password: str


class UserResponse(UserBase):
    id: UUID
    created_at: datetime

    full_name: Optional[str] = None
    school: Optional[str] = None
    grade_level: Optional[str] = None
    phone_number: Optional[str] = None
    profile_image_url: Optional[str] = None

    class Config:
        from_attributes = True


class UserProfileUpdate(BaseModel):
    # user can edit these from profile page
    full_name: Optional[str] = None
    school: Optional[str] = None
    grade_level: Optional[str] = None
    phone_number: Optional[str] = None
    profile_image_url: Optional[str] = None


class ParentStudentLinkBase(BaseModel):
    parent_id: UUID
    student_id: UUID