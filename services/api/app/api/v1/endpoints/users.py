from typing import Any

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, get_db
from app.models.user import User
from shared_python.schemas.user import UserProfileUpdate, UserResponse

router = APIRouter()


@router.get("/me", response_model=UserResponse)
def read_user_me(
    current_user: User = Depends(get_current_user),
) -> Any:
    return current_user


@router.put("/me", response_model=UserResponse)
def update_user_me(
    profile_in: UserProfileUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
) -> Any:
    # simple profile edit for checkpoint
    if profile_in.full_name is not None:
        current_user.full_name = profile_in.full_name.strip()

    if profile_in.school is not None:
        current_user.school = profile_in.school.strip()

    if profile_in.grade_level is not None:
        current_user.grade_level = profile_in.grade_level.strip()

    if profile_in.phone_number is not None:
        current_user.phone_number = profile_in.phone_number.strip()

    if profile_in.profile_image_url is not None:
        current_user.profile_image_url = profile_in.profile_image_url.strip()

    db.add(current_user)
    db.commit()
    db.refresh(current_user)

    return current_user