describe("Login", () => {
    it("logs in successfully", () => {
        cy.visit("/login");
        cy.get("#username").type("testuser");
        cy.get("#password").type("password");
        cy.get("button[type=submit]").click();
        cy.location('pathname').should('eq', '/challenges');
    });

    it("does not accept invalid psw", () => {
        cy.visit("/login");
        cy.get("#username").type("testuser");
        cy.get("#password").type("wrong_psw");
        cy.get("button[type=submit]").click();
        cy.contains("failed");
    });
});
