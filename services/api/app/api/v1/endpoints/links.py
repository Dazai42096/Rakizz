from typing import Any, List
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.api.deps import get_db, require_role, get_current_user
from app.models.user import User
from app.crud import crud_link
from shared_python.enums import Role
from shared_python.schemas.user import ParentStudentLinkBase, UserResponse
from uuid import UUID

router = APIRouter()

@router.post("/")
def link_student(link_in: ParentStudentLinkBase, db: Session = Depends(get_db), current_parent: User = Depends(require_role(Role.PARENT))) -> Any:
    if str(current_parent.id) != str(link_in.parent_id):
        raise HTTPException(status_code=403, detail="Cannot link to another parent's account")
    
    student = db.query(User).filter(User.id == link_in.student_id, User.role == Role.STUDENT.value).first()
    if not student:
        raise HTTPException(status_code=404, detail="Student not found")

    crud_link.link_student_to_parent(db, parent_id=link_in.parent_id, student_id=link_in.student_id)
    return {"status": "success"}

@router.get("/students", response_model=List[UserResponse])
def get_students(db: Session = Depends(get_db), current_parent: User = Depends(require_role(Role.PARENT))) -> Any:
    return crud_link.get_linked_students(db, parent_id=current_parent.id)

@router.get("/parents", response_model=List[UserResponse])
def get_parents(db: Session = Depends(get_db), current_student: User = Depends(require_role(Role.STUDENT))) -> Any:
    return crud_link.get_linked_parents(db, student_id=current_student.id)
