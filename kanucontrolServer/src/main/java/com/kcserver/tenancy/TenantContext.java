package com.kcserver.tenancy;

public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT =
            new ThreadLocal<>();

    private TenantContext() {}

    public static void setCurrentTenant(String tenant) {
        if (TenantTraceConfig.TRACE) {
            System.out.println(">>> [CTX] SET TENANT = " + tenant +
                    " | thread=" + Thread.currentThread().getName());
        }
        CURRENT_TENANT.set(tenant);
    }

    public static String getCurrentTenant() {
        String t = CURRENT_TENANT.get();
        if (TenantTraceConfig.TRACE) {
            System.out.println(">>> [CTX] GET TENANT = " + t +
                    " | thread=" + Thread.currentThread().getName());
        }
        return t;
    }

    public static void clear() {
        if (TenantTraceConfig.TRACE) {
            System.out.println(">>> [CTX] CLEAR TENANT | thread=" +
                    Thread.currentThread().getName());
        }
        CURRENT_TENANT.remove();
    }

    // Legacy
    public static void setTenant(String tenant) {
        setCurrentTenant(tenant);
    }

    public static String getTenant() {
        return getCurrentTenant();
    }
}