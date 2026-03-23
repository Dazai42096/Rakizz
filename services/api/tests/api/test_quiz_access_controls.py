import uuid

def test_quiz_access_limits(client):
    # Setup users
    client.post("/api/v1/auth/register", json={"email": "stua@test.com", "password": "pass", "role": "student"})
    student_a_token = client.post("/api/v1/auth/login", data={"username": "stua@test.com", "password": "pass"}).json()["access_token"]
    
    client.post("/api/v1/auth/register", json={"email": "stub@test.com", "password": "pass", "role": "student"})
    student_b_token = client.post("/api/v1/auth/login", data={"username": "stub@test.com", "password": "pass"}).json()["access_token"]

    # Student A creates material
    mat_res = client.post("/api/v1/materials/", 
                          json={"title": "Notes", "source_url": "https://example.com/notes.pdf", "file_format": "PDF"},
                          headers={"Authorization": f"Bearer {student_a_token}"})
    assert mat_res.status_code == 200
    material_id = mat_res.json()["id"]

    # Student B tries to generate quiz -> 403 Forbidden
    gen_res_b = client.post("/api/v1/quizzes/generate", json={"material_id": material_id, "difficulty": "EASY"}, headers={"Authorization": f"Bearer {student_b_token}"})
    assert gen_res_b.status_code == 403

    # Missing Material 404
    gen_res_404 = client.post("/api/v1/quizzes/generate", json={"material_id": str(uuid.uuid4()), "difficulty": "EASY"}, headers={"Authorization": f"Bearer {student_a_token}"})
    assert gen_res_404.status_code == 404

    # Student A correctly generates quiz
    gen_res_a = client.post("/api/v1/quizzes/generate", json={"material_id": material_id, "difficulty": "EASY"}, headers={"Authorization": f"Bearer {student_a_token}"})
    assert gen_res_a.status_code == 200
    quiz_set_id = gen_res_a.json()["id"]
    
    # Assert generated questions don't leak answers payload (QuizQuestionPublic isolation)
    question = gen_res_a.json()["questions"][0]
    assert "correct_answer" not in question
    assert "explanation" not in question
    assert "source_chunk_snippet" not in question

    # Assert GET Quiz retrieval doesn't leak limits natively
    get_res = client.get(f"/api/v1/quizzes/{quiz_set_id}", headers={"Authorization": f"Bearer {student_a_token}"})
    assert get_res.status_code == 200
    question_get = get_res.json()["questions"][0]
    assert "correct_answer" not in question_get
    assert "explanation" not in question_get
    assert "source_chunk_snippet" not in question_get
