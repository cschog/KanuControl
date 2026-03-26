package com.kcserver.integration;

import com.kcserver.dto.abrechnung.AbrechnungBelegCreateDTO;
import com.kcserver.dto.abrechnung.AbrechnungBelegDTO;
import com.kcserver.entity.*;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.finanz.FinanzGruppeService;
import com.kcserver.repository.AbrechnungBelegRepository;
import com.kcserver.repository.FinanzGruppeRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
class FinanzGruppeIntegrationTest extends AbstractFinanzIntegrationTest {

    @Autowired
    FinanzGruppeService finanzGruppeService;

    @Autowired
    FinanzGruppeRepository finanzGruppeRepository;

    @Autowired
    TeilnehmerRepository teilnehmerRepository;

    @Autowired
    VeranstaltungRepository veranstaltungRepository;

    @Autowired
    AbrechnungBelegRepository belegRepository;

    @Test
    void assignKuerzel_shouldCreateAndAssign() {

        Long veranstaltungId = createTestVeranstaltung();
        Veranstaltung v = veranstaltungRepository.findById(veranstaltungId).orElseThrow();

        Teilnehmer t = createTeilnehmer(v, BigDecimal.TEN);

        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                t.getId(),
                "SG"
        );

        Teilnehmer updated =
                teilnehmerRepository.findById(t.getId()).orElseThrow();

        assertThat(updated.getFinanzGruppe()).isNotNull();
        assertThat(updated.getFinanzGruppe().getKuerzel()).isEqualTo("SG");
    }

    @Test
    void assignTeilnehmerBulk_shouldAssignMultiple() {

        Long veranstaltungId = createTestVeranstaltung();
        Veranstaltung v = veranstaltungRepository.findById(veranstaltungId).orElseThrow();

        Teilnehmer t1 = createTeilnehmer(v, BigDecimal.TEN);
        Teilnehmer t2 = createTeilnehmer(v, BigDecimal.ONE);

        FinanzGruppe gruppe =
                finanzGruppeService.create(veranstaltungId, "TEAM");

        finanzGruppeService.assignTeilnehmerBulk(
                veranstaltungId,
                gruppe.getId(),
                List.of(t1.getId(), t2.getId())
        );

        Teilnehmer updated1 =
                teilnehmerRepository.findById(t1.getId()).orElseThrow();

        Teilnehmer updated2 =
                teilnehmerRepository.findById(t2.getId()).orElseThrow();

        assertThat(updated1.getFinanzGruppe()).isEqualTo(gruppe);
        assertThat(updated2.getFinanzGruppe()).isEqualTo(gruppe);
    }

    @Test
    void delete_shouldRemoveGroup() {

        Long veranstaltungId = createTestVeranstaltung();

        FinanzGruppe gruppe =
                finanzGruppeService.create(veranstaltungId, "DEL");

        finanzGruppeService.delete(
                veranstaltungId,
                gruppe.getId()
        );

        assertThat(
                finanzGruppeRepository.findById(gruppe.getId())
        ).isEmpty();
    }
    @Test
    void shouldNotDeleteGruppeWhenTeilnehmerExist() {

        Long veranstaltungId = createTestVeranstaltung();
        Veranstaltung v = veranstaltungRepository.findById(veranstaltungId).orElseThrow();

        Teilnehmer t = createTeilnehmer(v, BigDecimal.TEN);

        FinanzGruppe gruppe =
                finanzGruppeService.create(veranstaltungId, "BLOCK");

        // Teilnehmer zuweisen
        finanzGruppeService.assignTeilnehmerBulk(
                veranstaltungId,
                gruppe.getId(),
                List.of(t.getId())
        );

        // DELETE muss scheitern
        assertThatThrownBy(() ->
                finanzGruppeService.delete(
                        veranstaltungId,
                        gruppe.getId()
                )
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Teilnehmer");
    }

    @Test
    void shouldNotDeleteGruppeWhenBelegeExist() {

        Long veranstaltungId = createTestVeranstaltung();
        Veranstaltung v = veranstaltungRepository.findById(veranstaltungId).orElseThrow();

        // 🔹 Teilnehmer (für Buchung Pflicht)
        Teilnehmer teilnehmer = createTeilnehmer(v, BigDecimal.TEN);

        // 🔹 Gruppe
        FinanzGruppe gruppe =
                finanzGruppeService.create(veranstaltungId, "BLOCK");

        // 🔹 Abrechnung erzeugen (minimal gültig)
        Abrechnung abrechnung = new Abrechnung();
        abrechnung.setVeranstaltung(v);
        abrechnungRepository.save(abrechnung);

        // 🔹 Beleg erzeugen
        AbrechnungBelegCreateDTO dto = new AbrechnungBelegCreateDTO();
        dto.setKuerzel("BLOCK");
        dto.setDatum(LocalDate.now());

        AbrechnungBelegDTO belegDTO =
                abrechnungBelegService.createBeleg(veranstaltungId, dto);

        AbrechnungBeleg beleg =
                belegRepository.findById(belegDTO.getId()).orElseThrow();

        // 🔹 Buchung erzeugen (Pflicht!)
        AbrechnungBuchung buchung = new AbrechnungBuchung();
        buchung.setKategorie(FinanzKategorie.TEILNEHMERBEITRAG);
        buchung.setBetrag(BigDecimal.TEN);
        buchung.setDatum(LocalDate.now());
        buchung.setBeschreibung("Testbuchung");

        beleg.addPosition(buchung);

        belegRepository.save(beleg);

        // 🔥 Jetzt löschen versuchen
        assertThatThrownBy(() ->
                finanzGruppeService.delete(
                        veranstaltungId,
                        gruppe.getId()
                )
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Belege");

        assertThat(
                finanzGruppeRepository.findById(gruppe.getId())
        ).isPresent();
    }
}