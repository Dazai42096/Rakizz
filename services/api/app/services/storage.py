class BaseStorage:
    def resolve_url(self, url: str) -> str:
        return url

class MockStorage(BaseStorage):
    def resolve_url(self, url: str) -> str:
        return url

storage_service = MockStorage()
