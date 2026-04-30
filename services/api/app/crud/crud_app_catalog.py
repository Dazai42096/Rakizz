from uuid import UUID

from sqlalchemy.orm import Session

from app.models.policy import StudentInstalledApp
from shared_python.schemas.policy import InstalledAppCreate


def replace_student_apps(
    db: Session,
    student_id: UUID,
    apps: list[InstalledAppCreate],
) -> list[StudentInstalledApp]:
    # simple way: remove old app list then save the new one
    db.query(StudentInstalledApp).filter(
        StudentInstalledApp.student_id == student_id
    ).delete()

    saved_apps = []

    for app in apps:
        db_app = StudentInstalledApp(
            student_id=student_id,
            package_name=app.package_name.strip(),
            app_name=app.app_name.strip(),
            category=app.category,
        )

        db.add(db_app)
        saved_apps.append(db_app)

    db.commit()

    for app in saved_apps:
        db.refresh(app)

    return saved_apps


def get_student_apps(
    db: Session,
    student_id: UUID,
) -> list[StudentInstalledApp]:
    # parent uses this to see apps from student phone
    return (
        db.query(StudentInstalledApp)
        .filter(StudentInstalledApp.student_id == student_id)
        .order_by(StudentInstalledApp.app_name.asc())
        .all()
    )