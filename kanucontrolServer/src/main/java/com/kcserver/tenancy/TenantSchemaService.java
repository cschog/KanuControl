package com.kcserver.tenancy;

public interface TenantSchemaService {
    void ensureSchemaExists(String tenant);
}