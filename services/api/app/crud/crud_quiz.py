from uuid import UUID

from sqlalchemy.orm import Session

from app.models.material import Material
from app.models.quiz import QuizAttempt, QuizQuestion, QuizSet


def create_quiz_set(
    db: Session,
    material_id: UUID,
):
    db_obj = QuizSet(
        material_id=material_id,
    )

    db.add(db_obj)
    db.commit()
    db.refresh(db_obj)

    return db_obj


def create_quiz_question(
    db: Session,
    quiz_set_id: UUID,
    text: str,
    options: list,
    correct: str,
    explanation: str,
    snippet: str,
):
    db_obj = QuizQuestion(
        quiz_set_id=quiz_set_id,
        question_text=text,
        options_json=options,
        correct_answer=correct,
        explanation=explanation,
        source_chunk_snippet=snippet,
    )

    db.add(db_obj)
    db.commit()
    db.refresh(db_obj)

    return db_obj


def get_quiz_set(
    db: Session,
    quiz_set_id: UUID,
):
    return (
        db.query(QuizSet)
        .filter(QuizSet.id == quiz_set_id)
        .first()
    )


def get_quiz_questions(
    db: Session,
    quiz_set_id: UUID,
):
    return (
        db.query(QuizQuestion)
        .filter(QuizQuestion.quiz_set_id == quiz_set_id)
        .all()
    )


def get_quiz_sets_for_student(
    db: Session,
    student_id: UUID,
):
    # get all quizzes made from this student's materials
    return (
        db.query(QuizSet)
        .join(Material, QuizSet.material_id == Material.id)
        .filter(Material.owner_id == student_id)
        .order_by(QuizSet.generation_date.desc())
        .all()
    )


def get_quiz_sets_for_students(
    db: Session,
    student_ids: list[UUID],
):
    # parent can see quizzes for linked students
    if not student_ids:
        return []

    return (
        db.query(QuizSet)
        .join(Material, QuizSet.material_id == Material.id)
        .filter(Material.owner_id.in_(student_ids))
        .order_by(QuizSet.generation_date.desc())
        .all()
    )


def get_old_questions_for_material(
    db: Session,
    material_id: UUID,
):
    # used so the generated quiz does not repeat a lot
    return (
        db.query(QuizQuestion)
        .join(QuizSet, QuizQuestion.quiz_set_id == QuizSet.id)
        .filter(QuizSet.material_id == material_id)
        .all()
    )


def create_attempt(
    db: Session,
    quiz_set_id: UUID,
    student_id: UUID,
    score: float,
    passed: bool,
):
    db_obj = QuizAttempt(
        quiz_set_id=quiz_set_id,
        student_id=student_id,
        score=score,
        passed=passed,
    )

    db.add(db_obj)
    db.commit()
    db.refresh(db_obj)

    return db_obj