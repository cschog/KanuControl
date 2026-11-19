package kc.isolated;
import com.kcserver.vermietung.controller.*;
import com.kcserver.vermietung.service.*;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class WorkflowSecurityTest {
 @Configuration @EnableMethodSecurity(proxyTargetClass=true) static class Config {
  @Bean JahresabrechnungController jahresabrechnungen(){return new JahresabrechnungController(mock(JahresabrechnungService.class));}
  @Bean BuchungFinanzenController finanzen(){return new BuchungFinanzenController(mock(BuchungFinanzenService.class));}
  @Bean BuchungController buchungen(){return new BuchungController(mock(BuchungService.class));}
  @Bean StammdatenMergeController merge(){return new StammdatenMergeController(mock(StammdatenMergeService.class));}
 }
 @AfterEach void clear(){SecurityContextHolder.clearContext();}
 @Test void korrekturRuecknahmeUndMergeSindAufVermietungUndAdminBegrenzt(){try(var ctx=new AnnotationConfigApplicationContext(Config.class)){
  var jahr=ctx.getBean(JahresabrechnungController.class);
  var fin=ctx.getBean(BuchungFinanzenController.class);var buch=ctx.getBean(BuchungController.class);var merge=ctx.getBean(StammdatenMergeController.class);
  var auswahl=new BuchungFinanzenController.Auswahl(List.of(1L));var auftrag=new StammdatenMergeService.Auftrag(1L,2L,Map.of(),"geprueft");
  SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user","",List.of(new SimpleGrantedAuthority("ROLE_USER"))));
  assertThrows(AccessDeniedException.class,()->jahr.list(1L));assertThrows(AccessDeniedException.class,()->jahr.preview(1L,2026));assertThrows(AccessDeniedException.class,()->jahr.create(1L,2026,new JahresabrechnungService.Auftrag("x")));assertThrows(AccessDeniedException.class,()->jahr.pdf(1L,2026));
  assertThrows(AccessDeniedException.class,()->fin.entfernen(1L,auswahl));assertThrows(AccessDeniedException.class,()->buch.stornoZuruecknehmen(1L));assertThrows(AccessDeniedException.class,()->merge.anwenden("mieter",auftrag));
  for(String role:List.of("ROLE_ADMIN","ROLE_VERMIETUNG")){SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user","",List.of(new SimpleGrantedAuthority(role))));assertDoesNotThrow(()->jahr.list(1L));assertDoesNotThrow(()->jahr.preview(1L,2026));assertDoesNotThrow(()->jahr.create(1L,2026,new JahresabrechnungService.Auftrag("x")));assertDoesNotThrow(()->jahr.pdf(1L,2026));assertDoesNotThrow(()->fin.entfernen(1L,auswahl));assertDoesNotThrow(()->buch.stornoZuruecknehmen(1L));assertDoesNotThrow(()->merge.anwenden("veranstalter",auftrag));}
 }}
}
