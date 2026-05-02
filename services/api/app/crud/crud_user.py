import random

from sqlalchemy.orm import Session

from app.core.security import get_password_hash
from app.models.user import User
from shared_python.enums import Role
from shared_python.schemas.user import UserCreate


def get_user_by_email(db: Session, email: str) -> User | None:
    return db.query(User).filter(User.email == email).first()


def get_user_by_pair_code(db: Session, pair_code: str) -> User | None:
    return (
        db.query(User)
        .filter(
            User.pair_code == pair_code,
            User.role == Role.STUDENT.value,
        )
        .first()
    )


def create_user(db: Session, user_in: UserCreate) -> User:
    db_obj = User(
        email=user_in.email,
        password_hash=get_password_hash(user_in.password),
        role=user_in.role.value,
    )

    # student gets pair code when account is created
    if user_in.role == Role.STUDENT:
        db_obj.pair_code = make_unique_pair_code(db)

    db.add(db_obj)
    db.commit()
    db.refresh(db_obj)

    return db_obj


def make_unique_pair_code(db: Session) -> str:
    # example: RKZ-482913
    while True:
        code = f"RKZ-{random.randint(100000, 999999)}"
        exists = db.query(User).filter(User.pair_code == code).first()

        if not exists:
            return code