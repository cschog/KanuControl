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
                .sorted(Comparator.comparing(Beitragsregel::getSortierung))
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
}