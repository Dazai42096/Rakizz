from typing import Any

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, get_db
from app.crud.crud_user import make_unique_pair_code
from app.models.user import User
from shared_python.enums import Role
from shared_python.schemas.user import PairCodeResponse, UserProfileUpdate, UserResponse

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
    # simple profile save
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


@router.post("/me/pair-code", response_model=PairCodeResponse)
def generate_my_pair_code(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
) -> Any:
    if current_user.role != Role.STUDENT.value:
        raise HTTPException(
            status_code=403,
            detail="Only student accounts can generate pair code",
        )

    # keep same code if it already exists
    if not current_user.pair_code:
        current_user.pair_code = make_unique_pair_code(db)
        db.add(current_user)
        db.commit()
        db.refresh(current_user)

    return PairCodeResponse(pair_code=current_user.pair_code)