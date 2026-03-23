from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.api.deps import get_db, get_current_user
from app.models.user import User
from app.crud import crud_assignment, crud_link
from shared_python.schemas.assignment import AssignmentCreate, AssignmentUpdateStatus, AssignmentUpdateMetadata, AssignmentResponse
from shared_python.enums import Role, AssignmentStatus
from typing import List
from uuid import UUID
from app.services import reminders

router = APIRouter()

@router.post("/", response_model=AssignmentResponse)
def create_assignment(assignment_in: AssignmentCreate, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    if current_user.role != Role.PARENT.value:
        raise HTTPException(status_code=403, detail="Only parents can create assignments")
        
    linked_students = [s.id for s in crud_link.get_linked_students(db, parent_id=current_user.id)]
    if assignment_in.student_id not in linked_students:
        raise HTTPException(status_code=403, detail="Cannot assign task to unlinked student")
            
    assignment = crud_assignment.create_assignment(db, obj_in=assignment_in)
    reminders.schedule(assignment.id, assignment.due_date)
    return assignment

@router.get("/", response_model=List[AssignmentResponse])
def list_assignments(db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    if current_user.role == Role.STUDENT.value:
        return crud_assignment.get_assignments_by_student(db, student_id=current_user.id)
    elif current_user.role == Role.PARENT.value:
        students = crud_link.get_linked_students(db, parent_id=current_user.id)
        all_assignments = []
        for s in students:
            all_assignments.extend(crud_assignment.get_assignments_by_student(db, student_id=s.id))
        return all_assignments
    return []

@router.patch("/{id}/status", response_model=AssignmentResponse)
def update_status(id: UUID, status_in: AssignmentUpdateStatus, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    assignment = crud_assignment.get_assignment(db, assignment_id=id)
    if not assignment:
        raise HTTPException(status_code=404, detail="Not found")
        
    if current_user.role == Role.STUDENT.value and str(assignment.student_id) != str(current_user.id):
        raise HTTPException(status_code=403, detail="Not authorized")
        
    if current_user.role == Role.PARENT.value:
        linked_students = [s.id for s in crud_link.get_linked_students(db, parent_id=current_user.id)]
        if assignment.student_id not in linked_students:
            raise HTTPException(status_code=403, detail="Not authorized")

    crud_assignment.update_assignment_status(db, db_obj=assignment, obj_in=status_in)
    if status_in.status == AssignmentStatus.COMPLETED:
        reminders.cancel(assignment.id)
    return assignment

@router.patch("/{id}/metadata", response_model=AssignmentResponse)
def update_metadata(id: UUID, meta_in: AssignmentUpdateMetadata, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    assignment = crud_assignment.get_assignment(db, assignment_id=id)
    if not assignment:
        raise HTTPException(status_code=404, detail="Not found")
        
    if current_user.role == Role.STUDENT.value:
        raise HTTPException(status_code=403, detail="Students cannot modify assignment metadata")
        
    if current_user.role == Role.PARENT.value:
        linked_students = [s.id for s in crud_link.get_linked_students(db, parent_id=current_user.id)]
        if assignment.student_id not in linked_students:
            raise HTTPException(status_code=403, detail="Not authorized")
            
    crud_assignment.update_assignment_metadata(db, db_obj=assignment, obj_in=meta_in)
    if meta_in.due_date:
        reminders.reschedule(assignment.id, meta_in.due_date)
    return assignment

@router.delete("/{id}")
def delete_assignment(id: UUID, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    assignment = crud_assignment.get_assignment(db, assignment_id=id)
    if not assignment:
        raise HTTPException(status_code=404, detail="Not found")
        
    if current_user.role == Role.STUDENT.value:
        raise HTTPException(status_code=403, detail="Students cannot delete tasks natively")
        
    crud_assignment.delete_assignment(db, assignment_id=id)
    reminders.cancel(id)
    return {"status": "success"}
