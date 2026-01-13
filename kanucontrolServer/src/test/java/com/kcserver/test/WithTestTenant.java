package com.kcserver.test;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(TestTenantExtension.class)
public @interface WithTestTenant {

    /**
     * Name des Tenant-Schemas
     */
    String value();
}