package com.kcserver.unit;
import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.vermietung.service.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class KostenloseBuchungSchedulerTest {
 @Test void schliesstMandantenSeparatAbUndStelltKontextAuchBeiFehlerWiederHer() throws Exception {
  var jdbc=mock(JdbcTemplate.class);var service=mock(BuchungService.class);
  when(jdbc.queryForList(anyString(),eq(String.class))).thenReturn(List.of("ekc_a","ekc_b"));
  var seen=new ArrayList<String>();when(service.aktualisiereKostenloseBuchungen()).thenAnswer(i->{seen.add(TenantContext.getCurrentTenant());if(seen.size()==1)throw new IllegalStateException("Testfehler");return 1;});
  TenantContext.setCurrentTenant("ekc_previous");
  try {new KostenloseBuchungScheduler(jdbc,service).abschliessen();assertEquals(List.of("ekc_a","ekc_b"),seen);assertEquals("ekc_previous",TenantContext.getCurrentTenant());}
  finally {TenantContext.clear();}
  var scheduled=KostenloseBuchungScheduler.class.getMethod("abschliessen").getAnnotation(org.springframework.scheduling.annotation.Scheduled.class);
  assertEquals(60000,scheduled.fixedDelay());
 }
}
