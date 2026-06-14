const API = 'http://localhost:3000/api';

function showLogin() {
    document.getElementById('loginForm').style.display = 'block';
    document.getElementById('registerForm').style.display = 'none';
}

function showRegister() {
    document.getElementById('loginForm').style.display = 'none';
    document.getElementById('registerForm').style.display = 'block';
}

async function register() {
    const data = {
        fullname: document.getElementById('regFullname').value,
        username: document.getElementById('regUsername').value,
        email: document.getElementById('regEmail').value,
        password: document.getElementById('regPassword').value
    };
    const res = await fetch(`${API}/register`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    });
    const result = await res.json();
    if (res.ok) {
        alert('Registration successful! Please login.');
        showLogin();
    } else {
        document.querySelector('#registerForm .error').textContent = result.error;
    }
}

async function login() {
    const res = await fetch(`${API}/login`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            username: document.getElementById('loginUsername').value,
            password: document.getElementById('loginPassword').value
        }),
        credentials: 'include'
    });
    const result = await res.json();
    if (res.ok) {
        localStorage.setItem('user', JSON.stringify(result.user));
        loadApp();
    } else {
        document.querySelector('#loginForm .error').textContent = result.error;
    }
}

async function loadApp() {
    document.getElementById('auth').style.display = 'none';
    document.getElementById('app').style.display = 'block';
    const user = JSON.parse(localStorage.getItem('user'));
    document.getElementById('userName').innerHTML = `Welcome, ${user.fullname}`;
    await loadPosts();
}

async function loadPosts() {
    const res = await fetch(`${API}/posts`, {credentials: 'include'});
    const data = await res.json();
    const postsDiv = document.getElementById('posts');
    if (data.posts.length === 0) {
        postsDiv.innerHTML = '<p>No posts yet. Be the first!</p>';
        return;
    }
    postsDiv.innerHTML = data.posts.map(post => `
        <div class="post">
            <img src="${post.image_url}" alt="Post">
            <div class="post-info">
                <strong>${post.username}</strong> (${post.fullname})<br>
                ${post.caption || ''}<br>
                <small>${new Date(post.created_at).toLocaleString()}</small>
            </div>
        </div>
    `).join('');
}

async function createPost() {
    const image_url = document.getElementById('imageUrl').value;
    if (!image_url) return alert('Please enter image URL');
    
    await fetch(`${API}/posts`, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            image_url: image_url,
            caption: document.getElementById('caption').value
        }),
        credentials: 'include'
    });
    alert('Post shared!');
    document.getElementById('imageUrl').value = '';
    document.getElementById('caption').value = '';
    await loadPosts();
}

async function logout() {
    await fetch(`${API}/logout`, {method: 'POST', credentials: 'include'});
    localStorage.clear();
    location.reload();
}

fetch(`${API}/me`, {credentials: 'include'}).then(res => res.json()).then(data => {
    if (data.user) loadApp();
});
