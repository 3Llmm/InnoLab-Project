package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.models.LdapCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LdapConnectionTestController {
    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private LdapContextSource contextSource;



    @PostMapping("/test-ldap-connection")
    public String testLdapConnection(@RequestBody LdapCredentials credentials) {
        try {
            // Construct proper DN from username
            String userDn = "uid=" + credentials.getUsername() + ",ou=users," + contextSource.getBaseLdapPathAsString();

            // Test if we can get a context with the provided user credentials
            contextSource.getContext(userDn, credentials.getPassword());

            // Try to perform a simple searchgit
            boolean hasEntries = !ldapTemplate.search("", "(objectClass=*)", (AttributesMapper<Boolean>) (attrs) -> true).isEmpty();

            return "LDAP Connection Successful! Directory has entries: " + hasEntries;
        } catch (Exception e) {
            e.printStackTrace();
            return "LDAP Connection Failed: " + e.getMessage() + " (Type: " + e.getClass().getName() + ")";
        }
    }


}

