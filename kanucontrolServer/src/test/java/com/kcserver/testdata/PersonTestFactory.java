package com.kcserver.testdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcserver.dto.mitglied.MitgliedDTO;
import com.kcserver.dto.person.PersonDTO;
import com.kcserver.enumtype.MitgliedFunktion;
import com.kcserver.enumtype.Sex;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tenant-agnostische Test-Factory für Personen inkl. Mitgliedschaft
 */
public class PersonTestFactory {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final RequestPostProcessor tenant;

    public PersonTestFactory(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            RequestPostProcessor tenant
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.tenant = tenant;
    }

    /* =========================================================
       Create or Reuse (Basis)
       ========================================================= */

    public Long createOrReuse(
            String vorname,
            String name,
            LocalDate geburtsdatum,
            Long vereinId
    ) throws Exception {

        Optional<Long> existing =
                findPersonId(vorname, name, geburtsdatum, vereinId);

        if (existing.isPresent()) {
            return existing.get();
        }

        return createPerson(vorname, name, geburtsdatum, vereinId);
    }

    /* =========================================================
       Create or Reuse (mit Ort & PLZ)
       ========================================================= */

    public Long createOrReuse(
            String vorname,
            String name,
            LocalDate geburtsdatum,
            Long vereinId,
            String ort,
            String plz
    ) throws Exception {

        PersonDTO dto = basePerson(vorname, name, geburtsdatum);

        if (vereinId != null) {
            dto.setMitgliedschaften(List.of(defaultMitglied(vereinId)));
        }
        dto.setOrt(ort);
        dto.setPlz(plz);

        if (vereinId != null) {
            dto.setMitgliedschaften(List.of(defaultMitglied(vereinId)));
        }

        MvcResult result =
                mockMvc.perform(
                                post("/api/person")
                                        .with(tenant)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                        .andExpect(status().isCreated())
                        .andReturn();

        return objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("id")
                .asLong();
    }

    /* =========================================================
       Create Person (immer neu)
       ========================================================= */

    public Long createPerson(
            String vorname,
            String name,
            LocalDate geburtsdatum,
            Long vereinId
    ) throws Exception {

        PersonDTO dto = basePerson(vorname, name, geburtsdatum);
        if (vereinId != null) {
            dto.setMitgliedschaften(List.of(defaultMitglied(vereinId)));
        }

        MvcResult result =
                mockMvc.perform(
                                post("/api/person")
                                        .with(tenant)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(dto))
                        )
                        .andExpect(status().isCreated())
                        .andReturn();

        return objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("id")
                .asLong();
    }

    /* =========================================================
       Suche bestehende Person
       ========================================================= */

    private Optional<Long> findPersonId(
            String vorname,
            String name,
            LocalDate geburtsdatum,
            Long vereinId
    ) throws Exception {

        var request = get("/api/person/search")
                .with(tenant)
                .param("vorname", vorname)
                .param("name", name);

        if (vereinId != null) {
            request = request.param("vereinId", vereinId.toString());
        }

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var json = objectMapper.readTree(result.getResponse().getContentAsString());

        if (!json.isArray() || json.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(json.get(0).get("id").asLong());
    }

    /* =========================================================
       Helpers
       ========================================================= */

    private PersonDTO basePerson(
            String vorname,
            String name,
            LocalDate geburtsdatum
    ) {
        PersonDTO dto = new PersonDTO();
        dto.setVorname(vorname);
        dto.setName(name);
        dto.setGeburtsdatum(geburtsdatum);
        dto.setSex(Sex.WEIBLICH);
        dto.setAktiv(true);
        return dto;
    }

    private MitgliedDTO defaultMitglied(Long vereinId) {
        if (vereinId == null) {
            throw new IllegalArgumentException(
                    "defaultMitglied darf nicht mit vereinId=null aufgerufen werden"
            );
        }

        MitgliedDTO mitglied = new MitgliedDTO();
        mitglied.setVereinId(vereinId);
        mitglied.setHauptVerein(true);
        mitglied.setFunktion(MitgliedFunktion.JUGENDWART);
        return mitglied;
    }
}