import glob

files = glob.glob('c:/Users/azmih/OneDrive/Desktop/Edu/4th Year/Rakiz/services/api/app/models/*.py')
for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        data = file.read()
    
    modified = False
    
    # Standard replacement string
    if 'JSONB' in data:
        data = data.replace('from sqlalchemy.dialects.postgresql import UUID, JSONB', 'from sqlalchemy.dialects.postgresql import UUID\nfrom sqlalchemy import JSON')
        data = data.replace('JSONB', 'JSON')
        modified = True
        
    if modified:
        with open(f, 'w', encoding='utf-8') as file:
            file.write(data)
