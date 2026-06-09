package com.kcserver.service.pdf;

import com.kcserver.entity.Planung;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.PdfDokumentTyp;
import com.kcserver.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DokumentValidationService {

    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final PlanungRepository planungRepository;
    private final AbrechnungBuchungRepository abrechnungBuchungRepository;

    public ValidationResult validate(
            Long veranstaltungId,
            PdfDokumentTyp dokumentTyp
    ) {

        Veranstaltung veranstaltung =
                veranstaltungRepository
                        .findByIdWithRelations(veranstaltungId)
                        .orElseThrow();

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository
                        .findAllWithPerson(veranstaltungId);

        return switch (dokumentTyp) {

            case ANMELDUNG ->
                    validateAnmeldung(
                            veranstaltung
                    );

            case ERHEBUNGSBOGEN ->
                    validateErhebungsbogen(
                            veranstaltung,
                            teilnehmer
                    );

            case ABRECHNUNG ->
                    validateAbrechnung(
                            veranstaltung,
                            teilnehmer
                    );

            case TEILNEHMERLISTE ->
                    validateTeilnehmerliste(
                            veranstaltung,
                            teilnehmer
                    );

            default ->
                    ValidationResult.valid();
        };
    }

    private ValidationResult validateErhebungsbogen(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer
    ) {

        List<String> fehler = new ArrayList<>();

        validateLeiter(veranstaltung, fehler);
        validateVerein(veranstaltung, fehler);
        validateGeburtsdaten(teilnehmer, fehler);
        validateFoerderfaehigeTeilnehmer(
                veranstaltung,
                teilnehmer,
                fehler
        );

        return buildResult(fehler);
    }

    private ValidationResult validateAnmeldung(
            Veranstaltung veranstaltung

    ) {

        List<String> fehler = new ArrayList<>();

        validateLeiter(veranstaltung, fehler);
        validateVerein(veranstaltung, fehler);
        validatePlanung(veranstaltung, fehler);

        return buildResult(fehler);
    }

    private ValidationResult validateAbrechnung(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer
    ) {

        List<String> fehler = new ArrayList<>();

        validateLeiter(veranstaltung, fehler);
        validateVerein(veranstaltung, fehler);
        validateGeburtsdaten(teilnehmer, fehler);
        validateIstFinanzen(veranstaltung, fehler);
        validateFoerderfaehigeTeilnehmer(
                veranstaltung,
                teilnehmer,
                fehler
        );

        return buildResult(fehler);
    }

    private ValidationResult validateTeilnehmerliste(
            Veranstaltung veranstaltung,
            List<Teilnehmer> teilnehmer
    ) {
        List<String> fehler = new ArrayList<>();

        validateLeiter(veranstaltung, fehler);
        validateVerein(veranstaltung, fehler);
        validateGeburtsdaten(teilnehmer, fehler);
        validateTeilnehmerAnschriften(
                teilnehmer,
                fehler
        );
        validateFoerderfaehigeTeilnehmer(
                veranstaltung,
                teilnehmer,
                fehler
        );

        return buildResult(fehler);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void validateLeiter(
            Veranstaltung veranstaltung,
            List<String> fehler
    ) {

        if (veranstaltung.getLeiter() == null) {
            fehler.add("Kein Leiter hinterlegt.");
            return;
        }

        if (isBlank(veranstaltung.getLeiter().getStrasse())) {
            fehler.add("Beim Leiter fehlt die Anschrift.");
        }

        if (isBlank(veranstaltung.getLeiter().getTelefon())) {
            fehler.add("Beim Leiter fehlt die Telefonnummer.");
        }
    }

    private void validateVerein(
            Veranstaltung veranstaltung,
            List<String> fehler
    ) {

        if (veranstaltung.getVerein() == null) {
            fehler.add("Kein Verein hinterlegt.");
            return;
        }

        if (isBlank(veranstaltung.getVerein().getPlz())) {
            fehler.add("Beim Verein fehlt die PLZ.");
        }
    }

    private ValidationResult buildResult(
            List<String> fehler
    ) {

        return fehler.isEmpty()
                ? ValidationResult.valid()
                : ValidationResult.invalid(fehler);
    }

    private void validateGeburtsdaten(
            List<Teilnehmer> teilnehmer,
            List<String> fehler
    ) {

        long count =
                teilnehmer.stream()
                        .map(Teilnehmer::getPerson)
                        .filter(Objects::nonNull)
                        .filter(p -> p.getGeburtsdatum() == null)
                        .count();

        if (count > 0) {
            fehler.add(count + " Teilnehmer ohne Geburtsdatum.");
        }
    }
    private void validateTeilnehmerAnschriften(
            List<Teilnehmer> teilnehmer,
            List<String> fehler
    ) {

        teilnehmer.stream()
                .map(Teilnehmer::getPerson)
                .filter(p -> isBlank(p.getPlz()))
                .forEach(p ->
                        fehler.add(
                                "PLZ fehlt bei "
                                        + p.getVorname()
                                        + " "
                                        + p.getName()
                        )
                );
    }
    private void validatePlanung(
            Veranstaltung veranstaltung,
            List<String> fehler
    ) {

        Planung planung =
                planungRepository
                        .findByVeranstaltungIdWithPositionen(
                                veranstaltung.getId()
                        )
                        .orElse(null);

        if (planung == null) {
            fehler.add("Es wurde noch keine Planung erfasst.");
            return;
        }

        boolean hatKosten =
                planung.getPositionen().stream()
                        .filter(p -> p.getKategorie().isKosten())
                        .anyMatch(p ->
                                p.getBetrag() != null
                                        && p.getBetrag().signum() > 0
                        );

        boolean hatEinnahmen =
                planung.getPositionen().stream()
                        .filter(p -> p.getKategorie().isEinnahme())
                        .anyMatch(p ->
                                p.getBetrag() != null
                                        && p.getBetrag().signum() > 0
                        );

        if (!hatKosten) {
            fehler.add(
                    "Es wurden keine geplanten Kosten erfasst."
            );
        }

        if (!hatEinnahmen) {
            fehler.add(
                    "Es wurden keine geplanten Einnahmen erfasst."
            );
        }
    }
    private void validateIstFinanzen(
            Veranstaltung veranstaltung,
            List<String> fehler
    ) {

        boolean hatAbrechnungsdaten =
                abrechnungBuchungRepository
                        .existsByBeleg_Abrechnung_Veranstaltung_Id(
                                veranstaltung.getId()
                        );

        if (!hatAbrechnungsdaten) {
            fehler.add("Es wurden noch keine Buchungen erfasst.");
            return;
        }

        boolean hatKosten =
                abrechnungBuchungRepository
                        .existsKosten(
                                veranstaltung.getId()
                        );

        boolean hatEinnahmen =
                abrechnungBuchungRepository
                        .existsEinnahmen(
                                veranstaltung.getId()
                        );

        if (!hatKosten) {
            fehler.add("Es wurden keine Ist-Kosten erfasst.");
        }

        if (!hatEinnahmen) {
            fehler.add("Es wurden keine Ist-Einnahmen erfasst.");
        }
    }
    private void validateFoerderfaehigeTeilnehmer(

            Veranstaltung veranstaltung,

            List<Teilnehmer> teilnehmer,

            List<String> fehler

    ) {

        LocalDate stichtag =
                veranstaltung.getBeginnDatum();

        long anzahlFoerderfaehig =

                teilnehmer.stream()

                        .map(Teilnehmer::getPerson)

                        .filter(Objects::nonNull)

                        .filter(p -> p.getGeburtsdatum() != null)

                        .filter(p -> {

                            int alter =

                                    Period.between(

                                                    p.getGeburtsdatum(),

                                                    stichtag

                                            )

                                            .getYears();

                            return alter >= 6 && alter <= 20;

                        })

                        .count();

        if (anzahlFoerderfaehig < 7) {

            fehler.add(

                    "Es sind nur "

                            + anzahlFoerderfaehig

                            + " förderfähige Teilnehmer vorhanden. "

                            + "Erforderlich sind mindestens 7 Teilnehmer "

                            + "im Alter von 6 bis einschließlich 20 Jahren."

            );

        }

    }
}

