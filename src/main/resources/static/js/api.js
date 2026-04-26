const BASE_URL = '/api';

const utils = {
    showToast(message, type = 'success') {
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        document.body.appendChild(toast);
        setTimeout(() => {
            toast.classList.add('fade-out');
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    },
    showError(err) {
        let msg = 'An error occurred';
        if (typeof err === 'string') msg = err;
        else if (err.error) msg = err.error;
        else if (err.message) msg = err.message;
        
        // Handle validation errors (object mapping)
        if (typeof err === 'object' && !err.error && !err.message) {
            msg = Object.values(err).join(', ');
        }
        
        this.showToast(msg, 'error');
    }
};

const api = {
    async get(endpoint) {
        const token = localStorage.getItem('token');
        const response = await fetch(`${BASE_URL}${endpoint}`, {
            headers: {
                'Authorization': token ? `Bearer ${token}` : ''
            }
        });
        if (!response.ok) {
            const err = await response.json().catch(() => ({error: 'Network response was not ok'}));
            throw err;
        }
        // Handle empty or non-JSON responses
        const text = await response.text();
        try {
            return text ? JSON.parse(text) : {};
        } catch (e) {
            return { message: text };
        }
    },

    async post(endpoint, data) {
        const token = localStorage.getItem('token');
        const isFormData = data instanceof FormData;
        
        const headers = {
            'Authorization': token ? `Bearer ${token}` : ''
        };
        if (!isFormData) {
            headers['Content-Type'] = 'application/json';
        }

        const response = await fetch(`${BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: headers,
            body: isFormData ? data : JSON.stringify(data)
        });
        if (!response.ok) {
            const err = await response.json().catch(() => ({error: 'Network response was not ok'}));
            throw err;
        }
        const text = await response.text();
        try {
            return text ? JSON.parse(text) : {};
        } catch (e) {
            return { message: text };
        }
    },

    async put(endpoint, data) {
        const token = localStorage.getItem('token');
        const response = await fetch(`${BASE_URL}${endpoint}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : ''
            },
            body: JSON.stringify(data)
        });
        if (!response.ok) {
            const err = await response.json().catch(() => ({error: 'Network response was not ok'}));
            throw err;
        }
        const text = await response.text();
        try {
            return text ? JSON.parse(text) : {};
        } catch (e) {
            return { message: text };
        }
    },

    async delete(endpoint) {
        const token = localStorage.getItem('token');
        const response = await fetch(`${BASE_URL}${endpoint}`, {
            method: 'DELETE',
            headers: {
                'Authorization': token ? `Bearer ${token}` : ''
            }
        });
        if (!response.ok) {
            const err = await response.json().catch(() => ({error: 'Network response was not ok'}));
            throw err;
        }
        const text = await response.text();
        try {
            return text ? JSON.parse(text) : {};
        } catch (e) {
            return { message: text };
        }
    },

    async patch(endpoint, data) {
        const token = localStorage.getItem('token');
        const response = await fetch(`${BASE_URL}${endpoint}`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : ''
            },
            body: JSON.stringify(data)
        });
        if (!response.ok) {
            const err = await response.json().catch(() => ({error: 'Network response was not ok'}));
            throw err;
        }
        const text = await response.text();
        try {
            return text ? JSON.parse(text) : {};
        } catch (e) {
            return { message: text };
        }
    }
};

const auth = {
    login: async (email, password) => {
        try {
            const data = await api.post('/auth/login', { email, password });
            localStorage.setItem('token', data.token);
            localStorage.setItem('user', JSON.stringify(data));
            return data;
        } catch (e) {
            throw e;
        }
    },
    register: async (name, email, password) => {
        return api.post('/auth/register', { name, email, password });
    },
    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login.html';
    },
    isAuthenticated: () => !!localStorage.getItem('token'),
    getUser: () => JSON.parse(localStorage.getItem('user'))
};

function updateUI() {
    const user = auth.getUser();
    const authSection = document.getElementById('auth-section');
    if (!authSection) return;
    
    if (user) {
        authSection.innerHTML = `
            <span style="color: var(--text-main); font-weight: 500; margin-right: 1rem;">Hi, ${user.name}</span>
            <button class="btn btn-outline" onclick="auth.logout()" style="padding: 0.4rem 1.2rem; font-size: 0.9rem;">Logout</button>
        `;
        const wl = document.getElementById('wishlist-link');
        const ol = document.getElementById('orders-link');
        const bl = document.getElementById('bulk-link');
        const pl = document.getElementById('profile-link');
        const al = document.getElementById('admin-link');
        
        if (wl) wl.style.display = 'inline-block';
        if (ol) ol.style.display = 'inline-block';
        if (bl) bl.style.display = 'inline-block';
        if (pl) pl.style.display = 'inline-block';
        
        if (user.role === 'ROLE_ADMIN' && al) {
            al.style.display = 'inline-block';
        }
    } else {
        authSection.innerHTML = `
            <a href="/login.html" class="btn btn-outline" style="padding: 0.4rem 1.2rem; font-size: 0.9rem;">Login</a>
            <a href="/register.html" class="btn btn-primary" style="padding: 0.4rem 1.2rem; font-size: 0.9rem;">Register</a>
        `;
    }
}

document.addEventListener('DOMContentLoaded', updateUI);
