package com.kcserver.finanz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.planung.PlanungPositionCreateDTO;
import com.kcserver.entity.Person;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.entity.Verein;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.enumtype.Sex;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.repository.VereinRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlanungControllerTest extends AbstractTenantIntegrationTest {

    @Autowired ObjectMapper objectMapper;

    @Autowired
    VereinRepository vereinRepository;
    @Autowired
    PersonRepository personRepository;
    @Autowired
    VeranstaltungRepository veranstaltungRepository;

    @Test
    void shouldCreateAndGetPlanung() throws Exception {

        Long veranstaltungId = createTestVeranstaltung();

        mockMvc.perform(
                        tenantRequest(
                                get("/api/veranstaltungen/{id}/planung", veranstaltungId)
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.veranstaltungId").value(veranstaltungId));
    }

    @Test
    void shouldAddPlanungPosition() throws Exception {

        Long veranstaltungId = createTestVeranstaltung();

        PlanungPositionCreateDTO dto = new PlanungPositionCreateDTO();
        dto.setKategorie(FinanzKategorie.VERPFLEGUNG); // ← wichtig
        dto.setBetrag(new BigDecimal("100.00"));

        mockMvc.perform(
                        tenantRequest(
                                post("/api/veranstaltungen/{id}/planung/positionen", veranstaltungId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                )
                .andExpect(status().isCreated());
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