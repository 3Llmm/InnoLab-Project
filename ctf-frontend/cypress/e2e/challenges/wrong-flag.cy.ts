describe("Wrong Flag Submission", () =>{
    it("does not accept incorrect flag", () =>{
        cy.login();
        cy.visit("/challenges/web-101");
        cy.get("#flag").type("wrong_flag");
        cy.get("button[type=\"submit\"]").click();
        cy.contains("Incorrect");
    })
});