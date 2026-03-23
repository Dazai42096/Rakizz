import os

path = "c:/Users/azmih/OneDrive/Desktop/Edu/4th Year/Rakiz/services/api/app/api/deps.py"
with open(path, 'r', encoding='utf-8') as f:
    data = f.read()

data = data.replace(
    'user = db.query(User).filter(User.id == token_data).first()',
    'import uuid\n    user = db.query(User).filter(User.id == uuid.UUID(token_data)).first()'
)

with open(path, 'w', encoding='utf-8') as f:
    f.write(data)
