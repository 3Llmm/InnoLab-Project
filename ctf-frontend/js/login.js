document.addEventListener('DOMContentLoaded', function() {
  const loginForm = document.querySelector('form');
  
  if (loginForm) {
    loginForm.addEventListener('submit', function(e) {
      e.preventDefault();
      
      const username = document.getElementById('username').value;
      const password = document.getElementById('password').value;
      
      console.log(username, password);
      // Create credentials object matching LdapCredentials on backend
      const credentials = {
        username: username,
        password: password
      };
      
      fetch('http://localhost:8080/api/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(credentials)
      })
      .then(response => {
        if (!response.ok) {
          throw new Error('Authentication failed');
        }
        return response.json();
      })
      .then(data => {
        // Store JWT token from the server
        localStorage.setItem('jwtToken', data.token);
        localStorage.setItem('username', username);
        
        // Show success message
        alert(data.message || 'Login successful' + data.token);
        
        // Redirect to home page
        window.location.href = 'index.html';
      })
      .catch(error => {
        console.error('Login error:', error);
        alert('Login failed. Please check your credentials and try again.');
      });
    });
  }
});