package com.kcserver.integration.support;

import com.kcserver.tenancy.TenantSchemaProvisioner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTenantSmokeTest {

    @Autowired
    TenantSchemaProvisioner provisioner;

    protected final String TENANT = "ekc_test";

    @BeforeAll
    void setupTenant() {
        provisioner.createFromBaseline(TENANT);
    }
}
