import urllib.request
import urllib.error
import json

# Register
reg_data = json.dumps({"name":"Admin", "email":"admin2@mountedge.com", "password":"password"}).encode('utf-8')
req = urllib.request.Request('http://localhost:8080/api/auth/register', data=reg_data, headers={'Content-Type': 'application/json'})
try:
    urllib.request.urlopen(req)
    print("Registered admin")
except Exception as e:
    print("Registration failed:", e)

# Login
login_data = json.dumps({"email":"admin2@mountedge.com", "password":"password"}).encode('utf-8')
req = urllib.request.Request('http://localhost:8080/api/auth/login', data=login_data, headers={'Content-Type': 'application/json'})

try:
    response = urllib.request.urlopen(req)
    token = json.loads(response.read().decode('utf-8'))['token']
    print("Got token")
except Exception as e:
    print("Login failed:", e)
    exit(1)

# Wait, the registered user is ROLE_USER. I need ROLE_ADMIN to add products and get stats.
# Let's hit the db directly to make them admin?
