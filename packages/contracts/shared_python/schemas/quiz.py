from pydantic import BaseModel
from typing import List, Dict, Optional
from uuid import UUID
from enum import Enum


class DifficultyLevel(str, Enum):
    EASY = "EASY"
    MEDIUM = "MEDIUM"
    HARD = "HARD"

    # this is what the app will use now
    # student will not choose the level
    MIXED = "MIXED"


class QuizGenerateRequest(BaseModel):
    material_id: UUID
    difficulty: DifficultyLevel


class QuizQuestionPublic(BaseModel):
    id: UUID
    question_text: str
    options: List[str]

    class Config:
        from_attributes = True


class QuizQuestionReview(BaseModel):
    id: UUID
    question_text: str
    options: List[str]
    correct_answer: str
    explanation: str
    source_chunk_snippet: Optional[str] = None

    class Config:
        from_attributes = True


class QuizSetPublicResponse(BaseModel):
    id: UUID
    material_id: UUID
    questions: List[QuizQuestionPublic]

    class Config:
        from_attributes = True


class QuizAttemptCreate(BaseModel):
    answers: Dict[UUID, str]


class QuizAttemptResultResponse(BaseModel):
    id: UUID
    quiz_set_id: UUID
    student_id: UUID
    score: float
    passed: bool
    review_data: List[QuizQuestionReview]

    class Config:
        from_attributes = True