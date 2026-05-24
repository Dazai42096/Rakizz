from typing import Any, List
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from sqlalchemy.orm import Session

from app.api.deps import get_db, require_role
from app.crud import crud_link
from app.models.user import User
from shared_python.enums import Role
from shared_python.schemas.user import ParentStudentLinkBase, UserResponse

router = APIRouter()


class PairCodeLinkRequest(BaseModel):
    pair_code: str


def user_to_response(user: User) -> dict:
    return {
        "id": str(user.id),
        "email": user.email,
        "role": user.role,
        "pair_code": user.pair_code,
        "full_name": user.full_name,
        "school": user.school,
        "grade_level": user.grade_level,
        "phone_number": user.phone_number,
        "profile_image_url": user.profile_image_url,
    }


@router.post("/")
def link_student(
    link_in: ParentStudentLinkBase,
    db: Session = Depends(get_db),
    current_parent: User = Depends(require_role(Role.PARENT)),
) -> Any:
    if str(current_parent.id) != str(link_in.parent_id):
        raise HTTPException(
            status_code=403,
            detail="Cannot link to another parent's account",
        )

    student = (
        db.query(User)
        .filter(
            User.id == link_in.student_id,
            User.role == Role.STUDENT.value,
        )
        .first()
    )

    if not student:
        raise HTTPException(
            status_code=404,
            detail="Student not found",
        )

    crud_link.link_student_to_parent(
        db,
        parent_id=link_in.parent_id,
        student_id=link_in.student_id,
    )

    return {"status": "success"}


@router.post("/pair-code")
def link_student_by_pair_code(
    link_in: PairCodeLinkRequest,
    db: Session = Depends(get_db),
    current_parent: User = Depends(require_role(Role.PARENT)),
) -> Any:
    code = link_in.pair_code.strip().upper()

    if not code:
        raise HTTPException(
            status_code=400,
            detail="Pair code is required",
        )

    student = (
        db.query(User)
        .filter(
            User.pair_code == code,
            User.role == Role.STUDENT.value,
        )
        .first()
    )

    if not student:
        raise HTTPException(
            status_code=404,
            detail="Student pair code not found",
        )

    crud_link.link_student_to_parent(
        db,
        parent_id=current_parent.id,
        student_id=student.id,
    )

    return {
        "status": "success",
        "student": user_to_response(student),
    }


@router.get("/students", response_model=List[UserResponse])
def get_students(
    db: Session = Depends(get_db),
    current_parent: User = Depends(require_role(Role.PARENT)),
) -> Any:
    return crud_link.get_linked_students(
        db,
        parent_id=current_parent.id,
    )


@router.get("/parents", response_model=List[UserResponse])
def get_parents(
    db: Session = Depends(get_db),
    current_student: User = Depends(require_role(Role.STUDENT)),
) -> Any:
    return crud_link.get_linked_parents(
        db,
        student_id=current_student.id,
    )