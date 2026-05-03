import datetime
from typing import Any

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.api.deps import get_current_user, get_db
from app.models.material import Material
from app.models.policy import BlockingPolicy
from app.models.quiz import QuizAttempt, UnlockSession
from app.models.user import User
from shared_python.enums import Role
from shared_python.schemas.unlock import (
    UnlockCheckRequest,
    UnlockCheckResponse,
    UnlockGrantRequest,
    UnlockGrantResponse,
)

router = APIRouter()


@router.post("/check", response_model=UnlockCheckResponse)
def check_blocked_app(
    request: UnlockCheckRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
) -> Any:
    if current_user.role != Role.STUDENT.value:
        raise HTTPException(
            status_code=403,
            detail="Only student can check blocked apps",
        )

    package_name = request.package_name.strip()

    if not package_name:
        raise HTTPException(
            status_code=400,
            detail="App name is required",
        )

    # if student already passed quiz, do not block again for now
    old_unlock = _get_active_unlock(
        db=db,
        student_id=current_user.id,
        package_name=package_name,
    )

    if old_unlock:
        return UnlockCheckResponse(
            blocked=False,
            package_name=package_name,
            message="This app is already unlocked for now.",
            unlocked_until=old_unlock.expires_at,
        )

    active_rule = None

    # if accessibility service opened Rakizz, we trust it for checkpoint
    # if not forced, backend checks the parent rule normally
    if not request.force_blocked:
        active_rule = _find_active_rule_for_app(
            db=db,
            student_id=current_user.id,
            package_name=package_name,
        )

        if not active_rule:
            return UnlockCheckResponse(
                blocked=False,
                package_name=package_name,
                message="No active focus rule for this app right now.",
            )

    # use newest material for unlock quiz
    material = (
        db.query(Material)
        .filter(Material.owner_id == current_user.id)
        .order_by(Material.created_at.desc())
        .first()
    )

    if not material:
        return UnlockCheckResponse(
            blocked=True,
            package_name=package_name,
            message="This app is blocked, but no study material was found. Upload material first.",
        )

    app_name = None

    if active_rule:
        app_name = _find_app_name_from_rule(active_rule, package_name)

    return UnlockCheckResponse(
        blocked=True,
        package_name=package_name,
        app_name=app_name,
        message="This app is blocked. Pass a mixed AI quiz to unlock it.",
        material_id=material.id,
    )


@router.post("/grant", response_model=UnlockGrantResponse)
def grant_unlock_after_quiz(
    request: UnlockGrantRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
) -> Any:
    if current_user.role != Role.STUDENT.value:
        raise HTTPException(
            status_code=403,
            detail="Only student can unlock apps",
        )

    attempt = (
        db.query(QuizAttempt)
        .filter(
            QuizAttempt.id == request.quiz_attempt_id,
            QuizAttempt.student_id == current_user.id,
        )
        .first()
    )

    if not attempt:
        raise HTTPException(
            status_code=404,
            detail="Quiz attempt not found",
        )

    if not attempt.passed:
        raise HTTPException(
            status_code=400,
            detail="Quiz was not passed",
        )

    minutes = request.granted_minutes

    if minutes <= 0:
        minutes = 15

    if minutes > 60:
        minutes = 60

    now = datetime.datetime.utcnow()
    expires_at = now + datetime.timedelta(minutes=minutes)

    unlock = UnlockSession(
        student_id=current_user.id,
        quiz_attempt_id=attempt.id,
        package_name=request.package_name.strip(),
        granted_minutes=minutes,
        expires_at=expires_at,
    )

    db.add(unlock)
    db.commit()
    db.refresh(unlock)

    return UnlockGrantResponse(
        status="unlocked",
        package_name=unlock.package_name or request.package_name,
        granted_minutes=unlock.granted_minutes,
        expires_at=unlock.expires_at,
    )


def _get_active_unlock(
    db: Session,
    student_id,
    package_name: str,
) -> UnlockSession | None:
    now = datetime.datetime.utcnow()

    return (
        db.query(UnlockSession)
        .filter(
            UnlockSession.student_id == student_id,
            UnlockSession.package_name == package_name,
            UnlockSession.expires_at > now,
        )
        .order_by(UnlockSession.expires_at.desc())
        .first()
    )


def _find_active_rule_for_app(
    db: Session,
    student_id,
    package_name: str,
) -> BlockingPolicy | None:
    rules = (
        db.query(BlockingPolicy)
        .filter(BlockingPolicy.student_id == student_id)
        .all()
    )

    for rule in rules:
        config = rule.config_json or {}

        if not _time_is_active(config):
            continue

        if _rule_has_app(config, package_name):
            return rule

    return None


def _time_is_active(
    config: dict,
) -> bool:
    start_time = str(config.get("start_time") or "").strip()
    end_time = str(config.get("end_time") or "").strip()

    # if no time exists, treat it as active for preview
    if not start_time or not end_time:
        return True

    now = datetime.datetime.now().strftime("%H:%M")

    if start_time <= end_time:
        return start_time <= now <= end_time

    return now >= start_time or now <= end_time


def _rule_has_app(
    config: dict,
    package_name: str,
) -> bool:
    blocked_apps = config.get("blocked_apps") or []
    package_lower = package_name.lower()

    for app in blocked_apps:
        app_package = str(app.get("package_name") or "").lower()
        app_name = str(app.get("app_name") or "").lower()

        if package_lower == app_package:
            return True

        if package_lower == app_name:
            return True

    old_package = str(config.get("package_name") or "").lower()

    return package_lower == old_package


def _find_app_name_from_rule(
    rule: BlockingPolicy,
    package_name: str,
) -> str | None:
    config = rule.config_json or {}
    blocked_apps = config.get("blocked_apps") or []
    package_lower = package_name.lower()

    for app in blocked_apps:
        app_package = str(app.get("package_name") or "").lower()
        app_name = str(app.get("app_name") or "")

        if package_lower == app_package or package_lower == app_name.lower():
            return app_name

    return None