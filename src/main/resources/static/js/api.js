const BASE_URL = '/api';

const api = {
    async get(endpoint) {
        const token = localStorage.getItem('token');
        const response = await fetch(`${BASE_URL}${endpoint}`, {
            headers: {
                'Authorization': token ? `Bearer ${token}` : ''
            }
        });
        if (!response.ok) throw await response.json();
        return response.json();
    },

    async post(endpoint, data) {
        const token = localStorage.getItem('token');
        const response = await fetch(`${BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token ? `Bearer ${token}` : ''
            },
            body: JSON.stringify(data)
        });
        if (!response.ok) throw await response.json();
        return response.json();
    },

    async delete(endpoint) {
        const token = localStorage.getItem('token');
        const response = await fetch(`${BASE_URL}${endpoint}`, {
            method: 'DELETE',
            headers: {
                'Authorization': token ? `Bearer ${token}` : ''
            }
        });
        if (!response.ok) throw await response.json();
        return response.json();
    }
};

const auth = {
    login: async (email, password) => {
        const data = await api.post('/auth/login', { email, password });
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify(data));
        return data;
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
