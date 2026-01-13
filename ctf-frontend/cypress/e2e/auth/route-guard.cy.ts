describe("Route Guard", () => {
    it("redirects to login if auth needed", ()=> {
       cy.visit("/challenges");
       cy.location('pathname').should('eq', '/login');
    });

    it("shows challenges after login", () => {
      cy.login();
      cy.visit("/challenges");
      cy.location('pathname').should('eq', '/challenges');
      cy.get('h1').should('contain', 'Challenges');
      cy.get('input[placeholder="Search challenges..."]').should('exist')
    })
});