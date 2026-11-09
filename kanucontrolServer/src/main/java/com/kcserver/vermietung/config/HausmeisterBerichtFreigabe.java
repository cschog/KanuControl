package com.kcserver.vermietung.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.Arrays;
import java.util.Set;

/** Fail closed: Betriebsumgebung unabhängig vom Spring-Profil setzen. */
@Slf4j
@Component
public class HausmeisterBerichtFreigabe {
    private final boolean enabled;
    private final String environment;

    public HausmeisterBerichtFreigabe(Environment config) {
        String runtime = config.getProperty("kc.runtime-environment", "UNKNOWN").trim();
        String app = config.getProperty("app.environment", "UNKNOWN").trim();
        Set<String> profiles = Set.copyOf(Arrays.asList(config.getActiveProfiles()));
        environment = Set.of("PROD", "DEV", "TEST").contains(runtime) ? runtime : "UNKNOWN";
        enabled = config.getProperty("kc.hausmeisterbericht.enabled", Boolean.class, false)
                && "PROD".equals(runtime) && "PROD".equals(app)
                && profiles.contains("prod")
                && profiles.stream().noneMatch(p -> Set.of("dev", "test", "junit").contains(p.toLowerCase(java.util.Locale.ROOT)));
    }

    @PostConstruct
    public void logStatus() {
        log.info("Hausmeisterbericht: aktiviert={}, Betriebsumgebung={} (Freigabe und Umgebung geprüft)", enabled, environment);
    }
    public boolean isEnabled() { return enabled; }
    public String getEnvironment() { return environment; }
    public void requireEnabled() {
        if (!enabled) throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Der Hausmeisterbericht ist gesperrt. Erzeugung und Versand sind ausschließlich in freigegebenem PROD erlaubt; in DEV und TEST bleiben sie deaktiviert.");
    }
}
