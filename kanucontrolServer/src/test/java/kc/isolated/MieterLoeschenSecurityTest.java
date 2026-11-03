package kc.isolated;
import com.kcserver.vermietung.controller.MieterController;
import com.kcserver.vermietung.service.MieterService;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class MieterLoeschenSecurityTest {
 @Configuration @EnableMethodSecurity(proxyTargetClass=true) static class Config {
  @Bean MieterService service(){return mock(MieterService.class);}
  @Bean MieterController controller(MieterService s){return new MieterController(s);}
 }
 AnnotationConfigApplicationContext ctx; MieterController controller;
 @BeforeEach void setup(){ctx=new AnnotationConfigApplicationContext(Config.class);controller=ctx.getBean(MieterController.class);}
 @AfterEach void close(){SecurityContextHolder.clearContext();ctx.close();}
 void role(String role){SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("test","unused",List.of(new SimpleGrantedAuthority("ROLE_"+role))));}
 @Test void adminUndVermietungDuerfenLoeschen(){for(String name:List.of("ADMIN","VERMIETUNG")){role(name);assertDoesNotThrow(()->controller.delete(5L));}verify(ctx.getBean(MieterService.class),times(2)).delete(5L);}
 @Test void andereRollenDuerfenWederLoeschenNochArchivieren(){for(String name:List.of("KJFP","USER","HOMEPAGE_INTEGRATION")){role(name);assertThrows(AccessDeniedException.class,()->controller.delete(5L));assertThrows(AccessDeniedException.class,()->controller.deactivate(5L));assertThrows(AccessDeniedException.class,()->controller.deactivateLegacy(5L));}verifyNoInteractions(ctx.getBean(MieterService.class));}
}
