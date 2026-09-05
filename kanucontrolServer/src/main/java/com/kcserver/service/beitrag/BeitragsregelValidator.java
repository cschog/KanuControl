package com.kcserver.service.beitrag;

import com.kcserver.entity.Beitragsregel;
import com.kcserver.exception.ErrorMessages;
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

        if (regeln == null || regeln.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.MINDESTENS_EINE_BEITRAGSREGEL_REQUIRED
            );
        }

        Map<String, List<Beitragsregel>> gruppiert =
                regeln.stream()
                        .collect(Collectors.groupingBy(r ->
                                r.getRolle() == null
                                        ? "TN"
                                        : r.getRolle().name()
                        ));

        for (List<Beitragsregel> gruppe : gruppiert.values()) {

            List<Beitragsregel> sorted = gruppe.stream()
                    .sorted(Comparator.comparing(Beitragsregel::getSortierung))
                    .toList();

            boolean offeneRegelGefunden = false;

            int lastBis = -1;

            for (int i = 0; i < sorted.size(); i++) {

                Beitragsregel r = sorted.get(i);

                if (r.getSortierung() == null) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            ErrorMessages.SORTIERUNG_REQUIRED
                    );
                }

                Integer bis = r.getAlterBis();

                // =========================
                // NEGATIV
                // =========================

                if (bis != null && bis < 0) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                           ErrorMessages.ALTER_BIS_MUST_NOT_BE_NEGATIVE
                    );
                }

                // =========================
                // OFFENE REGEL
                // =========================

                if (bis == null) {

                    if (offeneRegelGefunden) {

                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                ErrorMessages.ONLY_ONE_OPEN_AGE_RULE
                        );
                    }

                    offeneRegelGefunden = true;

                    // offene Regel muss letzte sein
                    if (i < sorted.size() - 1) {

                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                               ErrorMessages.OPEN_AGE_RULE_MUST_BE_LAST
                        );
                    }

                    continue;
                }

                // =========================
                // REIHENFOLGE
                // =========================

                if (bis <= lastBis) {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                           ErrorMessages.ALTER_BIS_MUST_BE_ASCENDING
                    );
                }

                lastBis = bis;
            }

            // =========================
            // LETZTE MUSS OFFEN SEIN
            // =========================

            if (!offeneRegelGefunden) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                       ErrorMessages.LAST_BEITRAGSREGEL_MUST_BE_OPEN
                );
            }
        }
    }
}