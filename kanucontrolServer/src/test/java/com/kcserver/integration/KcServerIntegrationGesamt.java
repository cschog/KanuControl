package com.kcserver.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class KcServerIntegrationGesamt {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testAuthenticationAndDatabase() {
        // Obtain a token from Keycloak
        RestTemplate keycloakRestTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String requestBody = "client_id=kc_server" +
                "&client_secret=OkEJPqHgEPuCz3rSpCsw3R3xHx00aV1W" +
                "&username=cschog" +
                "&password=CHsab581" +
                "&grant_type=password";

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<KeycloakTokenResponse> response = keycloakRestTemplate.postForEntity(
                "http://localhost:9080/auth/realms/KanuControl/protocol/openid-connect/token",
                request,
                KeycloakTokenResponse.class
        );

        // Validate the token response
        assert response.getStatusCode() == HttpStatus.OK;
        String accessToken = response.getBody().getAccessToken();

        // Use the token to access a secured endpoint in your Spring Boot app
        headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> securedResponse = restTemplate.exchange(
                "/api/secured-endpoint",
                HttpMethod.GET,
                entity,
                String.class
        );

        // Validate the response
        assert securedResponse.getStatusCode() == HttpStatus.OK;

        // Validate database schema creation using Liquibase
        // (You can query the database directly to check the schema)
    }

    // Helper class to deserialize Keycloak token response
    public static class KeycloakTokenResponse {
        private String access_token;

        public String getAccessToken() {
            return access_token;
        }

        public void setAccessToken(String access_token) {
            this.access_token = access_token;
        }
    }
}