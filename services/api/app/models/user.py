from sqlalchemy import Column, String, DateTime, Table, ForeignKey
from sqlalchemy.dialects.postgresql import UUID
import uuid
import datetime
from app.db.base import Base

parent_student_links = Table(
    'parent_student_links',
    Base.metadata,
    Column('parent_id', UUID(as_uuid=True), ForeignKey('users.id', ondelete="CASCADE"), primary_key=True),
    Column('student_id', UUID(as_uuid=True), ForeignKey('users.id', ondelete="CASCADE"), primary_key=True)
)

class User(Base):
    __tablename__ = "users"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    email = Column(String, unique=True, index=True, nullable=False)
    password_hash = Column(String, nullable=False)
    role = Column(String, nullable=False)
    created_at = Column(DateTime, default=datetime.datetime.utcnow)
