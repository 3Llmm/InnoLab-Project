describe('Navigation', () => {
    it("navigates to all pages successfully when logged in", () => {
        cy.login();
        cy.get("span").contains("CTF Platform").click();
        cy.get("h1").should("contain","CTF","Platform");
        cy.location("pathname").should("eq","/");
        cy.get("nav").contains("a","Home").click();
        cy.location("pathname").should("eq","/");
        cy.get("h1").should("contain","CTF","Platform");
        cy.get("nav").contains("a","Challenges").click();
        cy.location("pathname").should("eq","/challenges");
        cy.get("h1").should("contain","Challenges");
        cy.get('input[placeholder*="Search"]').should('exist')
        cy.get("nav").contains("a","Courses").click();
        cy.location("pathname").should("eq","/courses");
        cy.get("h1").should("contain","Courses");
        cy.get("nav").contains("a","Scoreboard").click();
        cy.location("pathname").should("eq","/scoreboard");
        cy.get("h1").should("contain","Scoreboard");
        cy.get("nav").contains("a","About").click();
        cy.location("pathname").should("eq","/about");
        cy.get("h1").should("contain","About");
        cy.get("nav").contains("a","Help").click();
        cy.location("pathname").should("eq","/help");
        cy.get("h1").should("contain","Help");
        cy.get('nav').find('a[href="/profile"]').click()
        cy.location("pathname").should("eq","/profile");
    });

    it("redirects to login if auth needed", () => {
        cy.visit("/");
        cy.get("span").contains("CTF Platform").click();
        cy.location("pathname").should("eq","/");
        cy.get("h1").should("contain","CTF","Platform");
        cy.get("nav").contains("a","Home").click();
        cy.location("pathname").should("eq","/");
        cy.get("h1").should("contain","CTF","Platform");
        cy.get("nav").contains("a","Challenges").click();
        cy.location("pathname").should("eq","/login");
        cy.get("nav").contains("a","Courses").click();
        cy.location("pathname").should("eq","/courses");
        cy.get("h1").should("contain","Courses");
        cy.get("nav").contains("a","Scoreboard").click();
        cy.location("pathname").should("eq","/scoreboard");
        cy.get("h1").should("contain","Scoreboard");
        cy.get("nav").contains("a","About").click();
        cy.location("pathname").should("eq","/about");
        cy.get("h1").should("contain","About");
        cy.get("nav").contains("a","Help").click();
        cy.location("pathname").should("eq","/help");
        cy.get("h1").should("contain","Help");
        cy.get('nav').find('a[href="/profile"]').should('not.exist')

    })
});