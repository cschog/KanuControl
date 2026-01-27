package com.kcserver.person;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.PersonDTO;
import com.kcserver.dto.PersonSaveDTO;
import com.kcserver.enumtype.Sex;
import com.kcserver.integration.support.AbstractTenantIntegrationTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for creating Person entities.
 * Verifies validation rules and business constraints,
 * especially duplicate prevention based on
 * (vorname, name, geburtsdatum).
 */

@Tag("person-crud")
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class PersonCreateTest extends AbstractTenantIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired(required = false)
    AuditorAware<String> auditorAware;

    @Test
    void auditCheck() {
        System.out.println("Auditor bean = " + auditorAware);
        System.out.println("Auditor value = " +
                (auditorAware == null ? "NULL" : auditorAware.getCurrentAuditor()));
    }

    @Test
    void createPerson_withMinimalValidData_returns201AndId() throws Exception {

        // given
        PersonDTO dto = new PersonDTO();
        dto.setVorname("Max");
        dto.setName("Mustermann");
        dto.setSex(Sex.MAENNLICH);
        dto.setGeburtsdatum(LocalDate.of(1990, 1, 1));

        // when / then
        mockMvc.perform(
                        tenantRequest(
                                post("/api/person")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.vorname").value("Max"))
                .andExpect(jsonPath("$.name").value("Mustermann"));
    }
    @Test
    void createPerson_sameNameAndBirthdate_twice_returns409() throws Exception {

        PersonDTO dto = new PersonDTO();
        dto.setVorname("Max");
        dto.setName("Mustermann");
        dto.setSex(Sex.MAENNLICH);
        dto.setGeburtsdatum(LocalDate.of(1990, 1, 1));

        // 1️⃣ erster Create → OK
        mockMvc.perform(
                        post("/api/person")
                                .with(jwt().jwt(jwt -> jwt.claim("tenant", "test")))
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated());

        // 2️⃣ gleicher Create → ❌ Conflict
        mockMvc.perform(
                        post("/api/person")
                                .with(jwt().jwt(jwt -> jwt.claim("tenant", "test")))
                                .with(jwt())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isConflict());
    }
    @Test
    void createPerson_sameNameDifferentBirthdate_isAllowed() throws Exception {

        PersonDTO p1 = new PersonDTO();
        p1.setVorname("Max");
        p1.setName("Mustermann");
        p1.setSex(Sex.MAENNLICH);
        p1.setGeburtsdatum(LocalDate.of(1990, 1, 1));

        PersonDTO p2 = new PersonDTO();
        p2.setVorname("Max");
        p2.setName("Mustermann");
        p2.setSex(Sex.MAENNLICH);
        p2.setGeburtsdatum(LocalDate.of(1995, 1, 1));

        mockMvc.perform(post("/api/person")
                        .with(jwt().jwt(jwt -> jwt.claim("tenant", "test")))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/person")
                        .with(jwt().jwt(jwt -> jwt.claim("tenant", "test")))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p2)))
                .andExpect(status().isCreated());
    }
    @Test
    void createPerson_sameNameWithoutBirthdate_isAllowedMultipleTimes() throws Exception {

        PersonDTO dto = new PersonDTO();
        dto.setVorname("Anna");
        dto.setName("Schmidt");
        dto.setSex(Sex.WEIBLICH);
        dto.setGeburtsdatum(null);

        mockMvc.perform(post("/api/person")
                        .with(jwt().jwt(jwt -> jwt.claim("tenant", "test")))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/person")
                        .with(jwt().jwt(jwt -> jwt.claim("tenant", "test")))
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void createPerson_returnsPersonDetailDTO() throws Exception {

        PersonSaveDTO dto = new PersonSaveDTO();
        dto.setVorname("Lisa");
        dto.setName("Schmidt");
        dto.setSex(Sex.WEIBLICH);
        dto.setGeburtsdatum(LocalDate.of(2001, 3, 3));

        mockMvc.perform(
                        tenantRequest(
                                post("/api/person")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.mitgliedschaften").isArray());
    }
}