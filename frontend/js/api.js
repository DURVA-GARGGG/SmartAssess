// Smart Examination & Coding Assessment Platform API Client
const API_BASE = window.EXAM_API_BASE || 'http://localhost:8080/api';

function getToken() {
    return localStorage.getItem('token');
}

function getHeaders() {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json'
    };
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
}

function showMsg(id, msg, isError = false) {
    const el = document.getElementById(id);
    if (el) {
        el.textContent = msg;
        el.className = `alert ${isError ? 'error' : 'success'}`;
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('name');
    localStorage.removeItem('active_attempt_id');
    localStorage.removeItem('active_exam_id');
    window.location.href = 'index.html';
}

function checkAuth(expectedRole) {
    const token = getToken();
    const role = localStorage.getItem('role');
    if (!token) {
        window.location.href = 'index.html';
        return false;
    }
    if (expectedRole && role !== expectedRole) {
        alert('Unauthorized access: Requires ' + expectedRole + ' role.');
        window.location.href = 'index.html';
        return false;
    }
    const userDisplay = document.getElementById('userNameDisplay');
    if (userDisplay) {
        userDisplay.textContent = localStorage.getItem('name') || role;
    }
    return true;
}
