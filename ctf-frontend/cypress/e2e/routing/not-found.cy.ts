describe("invalid routes", () => {
    it("shows 404 if path doesnt exist", () => {
        cy.visit("/invalid_path", { failOnStatusCode: false });
        cy.contains("404");
        cy.location('pathname').should('eq', '/invalid_path');
    });
});