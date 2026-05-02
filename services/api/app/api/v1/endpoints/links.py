from typing import Any, List

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.api.deps import get_db, require_role
from app.crud import crud_link
from app.crud.crud_user import get_user_by_pair_code
from app.models.user import User
from shared_python.enums import Role
from shared_python.schemas.user import (
    PairCodeLinkRequest,
    PairCodeLinkResponse,
    ParentStudentLinkBase,
    UserResponse,
)

router = APIRouter()


@router.post("/")
def link_student(
    link_in: ParentStudentLinkBase,
    db: Session = Depends(get_db),
    current_parent: User = Depends(require_role(Role.PARENT)),
) -> Any:
    # old endpoint kept in case we need it for testing
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
        parent_id=current_parent.id,
        student_id=student.id,
    )

    return {"status": "success"}


@router.post("/pair-code", response_model=PairCodeLinkResponse)
def link_student_by_pair_code(
    request: PairCodeLinkRequest,
    db: Session = Depends(get_db),
    current_parent: User = Depends(require_role(Role.PARENT)),
) -> Any:
    pair_code = request.pair_code.strip().upper()

    if not pair_code:
        raise HTTPException(
            status_code=400,
            detail="Pair code is required",
        )

    # parent enters RKZ code here
    student = get_user_by_pair_code(
        db=db,
        pair_code=pair_code,
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

    return PairCodeLinkResponse(
        status="success",
        student=student,
    )


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