package com.kcserver.tenancy;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TenantFilter implements Filter {

    private final TenantResolver tenantResolver;

    public TenantFilter(TenantResolver tenantResolver) {
        this.tenantResolver = tenantResolver;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Resolve tenant ID and set it in a context or thread-local storage
        String tenantId = tenantResolver.resolveTenantFromToken();

        // Optionally, store tenant ID in a thread-local or context
        TenantContext.setTenantId(tenantId);

        // Continue filter chain
        chain.doFilter(request, response);
    }
}