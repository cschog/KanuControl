package com.kcserver.service.pdf;

import com.kcserver.entity.Planung;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.enumtype.PdfDokumentTyp;
import com.kcserver.service.FoerderService;
import com.kcserver.validation.ValidationResult;
import com.kcserver.repository.*;
import com.kcserver.repository.abrechnung.AbrechnungBelegRepository;
import com.kcserver.repository.abrechnung.AbrechnungBuchungRepository;
import com.kcserver.repository.zahlungsnachweis.ZahlungsnachweisRepository;
import com.kcserver.repository.fahrkosten.ReisekostenabrechnungRepository;
import com.kcserver.service.VereinDataStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.kcserver.dto.validation.DataStatus;
import com.kcserver.dto.validation.VereinDataStatusDTO;


@Service
@RequiredArgsConstructor
public class DokumentValidationService {

    private final VeranstaltungRepository veranstaltungRepository;
    private final TeilnehmerRepository teilnehmerRepository;
    private final PlanungRepository planungRepository;
    private final AbrechnungBuchungRepository abrechnungBuchungRepository;
    private final AbrechnungBelegRepository abrechnungBelegRepository;
    private final ZahlungsnachweisRepository zahlungsnachweisRepository;
    private final ReisekostenabrechnungRepository reisekostenabrechnungRepository;
    private final VereinDataStatusService vereinDataStatusService;
    private final FoerderService foerderService;


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

        ValidationResult result = switch (dokumentTyp) {

            case ANMELDUNG ->
                    validateAnmeldung(veranstaltung);

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

            case REISEKOSTENABRECHNUNG ->
                    validateReisekostenabrechnung(
                            veranstaltung
                    );

            case ZAHLUNGSNACHWEISE ->
                    validateZahlungsnachweise(
                            veranstaltung
                    );

            case BELEGE ->
                    validateBelege(
                            veranstaltung
                    );

            case TEILNEHMER_DATENKONTROLLE ->
                    validateTeilnehmerDatenkontrolle(
                            veranstaltung
                    );

            default ->
                    ValidationResult.valid();
        };

        // Globale Warnungen für alle PDF-Dokumente
        validateVeranstalterWarnings(veranstaltung, result);

