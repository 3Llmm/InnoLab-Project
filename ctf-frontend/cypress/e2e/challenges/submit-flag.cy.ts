describe("Flag Submission", () => {
    it("accepts correct flag", () => {
        cy.login();
        cy.visit("/challenges/1");
        cy.get("#flag").type("CTF{correct_flag}");
        cy.get("#submit").click();
        cy.contains("Correct");
    });
});
