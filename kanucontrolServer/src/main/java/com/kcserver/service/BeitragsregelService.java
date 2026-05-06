package com.kcserver.service;

import com.kcserver.entity.Beitragsregel;
import com.kcserver.entity.Beitragsstruktur;
import com.kcserver.enumtype.TeilnehmerRolle;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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

        return struktur.getRegeln().stream()

                // Alter match
                .filter(r ->
                        (r.getAlterVon() == null || alter >= r.getAlterVon()) &&
                                (r.getAlterBis() == null || alter <= r.getAlterBis())
                )

                // Rolle match
                .filter(r ->
                        r.getRolle() == null || r.getRolle() == rolle
                )

                // 🔥 Wichtig: spezifischste Regel gewinnt
                .min(Comparator.comparingInt(r ->
                        (r.getRolle() == null ? 1 : 0) +
                                (r.getAlterVon() == null ? 1 : 0)
                ));
    }
}