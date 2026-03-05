package com.kcserver.finanz;

import com.kcserver.dto.abrechnung.AbrechnungBuchungCreateDTO;
import com.kcserver.entity.Abrechnung;
import com.kcserver.enumtype.AbrechnungsStatus;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.repository.AbrechnungBuchungRepository;
import com.kcserver.repository.AbrechnungRepository;
import com.kcserver.testsupport.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AbrechnungServiceTest extends AbstractTenantIntegrationTest {

    @Autowired
    AbrechnungService abrechnungService;

    @Autowired
    AbrechnungBuchungService buchungService;

    @Autowired
    AbrechnungRepository abrechnungRepository;

    @Autowired
    AbrechnungBuchungRepository buchungRepository;

    @Autowired
    TestDataFactory factory;

    Long veranstaltungId;

    /* ========================================================= */

    @BeforeEach
    void setup() {

        veranstaltungId = factory.createTestVeranstaltung();

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

        addBuchung(FinanzKategorie.UNTERKUNFT, "100.00");
        addBuchung(FinanzKategorie.TEILNEHMERBEITRAG, "100.00");

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

        addBuchung(FinanzKategorie.UNTERKUNFT, "100.00");

        assertThatThrownBy(() ->
                abrechnungService.abschliessen(veranstaltungId)
        ).isInstanceOf(ResponseStatusException.class);
    }

    /* ========================================================= */

    @Test
    void shouldPreventDoubleClose() {

        addBuchung(FinanzKategorie.UNTERKUNFT, "100.00");
        addBuchung(FinanzKategorie.TEILNEHMERBEITRAG, "100.00");

        abrechnungService.abschliessen(veranstaltungId);

        assertThatThrownBy(() ->
                abrechnungService.abschliessen(veranstaltungId)
        ).isInstanceOf(ResponseStatusException.class);
    }

    /* ========================================================= */

    @Test
    void shouldNotAllowAddWhenClosed() {

        addBuchung(FinanzKategorie.UNTERKUNFT, "100.00");
        addBuchung(FinanzKategorie.TEILNEHMERBEITRAG, "100.00");

        abrechnungService.abschliessen(veranstaltungId);

        AbrechnungBuchungCreateDTO dto = new AbrechnungBuchungCreateDTO();
        dto.setKategorie(FinanzKategorie.UNTERKUNFT);
        dto.setBetrag(new BigDecimal("50.00"));
        dto.setDatum(LocalDate.now());

        assertThatThrownBy(() ->
                buchungService.addBuchung(veranstaltungId, dto)
        ).isInstanceOf(ResponseStatusException.class);
    }

    /* ========================================================= */

    private void addBuchung(FinanzKategorie kategorie, String betrag) {

        AbrechnungBuchungCreateDTO dto = new AbrechnungBuchungCreateDTO();

        dto.setKategorie(kategorie);
        dto.setBetrag(new BigDecimal(betrag));
        dto.setDatum(LocalDate.now());

        buchungService.addBuchung(veranstaltungId, dto);
    }
}