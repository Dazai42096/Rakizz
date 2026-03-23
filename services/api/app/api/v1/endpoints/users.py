from typing import Any
from fastapi import APIRouter, Depends
from app.models.user import User
from app.api.deps import get_current_user
from shared_python.schemas.user import UserResponse

router = APIRouter()

@router.get("/me", response_model=UserResponse)
def read_user_me(current_user: User = Depends(get_current_user)) -> Any:
    return current_user
