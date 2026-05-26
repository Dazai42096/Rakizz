from pathlib import Path
from uuid import uuid4
import mimetypes
import os
import shutil

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
        # Project root for services/api
        self.project_root = Path(__file__).resolve().parents[2]

        # Render local filesystem is temporary unless you attach a persistent disk.
        # This keeps uploads in one clear backend folder while the instance is alive.
        configured_upload_dir = os.getenv("RAKIZZ_UPLOAD_DIR", "").strip()

        if configured_upload_dir:
            self.upload_dir = Path(configured_upload_dir).expanduser().resolve()
        else:
            self.upload_dir = self.project_root / "uploads" / "materials"

        self.upload_dir.mkdir(parents=True, exist_ok=True)

    def resolve_url(self, url: str) -> str:
        return url.strip()

    def _validate_extension(self, filename: str) -> str:
        ext = Path(filename).suffix.lower()

        if not filename or not ext or ext not in self.allowed_extensions:
            allowed = ", ".join(sorted(self.allowed_extensions))
            raise ValueError(f"Unsupported file type. Allowed types: {allowed}")

        return ext

    def _safe_filename(self, filename: str) -> str:
        original = Path(filename or "material").name
        ext = self._validate_extension(original)
        return f"{uuid4().hex}{ext}"

    async def save_upload(self, file: UploadFile) -> str:
        filename = file.filename or ""
        stored_name = self._safe_filename(filename)
        destination = (self.upload_dir / stored_name).resolve()

        content = await file.read()
        await file.close()

        if not content:
            raise ValueError("Uploaded file is empty")

        if len(content) > self.max_file_size_bytes:
            raise ValueError("File is too large. Maximum size is 25 MB")

        with destination.open("wb") as output_file:
            output_file.write(content)

        # Store a relative path when possible.
        # This is safer than storing a machine-specific absolute path in PostgreSQL.
        try:
            relative_path = destination.relative_to(self.project_root.resolve())
            return str(relative_path).replace("\\", "/")
        except ValueError:
            return str(destination)

    def get_local_file_path(self, source_url: str) -> Path | None:
        if not source_url:
            return None

        clean_source = source_url.strip()

        # External URLs are not local files.
        if clean_source.startswith("http://") or clean_source.startswith("https://"):
            return None

        source_path = Path(clean_source)

        candidates: list[Path] = []

        # New format: uploads/materials/file.txt
        if not source_path.is_absolute():
            candidates.append((self.project_root / source_path).resolve())
            candidates.append((self.upload_dir / source_path.name).resolve())

        # Old format: absolute path saved in DB
        if source_path.is_absolute():
            candidates.append(source_path.resolve())

        # Fallback: if DB contains an old absolute path from another Render deploy,
        # try to locate the same filename in the current upload directory.
        if source_path.name:
            candidates.append((self.upload_dir / source_path.name).resolve())

        upload_root = self.upload_dir.resolve()

        for candidate in candidates:
            try:
                candidate.relative_to(upload_root)
            except ValueError:
                continue

            if candidate.exists() and candidate.is_file():
                return candidate

        return None

    def guess_media_type(self, file_path: Path) -> str:
        mime_type, _ = mimetypes.guess_type(str(file_path))
        return mime_type or "application/octet-stream"

    def copy_to_downloads_folder(self, source_path: Path, download_name: str) -> Path:
        downloads_dir = self.project_root / "downloads"
        downloads_dir.mkdir(parents=True, exist_ok=True)

        safe_name = Path(download_name).name
        destination = downloads_dir / safe_name

        shutil.copyfile(source_path, destination)
        return destination

    def delete_source(self, source_url: str) -> None:
        candidate = self.get_local_file_path(source_url)

        if candidate and candidate.exists() and candidate.is_file():
            candidate.unlink()


class MockStorage(BaseStorage):
    pass


storage_service = MockStorage()