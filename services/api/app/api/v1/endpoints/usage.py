from datetime import datetime, timedelta
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, get_db
from app.crud import crud_link, crud_usage
from app.models.user import User
from shared_python.enums import Role
from shared_python.schemas.policy import (
    UsagePackageSummary,
    UsageSummaryResponse,
    UsageSyncRequest,
    UsageSyncResponse,
)

router = APIRouter()


def _is_linked_student(db: Session, parent_id: UUID, student_id: UUID) -> bool:
    # check if parent is linked to this student
    students = crud_link.get_linked_students(db, parent_id=parent_id)

    for student in students:
        if student.id == student_id:
            return True

    return False


@router.post("/sync", response_model=UsageSyncResponse)
def sync_usage(
    usage_in: UsageSyncRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    # only the student app sends usage data
    if current_user.role != Role.STUDENT.value:
        raise HTTPException(status_code=403, detail="Only students can sync usage")

    saved_events = crud_usage.create_usage_events(
        db,
        student_id=current_user.id,
        events=usage_in.events,
    )

    return UsageSyncResponse(saved_count=len(saved_events))


@router.get("/summary", response_model=UsageSummaryResponse)
def get_usage_summary(
    student_id: UUID | None = Query(default=None),
    days: int = Query(default=7, ge=1, le=90),
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    # student can view his own usage
    if current_user.role == Role.STUDENT.value:
        target_student_id = current_user.id

    # parent can view linked student usage
    elif current_user.role == Role.PARENT.value:
        if student_id is None:
            raise HTTPException(
                status_code=422,
                detail="student_id is required for parent reports",
            )

        if not _is_linked_student(db, parent_id=current_user.id, student_id=student_id):
            raise HTTPException(
                status_code=403,
                detail="Student is not linked to this parent",
            )

        target_student_id = student_id

    else:
        raise HTTPException(status_code=403, detail="Not allowed")

    since_time = datetime.utcnow() - timedelta(days=days)

    rows = crud_usage.get_usage_summary(
        db,
        student_id=target_student_id,
        since_time=since_time,
    )

    packages = [
        UsagePackageSummary(
            package_name=row["package_name"],
            duration_sec=row["duration_sec"],
        )
        for row in rows
    ]

    total_duration = sum(item.duration_sec for item in packages)

    return UsageSummaryResponse(
        student_id=target_student_id,
        days=days,
        total_duration_sec=total_duration,
        packages=packages,
    )