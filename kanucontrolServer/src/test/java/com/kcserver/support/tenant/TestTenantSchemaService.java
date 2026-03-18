package com.kcserver.support.tenant;

import com.kcserver.tenancy.TenantSchemaService;
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
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + tenantId);
        jdbcTemplate.execute("SET search_path TO " + tenantId);
    }
}