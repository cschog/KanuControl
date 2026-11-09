package com.kcserver.unit;

import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.vermietung.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HausmeisterBerichtTest {
    private static com.kcserver.vermietung.config.HausmeisterBerichtFreigabe freigabe() {
        var env = new org.springframework.mock.env.MockEnvironment()
                .withProperty("kc.runtime-environment", "PROD")
                .withProperty("app.environment", "PROD")
                .withProperty("kc.hausmeisterbericht.enabled", "true");
        env.setActiveProfiles("prod");
        return new com.kcserver.vermietung.config.HausmeisterBerichtFreigabe(env);
    }
    @Test void mandantenWerdenGetrenntVerarbeitetUndKontextWiederhergestellt() {
        var jdbc = mock(JdbcTemplate.class);
        var service = mock(HausmeisterBerichtService.class);
        when(jdbc.queryForList(anyString(), eq(String.class))).thenReturn(List.of("ekc_eschweilerkanuclub", "abc_andererverein"));
        when(service.objektIds()).thenAnswer(call -> {
            assertTrue(List.of("ekc_eschweilerkanuclub", "abc_andererverein").contains(TenantContext.getCurrentTenant()));
            return List.of(1L);
        });
        doAnswer(call -> {
            if (TenantContext.getCurrentTenant().equals("ekc_eschweilerkanuclub")) throw new IllegalStateException("SMTP offline");
            return null;
        }).when(service).wocheSenden(eq(1L), any());
        TenantContext.setCurrentTenant("vorher");
        try {
            new HausmeisterBerichtScheduler(jdbc, service, freigabe()).senden();
            verify(service, times(2)).wocheSenden(eq(1L), any());
            assertEquals("vorher", TenantContext.getCurrentTenant());
            verify(jdbc).queryForList(contains("^[a-z]{3,5}_"), eq(String.class));
        } finally { TenantContext.clear(); }
    }
}
