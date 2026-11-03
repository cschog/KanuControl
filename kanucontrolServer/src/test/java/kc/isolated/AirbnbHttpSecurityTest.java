package kc.isolated;
import com.kcserver.core.config.SecurityConfig;
import com.kcserver.core.tenancy.*;
import com.kcserver.vermietung.controller.PublicAirbnbController;
import com.kcserver.vermietung.ical.*;
import com.kcserver.vermietung.service.AirbnbExportService;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.*;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.cors.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class AirbnbHttpSecurityTest {
    static final String TOKEN="a".repeat(43),PATH="/api/kalender/airbnb/"+TOKEN+".ics";
    @RestController static class PrivateProbe {@GetMapping({"/api/probe","/api/kalender/airbnb/admin"}) String get(){return "private";}}
    @Configuration @EnableWebSecurity @EnableWebMvc @Import(SecurityConfig.class) static class Config {
        @Bean AirbnbExportRegistry registry(){return mock(AirbnbExportRegistry.class);}
        @Bean TenantSchemaService schemas(){return mock(TenantSchemaService.class);}
        @Bean AirbnbExportService service(){return mock(AirbnbExportService.class);}
        @Bean PublicAirbnbController controller(AirbnbExportService s){return new PublicAirbnbController(s);}
        @Bean com.kcserver.vermietung.controller.AirbnbExportController adminController(AirbnbExportService s){return new com.kcserver.vermietung.controller.AirbnbExportController(s);}
        @Bean PrivateProbe probe(){return new PrivateProbe();}
        @Bean PublicAirbnbTenantFilter publicFeedFilter(AirbnbExportRegistry r,TenantSchemaService s){return new PublicAirbnbTenantFilter(r);}
        @Bean TenantFilter tenantFilter(TenantSchemaService s){return new JwtTenantFilter(s);}
        @Bean JwtDecoder decoder(){return mock(JwtDecoder.class);}
        @Bean CorsConfigurationSource corsConfigurationSource(){return request->new CorsConfiguration();}
    }
    AnnotationConfigWebApplicationContext ctx;MockMvc mvc;AirbnbExportRegistry registry;TenantSchemaService schemas;
    @BeforeEach void setup(){ctx=new AnnotationConfigWebApplicationContext();ctx.setServletContext(new MockServletContext());ctx.register(Config.class);ctx.refresh();mvc=MockMvcBuilders.webAppContextSetup(ctx).apply(springSecurity()).build();registry=ctx.getBean(AirbnbExportRegistry.class);schemas=ctx.getBean(TenantSchemaService.class);when(registry.lookup(TOKEN)).thenReturn(Optional.of(new AirbnbExportRegistry.Config("club_a",7,true,Set.of(10L,11L),1,true)));when(ctx.getBean(AirbnbExportService.class).render(any())).thenAnswer(inv->{assertEquals("club_a",TenantContext.getCurrentTenant());return "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nEND:VCALENDAR\r\n";});}
    @AfterEach void close(){TenantContext.clear();ctx.close();}
    @Test void gueltigerGeheimlinkBrauchtKeinJwtUndWaehltGenauSeinenMandanten() throws Exception{mvc.perform(get(PATH)).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith("text/calendar")).andExpect(header().string("Cache-Control","no-store")).andExpect(content().string(org.hamcrest.Matchers.containsString("BEGIN:VCALENDAR")));verifyNoInteractions(schemas);assertNull(TenantContext.getCurrentTenant());}
    @Test void headIstErlaubt() throws Exception{mvc.perform(head(PATH)).andExpect(status().isOk());assertNull(TenantContext.getCurrentTenant());}
    @Test void unbekannterTokenErzeugtKeinSchemaUndKeineDatenabfrage() throws Exception{mvc.perform(get("/api/kalender/airbnb/"+"b".repeat(43)+".ics")).andExpect(status().isNotFound());verifyNoInteractions(schemas,ctx.getBean(AirbnbExportService.class));}
    @Test void andereRoutenUndMethodenBleibenGeschuetzt() throws Exception{mvc.perform(get("/api/probe")).andExpect(status().isUnauthorized());mvc.perform(get("/api/kalender/airbnb/admin")).andExpect(status().isUnauthorized());mvc.perform(post(PATH)).andExpect(status().isUnauthorized());mvc.perform(get("/api/vermietung/objekte/7/airbnb-export")).andExpect(status().isUnauthorized());}
    @Test void kontextWirdAuchBeiFehlernGeloescht(){doThrow(new IllegalStateException("Test")).when(ctx.getBean(AirbnbExportService.class)).render(any());assertThrows(Exception.class,()->mvc.perform(get(PATH)));assertNull(TenantContext.getCurrentTenant());}

    @Test void nurAdminUndVermietungDuerfenExportVerwalten() throws Exception {
        when(ctx.getBean(AirbnbExportService.class).get(7L)).thenReturn(new com.kcserver.vermietung.dto.AirbnbExportDTO(false,Set.of(10L,11L),0L,false,null));
        for(String role:List.of("ADMIN","VERMIETUNG")) {
            var auth=org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt()
                    .jwt(jwt->jwt.claim("tenant","club_a"))
                    .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_"+role));
            mvc.perform(get("/api/vermietung/objekte/7/airbnb-export").with(auth)).andExpect(status().isOk());
        }
        var auth=org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt->jwt.claim("tenant","club_a"))
                .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));
        mvc.perform(get("/api/vermietung/objekte/7/airbnb-export").with(auth)).andExpect(status().isForbidden());
    }
    @Test void queryParameterKoennenDenMandantenNichtAendern() throws Exception {
        mvc.perform(get(PATH+"?tenant=club_b")).andExpect(status().isOk());assertNull(TenantContext.getCurrentTenant());
    }
}
