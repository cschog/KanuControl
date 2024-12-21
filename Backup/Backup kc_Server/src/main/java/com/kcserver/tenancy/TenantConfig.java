package com.kcserver.tenancy;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class TenantConfig {
    private String tenantId;
    private String databaseUrl;
    private String schemaName;

    // Constructors
    public TenantConfig(String tenantId, String databaseUrl, String schemaName) {
        this.tenantId = tenantId;
        this.databaseUrl = databaseUrl;
        this.schemaName = schemaName;
    }

    // Getters and Setters
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
}