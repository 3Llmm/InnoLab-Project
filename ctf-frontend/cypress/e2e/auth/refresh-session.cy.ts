describe("Refresh Session", () => {
    it("stays logged in after refresh", () => {
        cy.login();
        cy.visit("/challenges");
        cy.reload();
        cy.location("pathname").should("eq","/challenges");
        cy.contains("button","Logout").should("exist");
    })
})