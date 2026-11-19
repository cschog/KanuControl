package com.kcserver.vermietung.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.core.tenancy.TenantContext;
import com.kcserver.vermietung.entity.VermietungAenderung;
import com.kcserver.vermietung.repository.VermietungAenderungRepository;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VermietungAuditTest {
    VermietungAenderungRepository repository=mock(VermietungAenderungRepository.class);
    ObjectMapper mapper=new ObjectMapper();
    @AfterEach void clear(){TenantContext.clear();SecurityContextHolder.clearContext();}
    @Test void writesAuthenticatedActorTimestampAndStructuredData()throws Exception{
        TenantContext.setCurrentTenant("club_a");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("review-user","",List.of()));
        new VermietungAenderungsprotokoll(repository,mapper).schreiben("TEST",Map.of("buchungId",42));
        var value=ArgumentCaptor.forClass(VermietungAenderung.class);verify(repository).save(value.capture());
        assertEquals("review-user",value.getValue().getBenutzer());assertEquals("TEST",value.getValue().getAktion());
        assertNotNull(value.getValue().getZeitpunkt());assertEquals(42,mapper.readTree(value.getValue().getDaten()).get("buchungId").asInt());
    }
    @Test void missingTenantOrAuthenticationCannotWrite(){
        var service=new VermietungAenderungsprotokoll(repository,mapper);
        assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.schreiben("TEST",Map.of()));
        TenantContext.setCurrentTenant("club_a");
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service.schreiben("TEST",Map.of()));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user","secret"));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->service.schreiben("TEST",Map.of()));
        verifyNoInteractions(repository);
    }
}
