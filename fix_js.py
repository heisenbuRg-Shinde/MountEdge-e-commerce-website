import os

static_dir = os.path.join(os.getcwd(), 'src', 'main', 'resources', 'static')

def fix_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content
    content = content.replace('`₹', '`$')

    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Fixed {filepath}")

for root, dirs, files in os.walk(static_dir):
    for file in files:
        if file.endswith('.html') or file.endswith('.js'):
            fix_file(os.path.join(root, file))

print("Fix complete.")
