describe("Smoke Test", () => {
    it("loads the CTF application", () => {
        cy.visit("/");
        cy.contains("CTF");
    });
});
