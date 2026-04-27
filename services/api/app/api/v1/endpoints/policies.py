from typing import List
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, get_db
from app.crud import crud_link, crud_policy
from app.models.user import User
from shared_python.enums import Role
from shared_python.schemas.policy import PolicyCreate, PolicyResponse

router = APIRouter()


def _is_linked_student(db: Session, parent_id: UUID, student_id: UUID) -> bool:
    # check if this student is linked to this parent
    students = crud_link.get_linked_students(db, parent_id=parent_id)

    for student in students:
        if student.id == student_id:
            return True

    return False


@router.get("/", response_model=List[PolicyResponse])
def list_policies(
    student_id: UUID | None = Query(default=None),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    # student can only see his own policies
    if current_user.role == Role.STUDENT.value:
        if student_id is not None and student_id != current_user.id:
            raise HTTPException(
                status_code=403,
                detail="Students can only view their own policies",
            )

        return crud_policy.get_policies_by_student(db, student_id=current_user.id)

    # parent can see policies for linked students
    if current_user.role == Role.PARENT.value:
        if student_id is not None:
            if not _is_linked_student(db, parent_id=current_user.id, student_id=student_id):
                raise HTTPException(
                    status_code=403,
                    detail="Student is not linked to this parent",
                )

            return crud_policy.get_policies_by_student(db, student_id=student_id)

        return crud_policy.get_policies_by_parent(db, parent_id=current_user.id)

    raise HTTPException(status_code=403, detail="Not allowed")


@router.post("/", response_model=PolicyResponse)
def create_policy(
    policy_in: PolicyCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    # only parent can create blocking rules
    if current_user.role != Role.PARENT.value:
        raise HTTPException(status_code=403, detail="Only parents can create policies")

    # parent cannot create rules for an unlinked student
    if not _is_linked_student(db, parent_id=current_user.id, student_id=policy_in.student_id):
        raise HTTPException(
            status_code=403,
            detail="Student is not linked to this parent",
        )

    return crud_policy.create_policy(
        db,
        parent_id=current_user.id,
        obj_in=policy_in,
    )


@router.delete("/{policy_id}")
def delete_policy(
    policy_id: UUID,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    policy = crud_policy.get_policy(db, policy_id=policy_id)

    if policy is None:
        raise HTTPException(status_code=404, detail="Policy not found")

    # parent can delete only their own rule
    if current_user.role != Role.PARENT.value or policy.parent_id != current_user.id:
        raise HTTPException(
            status_code=403,
            detail="Not allowed to delete this policy",
        )

    crud_policy.delete_policy(db, db_obj=policy)

    return {"status": "success"}