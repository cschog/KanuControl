package com.kcserver.unit;

import com.kcserver.vermietung.entity.*;
import com.kcserver.vermietung.enumtype.Buchungsstatus;
import com.kcserver.vermietung.repository.*;
import com.kcserver.vermietung.service.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuchungStatusBenachrichtigungTest {
    private BuchungRepository repository;
    private HomepageStatusMailService mails;
    private BuchungService service;
    private Buchung buchung;
    private HomepageStatusMailService.StatusMail preparedStatus;
    @BeforeEach void setup() {
        repository=mock(BuchungRepository.class);mails=mock(HomepageStatusMailService.class);
        service=new BuchungService(repository,mock(MieterRepository.class),mock(BuchungsnummerCounterRepository.class),
            mock(com.kcserver.kjfp.repository.PersonRepository.class),mock(com.kcserver.core.repository.VereinRepository.class),
            mock(MietobjektRepository.class),mock(MietbereichRepository.class),mock(MietpreisRepository.class),mock(MietbereichRabattRepository.class),
            mock(ZaehlerRepository.class),mock(ZaehlerstandRepository.class),mock(VerbrauchsabrechnungService.class),
            mock(BuchungVerbrauchskostenRepository.class),mock(BuchungFinanzbewegungRepository.class),mails);
        preparedStatus=mock(HomepageStatusMailService.StatusMail.class);
        when(mails.statusVorbereiten(any(),any(),any(),anyString())).thenReturn(preparedStatus);
        buchung=new Buchung();buchung.setId(1L);buchung.setHerkunft("DIREKT");
        Mietobjekt objekt=new Mietobjekt();objekt.setId(7L);buchung.setMietobjekt(objekt);buchung.setMieter(new com.kcserver.core.entity.Person());
        buchung.setAnreise(LocalDateTime.now().plusDays(5));buchung.setAbreise(LocalDateTime.now().plusDays(7));
        when(repository.findLockedById(1L)).thenReturn(Optional.of(buchung));when(repository.save(any())).thenAnswer(i->i.getArgument(0));
    }
    @Test void expliziteVersandentscheidungVormerktAuchFuerDirektInKcErstellteBuchungenEineMail() {
        service.aendereStatus(1L,Buchungsstatus.RESERVIERT,List.of(),true,"geprueft");
        service.aendereStatus(1L,Buchungsstatus.BESTAETIGT,List.of(),true,"geprueft");
        service.aendereStatus(1L,Buchungsstatus.STORNIERT,List.of(),true,"geprueft");
        verify(mails).statusVorbereiten(buchung,Buchungsstatus.RESERVIERT,Buchungsstatus.ANFRAGE,"geprueft");
        verify(mails).statusVorbereiten(buchung,Buchungsstatus.BESTAETIGT,Buchungsstatus.RESERVIERT,"geprueft");
        verify(mails).statusVorbereiten(buchung,Buchungsstatus.STORNIERT,Buchungsstatus.BESTAETIGT,"geprueft");
        verify(mails,times(3)).statusSenden(preparedStatus);
        assertEquals(Buchungsstatus.STORNIERT,buchung.getStatus());
    }
    @Test void fehlendeVersandentscheidungSendetKeineMail() {
        service.aendereStatus(1L,Buchungsstatus.RESERVIERT);
        assertEquals(Buchungsstatus.RESERVIERT,buchung.getStatus());
        verifyNoInteractions(mails);
    }
    @Test void wiederholterStatusErzeugtKeineZweiteMail() {
        service.aendereStatus(1L,Buchungsstatus.RESERVIERT,List.of(),true,"geprueft");
        assertThrows(IllegalArgumentException.class,()->service.aendereStatus(1L,Buchungsstatus.RESERVIERT,List.of(),true,"geprueft"));
        verify(mails,times(1)).statusVorbereiten(buchung,Buchungsstatus.RESERVIERT,Buchungsstatus.ANFRAGE,"geprueft");
    }
    @Test void ungueltigerUebergangSendetKeineMail() {
        assertThrows(IllegalArgumentException.class,()->service.aendereStatus(1L,Buchungsstatus.ABGESCHLOSSEN,List.of(),true,"geprueft"));verifyNoInteractions(mails);
    }
    @Test void versandfehlerLaesstDenBereitsGespeichertenStatusBestehen() {
        PlatformTransactionManager tx=mock(PlatformTransactionManager.class);
        when(tx.getTransaction(any(TransactionDefinition.class))).thenReturn(new SimpleTransactionStatus());
        java.util.concurrent.atomic.AtomicBoolean committed=new java.util.concurrent.atomic.AtomicBoolean();
        doAnswer(i -> { committed.set(true); return null; }).when(tx).commit(any());
        service.configureStatusTransaction(tx);
        doAnswer(i -> {
            assertTrue(committed.get(),"Status-Transaktion muss vor dem Mailversand committet sein");
            assertEquals(Buchungsstatus.RESERVIERT,buchung.getStatus());
            throw new IllegalStateException("Mail queue unavailable");
        }).when(mails).statusSenden(preparedStatus);
        assertThrows(IllegalStateException.class,()->service.aendereStatus(1L,Buchungsstatus.RESERVIERT,List.of(),true,"geprueft"));
        assertEquals(Buchungsstatus.RESERVIERT,buchung.getStatus());
        assertTrue(committed.get());
        verify(repository).save(buchung);
    }
    @Test void direktReservierungBereitetPdfVorCommitVorUndVersendetErstDanach() {
        buchung.setBuchungsquelle(com.kcserver.vermietung.enumtype.Buchungsquelle.DIREKT);
        var versand=mock(ReservierungsversandService.class);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"reservierungsversand",versand);
        var vorbereitet=mock(HomepageStatusMailService.ReservierungMail.class);
        PlatformTransactionManager tx=mock(PlatformTransactionManager.class);
        when(tx.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        var committed=new java.util.concurrent.atomic.AtomicBoolean();
        doAnswer(i->{committed.set(true);return null;}).when(tx).commit(any());
        service.configureStatusTransaction(tx);
        when(mails.reservierungVorbereiten(buchung,"geprueft")).thenAnswer(i->{
            assertFalse(committed.get());assertEquals(Buchungsstatus.ANFRAGE,buchung.getStatus());return vorbereitet;
        });
        when(versand.versenden(1L,vorbereitet)).thenAnswer(i->{
            assertTrue(committed.get());assertEquals(Buchungsstatus.RESERVIERT,buchung.getStatus());return null;
        });
        var result=service.aendereStatus(1L,Buchungsstatus.RESERVIERT,List.of(),true,"geprueft");
        assertNotNull(result.getReservierungMailGesendetAm());assertNull(result.getReservierungMailFehler());
        verify(mails,never()).senden(any(),any(),any());verify(versand).versenden(1L,vorbereitet);
    }
    @Test void ungueltigesPdfVerhindertStatuswechselUndVersand() {
        buchung.setBuchungsquelle(com.kcserver.vermietung.enumtype.Buchungsquelle.DIREKT);
        when(mails.reservierungVorbereiten(buchung,"geprueft")).thenThrow(new IllegalArgumentException("Bankdaten fehlen"));
        assertThrows(IllegalArgumentException.class,()->service.aendereStatus(1L,Buchungsstatus.RESERVIERT,List.of(),true,"geprueft"));
        assertEquals(Buchungsstatus.ANFRAGE,buchung.getStatus());verify(repository,never()).save(any());
        verify(mails,never()).senden(any(),any(),any());
    }
    @Test void direktReservierungsVersandfehlerMeldetGespeichertenStatus() {
        buchung.setBuchungsquelle(com.kcserver.vermietung.enumtype.Buchungsquelle.DIREKT);
        var versand=mock(ReservierungsversandService.class);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"reservierungsversand",versand);
        var vorbereitet=mock(HomepageStatusMailService.ReservierungMail.class);
        when(mails.reservierungVorbereiten(buchung,"geprueft")).thenReturn(vorbereitet);
        when(versand.versenden(1L,vorbereitet)).thenReturn("Die Buchung ist reserviert. SMTP ist nicht erreichbar.");
        var e=assertThrows(org.springframework.web.server.ResponseStatusException.class,()->service.aendereStatus(1L,Buchungsstatus.RESERVIERT,List.of(),true,"geprueft"));
        assertEquals(502,e.getStatusCode().value());assertEquals(Buchungsstatus.RESERVIERT,buchung.getStatus());
        verify(repository).save(buchung);
    }
    @Test void automatischeUmstellungBeimLesenSendetOhneBenutzerentscheidungKeineMail() {
        buchung.setStatus(Buchungsstatus.BESTAETIGT);buchung.setAbreise(LocalDateTime.now().minusDays(1));
        service.getById(1L);service.getById(1L);
        verifyNoInteractions(mails);
    }
    @Test void automatischeSammelUmstellungSendetOhneBenutzerentscheidungKeineMail() {
        buchung.setStatus(Buchungsstatus.BESTAETIGT);buchung.setAbreise(LocalDateTime.now().minusDays(1));
        when(repository.findByStatusAndAbreiseLessThanEqual(eq(Buchungsstatus.BESTAETIGT),any()))
            .thenAnswer(i->buchung.getStatus()==Buchungsstatus.BESTAETIGT?List.of(buchung):List.of());
        assertEquals(1,service.aktualisiereAbrechenbareBuchungen());assertEquals(0,service.aktualisiereAbrechenbareBuchungen());
        verifyNoInteractions(mails);
    }
    @Test void fehlendeVorschaufreigabeVerhindertDenStatuswechselVorJederAenderung() {
        var error=assertThrows(org.springframework.web.server.ResponseStatusException.class,
                ()->service.aendereStatus(1L,Buchungsstatus.RESERVIERT,List.of(),true));
        assertEquals(409,error.getStatusCode().value());assertEquals(Buchungsstatus.ANFRAGE,buchung.getStatus());
        verifyNoInteractions(repository,mails);
    }
    @Test void veralteteVorschauVerhindertStatusSpeichernUndVersand() {
        when(mails.statusVorbereiten(any(),any(),any(),eq("veraltet"))).thenThrow(
                new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT,"Vorschau veraltet"));
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                ()->service.aendereStatus(1L,Buchungsstatus.RESERVIERT,List.of(),true,"veraltet"));
        assertEquals(Buchungsstatus.ANFRAGE,buchung.getStatus());verify(repository,never()).save(any());
        verify(mails,never()).statusSenden(any());
    }
    @Test void statusVorschauLaesstDieBuchungUndDenVersandUnveraendert() {
        when(repository.findById(1L)).thenReturn(Optional.of(buchung));
        service.statusMailVorschau(1L,Buchungsstatus.RESERVIERT);
        verify(repository,never()).save(any());verify(repository,never()).findLockedById(any());
        verify(mails).statusVorschau(buchung,Buchungsstatus.RESERVIERT,Buchungsstatus.ANFRAGE);
        verify(mails,never()).statusSenden(any());assertEquals(Buchungsstatus.ANFRAGE,buchung.getStatus());
    }

    @Test void kostenloseBuchungWirdBeimLesenOhneMailAbgeschlossen() {
        buchung.setZahlungsvariante(com.kcserver.vermietung.enumtype.Zahlungsvariante.OHNE_ABRECHNUNG);
        buchung.setStatus(Buchungsstatus.BESTAETIGT);buchung.setAbreise(LocalDateTime.now(ZoneId.of("Europe/Berlin")).minusHours(1));
        buchung.setAbrechnungRevision(4);
        assertEquals(Buchungsstatus.ABGESCHLOSSEN, service.getById(1L).getStatus());
        assertEquals(4,buchung.getAbschlussRevision());verifyNoInteractions(mails);
    }
    @Test void hintergrundAbschlussIstIdempotentUndIgnoriertKostenpflichtigeBuchungen() {
        buchung.setZahlungsvariante(com.kcserver.vermietung.enumtype.Zahlungsvariante.OHNE_ABRECHNUNG);
        buchung.setStatus(Buchungsstatus.BESTAETIGT);buchung.setAbreise(LocalDateTime.now(ZoneId.of("Europe/Berlin")).minusHours(1));
        var bezahlt = new Buchung();bezahlt.setId(2L);bezahlt.setStatus(Buchungsstatus.BESTAETIGT);
        when(repository.findByStatusAndAbreiseLessThanEqual(eq(Buchungsstatus.BESTAETIGT),any())).thenReturn(List.of(buchung,bezahlt));
        assertEquals(1,service.aktualisiereKostenloseBuchungen());
        assertEquals(0,service.aktualisiereKostenloseBuchungen());
        assertEquals(Buchungsstatus.ABGESCHLOSSEN,buchung.getStatus());assertEquals(Buchungsstatus.BESTAETIGT,bezahlt.getStatus());
        verify(repository,never()).findLockedById(2L);verifyNoInteractions(mails);
    }
    @Test void hintergrundSchliesstWederUnbefristeteNochNachSperreVerschobeneBuchungAb() {
        buchung.setZahlungsvariante(com.kcserver.vermietung.enumtype.Zahlungsvariante.OHNE_ABRECHNUNG);
        buchung.setStatus(Buchungsstatus.BESTAETIGT);buchung.setAbreise(LocalDateTime.of(3000,12,31,14,0));
        when(repository.findByStatusAndAbreiseLessThanEqual(eq(Buchungsstatus.BESTAETIGT),any())).thenReturn(List.of(buchung));
        assertEquals(0,service.aktualisiereKostenloseBuchungen());
        buchung.setAbreise(LocalDateTime.now(ZoneId.of("Europe/Berlin")).plusHours(1));
        assertEquals(0,service.aktualisiereKostenloseBuchungen());assertEquals(Buchungsstatus.BESTAETIGT,buchung.getStatus());
        verifyNoInteractions(mails);
    }

}
