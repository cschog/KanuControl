package kc.isolated;

import com.kcserver.core.config.SecurityConfig;
import com.kcserver.core.tenancy.TenantFilter;
import com.kcserver.kjfp.controller.*;
import com.kcserver.kjfp.controller.abrechnung.*;
import com.kcserver.kjfp.service.planung.PlanungService;
import com.kcserver.kjfp.service.simulation.SimulationFacade;
import com.kcserver.kjfp.service.abrechnung.*;
import com.kcserver.kjfp.service.zahlungsnachweis.ZahlungsnachweisService;
import com.kcserver.kjfp.service.pdf.*;
import com.kcserver.core.service.veranstaltung.VeranstaltungService;
import com.kcserver.core.dto.veranstaltung.VeranstaltungDetailDTO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.cors.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Echte produktive Filterkette und Methodensicherheit; Fachservices sind isoliert. */
class KjfpHttpSecurityTest {
    @Configuration @EnableWebSecurity @EnableWebMvc @Import(SecurityConfig.class)
    static class Config {
        @Bean com.kcserver.vermietung.ical.PublicAirbnbTenantFilter feed(){return new com.kcserver.vermietung.ical.PublicAirbnbTenantFilter(mock(com.kcserver.vermietung.ical.AirbnbExportRegistry.class));}
        @Bean JwtDecoder decoder(){return mock(JwtDecoder.class);}
        // Tenant-Isolation wird separat mit echten Repositorys geprüft.
        @Bean TenantFilter tenantFilter(){return (request,response,chain)->chain.doFilter(request,response);}
        @Bean CorsConfigurationSource corsConfigurationSource(){return request->new CorsConfiguration();}
        @Bean PlanungService planung(){return mock(PlanungService.class);}
        @Bean SimulationFacade simulation(){return mock(SimulationFacade.class);}
        @Bean ZahlungsnachweisService zahlungen(){return mock(ZahlungsnachweisService.class);}
        @Bean DokumentService dokumente(){return mock(DokumentService.class);}
        @Bean AbrechnungBelegService belege(){return mock(AbrechnungBelegService.class);}
        @Bean PDFDokumentService pdf(){return mock(PDFDokumentService.class);}
        @Bean VeranstaltungService events(){return mock(VeranstaltungService.class);}
        @Bean PlanungController planungController(PlanungService s){return new PlanungController(s);}
        @Bean SimulationController simulationController(SimulationFacade s){return new SimulationController(s);}
        @Bean ZahlungsnachweisController zahlungenController(ZahlungsnachweisService s,DokumentService d){return new ZahlungsnachweisController(s,d);}
        @Bean DokumentController dokumentController(DokumentService s){return new DokumentController(s,new DokumentAusrichtung());}
        @Bean AbrechnungBelegController belegController(AbrechnungBelegService s){return new AbrechnungBelegController(s);}
        @Bean ReportController reports(PDFDokumentService p,VeranstaltungService v){return new ReportController(p,mock(DokumentValidationService.class),v,mock(PDFFahrkostenabrechnungService.class),mock(com.kcserver.kjfp.service.reisekosten.ReisekostenabrechnungService.class),mock(PDFFinanzausgleichService.class));}
    }
    AnnotationConfigWebApplicationContext context;MockMvc mvc;
    @BeforeEach void setup(){context=new AnnotationConfigWebApplicationContext();context.setServletContext(new MockServletContext());context.register(Config.class);context.refresh();mvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();}
    @AfterEach void close(){context.close();}
    @ParameterizedTest @ValueSource(strings={"ADMIN","KJFP","USER","VERMIETUNG","FAHRTEN","HOMEPAGE_INTEGRATION"})
    void rollenmatrixFuerLesenSchreibenUndPdfDownload(String role)throws Exception {
        boolean erlaubt=role.equals("ADMIN")||role.equals("KJFP");int status=erlaubt?200:403;
        var auth=jwt().authorities(new SimpleGrantedAuthority("ROLE_"+role));
        mvc.perform(get("/api/veranstaltungen/1/planung").with(auth)).andExpect(status().is(status));
        mvc.perform(get("/api/simulation/1").with(auth)).andExpect(status().is(status));
        mvc.perform(get("/api/veranstaltungen/1/zahlungsnachweise").with(auth)).andExpect(status().is(status));
        mvc.perform(get("/api/belege/1/dokumente").with(auth)).andExpect(status().is(status));
        mvc.perform(post("/api/veranstaltungen/1/planung/einreichen").with(auth)).andExpect(status().is(erlaubt?204:403));
        mvc.perform(put("/api/simulation/1").with(auth).contentType("application/json").content("{}")).andExpect(status().is(erlaubt?204:403));
        mvc.perform(delete("/api/veranstaltungen/1/abrechnung/belege/2").with(auth)).andExpect(status().is(erlaubt?204:403));
        var event=new VeranstaltungDetailDTO();event.setName("Prüfung");event.setBeginnDatum(java.time.LocalDate.of(2027,7,1));
        when(context.getBean(VeranstaltungService.class).getById(1L)).thenReturn(event);
        when(context.getBean(PDFDokumentService.class).generateBelege(1L)).thenReturn("%PDF-test".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
        var result=mvc.perform(get("/api/veranstaltungen/1/belege/pdf/download").with(auth)).andExpect(status().is(status));
        if(erlaubt)result.andExpect(content().contentType("application/pdf")).andExpect(header().string("Content-Disposition",org.hamcrest.Matchers.startsWith("attachment;"))).andExpect(content().bytes("%PDF-test".getBytes(java.nio.charset.StandardCharsets.US_ASCII)));
        else {verifyNoInteractions(context.getBean(PlanungService.class),context.getBean(SimulationFacade.class),context.getBean(ZahlungsnachweisService.class),context.getBean(DokumentService.class),context.getBean(AbrechnungBelegService.class));verify(context.getBean(PDFDokumentService.class),never()).generateBelege(anyLong());}
    }
    @Test void unangemeldetSindAuchDownloadsGesperrt()throws Exception {
        for(String path:new String[]{"/api/veranstaltungen/1/planung","/api/belege/1/dokumente","/api/veranstaltungen/1/belege/pdf/download"})mvc.perform(get(path)).andExpect(status().isUnauthorized());
    }
}
