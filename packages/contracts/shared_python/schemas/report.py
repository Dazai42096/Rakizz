from pydantic import BaseModel
from uuid import UUID
from datetime import date
from typing import Dict, Any

class ReportBase(BaseModel):
    student_id: UUID
    report_date: date
    aggregated_metrics_json: Dict[str, Any]

class ReportResponse(ReportBase):
    id: UUID

    class Config:
        from_attributes = True
