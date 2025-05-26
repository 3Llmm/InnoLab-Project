// Auth utility functions

// Check if user is logged in
function isAuthenticated() {
  return localStorage.getItem('jwtToken') !== null;
}

// Get JWT token
function getToken() {
  return localStorage.getItem('jwtToken');
}

// Get current username
function getUsername() {
  return localStorage.getItem('username');
}

// Logout function
function logout() {
  localStorage.removeItem('jwtToken');
  localStorage.removeItem('username');
  window.location.href = 'login.html';
}

// Function to add auth headers to fetch requests
function authFetch(url, options = {}) {
  const token = getToken();
  if (!token) {
    return Promise.reject('Not authenticated');
  }

  // Create default headers if not present
  if (!options.headers) {
    options.headers = {};
  }

  // Add Authorization header with JWT token
  options.headers.Authorization = `Bearer ${token}`;
  
  return fetch(url, options);
}

// Update UI based on auth state
function updateAuthUI() {
  const isLoggedIn = isAuthenticated();
  
  // Elements to be toggled based on auth state
  const loginLink = document.querySelector('a[href="../login.html"]');
  const registerLink = document.querySelector('a[href="../register.html"]');
  
  if (loginLink && registerLink) {
    if (isLoggedIn) {
      // User is logged in, change login to logout
      loginLink.textContent = 'Logout';
      loginLink.href = '#';
      loginLink.addEventListener('click', function(e) {
        e.preventDefault();
        logout();
      });
      
      // Hide register link when logged in
      registerLink.parentElement.style.display = 'none';
      
      // Add username display to navbar if not present
      const navbar = document.querySelector('.navbar-nav');
      if (navbar) {
        let usernameItem = document.querySelector('.username-display');
        if (!usernameItem) {
          const username = getUsername();
          usernameItem = document.createElement('li');
          usernameItem.className = 'nav-item username-display';
          usernameItem.innerHTML = `<span class="nav-link text-white py-3">Welcome, ${username}</span>`;
          navbar.insertBefore(usernameItem, loginLink.parentElement);
        }
      }
    } else {
      // User is not logged in, make sure login link is correct
      loginLink.textContent = 'Login';
      
      // Show register link
      if (registerLink.parentElement) {
        registerLink.parentElement.style.display = '';
      }
      
      // Remove username display if present
      const usernameItem = document.querySelector('.username-display');
      if (usernameItem) {
        usernameItem.remove();
      }
    }
  }
}

// Check authentication on page load
document.addEventListener('DOMContentLoaded', function() {
  updateAuthUI();
}); 