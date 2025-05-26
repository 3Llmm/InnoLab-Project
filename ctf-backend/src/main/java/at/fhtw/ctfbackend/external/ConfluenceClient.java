package at.fhtw.ctfbackend.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class ConfluenceClient {

    @Value("${CONFLUENCE_EMAIL}")
    private String confluenceEmail;

    @Value("${CONFLUENCE_API_TOKEN}")
    private String confluenceApiToken;

    private final RestTemplate restTemplate = new RestTemplate(); //a Spring class that simplifies interaction with RESTful web services. It's essentially a HTTP clien

    private String url = "https://technikum-wien-team-kjev9g23.atlassian.net/wiki/rest/api/content/";
    private String urlEnd="?expand=body.view";


    public String fetchSummaryFromConfluence(String pageId) {
        // This is your base URL and expands to get HTML content
        String FullUrl = url + pageId + urlEnd;

        // Encode auth
        String auth = confluenceEmail + ":" + confluenceApiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + encodedAuth);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ConfluencePageResponse> response = restTemplate.exchange(
                    FullUrl,
                    HttpMethod.GET,
                    entity,
                    ConfluencePageResponse.class
            );
            return response.getBody().getBody().getView().getValue(); // HTML content
        } catch (Exception e) {
            System.err.println("Error fetching summary for page " + pageId + ": " + e.getMessage());
            return "No summary available.";
        }
    }
}
