package kc.isolated;
import com.kcserver.vermietung.controller.SperrzeitController;
import com.kcserver.vermietung.service.SperrzeitService;
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
class SperrzeitSecurityTest {
 @Configuration @EnableMethodSecurity(proxyTargetClass=true) static class Config {
  @Bean SperrzeitService service(){return mock(SperrzeitService.class);}
  @Bean SperrzeitController controller(SperrzeitService s){return new SperrzeitController(s);}
 }
 AnnotationConfigApplicationContext ctx; SperrzeitController controller;
 @BeforeEach void setup(){ctx=new AnnotationConfigApplicationContext(Config.class);controller=ctx.getBean(SperrzeitController.class);}
 @AfterEach void close(){SecurityContextHolder.clearContext();ctx.close();}
 void role(String name){SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("test","unused",List.of(new SimpleGrantedAuthority("ROLE_"+name))));}
 @Test void vermietungDarfSperrenVerwalten(){role("VERMIETUNG");assertDoesNotThrow(()->controller.list(7L));assertDoesNotThrow(()->controller.create(7L,null));assertDoesNotThrow(()->controller.update(7L,1L,null));assertDoesNotThrow(()->controller.delete(7L,1L,0L));}
 @Test void adminDarfSperrenVerwalten(){role("ADMIN");assertDoesNotThrow(()->controller.create(7L,null));}
 @Test void homepageUndAndereRollenDuerfenKeineInternenGruendeLesen(){for(String name:List.of("HOMEPAGE_INTEGRATION","USER")){role(name);assertThrows(AccessDeniedException.class,()->controller.list(7L));assertThrows(AccessDeniedException.class,()->controller.create(7L,null));assertThrows(AccessDeniedException.class,()->controller.delete(7L,1L,0L));}verifyNoInteractions(ctx.getBean(SperrzeitService.class));}
}
