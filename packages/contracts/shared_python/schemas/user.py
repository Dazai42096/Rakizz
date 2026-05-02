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

    pair_code: Optional[str] = None

    full_name: Optional[str] = None
    school: Optional[str] = None
    grade_level: Optional[str] = None
    phone_number: Optional[str] = None
    profile_image_url: Optional[str] = None

    class Config:
        from_attributes = True


class UserProfileUpdate(BaseModel):
    # user can edit these fields
    full_name: Optional[str] = None
    school: Optional[str] = None
    grade_level: Optional[str] = None
    phone_number: Optional[str] = None
    profile_image_url: Optional[str] = None


class PairCodeResponse(BaseModel):
    pair_code: str


class PairCodeLinkRequest(BaseModel):
    pair_code: str


class PairCodeLinkResponse(BaseModel):
    status: str
    student: UserResponse


class ParentStudentLinkBase(BaseModel):
    parent_id: UUID
    student_id: UUID