describe("Logout", () => {
    it("logs out successfully", () => {
        cy.visit("/");
        cy.login();
        cy.contains("button","Logout").click();
        cy.location("pathname").should("eq","/login");
        cy.contains("a","Login").should("exist");
    });

    it("does not redirect if no auth needed", () => {
        cy.visit("/");
        cy.login();
        cy.visit("/");
        cy.contains("button","Logout").click();
        cy.location("pathname").should("eq","/");
        cy.contains("a","Login").should("exist");
    });

    it("cant access pages with auth after logout", () => {
        cy.visit("/");
        cy.login();
        cy.contains("button","Logout").click();
        cy.visit("/challenges");
        cy.location("pathname").should("eq","/login");
        cy.contains("a","Login").should("exist");
    });
});