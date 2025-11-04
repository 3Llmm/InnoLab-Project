package at.fhtw.ctfbackend.config;

import at.fhtw.ctfbackend.external.ConfluenceClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MockConfluenceConfig {

    @Bean
    @Primary
    public ConfluenceClient mockConfluenceClient() {
        return new ConfluenceClient() {
            @Override
            public String fetchSummaryFromConfluence(String pageId) {
                // Return fake HTML instead of calling the real API
                return "<p>Mock summary for page " + pageId + "</p>";
            }
        };
    }
}
