def test_health(client):
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok"}

def test_register_and_login(client):
    register_data = {"email": "test@test.com", "password": "password", "role": "student"}
    response = client.post("/api/v1/auth/register", json=register_data)
    assert response.status_code == 200
    assert response.json()["email"] == register_data["email"]

    login_data = {"username": "test@test.com", "password": "password"}
    response = client.post("/api/v1/auth/login", data=login_data)
    assert response.status_code == 200
    assert "access_token" in response.json()
