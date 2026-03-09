package com.kcserver.finanz;

import com.kcserver.entity.FinanzGruppe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FinanzGruppeServiceTest extends AbstractFinanzIntegrationTest {

    @Autowired
    FinanzGruppeService service;

    Long veranstaltungId;

    @BeforeEach
    void setup() {
        veranstaltungId = createTestVeranstaltung();
    }

    @Test
    void shouldCreateGroup() {

        FinanzGruppe g =
                service.create(veranstaltungId, "MS");

        assertThat(g.getId()).isNotNull();
        assertThat(g.getKuerzel()).isEqualTo("MS");
    }

    @Test
    void shouldNotAllowDuplicate() {

        service.create(veranstaltungId, "MS");

        assertThatThrownBy(() ->
                service.create(veranstaltungId, "MS"))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void shouldUpdateGroup() {

        FinanzGruppe g =
                service.create(veranstaltungId, "MS");

        FinanzGruppe updated =
                service.update(veranstaltungId, g.getId(), "CS");

        assertThat(updated.getKuerzel()).isEqualTo("CS");
    }

    @Test
    void shouldDeleteGroup() {

        FinanzGruppe g =
                service.create(veranstaltungId, "MS");

        service.delete(veranstaltungId, g.getId());

        assertThatThrownBy(() ->
                service.delete(veranstaltungId, g.getId()))
                .isInstanceOf(ResponseStatusException.class);
    }
}
