package com.kcserver.veranstaltung;

import com.kcserver.support.api.PersonTestFactory;
import com.kcserver.support.api.VereinTestFactory;
import com.kcserver.support.api.VeranstaltungTestFactory;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.fasterxml.jackson.databind.ObjectMapper;


class VeranstaltungRepositoryTest extends AbstractTenantIntegrationTest {

    VeranstaltungTestFactory veranstaltungFactory;
    VereinTestFactory vereinFactory;
    PersonTestFactory personFactory;

    Long vereinId;
    Long leiterId;

    @Autowired
    private com.kcserver.repository.VeranstaltungRepository veranstaltungRepository;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {

        veranstaltungFactory = new VeranstaltungTestFactory(mockMvc, objectMapper);
        vereinFactory = new VereinTestFactory(mockMvc, objectMapper);
        personFactory = new PersonTestFactory(mockMvc, objectMapper);

        vereinId = vereinFactory.createIfNotExists("EKC_TEST", "Eschweiler Kanu Club");

        leiterId = personFactory.create(b ->
                b.withVorname("Max")
                        .withName("Mustermann")
                        .withGeburtsdatum(java.time.LocalDate.of(1990, 1, 1))
        );
    }



    @Test
    void shouldFilterByName() throws Exception {

        // GIVEN
        veranstaltungFactory.create(vereinId, leiterId, "Kanu Camp");
        veranstaltungFactory.create(vereinId, leiterId, "Wanderung");

        // WHEN
        List<com.kcserver.entity.Veranstaltung> result =
                veranstaltungRepository.findAll((root, query, cb) ->
                        cb.like(cb.lower(root.get("name")), "%kanu%")
                );

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).containsIgnoringCase("Kanu");
    }
}