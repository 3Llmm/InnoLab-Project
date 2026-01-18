package at.fhtw.ctfbackend.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Component
public class ConfluenceClient {

    @Value("${CONFLUENCE_EMAIL}")
    private String confluenceEmail;

    @Value("${CONFLUENCE_API_TOKEN}")
    private String confluenceApiToken;

    private final RestTemplate restTemplate;

    private String url = "https://technikum-wien-team-kjev9g23.atlassian.net/wiki/rest/api/content/";
    private String urlEnd="?expand=body.view";

    public ConfluenceClient() throws KeyManagementException, NoSuchAlgorithmException {
        // Create a trust manager that trusts all certificates
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };

        // Install the all-trusting trust manager
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        // Create a custom RestTemplate with the trusting SSL context
        RestTemplate customRestTemplate = new RestTemplate();
        
        // Configure the custom RestTemplate to use our trusting SSL context
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        
        this.restTemplate = customRestTemplate;
    }


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
            e.printStackTrace();
            return "No summary available.";
        }
    }
}
