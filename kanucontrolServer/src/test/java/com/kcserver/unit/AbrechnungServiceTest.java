package com.kcserver.unit;

import com.kcserver.dto.abrechnung.AbrechnungBelegCreateDTO;
import com.kcserver.dto.abrechnung.AbrechnungBelegDTO;
import com.kcserver.dto.abrechnung.AbrechnungBuchungCreateDTO;
import com.kcserver.entity.Abrechnung;
import com.kcserver.enumtype.AbrechnungsStatus;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.finanz.AbrechnungBelegService;
import com.kcserver.finanz.AbrechnungService;
import com.kcserver.finanz.FinanzGruppeService;
import com.kcserver.repository.AbrechnungRepository;
import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VereinTestFactory;
import com.kcserver.support.api.VeranstaltungTestFactory;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class AbrechnungServiceTest extends AbstractTenantIntegrationTest {

    @Autowired
    AbrechnungService abrechnungService;

    @Autowired
    FinanzGruppeService finanzGruppeService;

    @Autowired
    AbrechnungBelegService belegService;

    @Autowired
    AbrechnungRepository abrechnungRepository;

    @Autowired
    com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    Long veranstaltungId;
    Long teilnehmerId;

    // ✅ Factories
    VereinTestFactory vereinFactory;
    PersonTestFactory personFactory;
    VeranstaltungTestFactory veranstaltungFactory;

    /* ========================================================= */

    @BeforeEach
    void setup() throws Exception {

        vereinFactory = new VereinTestFactory(mockMvc, objectMapper);
        personFactory = new PersonTestFactory(mockMvc, objectMapper);
        veranstaltungFactory = new VeranstaltungTestFactory(mockMvc, objectMapper);

        // 1️⃣ Verein
        Long vereinId = vereinFactory.create("TV", "Testverein");

        // 2️⃣ Leiter
        Long leiterId = personFactory.createWithVerein(vereinId, b ->
                b.withVorname("Max")
                        .withName("Mustermann")
                        .withGeburtsdatum(java.time.LocalDate.of(1990, 1, 1))
        );

        // 3️⃣ Veranstaltung
        veranstaltungId = veranstaltungFactory.create(
                vereinId,
                leiterId,
                "Test Abrechnung"
        );

        // 4️⃣ zusätzlicher Teilnehmer
        Long personId = personFactory.createWithVerein(vereinId, b ->
                b.withVorname("Anna")
                        .withName("Test")
                        .withGeburtsdatum(java.time.LocalDate.of(2000, 1, 1))
        );

        veranstaltungFactory.addTeilnehmer(veranstaltungId, personId);
        teilnehmerId = personId;

        // 5️⃣ Abrechnung vorbereiten
        abrechnungService.getOrCreate(veranstaltungId);

        Abrechnung a = abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseThrow();

        a.setStatus(AbrechnungsStatus.OFFEN);
        abrechnungRepository.save(a);
    }

    /* ========================================================= */

    @Test
    void shouldCloseBalancedAbrechnung() {

        addPosition(FinanzKategorie.UNTERKUNFT, "100.00");
        addPosition(FinanzKategorie.TEILNEHMERBEITRAG, "100.00");

        abrechnungService.abschliessen(veranstaltungId);

        Abrechnung a = abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseThrow();

        assertThat(a.getStatus())
                .isEqualTo(AbrechnungsStatus.ABGESCHLOSSEN);
    }

    /* ========================================================= */

    @Test
    void shouldPreventCloseWhenNotBalanced() {

        addPosition(FinanzKategorie.UNTERKUNFT, "100.00");

        assertThatThrownBy(() ->
                abrechnungService.abschliessen(veranstaltungId)
        ).isInstanceOf(ResponseStatusException.class);
    }

    /* ========================================================= */

    @Test
    void shouldPreventDoubleClose() {

        addPosition(FinanzKategorie.UNTERKUNFT, "100.00");
        addPosition(FinanzKategorie.TEILNEHMERBEITRAG, "100.00");

        abrechnungService.abschliessen(veranstaltungId);

        assertThatThrownBy(() ->
                abrechnungService.abschliessen(veranstaltungId)
        ).isInstanceOf(ResponseStatusException.class);
    }

    /* ========================================================= */

    @Test
    void shouldNotAllowChangesWhenClosed() {

        addPosition(FinanzKategorie.UNTERKUNFT, "100.00");
        addPosition(FinanzKategorie.TEILNEHMERBEITRAG, "100.00");

        abrechnungService.abschliessen(veranstaltungId);

        AbrechnungBelegCreateDTO belegDTO = new AbrechnungBelegCreateDTO();
        belegDTO.setDatum(LocalDate.now());
        belegDTO.setKuerzel("X1");

        assertThatThrownBy(() ->
                belegService.createBeleg(veranstaltungId, belegDTO)
        ).isInstanceOf(ResponseStatusException.class);
    }

    /* ========================================================= */

    private void addPosition(FinanzKategorie kategorie, String betrag) {

        finanzGruppeService.assignKuerzel(
                veranstaltungId,
                teilnehmerId,
                "X1"
        );

        AbrechnungBelegCreateDTO belegDTO = new AbrechnungBelegCreateDTO();
        belegDTO.setDatum(LocalDate.now());
        belegDTO.setKuerzel("X1");

        AbrechnungBelegDTO beleg =
                belegService.createBeleg(veranstaltungId, belegDTO);

        AbrechnungBuchungCreateDTO posDTO =
                new AbrechnungBuchungCreateDTO();

        posDTO.setKategorie(kategorie);
        posDTO.setBetrag(new BigDecimal(betrag));
        posDTO.setDatum(LocalDate.now());

        belegService.addPosition(
                veranstaltungId,
                beleg.getId(),
                posDTO
        );
    }
}