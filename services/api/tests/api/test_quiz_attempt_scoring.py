def test_quiz_attempt_scoring_integration(client):
    client.post("/api/v1/auth/register", json={"email": "score@test.com", "password": "pass", "role": "student"})
    token = client.post("/api/v1/auth/login", data={"username": "score@test.com", "password": "pass"}).json()["access_token"]
    
    mat_res = client.post("/api/v1/materials/", 
                          json={"title": "Score Gen", "source_url": "https://example.com/normal.pdf", "file_format": "PDF"},
                          headers={"Authorization": f"Bearer {token}"})
    material_id = mat_res.json()["id"]

    gen_res = client.post("/api/v1/quizzes/generate", json={"material_id": material_id, "difficulty": "MEDIUM"}, headers={"Authorization": f"Bearer {token}"})
    assert gen_res.status_code == 200
    quiz_set = gen_res.json()
    quiz_set_id = quiz_set["id"]
    question_id = quiz_set["questions"][0]["id"]

    # Submit Attempt with the structurally fixed mock correct_answer = "A"
    attempt_res = client.post(f"/api/v1/quizzes/{quiz_set_id}/attempts", 
                              json={"answers": {question_id: "A"}}, 
                              headers={"Authorization": f"Bearer {token}"})
    
    assert attempt_res.status_code == 200
    attempt_data = attempt_res.json()
    
    # Assert formal compute fractional paths
    assert attempt_data["score"] == 1.0
    assert attempt_data["passed"] is True
    
    # Assert Post-Attempt array exposes the QuizQuestionReview natively
    review = attempt_data["review_data"][0]
    assert review["id"] == question_id
    assert "correct_answer" in review
    assert review["correct_answer"] == "A"
    assert "explanation" in review
    assert "source_chunk_snippet" in review

    # Assert regular GET strictly masks Public scopes seamlessly
    get_res = client.get(f"/api/v1/quizzes/{quiz_set_id}", headers={"Authorization": f"Bearer {token}"})
    assert get_res.status_code == 200
    assert "correct_answer" not in get_res.json()["questions"][0]
