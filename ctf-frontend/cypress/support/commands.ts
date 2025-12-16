Cypress.Commands.add("login", () => {
    cy.visit("/login");
    cy.get("#username").type("testuser");
    cy.get("#password").type("password");
    cy.get("button[type=submit]").click();
});
