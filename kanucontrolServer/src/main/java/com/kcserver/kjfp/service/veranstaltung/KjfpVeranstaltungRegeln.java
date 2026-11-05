package com.kcserver.kjfp.service.veranstaltung;

import com.kcserver.core.entity.Veranstaltung;
import com.kcserver.core.entity.Person;
import com.kcserver.core.exception.ErrorMessages;
import com.kcserver.kjfp.enumtype.VeranstaltungTyp;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
public class KjfpVeranstaltungRegeln {


    public List<String> savedKjfpWarnings(Veranstaltung v) {
        return v.isKjfpAktiv() ? adjustFmJemType(v) : List.of();
    }

    public void validateLeiterAge(Person person) {
        if (person.getGeburtsdatum() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.VERANSTALTUNGSLEITER_GEBURTSDATUM_REQUIRED
            );
        }

        if (person.getGeburtsdatum().plusYears(18).isAfter(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ErrorMessages.VERANSTALTUNGSLEITER_MIND_ALTER
            );
        }
    }

    private List<String> adjustFmJemType(Veranstaltung veranstaltung) {
        List<String> warnings = new ArrayList<>();

        if (veranstaltung.getTyp() != VeranstaltungTyp.FM
                && veranstaltung.getTyp() != VeranstaltungTyp.JEM) {
            return warnings;
        }

        if (veranstaltung.getBeginnDatum() == null || veranstaltung.getEndeDatum() == null) {
            return warnings;
        }

        long dauerInTagen = ChronoUnit.DAYS.between(
                veranstaltung.getBeginnDatum(),
                veranstaltung.getEndeDatum()
        ) + 1;

        if (dauerInTagen <= 0) {
            warnings.add(
                    "Das Enddatum liegt vor dem Beginndatum."
            );
            return warnings;
        }

        if (dauerInTagen <= 4) {
            veranstaltung.setTyp(VeranstaltungTyp.FM);
        } else {
            veranstaltung.setTyp(VeranstaltungTyp.JEM);

            if (dauerInTagen > 21) {
                warnings.add(
                        "Die Veranstaltung dauert " + dauerInTagen
                                + " Tage. FM/JEM-Veranstaltungen dürfen maximal 21 Tage dauern." +
                                "Für die Förderung werden 21 Tage berücksichtigt."
                );
            }
        }


        return warnings;
    }
}
