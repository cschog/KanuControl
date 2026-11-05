package com.kcserver.fahrten;

import com.kcserver.core.config.JpaMultiTenantConfig;
import com.kcserver.fahrten.entity.*;
import com.kcserver.core.entity.Teilnehmer;
import com.kcserver.core.entity.Veranstaltung;
import org.hibernate.SessionFactory;
import jakarta.persistence.spi.PersistenceProvider;
import jakarta.persistence.spi.PersistenceUnitInfo;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import javax.sql.DataSource;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FahrtenEntityScanTest {
    @Test void produktiverJpaScanFindetFahrtenUndGemeinsamGenutzteEntities() {
        var factory = new JpaMultiTenantConfig().entityManagerFactory(
                mock(DataSource.class), mock(MultiTenantConnectionProvider.class),
                mock(CurrentTenantIdentifierResolver.class));
        var provider = mock(PersistenceProvider.class);
        var entityManagerFactory = mock(SessionFactory.class);
        when(provider.createContainerEntityManagerFactory(any(), any())).thenReturn(entityManagerFactory);
        factory.setPersistenceProvider(provider);
        factory.afterPropertiesSet();
        try {
            var info = ArgumentCaptor.forClass(PersistenceUnitInfo.class);
            verify(provider).createContainerEntityManagerFactory(info.capture(), any());
            var managed = info.getValue().getManagedClassNames();
            for (Class<?> entity : List.of(Fahrt.class, FahrtBesatzung.class, FahrtKanu.class,
                    Kanu.class, KanuLizenz.class, Gewaesser.class, Veranstaltung.class, Teilnehmer.class)) {
                assertTrue(managed.contains(entity.getName()), entity.getName());
            }
            assertFalse(managed.contains("com.kcserver.kjfp.entity.Fahrt"));
        } finally {
            factory.destroy();
        }
    }
}
