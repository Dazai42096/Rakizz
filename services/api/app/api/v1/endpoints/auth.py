from typing import Any

from fastapi import APIRouter, Depends, HTTPException
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session

from app.api.deps import get_db
from app.core.security import create_access_token, verify_password
from app.crud.crud_user import create_user, get_user_by_email
from shared_python.enums import Role
from shared_python.schemas.user import UserCreate

router = APIRouter()


@router.post("/login")
def login_access_token(
    db: Session = Depends(get_db),
    form_data: OAuth2PasswordRequestForm = Depends(),
) -> Any:
    user = get_user_by_email(db, email=form_data.username)

    if not user or not verify_password(form_data.password, user.password_hash):
        raise HTTPException(status_code=400, detail="Incorrect email or password")

    access_token = create_access_token(subject=user.id)

    return {
        "access_token": access_token,
        "token_type": "bearer",
    }


@router.post("/register")
def register_user(
    user_in: UserCreate,
    db: Session = Depends(get_db),
) -> Any:
    existing_user = get_user_by_email(db, email=user_in.email)

    if existing_user:
        raise HTTPException(
            status_code=400,
            detail="The user with this email already exists in the system.",
        )

    # admin should not be created from the normal app
    if user_in.role == Role.ADMIN:
        raise HTTPException(
            status_code=403,
            detail="Admin registration is not allowed here",
        )

    # this allows student and parent accounts
    user = create_user(db, user_in=user_in)

    access_token = create_access_token(subject=user.id)

    return {
        "access_token": access_token,
        "token_type": "bearer",
    }