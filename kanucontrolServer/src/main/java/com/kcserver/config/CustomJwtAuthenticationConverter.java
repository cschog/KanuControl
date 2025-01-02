package com.kcserver.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        // Extract roles or groups from JWT claims
        List<String> groups = jwt.getClaimAsStringList("groups"); // Replace "groups" with your claim name

        if (groups == null) {
            return List.of();
        }

        return groups.stream()
                .map(SimpleGrantedAuthority::new) // Map groups/roles to GrantedAuthority
                .collect(Collectors.toList());
    }
}
