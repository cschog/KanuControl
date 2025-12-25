package com.kcserver.config;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.util.Map;
import java.util.HashMap;

@Configuration
public class MultiTenantConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MultiTenantConnectionProvider multiTenantConnectionProvider;

    @Autowired
    private CurrentTenantIdentifierResolver tenantIdentifierResolver;

    // No direct usage of org.hibernate.SessionFactory
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.kcserver");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        // Force Spring to use the Jakarta EntityManagerFactory
        em.setEntityManagerFactoryInterface(EntityManagerFactory.class);

        Map<String, Object> properties = new HashMap<>();
        properties.put(AvailableSettings.MULTI_TENANT_CONNECTION_PROVIDER, multiTenantConnectionProvider);
        properties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, tenantIdentifierResolver);
        properties.put(AvailableSettings.SHOW_SQL, true);
        properties.put(AvailableSettings.FORMAT_SQL, true);

        em.setJpaPropertyMap(properties);
        return em;
    }
}