import glob
import os

files = glob.glob('c:/Users/azmih/OneDrive/Desktop/Edu/4th Year/Rakiz/services/api/**/*.py', recursive=True)
for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        data = file.read()
    if 'shared_python' in data:
        data = data.replace('shared_python', 'shared_python')
        with open(f, 'w', encoding='utf-8') as file:
            file.write(data)
