package com.kcserver.test;

import com.kcserver.tenancy.TenantContext;
import org.junit.jupiter.api.extension.*;

public class TestTenantExtension
        implements BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {

        Class<?> testClass = context.getRequiredTestClass();
        WithTestTenant annotation =
                testClass.getAnnotation(WithTestTenant.class);

        if (annotation == null) {
            throw new IllegalStateException(
                    "@WithTestTenant is required on test class " + testClass.getSimpleName()
            );
        }

        String tenant = annotation.value();
        TenantContext.setTenant(tenant);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        TenantContext.clear();
    }
}