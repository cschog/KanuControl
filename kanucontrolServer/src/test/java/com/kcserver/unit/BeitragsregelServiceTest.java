package com.kcserver.unit;

import com.kcserver.entity.Beitragsregel;
import com.kcserver.entity.Beitragsstruktur;
import com.kcserver.enumtype.TeilnehmerRolle;
import com.kcserver.service.beitrag.BeitragsregelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BeitragsregelServiceTest {

    private final BeitragsregelService service =
            new BeitragsregelService();

    @Test
    void findPlanungsRegelTeilnehmer_foerderalterLiegtInAltersstufe() {

        Beitragsstruktur struktur = new Beitragsstruktur();

        Beitragsregel r1 = new Beitragsregel();
        r1.setSortierung(0);
        r1.setAlterBis(2);
        r1.setBeitrag(BigDecimal.ZERO);

        Beitragsregel r2 = new Beitragsregel();
        r2.setSortierung(1);
        r2.setAlterBis(10);
        r2.setBeitrag(BigDecimal.valueOf(50));

        Beitragsregel r3 = new Beitragsregel();
        r3.setSortierung(2);
        r3.setAlterBis(22);
        r3.setBeitrag(BigDecimal.valueOf(150));

        Beitragsregel r4 = new Beitragsregel();
        r4.setSortierung(3);
        r4.setAlterBis(null);
        r4.setBeitrag(BigDecimal.valueOf(250));

        struktur.setRegeln(List.of(r1, r2, r3, r4));

        Optional<Beitragsregel> regel =
                service.findPlanungsRegelTeilnehmer(
                        struktur,
                        20
                );

        assertThat(regel).isPresent();

        assertThat(regel.get().getBeitrag())
                .isEqualByComparingTo("150.00");
    }

    @Test
    void findPlanungsRegelMitarbeiter_verwendetMitarbeiterregel() {

        Beitragsstruktur struktur = new Beitragsstruktur();

        Beitragsregel standard1 = new Beitragsregel();
        standard1.setSortierung(0);
        standard1.setAlterBis(20);
        standard1.setBeitrag(BigDecimal.valueOf(100));

        Beitragsregel standard2 = new Beitragsregel();
        standard2.setSortierung(1);
        standard2.setAlterBis(null);
        standard2.setBeitrag(BigDecimal.valueOf(300));

        Beitragsregel mitarbeiter = new Beitragsregel();
        mitarbeiter.setSortierung(0);
        mitarbeiter.setRolle(TeilnehmerRolle.MITARBEITER);
        mitarbeiter.setAlterBis(null);
        mitarbeiter.setBeitrag(BigDecimal.valueOf(80));

        struktur.setRegeln(List.of(
                standard1,
                standard2,
                mitarbeiter
        ));

        Optional<Beitragsregel> regel =
                service.findPlanungsRegelMitarbeiter(struktur);

        assertThat(regel).isPresent();

        assertThat(regel.get().getRolle())
                .isEqualTo(TeilnehmerRolle.MITARBEITER);

        assertThat(regel.get().getBeitrag())
                .isEqualByComparingTo("80.00");
    }

    private Beitragsregel regel(
            int sortierung,
            Integer alterBis,
            TeilnehmerRolle rolle,
            int beitrag
    ) {

        Beitragsregel r = new Beitragsregel();
        r.setSortierung(sortierung);
        r.setAlterBis(alterBis);
        r.setRolle(rolle);
        r.setBeitrag(BigDecimal.valueOf(beitrag));

        return r;
    }

    @Test
    void findPlanungsRegelMitarbeiter_verwendetHoechstenStandardbeitrag() {

        Beitragsstruktur struktur = new Beitragsstruktur();

        struktur.setRegeln(List.of(
                regel(0, 2, null, 0),
                regel(1, 10, null, 50),
                regel(2, 20, null, 100),
                regel(3, null, null, 300)
        ));

        Optional<Beitragsregel> regel =
                service.findPlanungsRegelMitarbeiter(struktur);

        assertThat(regel).isPresent();

        assertThat(regel.get().getRolle()).isNull();

        assertThat(regel.get().getBeitrag())
                .isEqualByComparingTo("300.00");
    }

    @Test
    void findPlanungsRegelTeilnehmer_verwendetOffeneLetzteRegel() {

        Beitragsstruktur struktur = new Beitragsstruktur();

        struktur.setRegeln(List.of(
                regel(0, 2, null, 0),
                regel(1, 10, null, 50),
                regel(2, null, null, 200)
        ));

        Optional<Beitragsregel> regel =
                service.findPlanungsRegelTeilnehmer(
                        struktur,
                        20
                );

        assertThat(regel).isPresent();

        assertThat(regel.get().getBeitrag())
                .isEqualByComparingTo("200.00");
    }

    @Test
    void findPlanungsRegelTeilnehmer_keineBeitragsstruktur() {

        Optional<Beitragsregel> regel =
                service.findPlanungsRegelTeilnehmer(
                        null,
                        20
                );

        assertThat(regel).isEmpty();
    }

    @Test
    void findPlanungsRegelTeilnehmer_keineRegeln() {

        Beitragsstruktur struktur = new Beitragsstruktur();
        struktur.setRegeln(null);

        Optional<Beitragsregel> regel =
                service.findPlanungsRegelTeilnehmer(
                        struktur,
                        20
                );

        assertThat(regel).isEmpty();
    }

    @Test
    void findPlanungsRegelTeilnehmer_leereRegeln() {

        Beitragsstruktur struktur = new Beitragsstruktur();
        struktur.setRegeln(List.of());

        Optional<Beitragsregel> regel =
                service.findPlanungsRegelTeilnehmer(
                        struktur,
                        20
                );

        assertThat(regel).isEmpty();
    }
    @Test
    void findPlanungsRegelMitarbeiter_keineRegeln() {

        Beitragsstruktur struktur = new Beitragsstruktur();
        struktur.setRegeln(List.of());

        Optional<Beitragsregel> regel =
                service.findPlanungsRegelMitarbeiter(struktur);

        assertThat(regel).isEmpty();
    }

    @Test
    void findPlanungsRegelMitarbeiter_nurMitarbeiterregel() {

        Beitragsstruktur struktur = new Beitragsstruktur();

        struktur.setRegeln(List.of(
                regel(
                        0,
                        null,
                        TeilnehmerRolle.MITARBEITER,
                        75
                )
        ));

        Optional<Beitragsregel> regel =
                service.findPlanungsRegelMitarbeiter(struktur);

        assertThat(regel).isPresent();

        assertThat(regel.get().getBeitrag())
                .isEqualByComparingTo("75.00");
    }
}

