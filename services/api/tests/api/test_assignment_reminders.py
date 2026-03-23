from unittest.mock import patch
from shared_python.enums import AssignmentStatus
from shared_python.schemas.assignment import AssignmentCreate

def test_reminder_lifecycle(client):
    # Setup users
    client.post("/api/v1/auth/register", json={"email": "parent_rem@test.com", "password": "pass", "role": "parent"})
    parent_token = client.post("/api/v1/auth/login", data={"username": "parent_rem@test.com", "password": "pass"}).json()["access_token"]
    parent_id = client.get("/api/v1/users/me", headers={"Authorization": f"Bearer {parent_token}"}).json()["id"]

    client.post("/api/v1/auth/register", json={"email": "student_rem@test.com", "password": "pass", "role": "student"})
    student_token = client.post("/api/v1/auth/login", data={"username": "student_rem@test.com", "password": "pass"}).json()["access_token"]
    student_id = client.get("/api/v1/users/me", headers={"Authorization": f"Bearer {student_token}"}).json()["id"]

    # Link parent -> student
    client.post("/api/v1/links/", json={"parent_id": parent_id, "student_id": student_id}, headers={"Authorization": f"Bearer {parent_token}"})

    with patch("app.api.v1.endpoints.assignments.reminders.schedule") as mock_schedule, \
         patch("app.api.v1.endpoints.assignments.reminders.reschedule") as mock_reschedule, \
         patch("app.api.v1.endpoints.assignments.reminders.cancel") as mock_cancel:

        # 1. Create -> schedule() fires
        res = client.post("/api/v1/assignments/", 
                          json={"student_id": student_id, "description": "Test Task", "due_date": "2030-01-01T00:00:00Z"},
                          headers={"Authorization": f"Bearer {parent_token}"})
        assert res.status_code == 200
        assignment_id = res.json()["id"]
        assert mock_schedule.called

        # 2. Update metadata -> reschedule() fires
        res_update = client.patch(f"/api/v1/assignments/{assignment_id}/metadata",
                                  json={"due_date": "2030-01-02T00:00:00Z"},
                                  headers={"Authorization": f"Bearer {parent_token}"})
        assert res_update.status_code == 200
        assert mock_reschedule.called

        # 3. Update status (Completion) -> cancel() fires
        res_status = client.patch(f"/api/v1/assignments/{assignment_id}/status",
                                  json={"status": AssignmentStatus.COMPLETED.value},
                                  headers={"Authorization": f"Bearer {parent_token}"})
        assert res_status.status_code == 200
        assert mock_cancel.called
        assert mock_cancel.call_count == 1

        # 4. Delete assignment -> cancel() fires again
        res_delete = client.delete(f"/api/v1/assignments/{assignment_id}",
                                   headers={"Authorization": f"Bearer {parent_token}"})
        assert res_delete.status_code == 200
        assert mock_cancel.call_count == 2
