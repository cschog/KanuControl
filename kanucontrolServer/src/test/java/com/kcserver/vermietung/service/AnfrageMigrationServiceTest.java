package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.Buchung;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.BuchungRepository;
import org.junit.jupiter.api.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnfrageMigrationServiceTest {
    BuchungRepository repository=mock(BuchungRepository.class);
    BuchungService bookings=mock(BuchungService.class);
    PlatformTransactionManager transactions=mock(PlatformTransactionManager.class);
    VermietungAenderungsprotokoll audit=mock(VermietungAenderungsprotokoll.class);
    AnfrageMigrationService service=new AnfrageMigrationService(repository,bookings,transactions,audit);
    @BeforeEach void setup(){when(transactions.getTransaction(any())).thenAnswer(i->new SimpleTransactionStatus());}
    @Test void listContainsOnlyLegacyRequestsAndPreservesDates(){
        var request=new Buchung();request.setId(1L);request.setBuchungsnummer("2026-001");request.setStatus(Buchungsstatus.ANFRAGE);
        request.setAnreise(LocalDateTime.of(2026,12,1,16,0));request.setAbreise(LocalDateTime.of(2026,12,3,14,0));
        var confirmed=new Buchung();confirmed.setStatus(Buchungsstatus.BESTAETIGT);
        when(repository.findAll()).thenReturn(List.of(request,confirmed));
        assertEquals(List.of(new AnfrageMigrationService.Altbuchung(1L,"2026-001","2026-12-01T16:00","2026-12-03T14:00")),service.offen());
        verifyNoInteractions(bookings,audit);verify(transactions).commit(any());
    }
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans={false,true})
    void migratesOnlyToExplicitTargetAndAudits(boolean reject){
        var request=new Buchung();request.setStatus(Buchungsstatus.ANFRAGE);
        when(repository.findLockedById(1L)).thenReturn(Optional.of(request));
        service.migrieren(1L,reject);
        var target=reject?Buchungsstatus.ABGELEHNT:Buchungsstatus.RESERVIERT;
        verify(bookings).anwendenGeprueft(1L,target,List.of());
        verify(audit).schreiben("ANFRAGE_MIGRIERT",Map.of("buchungId",1L,"vorher","ANFRAGE","nachher",target.name()));
        verify(transactions).commit(any());verify(transactions,never()).rollback(any());
    }
    @Test void alreadyMigratedRequestIsRejectedAndTransactionRolledBack(){
        var b=new Buchung();b.setStatus(Buchungsstatus.RESERVIERT);
        when(repository.findLockedById(1L)).thenReturn(Optional.of(b));
        assertThrows(IllegalArgumentException.class,()->service.migrieren(1L,false));
        verifyNoInteractions(bookings,audit);verify(transactions).rollback(any());
    }
    @Test void applicationFailureDoesNotRecordSuccessfulMigration(){
        var b=new Buchung();b.setStatus(Buchungsstatus.ANFRAGE);
        when(repository.findLockedById(1L)).thenReturn(Optional.of(b));
        when(bookings.anwendenGeprueft(anyLong(),any(),anyList())).thenThrow(new IllegalStateException("conflict"));
        assertThrows(IllegalStateException.class,()->service.migrieren(1L,false));
        verifyNoInteractions(audit);verify(transactions).rollback(any());
    }
}
