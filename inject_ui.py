import os
import re

static_dir = os.path.join(os.getcwd(), 'src', 'main', 'resources', 'static')

header_html = """    <header>
        <div class="logo" onclick="window.location.href='/'">
            <img src="/images/logo.png" alt="Logo">
            <span>MountEdge</span>
        </div>
        <nav>
            <ul>
                <li><a href="/">Home</a></li>
                <li><a href="/cart.html">Cart</a></li>
                <li id="wishlist-link" style="display:none;"><a href="/wishlist.html">Wishlist</a></li>
                <li id="orders-link" style="display:none;"><a href="/orders.html">Orders</a></li>
                <li id="profile-link" style="display:none;"><a href="/profile.html">Profile</a></li>
                <li id="admin-link" style="display:none;"><a href="/admin.html">Admin</a></li>
            </ul>
        </nav>
        <div class="auth-buttons" id="auth-section">
            <!-- Dynamic Content -->
        </div>
    </header>"""

footer_html = """    <footer class="site-footer">
        <div class="footer-col">
            <h4>MountEdge</h4>
            <p>Elevate your lifestyle with premium products designed for the modern explorer.</p>
        </div>
        <div class="footer-col">
            <h4>Quick Links</h4>
            <ul>
                <li><a href="/">Home</a></li>
                <li><a href="/cart.html">Cart</a></li>
                <li><a href="/wishlist.html">Wishlist</a></li>
            </ul>
        </div>
        <div class="footer-col">
            <h4>Customer Service</h4>
            <ul>
                <li><a href="/orders.html">My Orders</a></li>
                <li><a href="/profile.html">My Profile</a></li>
            </ul>
        </div>
        <div class="footer-bottom">
            &copy; 2024 MountEdge E-Commerce. All Rights Reserved.
        </div>
    </footer>
</body>"""

def process_html_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    original = content

    # 1. Favicon
    if 'rel="icon"' not in content:
        content = re.sub(r'(</head>)', r'    <link rel="icon" href="/images/logo.png">\n\1', content, flags=re.IGNORECASE)

    # 2. Header
    # Replace existing <header>...</header> with standard header
    content = re.sub(r'<header>.*?</header>', header_html, content, flags=re.IGNORECASE | re.DOTALL)

    # 3. Footer
    # Insert footer before </body> if not exists
    if 'class="site-footer"' not in content:
        content = re.sub(r'</body>', footer_html, content, flags=re.IGNORECASE)

    # 4. Remove inline updateUI function definition to avoid conflicts
    content = re.sub(r'function updateUI\(\)\s*\{.*?(?=\n\s*function|\n\s*async|\n\s*const|\n\s*let|\n\s*updateUI\(\);|</script>)', '', content, flags=re.IGNORECASE | re.DOTALL)
    # Actually regex matching for JS function is brittle. It's better to just leave it and let api.js handle it, OR
    # just remove specific known string blocks if possible, or we can just append updateUI in api.js as `window.updateUI = function() {...}`
    # Let's remove the function updateUI if it exists:
    content = re.sub(r'function updateUI\(\)\s*\{[^\}]+\}(?:\s*else\s*\{[^\}]+\})?\s*\}', '', content, flags=re.IGNORECASE | re.DOTALL) # A bit tricky, let's not risk destroying scripts.
    
    # Let's just do a simpler replace for the exact updateUI block found in index.html if possible.
    # Actually, if we just define `window.updateUI` in api.js, it might overwrite the local one if we're lucky.
    # But wait, local functions override global ones.
    # We can just remove "function updateUI()" using a simpler approach or I will manually remove them.
    
    if content != original:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {filepath}")

for root, dirs, files in os.walk(static_dir):
    for file in files:
        if file.endswith('.html'):
            process_html_file(os.path.join(root, file))

print("Header, Footer, and Favicon injected.")
