package com.kcserver.service;

import com.kcserver.entity.Beitragsregel;
import com.kcserver.entity.Beitragsstruktur;
import com.kcserver.enumtype.TeilnehmerRolle;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class BeitragsregelService {

    public Optional<Beitragsregel> findPassendeRegel(
            Beitragsstruktur struktur,
            int alter,
            TeilnehmerRolle rolle
    ) {

        if (struktur == null || struktur.getRegeln() == null) {
            return Optional.empty();
        }

        List<Beitragsregel> regeln = struktur.getRegeln()
                .stream()
                .sorted(
                        Comparator
                                .comparing(
                                        Beitragsregel::getSortierung,
                                        Comparator.nullsLast(Integer::compareTo)
                                )
                                .thenComparing(
                                        regel -> regel.getRolle() == null ? 1 : 0
                                )
                                .thenComparing(
                                        Beitragsregel::getAlterBis,
                                        Comparator.nullsLast(Integer::compareTo)
                                )
                )
                .toList();

        int alterVon = 0;

        for (Beitragsregel regel : regeln) {

            Integer alterBis = regel.getAlterBis();

            boolean alterMatch =
                    alter >= alterVon &&
                            (alterBis == null || alter <= alterBis);

            boolean rollenMatch =
                    regel.getRolle() == null ||
                            regel.getRolle() == rolle;

            if (alterMatch && rollenMatch) {
                return Optional.of(regel);
            }

            // nächste Regel beginnt nach alterBis
            if (alterBis != null) {
                alterVon = alterBis + 1;
            }
        }

        return Optional.empty();
    }

    public Optional<Beitragsregel> findPlanungsRegelTeilnehmer(
            Beitragsstruktur struktur,
            int foerderHoechstalter
    ) {

        if (struktur == null || struktur.getRegeln() == null) {
            return Optional.empty();
        }

        return struktur.getRegeln()
                .stream()
                // nur Standardregeln (Teilnehmer)
                .filter(r -> r.getRolle() == null)
                .sorted(
                        Comparator.comparing(
                                Beitragsregel::getSortierung,
                                Comparator.nullsLast(Integer::compareTo)
                        )
                )
                // erste Altersstufe, die das Förderhöchstalter einschließt
                .filter(r ->
                        r.getAlterBis() == null ||
                                r.getAlterBis() >= foerderHoechstalter
                )
                .findFirst();
    }

    private Optional<Beitragsregel> findRegelFuerRolleOderStandard(
            Beitragsstruktur struktur,
            TeilnehmerRolle rolle
    ) {

        if (struktur == null || struktur.getRegeln() == null) {
            return Optional.empty();
        }

        // 1. Rollenregel suchen
        Optional<Beitragsregel> rollenRegel =
                struktur.getRegeln()
                        .stream()
                        .filter(r -> r.getRolle() == rolle)
                        .sorted(Comparator.comparing(
                                Beitragsregel::getSortierung,
                                Comparator.nullsLast(Integer::compareTo)
                        ))
                        .findFirst();

        if (rollenRegel.isPresent()) {
            return rollenRegel;
        }

        // 2. Höchste Standardregel
        return struktur.getRegeln()
                .stream()
                .filter(r -> r.getRolle() == null)
                .max(Comparator.comparing(
                        Beitragsregel::getBeitrag
                ));
    }

    public Optional<Beitragsregel> findPlanungsRegelMitarbeiter(
            Beitragsstruktur struktur
    ) {
        return findRegelFuerRolleOderStandard(
                struktur,
                TeilnehmerRolle.MITARBEITER
        );
    }

    public Optional<Beitragsregel> findPlanungsRegelLeiter(
            Beitragsstruktur struktur
    ) {
        return findRegelFuerRolleOderStandard(
                struktur,
                TeilnehmerRolle.LEITER
        );
    }
}