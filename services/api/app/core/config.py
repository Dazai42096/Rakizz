from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    PROJECT_NAME: str = "Rakizz API"
    VERSION: str = "0.1.0"
    API_V1_STR: str = "/api/v1"
    
    JWT_SECRET: str = "CHANGE_ME_IN_PROD"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 10080 # 7 days
    
    DATABASE_URL: str = "postgresql://rakizz_user:rakizz_password@localhost/rakizz_db"

    class Config:
        case_sensitive = True

settings = Settings()
