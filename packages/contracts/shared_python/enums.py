from enum import Enum

class Role(str, Enum):
    STUDENT = "student"
    PARENT = "parent"
    ADMIN = "admin"

class AssignmentStatus(str, Enum):
    PENDING = "pending"
    COMPLETED = "completed"

class PolicyRuleType(str, Enum):
    TIME_WINDOW = "time_window"
    DAILY_LIMIT = "daily_limit"
    QUIZ_UNLOCK = "quiz_unlock"
