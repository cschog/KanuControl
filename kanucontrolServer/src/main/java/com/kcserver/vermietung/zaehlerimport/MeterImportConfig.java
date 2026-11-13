package com.kcserver.vermietung.zaehlerimport;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Getter;
import lombok.Setter;

/** Disabled until an operator supplies a scoped binding and SHA-256 key hash. */
@Component
@ConfigurationProperties(prefix = "kc.meter-import")
@Getter @Setter
public class MeterImportConfig {
    private boolean enabled;
    private String keyHash = "";
    private String tenant = "";
    private Long meterId;
    private String meterNumber = "";
    private String zone = "Europe/Berlin";

    public boolean configured() {
        return enabled && keyHash.matches("[a-f0-9]{64}")
            && tenant.matches("[a-z][a-z0-9_]{0,62}")
            && meterId != null && meterId > 0 && !meterNumber.isBlank();
    }
}
