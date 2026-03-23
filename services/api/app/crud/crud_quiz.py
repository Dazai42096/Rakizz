from sqlalchemy.orm import Session
from app.models.quiz import QuizSet, QuizQuestion, QuizAttempt
from uuid import UUID

def create_quiz_set(db: Session, material_id: UUID):
    db_obj = QuizSet(material_id=material_id)
    db.add(db_obj)
    db.commit()
    db.refresh(db_obj)
    return db_obj

def create_quiz_question(db: Session, quiz_set_id: UUID, text: str, options: list, correct: str, explanation: str, snippet: str):
    db_obj = QuizQuestion(
        quiz_set_id=quiz_set_id, 
        question_text=text, 
        options_json=options, 
        correct_answer=correct,
        explanation=explanation,
        source_chunk_snippet=snippet
    )
    db.add(db_obj)
    db.commit()
    db.refresh(db_obj)
    return db_obj

def get_quiz_set(db: Session, quiz_set_id: UUID):
    return db.query(QuizSet).filter(QuizSet.id == quiz_set_id).first()
    
def get_quiz_questions(db: Session, quiz_set_id: UUID):
    return db.query(QuizQuestion).filter(QuizQuestion.quiz_set_id == quiz_set_id).all()

def create_attempt(db: Session, quiz_set_id: UUID, student_id: UUID, score: float, passed: bool):
    db_obj = QuizAttempt(quiz_set_id=quiz_set_id, student_id=student_id, score=score, passed=passed)
    db.add(db_obj)
    db.commit()
    db.refresh(db_obj)
    return db_obj
