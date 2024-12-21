package com.kcserver.tenancy;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class TenantAwareDataSource extends AbstractRoutingDataSource {
    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    @Override
    protected Object determineCurrentLookupKey() {
        return getCurrentTenant();
    }

    public static void setCurrentTenant(String tenant) {
        currentTenant.set(tenant);
    }

    public static String getCurrentTenant() {
        return currentTenant.get();
    }

    public static void clear() {
        currentTenant.remove();
    }
}