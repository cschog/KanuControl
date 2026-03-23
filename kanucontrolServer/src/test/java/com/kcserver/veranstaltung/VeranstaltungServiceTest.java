package com.kcserver.veranstaltung;

import com.kcserver.dto.veranstaltung.VeranstaltungDetailDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungFilterDTO;
import com.kcserver.dto.veranstaltung.VeranstaltungListDTO;
import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.entity.Verein;
import com.kcserver.enumtype.VeranstaltungTyp;
import com.kcserver.repository.PersonRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.repository.VereinRepository;
import com.kcserver.service.VeranstaltungService;
import com.kcserver.support.builder.PersonBuilder;
import com.kcserver.support.builder.VeranstaltungBuilder;
import com.kcserver.support.builder.VeranstaltungCreateDTOBuilder;
import com.kcserver.support.builder.VereinBuilder;
import com.kcserver.support.tenant.AbstractTenantIntegrationTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Transactional
class VeranstaltungServiceTest extends AbstractTenantIntegrationTest {

    @Autowired
    private VeranstaltungService veranstaltungService;

    @Autowired
    private VeranstaltungRepository veranstaltungRepository;

    @Autowired
    private TeilnehmerRepository teilnehmerRepository;

    @Autowired
    private VereinRepository vereinRepository;

    @Autowired
    private PersonRepository personRepository;

    /* =========================================================
       RESET (🔥 wichtig für stabile Tests)
       ========================================================= */

    @BeforeEach
    void resetBuilder() {
        VereinBuilder.resetCounter();
        PersonBuilder.resetCounter();
    }

    /* =========================================================
       HELPERS
       ========================================================= */

    private Verein createVerein() {
        return vereinRepository.save(
                VereinBuilder.aVerein().build()
        );
    }

    private Person createPerson() {
        return personRepository.save(
                PersonBuilder.aPerson().build()
        );
    }

    private Veranstaltung createVeranstaltung(String name, boolean aktiv) {
        Verein verein = createVerein();
        Person leiter = createPerson();

        return veranstaltungRepository.save(
                VeranstaltungBuilder.aVeranstaltung()
                        .withName(name)
                        .withTyp(VeranstaltungTyp.JEM)
                        .withVerein(verein)
                        .withLeiter(leiter)
                        .withBeginnDatum(LocalDate.now())
                        .withEndeDatum(LocalDate.now().plusDays(5))
                        .withBeginnZeit(java.time.LocalTime.of(10, 0))
                        .withEndeZeit(java.time.LocalTime.of(18, 0))
                        .withTyp(VeranstaltungTyp.JEM)
                        .build()
        );
    }

    private Teilnehmer createTeilnehmer(Veranstaltung v) {
        Person p = createPerson();

        Teilnehmer t = new Teilnehmer();
        t.setVeranstaltung(v);
        t.setPerson(p); // 🔥 Pflichtfeld!

        return teilnehmerRepository.save(t);
    }

    /* =========================================================
       TESTS
       ========================================================= */

    @Test
    void shouldReturnAllWhenNoFilter() {

        createVeranstaltung("Test Event", true);

        List<VeranstaltungListDTO> result =
                veranstaltungService.getAll();

        assertThat(result).isNotEmpty();
    }

    @Test
    void shouldFilterByName() {

        Verein verein = createVerein();
        Person leiter = createPerson();

        createVeranstaltung("Kanu Camp");
        createVeranstaltung("Winterlager");

        VeranstaltungFilterDTO filter = new VeranstaltungFilterDTO();
        filter.setName("kanu");

        List<VeranstaltungListDTO> result =
                veranstaltungService.searchAll(filter);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldOnlyHaveOneActiveVeranstaltung() {

        Verein verein = createVerein();
        Person leiter = createPerson();

        var v1 = createVeranstaltungDTO("Event 1", verein, leiter);
        var v2 = createVeranstaltungDTO("Event 2", verein, leiter);

        List<Veranstaltung> all = veranstaltungRepository.findAll();

        assertThat(all)
                .filteredOn(Veranstaltung::isAktiv)
                .hasSize(1);

        assertThat(
                all.stream()
                        .filter(Veranstaltung::isAktiv)
                        .findFirst()
                        .orElseThrow()
                        .getId()
        ).isEqualTo(v2.getId());
    }

    private VeranstaltungDetailDTO createVeranstaltungDTO(
            String name,
            Verein verein,
            Person leiter
    ) {
        return veranstaltungService.create(
                VeranstaltungCreateDTOBuilder.aDTO()
                        .withName(name)
                        .withVerein(verein.getId())
                        .withLeiter(leiter.getId())
                        .build()
        );
    }

    @Test
    void shouldFilterByAktivAndVerein() {

        Verein verein = createVerein();
        Person leiter = createPerson();

        Veranstaltung aktiv = veranstaltungRepository.save(
                VeranstaltungBuilder.aVeranstaltung()
                        .withName("Aktiv")
                        .withTyp(VeranstaltungTyp.JEM)
                        .withVerein(verein)
                        .withLeiter(leiter)
                        .active()
                        .build()
        );

        veranstaltungRepository.save(
                VeranstaltungBuilder.aVeranstaltung()
                        .withName("Inaktiv")
                        .withTyp(VeranstaltungTyp.JEM)
                        .withVerein(verein)
                        .withLeiter(leiter)
                        .inactive()
                        .build()
        );

        VeranstaltungFilterDTO filter = new VeranstaltungFilterDTO();
        filter.setAktiv(true);
        filter.setVereinId(verein.getId());

        List<VeranstaltungListDTO> result =
                veranstaltungService.searchAll(filter);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId())
                .isEqualTo(aktiv.getId());
    }

    @Test
    void shouldNotDeleteWhenTeilnehmerExist() {

        Veranstaltung v = createVeranstaltung("Event", true);

        createTeilnehmer(v);

        assertThatThrownBy(() ->
                veranstaltungService.delete(v.getId())
        ).isInstanceOf(ResponseStatusException.class);
    }
    private VeranstaltungDetailDTO createVeranstaltung(String name) {
        Verein v = createVerein();
        Person p = createPerson();

        return veranstaltungService.create(
                VeranstaltungCreateDTOBuilder.aDTO()
                        .withName(name)
                        .withVerein(v.getId())
                        .withLeiter(p.getId())
                        .build()
        );
    }
}