import os
import re

static_dir = os.path.join(os.getcwd(), 'src', 'main', 'resources', 'static')

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content
    # Replace Rs. followed by optional space with ₹
    content = re.sub(r'Rs\.\s*', '₹ ', content)
    # Replace $ inside template literals like `$${total.toFixed(2)}` -> `₹${total.toFixed(2)}`
    content = content.replace('`$', '`₹')

    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {filepath}")

for root, dirs, files in os.walk(static_dir):
    for file in files:
        if file.endswith('.html') or file.endswith('.js'):
            process_file(os.path.join(root, file))

print("Currency replacement complete.")