        return result;
    }

    private void validateVeranstalterWarnings(
            Veranstaltung veranstaltung,
            ValidationResult result
    ) {

        if (veranstaltung.getVerein() == null) {
            return;
        }

        VereinDataStatusDTO dataStatus =
                vereinDataStatusService.determineStatus(
                        veranstaltung.getVerein(),
                        true
                );

        dataStatus.getFields().forEach((field, status) -> {

            if ("schutzkonzept".equals(field)
                    && status.getStatus() == DataStatus.WARNING) {

                result.addWarning(
                        status.getMessage(),
                        field
                );
            }
        });
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

    private ValidationResult validateAnmeldung(Veranstaltung veranstaltung) {

        ValidationResult result = new ValidationResult();

        validateLeiter(veranstaltung, result);
        validatePlanung(veranstaltung, result);

        if (veranstaltung.getVerein() == null) {
            result.addError(
                    "Kein Verein hinterlegt.",
                    "verein"
            );
            return result;
        }

        VereinDataStatusDTO dataStatus =
                vereinDataStatusService.determineStatus(
                        veranstaltung.getVerein(),
                        true
                );

        dataStatus.getFields().forEach((field, status) -> {

            if (status.getStatus() == DataStatus.ERROR) {
                result.addError(
                        status.getMessage(),
                        field
                );
            }

            if (status.getStatus() == DataStatus.WARNING) {
                result.addWarning(
                        status.getMessage(),
                        field
                );
            }
        });

        return result;
    }

    private void validateVereinDataStatus(
            Veranstaltung veranstaltung,
            List<String> fehler
    ) {

        if (veranstaltung.getVerein() == null) {
            return;
        }

        VereinDataStatusDTO dataStatus =
                vereinDataStatusService.determineStatus(
                        veranstaltung.getVerein(),
                        true
                );

        if (dataStatus.getStatus() == DataStatus.ERROR) {
            fehler.add(
                    "Die Daten des Veranstaltervereins sind noch nicht vollständig."
            );
        }
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
        validateFoerdersatz(veranstaltung, fehler);

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

    private void validateFoerdersatz(
            Veranstaltung veranstaltung,
            List<String> fehler
    ) {

        if (!foerderService.hatGueltigenFoerdersatz(veranstaltung)) {
            fehler.add(
                    "Für die Veranstaltung ist noch kein gültiger "
                            + "Fördersatz hinterlegt. "
                            + "Der KJFP-Zuschuss wurde deshalb vorübergehend "
                            + "mit dem Planungssatz von 10,00 € berechnet. "
                            + "Die PDF-Ausgabe der Abrechnung ist erst möglich, "
                            + "wenn der gültige Fördersatz hinterlegt wurde."
            );
        }
    }

    private ValidationResult validateTeilnehmerDatenkontrolle(
            Veranstaltung veranstaltung
    ) {

        /*
         * Die Datenkontrolle soll gerade dazu dienen,
         * fehlende oder fehlerhafte Teilnehmerdaten
         * auf der Veranstaltung zu erkennen und
         * handschriftlich zu korrigieren.
         *
         * Deshalb werden hier bewusst KEINE
         * Teilnehmerdaten validiert.
         *
         * Es reicht, dass die Veranstaltung existiert.
         */

        return ValidationResult.valid();
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

    private void validateLeiter(
            Veranstaltung veranstaltung,
            ValidationResult result
    ) {

        if (veranstaltung.getLeiter() == null) {
            result.addError(
                    "Kein Leiter hinterlegt.",
                    "leiter"
            );
            return;
        }

        if (isBlank(veranstaltung.getLeiter().getStrasse())) {
            result.addError(
                    "Beim Leiter fehlt die Anschrift.",
                    "leiterStrasse"
            );
        }

        if (isBlank(veranstaltung.getLeiter().getTelefon())) {
            result.addError(
                    "Beim Leiter fehlt die Telefonnummer.",
                    "leiterTelefon"
            );
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
                .filter(Objects::nonNull)
                .forEach(p -> {

                    String name =
                            p.getVorname() + " " + p.getName();

                    if (isBlank(p.getPlz())) {
                        fehler.add(
                                "PLZ fehlt bei " + name
                        );
                    }

                    if (isBlank(p.getOrt())) {
                        fehler.add(
                                "Ort fehlt bei " + name
                        );
                    }
                });
    }

    private void validatePlanung(
            Veranstaltung veranstaltung,
            ValidationResult result
    ) {

        Planung planung =
                planungRepository
                        .findByVeranstaltungIdWithPositionen(
                                veranstaltung.getId()
                        )
                        .orElse(null);

        if (planung == null) {
            result.addError(
                    "Es wurde noch keine Planung erfasst.",
                    "planung"
            );
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
            result.addError(
                    "Es wurden keine geplanten Kosten erfasst.",
                    "planungKosten"
            );
        }

        if (!hatEinnahmen) {
            result.addError(
                    "Es wurden keine geplanten Einnahmen erfasst.",
                    "planungEinnahmen"
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
    private ValidationResult validateBelege(
            Veranstaltung veranstaltung
    ) {

        List<String> fehler = new ArrayList<>();

        if (!abrechnungBelegRepository.existsByVeranstaltungId(
                veranstaltung.getId()
        )) {
            fehler.add(
                    "Es wurden noch keine Belege erfasst."
            );
        }

        return buildResult(fehler);
    }
    private ValidationResult validateReisekostenabrechnung(
            Veranstaltung veranstaltung
    ) {

        List<String> fehler = new ArrayList<>();

        if (!reisekostenabrechnungRepository.existsByVeranstaltungId(
                veranstaltung.getId()
        )) {
            fehler.add(
                    "Es wurde noch keine Fahrkostenabrechnung erfasst."
            );
        }

        return buildResult(fehler);
    }

    private ValidationResult validateZahlungsnachweise(
            Veranstaltung veranstaltung
    ) {

        List<String> fehler = new ArrayList<>();

        if (!zahlungsnachweisRepository.existsByVeranstaltungId(
                veranstaltung.getId()
        )) {
            fehler.add(
                    "Es wurden noch keine Zahlungsnachweise erfasst."
            );
        }

        return buildResult(fehler);
    }

}

