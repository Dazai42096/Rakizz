# Domain Model

## Core Entities & Relationships

### `User`
- **id**: UUID
- **email**: String
- **password_hash**: String
- **role**: Enum (`Student`, `Parent`, `Admin`)
- **created_at**: Timestamp

### `ParentStudentRelationship`
- **parent_id**: UUID (FK to User)
- **student_id**: UUID (FK to User)

### `Material`
- **id**: UUID
- **owner_id**: UUID (FK to User - Student)
- **title**: String
- **content_type**: Enum (`Document`, `Text`, `Link`)
- **payload**: String (Text snippet, S3 URI, or web link)
- **created_at**: Timestamp

### `Assignment`
- **id**: UUID
- **assignee_id**: UUID (FK to User - Student)
- **title**: String
- **due_date**: Timestamp
- **status**: Enum (`Pending`, `Completed`)

### `Quiz`
- **id**: UUID
- **material_id**: UUID (FK to Material)
- **questions_json**: JSONB (Stores questions, options, correct answers)

### `QuizAttempt`
- **id**: UUID
- **quiz_id**: UUID (FK to Quiz)
- **student_id**: UUID (FK to User - Student)
- **score_achieved**: Float (e.g., 0.85)

### `AppPolicy`
- **id**: UUID
- **target_student_id**: UUID (FK to User - Student)
- **policy_type**: Enum (`TimeWindow`, `DailyLimit`, `QuizUnlock`)
- **target_package_name**: String (e.g., `com.instagram.android`)
- **config**: JSONB (Specific parameters depending on the policy type)
- **is_active**: Boolean

### `UsageSummary`
- **id**: UUID
- **student_id**: UUID (FK to User)
- **date**: Date
- **package_name**: String
- **duration_ms**: Long (Time spent inside the app on this date)

## Entity Relationship Overview
The `User` (Parent) provisions an `AppPolicy` directed at a `User` (Student). The Student's Android client pulls the active `AppPolicy` objects. The Student leverages `Material` to generate a `Quiz`, submitting a `QuizAttempt` to fulfill a `QuizUnlock` policy dynamically. Over time, the Student's Android client pushes data translating to `UsageSummary` logs, visible back to the Parent.
