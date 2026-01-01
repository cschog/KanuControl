package com.kcserver;

import com.kcserver.entity.Erhebungsbogen;
import com.kcserver.entity.Person;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.Sex;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.repository.ErhebungsbogenRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.VeranstaltungRepository;
import com.kcserver.service.ErhebungsbogenServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErhebungsbogenServiceImplTest {

    @Mock
    private ErhebungsbogenRepository erhebungsbogenRepository;

    @Mock
    private VeranstaltungRepository veranstaltungRepository;

    @Mock
    private TeilnehmerRepository teilnehmerRepository;

    @InjectMocks
    private ErhebungsbogenServiceImpl service;

    @Test
    void altersgrenzen_muessen_korrekt_zugeordnet_werden() {

        // GIVEN
        LocalDate stichtag = LocalDate.of(2025, 7, 1);

        Veranstaltung v = new Veranstaltung();
        v.setId(1L);
        v.setBeginnDatum(stichtag);

        when(veranstaltungRepository.findById(1L))
                .thenReturn(Optional.of(v));

        Erhebungsbogen bogen = new Erhebungsbogen();
        bogen.setVeranstaltung(v);
        bogen.setStichtag(stichtag);
        bogen.setAbgeschlossen(false);

        when(erhebungsbogenRepository.findByVeranstaltung(v))
                .thenReturn(Optional.of(bogen));

        when(erhebungsbogenRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Teilnehmer> teilnehmer = List.of(
                teilnehmer("2019-07-01"), // genau 6
                teilnehmer("2012-07-01"), // genau 13
                teilnehmer("2011-07-01"), // genau 14
                teilnehmer("2007-07-01"), // genau 18
                teilnehmer("2020-07-01")  // unter 6
        );

        when(teilnehmerRepository.findByVeranstaltung(v))
                .thenReturn(teilnehmer);

        // WHEN
        Erhebungsbogen result = service.berechneStatistik(1L);

        // THEN
        assertThat(result.getTeilnehmerMaennlichU6()).isEqualTo(1);
        assertThat(result.getTeilnehmerMaennlich6_13()).isEqualTo(2);
        assertThat(result.getTeilnehmerMaennlich14_17()).isEqualTo(1);
        assertThat(result.getTeilnehmerMaennlich18Plus()).isEqualTo(1);
    }

    private Teilnehmer teilnehmer(String geburtsdatum) {
        Person p = new Person();
        p.setGeburtsdatum(LocalDate.parse(geburtsdatum));
        p.setSex(Sex.MAENNLICH);

        Teilnehmer t = new Teilnehmer();
        t.setPerson(p);
        t.setRolle(TeilnehmerRolle.TEILNEHMER);
        return t;
    }
}