package kc.isolated;
import com.kcserver.vermietung.controller.BuchungsnachrichtController;
import com.kcserver.vermietung.service.BuchungsnachrichtService;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class BuchungsnachrichtSecurityTest {
 @Configuration @EnableMethodSecurity(proxyTargetClass=true) static class Config{
  @Bean BuchungsnachrichtService service(){return mock(BuchungsnachrichtService.class);}
  @Bean BuchungsnachrichtController controller(BuchungsnachrichtService s){return new BuchungsnachrichtController(s);}
 }
 @AfterEach void cleanup(){SecurityContextHolder.clearContext();}
 @Test void nurVermietungUndAdminKoennenNachrichtenLesenUndAusfuehren(){try(var context=new AnnotationConfigApplicationContext(Config.class)){
  var c=context.getBean(BuchungsnachrichtController.class);var s=context.getBean(BuchungsnachrichtService.class);
  SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("x","n/a",List.of(new SimpleGrantedAuthority("ROLE_USER"))));assertThrows(AccessDeniedException.class,()->c.liste(1L));assertThrows(AccessDeniedException.class,()->c.ausfuehren(1L,"id",new BuchungsnachrichtService.Freigabe("x",false)));verifyNoInteractions(s);
  for(String role:List.of("ROLE_ADMIN","ROLE_VERMIETUNG")){SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("x","n/a",List.of(new SimpleGrantedAuthority(role))));assertDoesNotThrow(()->c.liste(1L));}
 }}
}
