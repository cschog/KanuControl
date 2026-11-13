package com.kcserver.veranstaltung;

import com.kcserver.core.dto.veranstaltung.*;
import com.kcserver.core.entity.*;
import com.kcserver.core.mapper.*;
import com.kcserver.core.repository.*;
import com.kcserver.core.service.veranstaltung.VeranstaltungServiceImpl;
import com.kcserver.kjfp.entity.*;
import com.kcserver.kjfp.entity.beitraege.Beitragsstruktur;
import com.kcserver.kjfp.repository.*;
import com.kcserver.kjfp.repository.beitrag.BeitragsstrukturRepository;
import com.kcserver.kjfp.service.finanz.FinanzGruppeService;
import com.kcserver.kjfp.service.planung.PlanungKopierService;
import com.kcserver.kjfp.service.veranstaltung.KjfpVeranstaltungRegeln;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VeranstaltungKopieTest {
    @Mock VeranstaltungRepository veranstaltungen;
    @Mock VereinRepository vereine;
    @Mock com.kcserver.kjfp.repository.PersonRepository personen;
    @Mock TeilnehmerRepository teilnehmer;
    @Mock VeranstaltungMapper mapper;
    @Mock KjfpVeranstaltungRegeln regeln;
    @Mock BeitragsstrukturRepository beitraege;
    @Mock UnterkunftsartRepository unterkuenfte;
    @Mock VerpflegungsmodellRepository verpflegung;
    @Mock FinanzGruppeService finanzgruppen;
    @Mock PlanungKopierService planungKopierService;
    @InjectMocks VeranstaltungServiceImpl service;
    private VeranstaltungCreateDTO dto() {
        var dto=new VeranstaltungCreateDTO(); dto.setName("Kopie"); dto.setVereinId(1L);dto.setLeiterId(2L);
        dto.setBeginnDatum(java.time.LocalDate.of(2027,7,1)); dto.setEndeDatum(java.time.LocalDate.of(2027,7,10));
        dto.setBeginnZeit(java.time.LocalTime.of(10,0));dto.setEndeZeit(java.time.LocalTime.of(18,0));
        return dto;
    }
    private void prerequisites() {
        Verein verein=new Verein();verein.setKjfpVerein(true);when(vereine.findById(1L)).thenReturn(Optional.of(verein));
        Person person=new Person();person.setAktiv(true);when(personen.findById(2L)).thenReturn(Optional.of(person));
    }
    @Test void copySavesAllEventDetailsAndCreatesPlanInSameOperation() {
        prerequisites();var dto=dto();dto.setKopieVonVeranstaltungId(4L);
        var source=new Veranstaltung();source.setKjfpAktiv(true);when(veranstaltungen.findByIdWithRelations(4L)).thenReturn(Optional.of(source));
        var unterkunft=new Unterkunftsart();when(unterkuenfte.findById(3L)).thenReturn(Optional.of(unterkunft));dto.setUnterkunftsartId(3L);
        var essen=new Verpflegungsmodell();when(verpflegung.findById(5L)).thenReturn(Optional.of(essen));dto.setVerpflegungsmodellId(5L);
        var struktur=new Beitragsstruktur();when(beitraege.findById(6L)).thenReturn(Optional.of(struktur));dto.setBeitragsstrukturId(6L);
        dto.setCountryCode(com.kcserver.kjfp.enumtype.CountryCode.FR);dto.setPlz("07150");dto.setOrt("Vallon");
        dto.setBeschreibung("Beschreibung");dto.setProgrammentwurfTitel("Programm");dto.setProgrammentwurfInhalt("<p>Start</p>");
        dto.setIndividuelleGebuehren(true);dto.setStandardGebuehr(new java.math.BigDecimal("80"));dto.setScope(com.kcserver.core.enumtype.VeranstaltungScope.VEREIN);
        dto.setFahrtenAktiv(true);
        var saved=new java.util.concurrent.atomic.AtomicReference<Veranstaltung>();
        when(veranstaltungen.save(any(Veranstaltung.class))).thenAnswer(i -> {Veranstaltung v=i.getArgument(0);v.setId(99L);saved.set(v);return v;});
        when(veranstaltungen.findByIdWithRelations(99L)).thenAnswer(i -> Optional.of(saved.get()));
        service.create(dto);
        var target=saved.get();
        assertThat(target.getUnterkunftsart()).isSameAs(unterkunft);assertThat(target.getVerpflegungsmodell()).isSameAs(essen);
        assertThat(target.getBeitragsstruktur()).isSameAs(struktur);assertThat(target.getOrt()).isEqualTo("Vallon");
        assertThat(target.getCountryCode()).isEqualTo(dto.getCountryCode());assertThat(target.getPlz()).isEqualTo("07150");
        assertThat(target.getBeschreibung()).isEqualTo("Beschreibung");assertThat(target.getProgrammentwurfInhalt()).isEqualTo("<p>Start</p>");
        assertThat(target.isIndividuelleGebuehren()).isTrue();assertThat(target.getStandardGebuehr()).isEqualByComparingTo("80");
        assertThat(target.getScope()).isEqualTo(dto.getScope());assertThat(target.isFahrtenAktiv()).isTrue();
        verify(planungKopierService).kopieren(4L,target);
        verify(teilnehmer).save(any(Teilnehmer.class)); // Nur die Leitung, keine Liste aus der Vorlage.
    }
    @Test void ordinaryCreationStillWorksWithoutCopySource() {
        prerequisites();
        var saved=new java.util.concurrent.atomic.AtomicReference<Veranstaltung>();
        when(veranstaltungen.save(any(Veranstaltung.class))).thenAnswer(i -> {Veranstaltung v=i.getArgument(0);v.setId(99L);saved.set(v);return v;});
        when(veranstaltungen.findByIdWithRelations(99L)).thenAnswer(i -> Optional.of(saved.get()));
        service.create(dto());
        assertThat(saved.get().getName()).isEqualTo("Kopie");
        verifyNoInteractions(planungKopierService);
    }
    @Test void missingTenantSourceDoesNotCreateAnythingOrDeactivateCurrentEvent() {
        prerequisites();var dto=dto();dto.setKopieVonVeranstaltungId(4L);
        when(veranstaltungen.findByIdWithRelations(4L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(dto)).isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException)e).getStatusCode().value()).isEqualTo(404));
        verify(veranstaltungen,never()).save(any(Veranstaltung.class));verify(veranstaltungen,never()).unsetAktiveVeranstaltung();
        verifyNoInteractions(planungKopierService);
    }
}
