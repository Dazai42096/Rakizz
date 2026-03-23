from sqlalchemy.orm import Session
from app.models.material import Assignment
from shared_python.schemas.assignment import AssignmentCreate, AssignmentUpdateMetadata, AssignmentUpdateStatus
from uuid import UUID

def create_assignment(db: Session, obj_in: AssignmentCreate):
    db_obj = Assignment(
        student_id=obj_in.student_id,
        description=obj_in.description,
        due_date=obj_in.due_date
    )
    db.add(db_obj)
    db.commit()
    db.refresh(db_obj)
    return db_obj

def get_assignment(db: Session, assignment_id: UUID):
    return db.query(Assignment).filter(Assignment.id == assignment_id).first()

def get_assignments_by_student(db: Session, student_id: UUID):
    return db.query(Assignment).filter(Assignment.student_id == student_id).all()

def update_assignment_metadata(db: Session, db_obj: Assignment, obj_in: AssignmentUpdateMetadata):
    if obj_in.description is not None:
        db_obj.description = obj_in.description
    if obj_in.due_date is not None:
        db_obj.due_date = obj_in.due_date
    db.commit()
    db.refresh(db_obj)
    return db_obj

def update_assignment_status(db: Session, db_obj: Assignment, obj_in: AssignmentUpdateStatus):
    db_obj.status = obj_in.status.value
    db.commit()
    db.refresh(db_obj)
    return db_obj

def delete_assignment(db: Session, assignment_id: UUID):
    db.query(Assignment).filter(Assignment.id == assignment_id).delete()
    db.commit()
