describe("Login", () => {
    it("logs in successfully", () => {
        cy.visit("/login");
        cy.get("#username").type("testuser");
        cy.get("#password").type("password");
        cy.get("button[type=submit]").click();
        cy.contains("Dashboard");
    });
});
