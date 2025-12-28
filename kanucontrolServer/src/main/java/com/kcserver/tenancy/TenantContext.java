package com.kcserver.tenancy;

public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT =
            new ThreadLocal<>();

    private TenantContext() {
        // utility class
    }

    /* =========================================================
       Neue, saubere API
       ========================================================= */

    public static void setCurrentTenant(String tenant) {
        CURRENT_TENANT.set(tenant);
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }

    /* =========================================================
       🔁 Legacy-Kompatibilität (wichtig!)
       ========================================================= */

    public static void setTenant(String tenant) {
        setCurrentTenant(tenant);
    }

    public static String getTenant() {
        return getCurrentTenant();
    }
}