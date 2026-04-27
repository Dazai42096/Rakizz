from uuid import UUID

from sqlalchemy.orm import Session

from app.models.policy import BlockingPolicy
from shared_python.schemas.policy import PolicyCreate


def create_policy(db: Session, parent_id: UUID, obj_in: PolicyCreate) -> BlockingPolicy:
    # create one blocking rule
    db_obj = BlockingPolicy(
        parent_id=parent_id,
        student_id=obj_in.student_id,
        rule_type=obj_in.rule_type.value,
        config_json=obj_in.config_json,
    )

    db.add(db_obj)
    db.commit()
    db.refresh(db_obj)

    return db_obj


def get_policy(db: Session, policy_id: UUID) -> BlockingPolicy | None:
    # get one policy by id
    return db.query(BlockingPolicy).filter(BlockingPolicy.id == policy_id).first()


def get_policies_by_student(db: Session, student_id: UUID) -> list[BlockingPolicy]:
    # student app uses this to get its rules
    return (
        db.query(BlockingPolicy)
        .filter(BlockingPolicy.student_id == student_id)
        .all()
    )


def get_policies_by_parent(db: Session, parent_id: UUID) -> list[BlockingPolicy]:
    # parent uses this to see rules they made
    return (
        db.query(BlockingPolicy)
        .filter(BlockingPolicy.parent_id == parent_id)
        .all()
    )


def delete_policy(db: Session, db_obj: BlockingPolicy) -> None:
    # delete one rule
    db.delete(db_obj)
    db.commit()