   // Add to challanges.html
   // Check if token exists before making the request
   const jwtToken = localStorage.getItem('jwtToken');
   console.log('JWT token:', jwtToken);
   if (!jwtToken) {
     document.getElementById('challenges-container').innerHTML = 
       `<p class="error-message">Authentication required. Please log in first.</p>`;
     console.error('No JWT token found in localStorage');
     return;
   }

   console.log('Attempting to fetch challenges with token:', jwtToken);
   
   fetch('http://localhost:8080/api/challenges', {
     headers: {
       'Authorization': `Bearer ${jwtToken}`,
       'Content-Type': 'application/json'
     }
   })
     .then(response => {
       console.log('Response status:', response.status);
       console.log('Response headers:', [...response.headers.entries()]);
       
       if (!response.ok) {
         if (response.status === 401) {
           throw new Error('Unauthorized: Please login again');
         } else if (response.status === 403) {
           throw new Error('Forbidden: You do not have permission to access this resource');
         } else {
           throw new Error(`Failed to fetch challenges: ${response.status}`);
         }
       }
       
       // Check if response has content before parsing JSON
       return response.text().then(text => {
         if (!text) {
           throw new Error('Empty response received from server');
         }
         try {
           return JSON.parse(text);
         } catch (e) {
           console.error('JSON parse error:', e);
           console.error('Raw response:', text);
           throw new Error('Invalid JSON response from server');
         }
       });
     })
     .then(challenges => {
       console.log('Challenges received:', challenges);
       // Render challenges
       const challengesContainer = document.getElementById('challenges-container');
       
       if (!challenges || challenges.length === 0) {
         challengesContainer.innerHTML = '<p>No challenges available at this time.</p>';
         return;
       }
       
       challenges.forEach(challenge => {
         const challengeElement = document.createElement('div');
         challengeElement.className = 'challenge-card';
         
         challengeElement.innerHTML = `
           <h3>${challenge.title}</h3>
           <p>${challenge.description}</p>
           <a href="${challenge.downloadUrl}" class="download-btn">Download Challenge</a>
         `;
         
         challengesContainer.appendChild(challengeElement);
       });
     })
     .catch(error => {
       console.error('Error fetching challenges:', error);
       const errorMessage = error.message || 'Failed to load challenges. Please try again later.';
       document.getElementById('challenges-container').innerHTML = 
         `<p class="error-message">${errorMessage}</p>`;
     });