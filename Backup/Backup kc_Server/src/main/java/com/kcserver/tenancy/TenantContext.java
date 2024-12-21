package com.kcserver.tenancy;

public class TenantContext {

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    // Retrieve the current tenant identifier
    public static String getCurrentTenant() {
        return currentTenant.get();
    }


    // Set the current tenant identifier
    public static void setCurrentTenant(String tenant) {
        currentTenant.set(tenant);
    }

    // Clear the tenant identifier (clean up the ThreadLocal)
    public static void clear() {
        currentTenant.remove();
    }
    /**
     * Sets the current tenant identifier.
     *
     * @param tenantId the tenant identifier to set
     */
    public static void setTenantId(String tenantId) {
        currentTenant.set(tenantId);
    }

}