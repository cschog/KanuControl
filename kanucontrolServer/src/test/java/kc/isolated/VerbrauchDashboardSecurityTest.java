package kc.isolated;
import com.kcserver.vermietung.controller.VerbrauchDashboardController;
import com.kcserver.vermietung.service.VerbrauchDashboardService;
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
class VerbrauchDashboardSecurityTest {
 @Configuration @EnableMethodSecurity(proxyTargetClass=true) static class Config {
  @Bean VerbrauchDashboardService service(){return mock(VerbrauchDashboardService.class);}
  @Bean VerbrauchDashboardController controller(VerbrauchDashboardService s){return new VerbrauchDashboardController(s);}
 }
 AnnotationConfigApplicationContext ctx; VerbrauchDashboardController controller;
 @BeforeEach void setup(){ctx=new AnnotationConfigApplicationContext(Config.class);controller=ctx.getBean(VerbrauchDashboardController.class);}
 @AfterEach void close(){SecurityContextHolder.clearContext();ctx.close();}
 void role(String role){SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("test","unused",List.of(new SimpleGrantedAuthority("ROLE_"+role))));}
 @Test void nurAdminUndVermietungDuerfenVerbrauchsdatenLesen(){
  for(String name:List.of("ADMIN","VERMIETUNG")){role(name);assertDoesNotThrow(()->controller.get(7L,2026));}
  for(String name:List.of("KJFP","USER","HOMEPAGE_INTEGRATION")){role(name);assertThrows(AccessDeniedException.class,()->controller.get(7L,2026));}
  verify(ctx.getBean(VerbrauchDashboardService.class),times(2)).get(7L,2026);
 }
}
