from uuid import UUID
import logging

logger = logging.getLogger(__name__)

def schedule(assignment_id: UUID, due_date):
    logger.info(f"Scheduled reminder for {assignment_id} at {due_date}")

def reschedule(assignment_id: UUID, new_due_date):
    logger.info(f"Rescheduled reminder for {assignment_id} to {new_due_date}")

def cancel(assignment_id: UUID):
    logger.info(f"Canceled reminder for {assignment_id}")
