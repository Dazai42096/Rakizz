from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, get_db
from app.crud import crud_app_catalog, crud_link
from app.models.user import User
from shared_python.enums import Role
from shared_python.schemas.policy import (
    InstalledAppsSyncRequest,
    InstalledAppsSyncResponse,
    StudentAppCatalogResponse,
)

router = APIRouter()


def _is_linked_student(db: Session, parent_id: UUID, student_id: UUID) -> bool:
    # parent can only see apps for linked students
    students = crud_link.get_linked_students(db, parent_id=parent_id)

    for student in students:
        if student.id == student_id:
            return True

    return False


@router.post("/sync", response_model=InstalledAppsSyncResponse)
def sync_installed_apps(
    request: InstalledAppsSyncRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    # only student phone sends installed apps
    if current_user.role != Role.STUDENT.value:
        raise HTTPException(status_code=403, detail="Only students can sync apps")

    saved_apps = crud_app_catalog.replace_student_apps(
        db=db,
        student_id=current_user.id,
        apps=request.apps,
    )

    return InstalledAppsSyncResponse(saved_count=len(saved_apps))


@router.get("/student/{student_id}", response_model=StudentAppCatalogResponse)
def get_apps_for_student(
    student_id: UUID,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    # student can see his own synced apps
    if current_user.role == Role.STUDENT.value:
        if current_user.id != student_id:
            raise HTTPException(status_code=403, detail="Students can only view their own apps")

    # parent can see linked student's apps
    elif current_user.role == Role.PARENT.value:
        if not _is_linked_student(db, parent_id=current_user.id, student_id=student_id):
            raise HTTPException(status_code=403, detail="Student is not linked to this parent")

    else:
        raise HTTPException(status_code=403, detail="Not allowed")

    apps = crud_app_catalog.get_student_apps(
        db=db,
        student_id=student_id,
    )

    return StudentAppCatalogResponse(
        student_id=student_id,
        apps=apps,
    )