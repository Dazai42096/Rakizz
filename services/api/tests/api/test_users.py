def test_read_users_me(client):
    client.post("/api/v1/auth/register", json={"email": "me@test.com", "password": "pass", "role": "admin"})
    response = client.post("/api/v1/auth/login", data={"username": "me@test.com", "password": "pass"})
    token = response.json()["access_token"]
    
    response = client.get("/api/v1/users/me", headers={"Authorization": f"Bearer {token}"})
    assert response.status_code == 200
    assert response.json()["email"] == "me@test.com"
