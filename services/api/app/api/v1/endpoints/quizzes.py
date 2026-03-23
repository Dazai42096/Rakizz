from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.api.deps import get_db, get_current_user
from app.models.user import User
from app.crud import crud_quiz, crud_material, crud_link
from shared_python.schemas.quiz import (
    QuizGenerateRequest, QuizSetPublicResponse, QuizQuestionPublic, QuizQuestionReview,
    QuizAttemptCreate, QuizAttemptResultResponse
)
from shared_python.enums import Role
from uuid import UUID
from app.services.ai_pipeline import ai_service

router = APIRouter()

@router.post("/generate", response_model=QuizSetPublicResponse)
def generate_quiz(req: QuizGenerateRequest, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    material = crud_material.get_material(db, req.material_id)
    if not material:
        raise HTTPException(status_code=404, detail="Material not found")
        
    if current_user.role == Role.STUDENT.value:
        if material.owner_id != current_user.id:
            raise HTTPException(status_code=403, detail="Not authorized")
    elif current_user.role == Role.PARENT.value:
        linked_students = [s.id for s in crud_link.get_linked_students(db, parent_id=current_user.id)]
        if material.owner_id not in linked_students:
            raise HTTPException(status_code=403, detail="Not authorized")
    else:
        raise HTTPException(status_code=403, detail="Admins restricted")
        
    raw_questions = ai_service.generate_quiz(material.source_url, req.difficulty)
    
    quiz_set = crud_quiz.create_quiz_set(db, material_id=material.id)
    public_questions = []
    
    for q in raw_questions:
        db_q = crud_quiz.create_quiz_question(db, quiz_set.id, q["question_text"], q["options"], q["correct_answer"], q["explanation"], q["source_chunk_snippet"])
        public_questions.append(QuizQuestionPublic(
            id=db_q.id,
            question_text=db_q.question_text,
            options=db_q.options_json
        ))
        
    return QuizSetPublicResponse(
        id=quiz_set.id,
        material_id=material.id,
        questions=public_questions
    )

@router.get("/{id}", response_model=QuizSetPublicResponse)
def get_quiz(id: UUID, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    quiz_set = crud_quiz.get_quiz_set(db, id)
    if not quiz_set:
        raise HTTPException(status_code=404, detail="Not found")
        
    material = crud_material.get_material(db, quiz_set.material_id)
    if current_user.role == Role.STUDENT.value:
        if material.owner_id != current_user.id:
            raise HTTPException(status_code=403, detail="Not authorized")
    elif current_user.role == Role.PARENT.value:
        linked_students = [s.id for s in crud_link.get_linked_students(db, parent_id=current_user.id)]
        if material.owner_id not in linked_students:
            raise HTTPException(status_code=403, detail="Not authorized")
            
    questions = crud_quiz.get_quiz_questions(db, quiz_set.id)
    public_questions = [QuizQuestionPublic(id=q.id, question_text=q.question_text, options=q.options_json) for q in questions]
    return QuizSetPublicResponse(id=quiz_set.id, material_id=quiz_set.material_id, questions=public_questions)

@router.post("/{id}/attempts", response_model=QuizAttemptResultResponse)
def submit_attempt(id: UUID, attempt_in: QuizAttemptCreate, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    if current_user.role != Role.STUDENT.value:
        raise HTTPException(status_code=403, detail="Only students can submit attempts")
        
    quiz_set = crud_quiz.get_quiz_set(db, id)
    if not quiz_set:
        raise HTTPException(status_code=404, detail="Not found")
        
    material = crud_material.get_material(db, quiz_set.material_id)
    if material.owner_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")
        
    questions = crud_quiz.get_quiz_questions(db, quiz_set.id)
    correct_count = 0
    review_data = []
    
    for q in questions:
        submitted_answer = attempt_in.answers.get(q.id)
        if submitted_answer == q.correct_answer:
            correct_count += 1
            
        review_data.append(QuizQuestionReview(
            id=q.id,
            question_text=q.question_text,
            options=q.options_json,
            correct_answer=q.correct_answer,
            explanation=q.explanation,
            source_chunk_snippet=q.source_chunk_snippet
        ))
    
    score = correct_count / len(questions) if questions else 0.0
    passed = score >= 0.7
    
    return QuizAttemptResultResponse(
        id=crud_quiz.create_attempt(db, quiz_set.id, current_user.id, score, passed).id,
        quiz_set_id=quiz_set.id,
        student_id=current_user.id,
        score=score,
        passed=passed,
        review_data=review_data
    )
