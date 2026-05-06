package com.kcserver.service;

import com.kcserver.entity.Beitragsregel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.Comparator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BeitragsregelValidator {

    public void validate(List<Beitragsregel> regeln) {

        Map<String, List<Beitragsregel>> gruppiert =
                regeln.stream()
                        .collect(Collectors.groupingBy(r ->
                                r.getRolle() == null
                                        ? "TN"
                                        : r.getRolle().name()
                        ));

        for (List<Beitragsregel> gruppe : gruppiert.values()) {

            // 1. Sortieren
            gruppe.sort(
                    Comparator.comparing(r ->
                            safeMin(r.getAlterVon())
                    )
            );

            // 2. Überlappung prüfen
            for (int i = 0; i < gruppe.size(); i++) {

                for (int j = i + 1; j < gruppe.size(); j++) {

                    Beitragsregel r1 = gruppe.get(i);
                    Beitragsregel r2 = gruppe.get(j);

                    if (altersbereichUeberlappt(r1, r2)) {

                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Überlappende Beitragsregeln"
                        );
                    }
                }
            }

            // 3. Lücken prüfen
            pruefeKeineLuecken(gruppe);
        }
    }

    /* =========================
       ALTER CHECK
       ========================= */

    private boolean altersbereichUeberlappt(Beitragsregel r1, Beitragsregel r2) {

        int von1 = safeMin(r1.getAlterVon());
        int bis1 = safeMax(r1.getAlterBis());

        int von2 = safeMin(r2.getAlterVon());
        int bis2 = safeMax(r2.getAlterBis());

        return von1 <= bis2 && von2 <= bis1;
    }

    private int safeMin(Integer v) {
        return v != null ? v : 0;
    }

    private int safeMax(Integer v) {
        return v != null ? v : Integer.MAX_VALUE;
    }

    private void pruefeKeineLuecken(List<Beitragsregel> regeln) {

        // Nur Regeln ohne Rolle (Basis-Regeln!)
        List<Beitragsregel> basis = regeln.stream()
                .filter(r -> r.getRolle() == null)
                .sorted(Comparator.comparing(r -> safeMin(r.getAlterVon())))
                .toList();

        int expected = 0;

        for (Beitragsregel r : basis) {

            int von = safeMin(r.getAlterVon());
            int bis = safeMax(r.getAlterBis());

            if (von > expected) {
                throw new IllegalStateException(
                        "Lücke in Beitragsregeln bei Alter " + expected
                );
            }

            expected = bis + 1;
        }
    }
}