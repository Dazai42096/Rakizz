def test_require_role_rejection(client):
    # Register student
    client.post("/api/v1/auth/register", json={"email": "stu_b@test.com", "password": "pass", "role": "student"})
    response = client.post("/api/v1/auth/login", data={"username": "stu_b@test.com", "password": "pass"})
    student_token = response.json()["access_token"]
    
    # Hit parent endpoint as student
    response = client.get("/api/v1/links/students", headers={"Authorization": f"Bearer {student_token}"})
    assert response.status_code == 403
    assert "Operation not permitted" in response.json()["detail"]
