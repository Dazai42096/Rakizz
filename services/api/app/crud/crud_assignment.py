from typing import Any
from uuid import UUID

from sqlalchemy.orm import Session

from app.models.material import Assignment
from shared_python.schemas.assignment import AssignmentUpdateMetadata, AssignmentUpdateStatus


def create_assignment(db: Session, obj_in: Any):
    db_obj = Assignment(
        student_id=obj_in.student_id,
        title=obj_in.title,
        description=obj_in.description,
        due_date=obj_in.due_date,
        status="PENDING",
    )
    db.add(db_obj)
    db.commit()
    db.refresh(db_obj)
    return db_obj


def get_assignment(db: Session, assignment_id: UUID):
    return db.query(Assignment).filter(Assignment.id == assignment_id).first()


def get_assignments_by_student(db: Session, student_id: UUID):
    return db.query(Assignment).filter(Assignment.student_id == student_id).all()


def update_assignment_metadata(
    db: Session,
    db_obj: Assignment,
    obj_in: AssignmentUpdateMetadata | Any,
):
    title = getattr(obj_in, "title", None)
    description = getattr(obj_in, "description", None)
    due_date = getattr(obj_in, "due_date", None)

    if title is not None:
        db_obj.title = title

    if description is not None:
        db_obj.description = description

    if due_date is not None:
        db_obj.due_date = due_date

    db.commit()
    db.refresh(db_obj)
    return db_obj


def update_assignment_status(db: Session, db_obj: Assignment, obj_in: AssignmentUpdateStatus):
    status_value = getattr(obj_in.status, "value", obj_in.status)
    db_obj.status = status_value
    db.commit()
    db.refresh(db_obj)
    return db_obj


def delete_assignment(db: Session, assignment_id: UUID):
    db.query(Assignment).filter(Assignment.id == assignment_id).delete()
    db.commit()