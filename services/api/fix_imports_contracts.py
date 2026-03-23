import glob

files = glob.glob('c:/Users/azmih/OneDrive/Desktop/Edu/4th Year/Rakiz/packages/contracts/shared_python/**/*.py', recursive=True)
for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        data = file.read()
    if 'packages.contracts.shared_python' in data:
        data = data.replace('packages.contracts.shared_python', 'shared_python')
        with open(f, 'w', encoding='utf-8') as file:
            file.write(data)
