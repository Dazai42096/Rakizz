from sqlalchemy import Column, String, DateTime, Integer, ForeignKey
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy import JSON
import uuid
import datetime
from app.db.base import Base

class BlockingPolicy(Base):
    __tablename__ = "blocking_policies"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    parent_id = Column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    student_id = Column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    rule_type = Column(String, nullable=False)
    config_json = Column(JSON, nullable=False)

class BlockedAppCatalog(Base):
    __tablename__ = "blocked_app_catalog"

    package_name = Column(String, primary_key=True)
    friendly_name = Column(String, nullable=False)
    category = Column(String, nullable=True)

class UsageEvent(Base):
    __tablename__ = "usage_events"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    student_id = Column(UUID(as_uuid=True), ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    package_name = Column(String, nullable=False)
    duration_sec = Column(Integer, nullable=False)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow)
