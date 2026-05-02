from typing import List
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, get_db
from app.crud import crud_link, crud_material, crud_quiz
from app.models.user import User
from app.services.ai_pipeline import ai_service
from shared_python.enums import Role
from shared_python.schemas.quiz import (
    QuizAttemptCreate,
    QuizAttemptResultResponse,
    QuizGenerateRequest,
    QuizQuestionPublic,
    QuizQuestionReview,
    QuizSetPublicResponse,
)

router = APIRouter()


# the app may call /quizzes or /quizzes/
# so i added both to avoid the 404 problem
@router.get("", response_model=List[QuizSetPublicResponse])
@router.get("/", response_model=List[QuizSetPublicResponse])
def list_quizzes(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    # student sees quizzes made from his own materials
    if current_user.role == Role.STUDENT.value:
        quiz_sets = crud_quiz.get_quiz_sets_for_student(
            db=db,
            student_id=current_user.id,
        )

    # parent sees quizzes for linked students only
    elif current_user.role == Role.PARENT.value:
        linked_students = crud_link.get_linked_students(
            db,
            parent_id=current_user.id,
        )

        linked_ids = [
            student.id
            for student in linked_students
        ]

        quiz_sets = crud_quiz.get_quiz_sets_for_students(
            db=db,
            student_ids=linked_ids,
        )

    else:
        raise HTTPException(
            status_code=403,
            detail="Admins restricted",
        )

    result = []

    for quiz_set in quiz_sets:
        questions = crud_quiz.get_quiz_questions(
            db=db,
            quiz_set_id=quiz_set.id,
        )

        public_questions = [
            QuizQuestionPublic(
                id=q.id,
                question_text=q.question_text,
                options=q.options_json,
            )
            for q in questions
        ]

        result.append(
            QuizSetPublicResponse(
                id=quiz_set.id,
                material_id=quiz_set.material_id,
                questions=public_questions,
            )
        )

    return result


@router.post("/generate", response_model=QuizSetPublicResponse)
def generate_quiz(
    req: QuizGenerateRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    material = crud_material.get_material(
        db,
        req.material_id,
    )

    if not material:
        raise HTTPException(
            status_code=404,
            detail="Material not found",
        )

    # student can generate quiz only from his own material
    if current_user.role == Role.STUDENT.value:
        if material.owner_id != current_user.id:
            raise HTTPException(
                status_code=403,
                detail="Not authorized",
            )

    # parent can generate quiz only from linked student material
    elif current_user.role == Role.PARENT.value:
        linked_students = [
            student.id
            for student in crud_link.get_linked_students(
                db,
                parent_id=current_user.id,
            )
        ]

        if material.owner_id not in linked_students:
            raise HTTPException(
                status_code=403,
                detail="Not authorized",
            )

    else:
        raise HTTPException(
            status_code=403,
            detail="Admins restricted",
        )

    # get old questions so the new quiz tries to be different
    old_questions = crud_quiz.get_old_questions_for_material(
        db=db,
        material_id=material.id,
    )

    old_question_texts = [
        q.question_text
        for q in old_questions
    ]

    old_source_snippets = [
        q.source_chunk_snippet or ""
        for q in old_questions
    ]

    # this is the quiz generator
    raw_questions = ai_service.generate_quiz(
        material_url=material.source_url,
        difficulty=req.difficulty,
        old_question_texts=old_question_texts,
        old_source_snippets=old_source_snippets,
    )

    quiz_set = crud_quiz.create_quiz_set(
        db=db,
        material_id=material.id,
    )

    public_questions = []

    for q in raw_questions:
        db_q = crud_quiz.create_quiz_question(
            db=db,
            quiz_set_id=quiz_set.id,
            text=q["question_text"],
            options=q["options"],
            correct=q["correct_answer"],
            explanation=q["explanation"],
            snippet=q["source_chunk_snippet"],
        )

        public_questions.append(
            QuizQuestionPublic(
                id=db_q.id,
                question_text=db_q.question_text,
                options=db_q.options_json,
            )
        )

    return QuizSetPublicResponse(
        id=quiz_set.id,
        material_id=material.id,
        questions=public_questions,
    )


@router.get("/{id}", response_model=QuizSetPublicResponse)
def get_quiz(
    id: UUID,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    quiz_set = crud_quiz.get_quiz_set(
        db,
        id,
    )

    if not quiz_set:
        raise HTTPException(
            status_code=404,
            detail="Not found",
        )

    material = crud_material.get_material(
        db,
        quiz_set.material_id,
    )

    if not material:
        raise HTTPException(
            status_code=404,
            detail="Material not found",
        )

    if current_user.role == Role.STUDENT.value:
        if material.owner_id != current_user.id:
            raise HTTPException(
                status_code=403,
                detail="Not authorized",
            )

    elif current_user.role == Role.PARENT.value:
        linked_students = [
            student.id
            for student in crud_link.get_linked_students(
                db,
                parent_id=current_user.id,
            )
        ]

        if material.owner_id not in linked_students:
            raise HTTPException(
                status_code=403,
                detail="Not authorized",
            )

    questions = crud_quiz.get_quiz_questions(
        db,
        quiz_set.id,
    )

    public_questions = [
        QuizQuestionPublic(
            id=q.id,
            question_text=q.question_text,
            options=q.options_json,
        )
        for q in questions
    ]

    return QuizSetPublicResponse(
        id=quiz_set.id,
        material_id=quiz_set.material_id,
        questions=public_questions,
    )


@router.post("/{id}/attempts", response_model=QuizAttemptResultResponse)
def submit_attempt(
    id: UUID,
    attempt_in: QuizAttemptCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    if current_user.role != Role.STUDENT.value:
        raise HTTPException(
            status_code=403,
            detail="Only students can submit attempts",
        )

    quiz_set = crud_quiz.get_quiz_set(
        db,
        id,
    )

    if not quiz_set:
        raise HTTPException(
            status_code=404,
            detail="Not found",
        )

    material = crud_material.get_material(
        db,
        quiz_set.material_id,
    )

    if not material:
        raise HTTPException(
            status_code=404,
            detail="Material not found",
        )

    if material.owner_id != current_user.id:
        raise HTTPException(
            status_code=403,
            detail="Not authorized",
        )

    questions = crud_quiz.get_quiz_questions(
        db,
        quiz_set.id,
    )

    correct_count = 0
    review_data = []

    for q in questions:
        submitted_answer = attempt_in.answers.get(q.id)

        if submitted_answer == q.correct_answer:
            correct_count += 1

        review_data.append(
            QuizQuestionReview(
                id=q.id,
                question_text=q.question_text,
                options=q.options_json,
                correct_answer=q.correct_answer,
                explanation=q.explanation,
                source_chunk_snippet=q.source_chunk_snippet,
            )
        )

    score = correct_count / len(questions) if questions else 0.0
    passed = score >= 0.7

    attempt = crud_quiz.create_attempt(
        db=db,
        quiz_set_id=quiz_set.id,
        student_id=current_user.id,
        score=score,
        passed=passed,
    )

    return QuizAttemptResultResponse(
        id=attempt.id,
        quiz_set_id=quiz_set.id,
        student_id=current_user.id,
        score=score,
        passed=passed,
        review_data=review_data,
    )