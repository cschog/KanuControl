package com.kcserver.core.audit;

/** Das Sitzungs-Audit und die Online-Anzeige erfassen persönliche Benutzerkonten. */
public final class AuditAccountPolicy {
    private static final String SERVICE_ACCOUNT_PREFIX = "service-account-";
    private AuditAccountPolicy() {}

    public static boolean isPersonalAccount(String username) {
        return username != null && !username.isBlank()
                && !username.regionMatches(true, 0, SERVICE_ACCOUNT_PREFIX, 0, SERVICE_ACCOUNT_PREFIX.length());
    }
}
