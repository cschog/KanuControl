package kc.isolated;
import com.kcserver.vermietung.controller.FinanzDashboardController;
import com.kcserver.vermietung.service.FinanzDashboardService;
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
class FinanzDashboardSecurityTest {
 @Configuration @EnableMethodSecurity(proxyTargetClass=true) static class Config {
  @Bean FinanzDashboardService service(){return mock(FinanzDashboardService.class);}
  @Bean FinanzDashboardController controller(FinanzDashboardService s){return new FinanzDashboardController(s);}
 }
 AnnotationConfigApplicationContext ctx; FinanzDashboardController controller;
 @BeforeEach void setup(){ctx=new AnnotationConfigApplicationContext(Config.class);controller=ctx.getBean(FinanzDashboardController.class);}
 @AfterEach void close(){SecurityContextHolder.clearContext();ctx.close();}
 void role(String role){SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("test","unused",List.of(new SimpleGrantedAuthority("ROLE_"+role))));}
 @Test void nurAdminUndVermietungDuerfenLesenUndGrenzeAendern(){
  for(String name:List.of("ADMIN","VERMIETUNG")){role(name);assertDoesNotThrow(()->controller.get(2026));assertDoesNotThrow(()->controller.save(2026,new com.kcserver.vermietung.dto.FinanzDashboardEinstellungDTO(new java.math.BigDecimal("25000"))));}
  for(String name:List.of("KJFP","USER","HOMEPAGE_INTEGRATION")){role(name);assertThrows(AccessDeniedException.class,()->controller.get(2026));assertThrows(AccessDeniedException.class,()->controller.save(2026,new com.kcserver.vermietung.dto.FinanzDashboardEinstellungDTO(new java.math.BigDecimal("25000"))));}
  verify(ctx.getBean(FinanzDashboardService.class),times(2)).get(2026);
  verify(ctx.getBean(FinanzDashboardService.class),times(2)).save(eq(2026),any());
 }
}
