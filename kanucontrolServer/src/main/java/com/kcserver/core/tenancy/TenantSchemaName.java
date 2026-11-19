package com.kcserver.core.tenancy;

/** Tenant identifiers must never become search-path lists or be silently truncated. */
public final class TenantSchemaName {
    private TenantSchemaName() {}

    public static String requireValid(String schema) {
        if (schema == null || !schema.matches("[a-z][a-z0-9_]{0,62}")) {
            throw new IllegalArgumentException("Ungültiger Tenant-Schema-Name: " + schema);
        }
        return schema;
    }

    public static String quoted(String schema) {
        return "\"" + requireValid(schema) + "\"";
    }
}
