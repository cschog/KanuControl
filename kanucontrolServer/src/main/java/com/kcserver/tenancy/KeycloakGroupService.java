package com.kcserver.tenancy;

import org.springframework.stereotype.Component;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.OAuth2Constants;

import java.util.List;


@Component
public class KeycloakGroupService {
    private final Keycloak keycloak;

    public KeycloakGroupService() {
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl("http://localhost:9080/auth")
                .realm("KanuControl")
                .clientId("kc_server")
                .clientSecret("OkEJPqHgEPuCz3rSpCsw3R3xHx00aV1W")
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .build();
    }

    public List<String> fetchGroups() {
        return keycloak.realm("your-realm").groups().groups().stream()
                .map(GroupRepresentation::getName)
                .toList();
    }
}
