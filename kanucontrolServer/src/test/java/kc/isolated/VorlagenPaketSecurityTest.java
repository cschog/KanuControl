package kc.isolated;
import com.kcserver.vermietung.controller.VorlagenPaketController;
import com.kcserver.vermietung.service.VorlagenPaketService;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockMultipartFile;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class VorlagenPaketSecurityTest {
    @Configuration @EnableMethodSecurity(proxyTargetClass=true) static class Config {
        @Bean VorlagenPaketService service(){return mock(VorlagenPaketService.class);}
        @Bean VorlagenPaketController controller(VorlagenPaketService s){return new VorlagenPaketController(s);}
    }
    @Test void alleEndpunktePruefenDieVerwaltungsrolle()throws Exception{
        try(var ctx=new AnnotationConfigApplicationContext(Config.class)){
            var c=ctx.getBean(VorlagenPaketController.class);var file=new MockMultipartFile("datei","x.zip","application/zip",new byte[]{1});
            for(String role:List.of("KJFP","HOMEPAGE_INTEGRATION","USER")){
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("erika","",List.of(new SimpleGrantedAuthority("ROLE_"+role))));
                assertThrows(AccessDeniedException.class,c::exportieren);assertThrows(AccessDeniedException.class,()->c.hochladen(file));assertThrows(AccessDeniedException.class,()->c.lesen("id"));assertThrows(AccessDeniedException.class,()->c.planen("id",null));assertThrows(AccessDeniedException.class,()->c.vorschau("id"));assertThrows(AccessDeniedException.class,()->c.uebernehmen("id"));assertThrows(AccessDeniedException.class,()->c.abbrechen("id"));
            }
            verifyNoInteractions(ctx.getBean(VorlagenPaketService.class));
            for(String role:List.of("VERMIETUNG","ADMIN")){
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("erika","",List.of(new SimpleGrantedAuthority("ROLE_"+role))));
                assertDoesNotThrow(c::exportieren);assertDoesNotThrow(()->c.hochladen(file));assertDoesNotThrow(()->c.lesen("id"));assertDoesNotThrow(()->c.planen("id",null));assertDoesNotThrow(()->c.vorschau("id"));assertDoesNotThrow(()->c.uebernehmen("id"));assertDoesNotThrow(()->c.abbrechen("id"));
                assertEquals("no-store",c.exportieren().getHeaders().getCacheControl());
            }
        }finally{SecurityContextHolder.clearContext();}
    }
}
