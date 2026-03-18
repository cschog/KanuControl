package com.kcserver.unit;

import com.kcserver.dto.planung.PlanungPositionCreateDTO;
import com.kcserver.entity.Person;
import com.kcserver.entity.PlanungPosition;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.entity.Verein;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.enumtype.Sex;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.finanz.PlanungPositionService;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import com.kcserver.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlanungPositionServiceTest extends AbstractTenantIntegrationTest {

    @Autowired
    PlanungPositionService service;
    @Autowired PlanungPositionRepository positionRepository;
    @Autowired
    VeranstaltungRepository veranstaltungRepository;
    @Autowired
    VereinRepository vereinRepository;
    @Autowired
    PersonRepository personRepository;



    @Test
    void shouldAddPosition() {

        Long veranstaltungId = createTestVeranstaltung();

        PlanungPositionCreateDTO dto = new PlanungPositionCreateDTO();
        dto.setKategorie(FinanzKategorie.UNTERKUNFT);
        dto.setBetrag(new BigDecimal("100"));

        service.addPosition(veranstaltungId, dto);

        assertThat(positionRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldUpdatePosition() {

        Long veranstaltungId = createTestVeranstaltung();

        PlanungPositionCreateDTO dto = new PlanungPositionCreateDTO();
        dto.setKategorie(FinanzKategorie.UNTERKUNFT);
        dto.setBetrag(new BigDecimal("100"));

        service.addPosition(veranstaltungId, dto);

        PlanungPosition pos = positionRepository.findAll().get(0);

        dto.setBetrag(new BigDecimal("150"));

        service.updatePosition(veranstaltungId, pos.getId(), dto);

        PlanungPosition updated = positionRepository.findById(pos.getId()).orElseThrow();

        assertThat(updated.getBetrag()).isEqualByComparingTo("150");
    }

    @Test
    void shouldDeletePosition() {

        Long veranstaltungId = createTestVeranstaltung();

        PlanungPositionCreateDTO dto = new PlanungPositionCreateDTO();
        dto.setKategorie(FinanzKategorie.UNTERKUNFT);
        dto.setBetrag(new BigDecimal("100"));

        service.addPosition(veranstaltungId, dto);

        PlanungPosition pos = positionRepository.findAll().get(0);

        service.deletePosition(veranstaltungId, pos.getId());

        assertThat(positionRepository.findAll()).isEmpty();
    }
    /* =========================================================
       HELPER
       ========================================================= */

    private Long createTestVeranstaltung() {

        Verein verein = new Verein();
        verein.setName("Testverein");
        verein.setAbk("TV");
        verein = vereinRepository.save(verein);

        Person leiter = new Person();
        leiter.setVorname("Max");
        leiter.setName("Mustermann");
        leiter.setSex(Sex.WEIBLICH);
        leiter.setAktiv(true);
        leiter = personRepository.save(leiter);

        Veranstaltung v = new Veranstaltung();
        v.setName("ControllerTest");
        v.setTyp(VeranstaltungTyp.JEM);
        v.setVerein(verein);
        v.setLeiter(leiter);
        v.setBeginnDatum(LocalDate.now());
        v.setEndeDatum(LocalDate.now().plusDays(1));
        v.setBeginnZeit(LocalTime.NOON);
        v.setEndeZeit(LocalTime.MIDNIGHT);
        v.setAktiv(true);

        return veranstaltungRepository.save(v).getId();
    }
}