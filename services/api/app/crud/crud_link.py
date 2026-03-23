from sqlalchemy.orm import Session
from app.models.user import User, parent_student_links
from uuid import UUID

def link_student_to_parent(db: Session, parent_id: UUID, student_id: UUID):
    stmt = parent_student_links.insert().values(parent_id=parent_id, student_id=student_id)
    db.execute(stmt)
    db.commit()

def get_linked_students(db: Session, parent_id: UUID):
    return db.query(User).join(parent_student_links, User.id == parent_student_links.c.student_id).filter(parent_student_links.c.parent_id == parent_id).all()

def get_linked_parents(db: Session, student_id: UUID):
    return db.query(User).join(parent_student_links, User.id == parent_student_links.c.parent_id).filter(parent_student_links.c.student_id == student_id).all()
