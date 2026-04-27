from pathlib import Path
from uuid import uuid4
import mimetypes

from fastapi import UploadFile


class BaseStorage:
    allowed_extensions = {
        ".pdf",
        ".ppt",
        ".pptx",
        ".doc",
        ".docx",
        ".txt",
        ".jpg",
        ".jpeg",
        ".png",
    }
    max_file_size_bytes = 25 * 1024 * 1024  # 25 MB

    def __init__(self) -> None:
        self.upload_dir = Path(__file__).resolve().parents[2] / "uploads" / "materials"
        self.upload_dir.mkdir(parents=True, exist_ok=True)

    def resolve_url(self, url: str) -> str:
        return url.strip()

    def _validate_extension(self, filename: str) -> str:
        ext = Path(filename).suffix.lower()
        if not filename or not ext or ext not in self.allowed_extensions:
            allowed = ", ".join(sorted(self.allowed_extensions))
            raise ValueError(f"Unsupported file type. Allowed types: {allowed}")
        return ext

    async def save_upload(self, file: UploadFile) -> str:
        filename = file.filename or ""
        ext = self._validate_extension(filename)

        content = await file.read()
        await file.close()

        if not content:
            raise ValueError("Uploaded file is empty")

        if len(content) > self.max_file_size_bytes:
            raise ValueError("File is too large. Maximum size is 25 MB")

        stored_name = f"{uuid4().hex}{ext}"
        destination = self.upload_dir / stored_name

        with destination.open("wb") as output_file:
            output_file.write(content)

        return str(destination.resolve())

    def get_local_file_path(self, source_url: str) -> Path | None:
        if not source_url:
            return None

        candidate = Path(source_url)
        if not candidate.is_absolute():
            candidate = candidate.resolve()
        else:
            candidate = candidate.resolve()

        try:
            candidate.relative_to(self.upload_dir.resolve())
        except ValueError:
            return None

        return candidate

    def guess_media_type(self, file_path: Path) -> str:
        mime_type, _ = mimetypes.guess_type(str(file_path))
        return mime_type or "application/octet-stream"

    def delete_source(self, source_url: str) -> None:
        candidate = self.get_local_file_path(source_url)
        if candidate and candidate.exists() and candidate.is_file():
            candidate.unlink()


class MockStorage(BaseStorage):
    pass


storage_service = MockStorage()