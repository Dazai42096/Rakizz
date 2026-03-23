from sqlalchemy.orm import Session
from app.models.user import User
from app.core.security import get_password_hash
from shared_python.schemas.user import UserCreate

def get_user_by_email(db: Session, email: str) -> User | None:
    return db.query(User).filter(User.email == email).first()

def create_user(db: Session, user_in: UserCreate) -> User:
    db_obj = User(
        email=user_in.email,
        password_hash=get_password_hash(user_in.password),
        role=user_in.role.value
    )
    db.add(db_obj)
    db.commit()
    db.refresh(db_obj)
    return db_obj
