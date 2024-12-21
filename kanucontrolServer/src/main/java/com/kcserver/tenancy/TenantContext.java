package com.kcserver.tenancy;

public class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    // Set the tenant identifier
    public static void setTenantId(String tenantId) {
        currentTenant.set(tenantId);
    }

    // Get the tenant identifier
    public static String getTenantId() {
        return currentTenant.get();
    }

    // Clear the tenant identifier (important to avoid memory leaks)
    public static void clear() {
        currentTenant.remove();
    }
}