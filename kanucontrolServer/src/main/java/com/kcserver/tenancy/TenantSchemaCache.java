package com.kcserver.tenancy;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TenantSchemaCache {

    private final Set<String> initializedTenants =
            ConcurrentHashMap.newKeySet();

    public boolean isInitialized(String tenant) {
        return initializedTenants.contains(tenant);
    }

    public boolean markInitialized(String tenant) {
        return initializedTenants.add(tenant);
        // true = war neu
        // false = war schon drin
    }
}