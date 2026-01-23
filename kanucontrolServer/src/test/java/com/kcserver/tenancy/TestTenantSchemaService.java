package com.kcserver.tenancy;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class TestTenantSchemaService implements TenantSchemaService {

    private final JdbcTemplate jdbcTemplate;

    public TestTenantSchemaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void initializeTenant(String tenantId) {
        // 🔥 DAS ist entscheidend
        jdbcTemplate.execute("SET search_path TO " + tenantId);
    }
}