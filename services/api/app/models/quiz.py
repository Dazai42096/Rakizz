from sqlalchemy import Column, String, DateTime, Float, Boolean, Integer, ForeignKey
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy import JSON
import uuid
import datetime

from app.db.base import Base


class QuizSet(Base):
    __tablename__ = "quiz_sets"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    material_id = Column(
        UUID(as_uuid=True),
        ForeignKey("materials.id", ondelete="CASCADE"),
        nullable=False,
    )
    generation_date = Column(DateTime, default=datetime.datetime.utcnow)


class QuizQuestion(Base):
    __tablename__ = "quiz_questions"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    quiz_set_id = Column(
        UUID(as_uuid=True),
        ForeignKey("quiz_sets.id", ondelete="CASCADE"),
        nullable=False,
    )

    question_text = Column(String, nullable=False)
    options_json = Column(JSON, nullable=False)
    correct_answer = Column(String, nullable=False)

    # saved so student can review the answer
    explanation = Column(String, nullable=True)
    source_chunk_snippet = Column(String, nullable=True)


class QuizAttempt(Base):
    __tablename__ = "quiz_attempts"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    quiz_set_id = Column(
        UUID(as_uuid=True),
        ForeignKey("quiz_sets.id", ondelete="CASCADE"),
        nullable=False,
    )
    student_id = Column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
    )

    score = Column(Float, nullable=False)
    passed = Column(Boolean, nullable=False)


class UnlockSession(Base):
    __tablename__ = "unlock_sessions"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)

    student_id = Column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
    )

    quiz_attempt_id = Column(
        UUID(as_uuid=True),
        ForeignKey("quiz_attempts.id", ondelete="CASCADE"),
        nullable=False,
    )

    # app unlocked after passing the quiz
    package_name = Column(String, nullable=True)

    granted_minutes = Column(Integer, nullable=False)
    expires_at = Column(DateTime, nullable=False)