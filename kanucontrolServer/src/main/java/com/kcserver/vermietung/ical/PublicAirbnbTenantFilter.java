package com.kcserver.vermietung.ical;
import com.kcserver.core.tenancy.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/** Authentifiziert ausschließlich den geheimen Feed-Schlüssel, bevor JPA/OSIV den Mandanten wählt. */
@Component @Profile("!junit") @RequiredArgsConstructor
public class PublicAirbnbTenantFilter extends OncePerRequestFilter {
    public static final String BINDING = PublicAirbnbTenantFilter.class.getName() + ".binding";
    private final AirbnbExportRegistry registry;
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        return !AirbnbFeedPath.matches(request.getMethod(), request.getRequestURI());
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String token = path.substring(AirbnbFeedPath.PREFIX.length(), path.length() - 4);
        var binding = registry.lookup(token);
        if (binding.isEmpty()) { response.setStatus(404); return; }
        try {
            TenantContext.setCurrentTenant(binding.get().tenant());
            request.setAttribute(BINDING, binding.get());
            chain.doFilter(request,response);
        } finally {
            request.removeAttribute(BINDING);
            TenantContext.clear();
        }
    }
}
