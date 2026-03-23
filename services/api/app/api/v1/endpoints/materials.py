from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.api.deps import get_db, get_current_user
from app.models.user import User
from app.crud import crud_material, crud_link
from shared_python.schemas.material import MaterialCreate, MaterialResponse
from shared_python.enums import Role
from typing import List
from uuid import UUID
from app.services.storage import storage_service

router = APIRouter()

@router.post("/", response_model=MaterialResponse)
def create_material(material_in: MaterialCreate, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    material_in.source_url = storage_service.resolve_url(material_in.source_url)
    return crud_material.create_material(db, owner_id=current_user.id, obj_in=material_in)

@router.get("/", response_model=List[MaterialResponse])
def list_materials(db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    if current_user.role == Role.STUDENT.value:
        return crud_material.get_materials_by_owner(db, owner_id=current_user.id)
    elif current_user.role == Role.PARENT.value:
        students = crud_link.get_linked_students(db, parent_id=current_user.id)
        all_materials = []
        for s in students:
            all_materials.extend(crud_material.get_materials_by_owner(db, owner_id=s.id))
        return all_materials
    return []

@router.get("/{id}", response_model=MaterialResponse)
def get_material(id: UUID, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    mat = crud_material.get_material(db, material_id=id)
    if not mat:
        raise HTTPException(status_code=404, detail="Material not found")
    
    if current_user.role == Role.STUDENT.value and mat.owner_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")
    if current_user.role == Role.PARENT.value:
        linked_students = [s.id for s in crud_link.get_linked_students(db, parent_id=current_user.id)]
        if mat.owner_id not in linked_students:
            raise HTTPException(status_code=403, detail="Not authorized (unlinked student)")
            
    return mat

@router.delete("/{id}")
def delete_material(id: UUID, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    mat = crud_material.get_material(db, material_id=id)
    if not mat:
        raise HTTPException(status_code=404, detail="Material not found")
    
    if current_user.role == Role.STUDENT.value and mat.owner_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")
    elif current_user.role == Role.PARENT.value:
         if mat.owner_id != current_user.id:
             raise HTTPException(status_code=403, detail="Parents cannot delete linked student's personal files")
             
    crud_material.delete_material(db, material_id=id)
    return {"status": "success"}
