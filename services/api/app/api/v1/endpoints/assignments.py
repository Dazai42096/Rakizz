from dataclasses import dataclass
from typing import Any, List
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, get_db
from app.crud import crud_assignment, crud_link
from app.models.user import User
from app.services import reminders
from shared_python.enums import AssignmentStatus, Role
from shared_python.schemas.assignment import (
    AssignmentCreate,
    AssignmentResponse,
    AssignmentUpdateMetadata,
    AssignmentUpdateStatus,
)

router = APIRouter()


@dataclass
class _CrudAssignmentCreatePayload:
    student_id: UUID
    title: str
    description: str
    due_date: Any


@dataclass
class _CrudAssignmentUpdateMetadataPayload:
    title: str | None = None
    description: str | None = None
    due_date: Any = None


def _normalize_status(value) -> AssignmentStatus:
    if isinstance(value, AssignmentStatus):
        return value

    raw = str(value)
    for candidate in (raw, raw.upper(), raw.lower()):
        try:
            return AssignmentStatus(candidate)
        except Exception:
            pass

    return AssignmentStatus.PENDING


def _get_assignment_title(assignment) -> str:
    title = getattr(assignment, "title", None)
    if isinstance(title, str):
        return title
    return ""


def _get_assignment_due_date(assignment):
    due_date = getattr(assignment, "due_date", None)
    if due_date is not None:
        return due_date

    due_at = getattr(assignment, "due_at", None)
    if due_at is not None:
        return due_at

    raise HTTPException(status_code=500, detail="Assignment due date is missing")


def _to_response(assignment, reminder_warning: str | None = None) -> AssignmentResponse:
    return AssignmentResponse(
        id=assignment.id,
        student_id=assignment.student_id,
        title=_get_assignment_title(assignment),
        description=assignment.description,
        due_at=_get_assignment_due_date(assignment),
        status=_normalize_status(assignment.status),
        reminder_warning=reminder_warning,
    )


def _get_linked_student_ids(db: Session, parent_id: UUID) -> list[UUID]:
    return [student.id for student in crud_link.get_linked_students(db, parent_id=parent_id)]


def _resolve_target_student_id(
    *,
    db: Session,
    current_user: User,
    requested_student_id: UUID | None,
) -> UUID:
    if current_user.role == Role.STUDENT.value:
        if requested_student_id is not None and str(requested_student_id) != str(current_user.id):
            raise HTTPException(
                status_code=403,
                detail="Students can only manage their own assignments",
            )
        return current_user.id

    if current_user.role == Role.PARENT.value:
        if requested_student_id is None:
            raise HTTPException(
                status_code=422,
                detail="student_id is required for parent-created assignments",
            )

        linked_students = _get_linked_student_ids(db, parent_id=current_user.id)
        if requested_student_id not in linked_students:
            raise HTTPException(
                status_code=403,
                detail="Cannot manage assignments for an unlinked student",
            )
        return requested_student_id

    raise HTTPException(status_code=403, detail="Not authorized")


def _authorize_existing_assignment(
    *,
    db: Session,
    current_user: User,
    assignment,
) -> None:
    _resolve_target_student_id(
        db=db,
        current_user=current_user,
        requested_student_id=assignment.student_id,
    )


@router.post("/", response_model=AssignmentResponse)
def create_assignment(
    assignment_in: AssignmentCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    target_student_id = _resolve_target_student_id(
        db=db,
        current_user=current_user,
        requested_student_id=assignment_in.student_id,
    )

    normalized_input = _CrudAssignmentCreatePayload(
        student_id=target_student_id,
        title=assignment_in.title.strip(),
        description=assignment_in.description.strip(),
        due_date=assignment_in.due_date,
    )

    assignment = crud_assignment.create_assignment(db, obj_in=normalized_input)

    reminder_warning = None
    try:
        reminders.schedule(assignment.id, _get_assignment_due_date(assignment))
    except Exception:
        reminder_warning = (
            "Assignment saved, but reminder scheduling failed. Please check reminders later."
        )

    return _to_response(assignment, reminder_warning=reminder_warning)


@router.get("/", response_model=List[AssignmentResponse])
def list_assignments(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    if current_user.role == Role.STUDENT.value:
        assignments = crud_assignment.get_assignments_by_student(
            db,
            student_id=current_user.id,
        )
        return [_to_response(assignment) for assignment in assignments]

    if current_user.role == Role.PARENT.value:
        students = crud_link.get_linked_students(db, parent_id=current_user.id)
        all_assignments = []
        for student in students:
            all_assignments.extend(
                crud_assignment.get_assignments_by_student(
                    db,
                    student_id=student.id,
                )
            )
        return [_to_response(assignment) for assignment in all_assignments]

    return []


@router.patch("/{id}/status", response_model=AssignmentResponse)
def update_status(
    id: UUID,
    status_in: AssignmentUpdateStatus,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    assignment = crud_assignment.get_assignment(db, assignment_id=id)
    if not assignment:
        raise HTTPException(status_code=404, detail="Not found")

    _authorize_existing_assignment(
        db=db,
        current_user=current_user,
        assignment=assignment,
    )

    updated = crud_assignment.update_assignment_status(
        db,
        db_obj=assignment,
        obj_in=status_in,
    )

    if _normalize_status(updated.status) == AssignmentStatus.COMPLETED:
        try:
            reminders.cancel(updated.id)
        except Exception:
            pass

    return _to_response(updated)


@router.patch("/{id}/metadata", response_model=AssignmentResponse)
def update_metadata(
    id: UUID,
    meta_in: AssignmentUpdateMetadata,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    assignment = crud_assignment.get_assignment(db, assignment_id=id)
    if not assignment:
        raise HTTPException(status_code=404, detail="Not found")

    _authorize_existing_assignment(
        db=db,
        current_user=current_user,
        assignment=assignment,
    )

    normalized_meta = _CrudAssignmentUpdateMetadataPayload(
        title=meta_in.title.strip() if meta_in.title is not None else None,
        description=meta_in.description.strip() if meta_in.description is not None else None,
        due_date=meta_in.due_date,
    )

    updated = crud_assignment.update_assignment_metadata(
        db,
        db_obj=assignment,
        obj_in=normalized_meta,
    )

    reminder_warning = None
    if meta_in.due_date is not None:
        try:
            reminders.reschedule(updated.id, meta_in.due_date)
        except Exception:
            reminder_warning = (
                "Assignment updated, but reminder rescheduling failed. Please check reminders later."
            )

    return _to_response(updated, reminder_warning=reminder_warning)


@router.delete("/{id}")
def delete_assignment(
    id: UUID,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    assignment = crud_assignment.get_assignment(db, assignment_id=id)
    if not assignment:
        raise HTTPException(status_code=404, detail="Not found")

    _authorize_existing_assignment(
        db=db,
        current_user=current_user,
        assignment=assignment,
    )

    crud_assignment.delete_assignment(db, assignment_id=id)

    try:
        reminders.cancel(id)
    except Exception:
        pass

    return {"status": "success"}