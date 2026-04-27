from datetime import datetime
from uuid import UUID

from sqlalchemy import func
from sqlalchemy.orm import Session

from app.models.policy import UsageEvent
from shared_python.schemas.policy import UsageEventCreate


def create_usage_events(
    db: Session,
    student_id: UUID,
    events: list[UsageEventCreate],
) -> list[UsageEvent]:
    # save usage records sent from android
    db_events = []

    for event in events:
        db_event = UsageEvent(
            student_id=student_id,
            package_name=event.package_name.strip(),
            duration_sec=event.duration_sec,
            timestamp=event.timestamp,
        )

        db.add(db_event)
        db_events.append(db_event)

    db.commit()

    for db_event in db_events:
        db.refresh(db_event)

    return db_events


def get_usage_summary(
    db: Session,
    student_id: UUID,
    since_time: datetime,
) -> list[dict]:
    # group usage by app name
    rows = (
        db.query(
            UsageEvent.package_name,
            func.sum(UsageEvent.duration_sec).label("total_duration_sec"),
        )
        .filter(UsageEvent.student_id == student_id)
        .filter(UsageEvent.timestamp >= since_time)
        .group_by(UsageEvent.package_name)
        .all()
    )

    summary = []

    for row in rows:
        summary.append(
            {
                "package_name": row.package_name,
                "duration_sec": int(row.total_duration_sec or 0),
            }
        )

    return summary