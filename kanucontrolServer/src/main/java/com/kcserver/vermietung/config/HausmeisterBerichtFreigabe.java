package com.kcserver.vermietung.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Set;

/** Freigabe ausschließlich für den automatischen Versand; manueller Versand ist unabhängig. */
@Slf4j
@Component
public class HausmeisterBerichtFreigabe {
    private final boolean enabled;
    private final boolean production;
    private final String environment;

    public HausmeisterBerichtFreigabe(Environment config) {
        String runtime = config.getProperty("kc.runtime-environment", "UNKNOWN").trim();
        String app = config.getProperty("app.environment", "UNKNOWN").trim();
        Set<String> profiles = Set.copyOf(Arrays.asList(config.getActiveProfiles()));
        environment = Set.of("PROD", "DEV", "TEST").contains(runtime) ? runtime : "UNKNOWN";
        production = "PROD".equals(runtime) && "PROD".equals(app)
                && profiles.contains("prod")
                && profiles.stream().noneMatch(p -> Set.of("dev", "test", "junit").contains(p.toLowerCase(java.util.Locale.ROOT)));
        enabled = production && config.getProperty("kc.hausmeisterbericht.enabled", Boolean.class, false);
    }

    public boolean isProduction() { return production; }

    @PostConstruct
    public void logStatus() {
        log.info("Automatischer Hausmeisterbericht: Servervorgabe={}, Betriebsumgebung={}, PROD-zulässig={} (zusätzlich gilt der gespeicherte Objektschalter)", enabled, environment, production);
    }
    public boolean isEnabled() { return enabled; }
    public String getEnvironment() { return environment; }
}
