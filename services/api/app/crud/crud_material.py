from sqlalchemy.orm import Session
from app.models.material import Material
from shared_python.schemas.material import MaterialCreate
from uuid import UUID


def create_material(db: Session, owner_id: UUID, obj_in: MaterialCreate):
    db_obj = Material(
        owner_id=owner_id,
        title=obj_in.title.strip(),
        source_url=obj_in.source_url.strip(),
    )
    db.add(db_obj)
    db.commit()
    db.refresh(db_obj)
    return db_obj


def get_materials_by_owner(db: Session, owner_id: UUID):
    return db.query(Material).filter(Material.owner_id == owner_id).all()


def get_material(db: Session, material_id: UUID):
    return db.query(Material).filter(Material.id == material_id).first()


def delete_material(db: Session, material_id: UUID):
    db.query(Material).filter(Material.id == material_id).delete()
    db.commit()