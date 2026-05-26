from pathlib import Path
from typing import List
from uuid import UUID

from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile
from fastapi.responses import FileResponse
from sqlalchemy.orm import Session

from app.api.deps import get_db, get_current_user
from app.crud import crud_material, crud_link
from app.models.user import User
from app.services.storage import storage_service
from shared_python.enums import Role
from shared_python.schemas.material import MaterialCreate, MaterialResponse

router = APIRouter()


def _authorize_material_access(mat, current_user: User, db: Session) -> None:
    if current_user.role == Role.STUDENT.value:
        if mat.owner_id != current_user.id:
            raise HTTPException(status_code=403, detail="Not authorized")
        return

    if current_user.role == Role.PARENT.value:
        linked_students = [
            student.id
            for student in crud_link.get_linked_students(
                db,
                parent_id=current_user.id,
            )
        ]

        if mat.owner_id not in linked_students:
            raise HTTPException(
                status_code=403,
                detail="Not authorized (unlinked student)",
            )

        return

    raise HTTPException(status_code=403, detail="Role not allowed")


def _safe_download_filename(title: str, local_path: Path) -> str:
    clean_title = (title or "material").strip() or "material"
    ext = local_path.suffix

    if ext and clean_title.lower().endswith(ext.lower()):
        return clean_title

    if ext:
        return f"{clean_title}{ext}"

    return clean_title


@router.post("/", response_model=MaterialResponse)
async def create_material(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
    title: str | None = Form(default=None),
    source_url: str | None = Form(default=None),
    file: UploadFile | None = File(default=None),
):
    clean_title = (title or "").strip()
    clean_source_url = (source_url or "").strip()

    if file is None and not clean_source_url:
        raise HTTPException(
            status_code=400,
            detail="Provide either an uploaded file or a source_url",
        )

    if file is not None and clean_source_url:
        raise HTTPException(
            status_code=400,
            detail="Provide either an uploaded file or a source_url, not both",
        )

    if file is not None:
        try:
            stored_source = await storage_service.save_upload(file)
        except ValueError as exc:
            raise HTTPException(status_code=400, detail=str(exc))

        final_title = clean_title or Path(file.filename or "material").stem

    else:
        stored_source = storage_service.resolve_url(clean_source_url)

        if not stored_source:
            raise HTTPException(status_code=400, detail="source_url is required")

        final_title = clean_title

        if not final_title:
            raise HTTPException(
                status_code=400,
                detail="title is required when creating a material from a URL",
            )

    material_in = MaterialCreate(
        title=final_title,
        source_url=stored_source,
    )

    return crud_material.create_material(
        db,
        owner_id=current_user.id,
        obj_in=material_in,
    )


@router.get("", response_model=List[MaterialResponse])
@router.get("/", response_model=List[MaterialResponse])
def list_materials(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    if current_user.role == Role.STUDENT.value:
        return crud_material.get_materials_by_owner(
            db,
            owner_id=current_user.id,
        )

    if current_user.role == Role.PARENT.value:
        students = crud_link.get_linked_students(
            db,
            parent_id=current_user.id,
        )

        all_materials = []

        for student in students:
            all_materials.extend(
                crud_material.get_materials_by_owner(
                    db,
                    owner_id=student.id,
                )
            )

        return all_materials

    return []


@router.get("/{id}/download")
def download_material(
    id: UUID,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    mat = crud_material.get_material(
        db,
        material_id=id,
    )

    if not mat:
        raise HTTPException(
            status_code=404,
            detail="Material not found",
        )

    _authorize_material_access(
        mat=mat,
        current_user=current_user,
        db=db,
    )

    local_path = storage_service.get_local_file_path(mat.source_url)

    if local_path is None or not local_path.exists():
        raise HTTPException(
            status_code=404,
            detail=(
                "Stored file not found. "
                "This can happen after a Render restart because uploaded files "
                "are stored on temporary server storage. Re-upload the material."
            ),
        )

    filename = _safe_download_filename(
        title=mat.title,
        local_path=local_path,
    )

    return FileResponse(
        path=str(local_path),
        media_type=storage_service.guess_media_type(local_path),
        filename=filename,
    )


@router.get("/{id}", response_model=MaterialResponse)
def get_material(
    id: UUID,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    mat = crud_material.get_material(
        db,
        material_id=id,
    )

    if not mat:
        raise HTTPException(
            status_code=404,
            detail="Material not found",
        )

    _authorize_material_access(
        mat=mat,
        current_user=current_user,
        db=db,
    )

    return mat


@router.delete("/{id}")
def delete_material(
    id: UUID,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user),
):
    mat = crud_material.get_material(
        db,
        material_id=id,
    )

    if not mat:
        raise HTTPException(
            status_code=404,
            detail="Material not found",
        )

    if current_user.role == Role.STUDENT.value:
        if mat.owner_id != current_user.id:
            raise HTTPException(status_code=403, detail="Not authorized")

    elif current_user.role == Role.PARENT.value:
        if mat.owner_id != current_user.id:
            raise HTTPException(
                status_code=403,
                detail="Parents cannot delete linked student's personal files",
            )

    else:
        raise HTTPException(status_code=403, detail="Role not allowed")

    storage_service.delete_source(mat.source_url)

    crud_material.delete_material(
        db,
        material_id=id,
    )

    return {"status": "success"}