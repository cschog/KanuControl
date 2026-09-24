package com.kcserver.service.finanz;

import com.kcserver.dto.finanzen.FinanzausgleichDTO;
import com.kcserver.entity.FinanzGruppe;
import com.kcserver.entity.Teilnehmer;
import com.kcserver.entity.Veranstaltung;
import com.kcserver.repository.finanz.FinanzGruppeRepository;
import com.kcserver.repository.abrechnung.AbrechnungBuchungRepository;
import com.kcserver.repository.zahlungsnachweis.ZahlungsnachweisRepository;
import com.kcserver.repository.fahrkosten.ReisekostenabrechnungRepository;
import com.kcserver.service.beitrag.TeilnehmerBeitragService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kcserver.exception.ErrorMessages;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.kcserver.dto.finanzen.FinanzausgleichPruefungDTO;
import com.kcserver.enumtype.FinanzausgleichBeitragsstatus;
import com.kcserver.enumtype.FinanzausgleichGesamtstatus;
import com.kcserver.enumtype.Zahlungsweg;
import com.kcserver.repository.TeilnehmerRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanzausgleichService {

    private final FinanzGruppeRepository finanzGruppeRepository;

    private final AbrechnungBuchungRepository buchungRepository;

    private final ZahlungsnachweisRepository zahlungsnachweisRepository;

    private final ReisekostenabrechnungRepository reisekostenRepository;

    private final TeilnehmerBeitragService teilnehmerBeitragService;
    private final TeilnehmerRepository teilnehmerRepository;


    public List<FinanzausgleichDTO> berechne(
            Long veranstaltungId
    ) {

        List<FinanzGruppe> finanzGruppen =
                finanzGruppeRepository
                        .findWithTeilnehmerByVeranstaltungId(
                                veranstaltungId
                        );

        if (finanzGruppen.isEmpty()) {
            return List.of();
        }

        Veranstaltung veranstaltung =
                finanzGruppen.getFirst()
                        .getVeranstaltung();


        // =====================================================
        // 1. Tatsächliche Ausgaben je Finanzgruppe
        // =====================================================

        Map<Long, FinanzSummen> finanzenMap =
                toFinanzSummenMap(
                        buchungRepository
                                .sumFinanzenByVeranstaltungGrouped(
                                        veranstaltungId
                                )
                );


        // =====================================================
        // 2. Fahrkosten je Finanzgruppe
        // =====================================================

        Map<Long, BigDecimal> fahrkostenMap =
                toBigDecimalMap(
                        reisekostenRepository
                                .sumGesamtBetragByFinanzGruppeGrouped(
                                        veranstaltungId
                                )
                );

// ====================================================
// 3. Tatsächlich gezahlte TN-Beiträge je FG
//    und Zahlungsweg
//
// Wichtig:
// Die Zahlung gehört zur Finanzgruppe des
// Zahlungsnachweises, nicht zur aktuellen Finanzgruppe
// des Teilnehmers.
// ===================================================
// 3. Quittungen
//
// Bei Quittungen ist die Finanzgruppe des
// Zahlungsnachweises maßgeblich.
// =====================================================

        Map<Long, BigDecimal> quittungenMap =
                toBigDecimalMap(
                        zahlungsnachweisRepository
                                .sumQuittungenByFinanzGruppeGrouped(
                                        veranstaltungId
                                )
                );

        Map<Long, BigDecimal> rueckzahlungenQuittungMap =
                toBigDecimalMap(
                        zahlungsnachweisRepository
                                .sumRueckzahlungenQuittungByFinanzGruppeGrouped(
                                        veranstaltungId
                                )
                );

// =====================================================
// 4. Überweisungen
//
// Bei Überweisungen ist die Finanzgruppe des
// jeweiligen Teilnehmers maßgeblich.
// =====================================================

        Map<Long, BigDecimal> ueberweisungenMap =
                toBigDecimalMap(
                        zahlungsnachweisRepository
                                .sumUeberweisungenByTeilnehmerFinanzGruppeGrouped(
                                        veranstaltungId
                                )
                );

        Map<Long, BigDecimal> ueberzahlungsUeberweisungenMap =
                toBigDecimalMap(
                        zahlungsnachweisRepository
                                .sumUeberzahlungsUeberweisungenByFinanzGruppeGrouped(
                                        veranstaltungId
                                )
                );

        Map<Long, BigDecimal> rueckzahlungenUeberweisungMap =
                toBigDecimalMap(
                        zahlungsnachweisRepository
                                .sumRueckzahlungenUeberweisungByFinanzGruppeGrouped(
                                        veranstaltungId
                                )
                );

        BigDecimal ueberweisungenGesamtVK =
                safe(
                        zahlungsnachweisRepository
                                .sumUeberweisungenByVeranstaltung(
                                        veranstaltungId
                                )
                );

        BigDecimal rueckzahlungenUeberweisungGesamt =
                safe(
                        zahlungsnachweisRepository
                                .sumRueckzahlungenByVeranstaltungAndZahlungsweg(
                                        veranstaltungId,
                                        Zahlungsweg.UEBERWEISUNG
                                )
                );

        // =====================================================
        // 4. Ergebnis je Finanzgruppe
        // =====================================================

        return finanzGruppen.stream()
                .map(finanzGruppe -> {

                    Long finanzGruppeId =
                            finanzGruppe.getId();


                    // -----------------------------------------
                    // Soll-Beiträge aller Teilnehmer der FG
                    // -----------------------------------------

                    BigDecimal teilnehmerBeitraegeSoll =
                            finanzGruppe
                                    .getTeilnehmer()
                                    .stream()
                                    .map(teilnehmer ->
                                            getSollBeitrag(
                                                    veranstaltung,
                                                    teilnehmer
                                            )
                                    )
                                    .reduce(
                                            BigDecimal.ZERO,
                                            BigDecimal::add
                                    );


                    // -----------------------------------------
                    // Tatsächliche Zahlungen
                    // -----------------------------------------

                    BigDecimal ueberweisungen;

                    if ("VK".equals(finanzGruppe.getKuerzel())) {

                        ueberweisungen =
                                ueberweisungenGesamtVK
                                        .subtract(rueckzahlungenUeberweisungGesamt);

                    } else {


                        ueberweisungen =
                                ueberweisungenMap.getOrDefault(
                                                finanzGruppeId,
                                                BigDecimal.ZERO
                                        )
                                        .add(
                                                ueberzahlungsUeberweisungenMap.getOrDefault(
                                                        finanzGruppeId,
                                                        BigDecimal.ZERO
                                                )
                                        )
                                        .subtract(
                                                rueckzahlungenUeberweisungMap.getOrDefault(
                                                        finanzGruppeId,
                                                        BigDecimal.ZERO
                                                )
                                        );
                    }

                    BigDecimal quittungen =
                            quittungenMap.getOrDefault(
                                    finanzGruppeId,
                                    BigDecimal.ZERO
                            ).subtract(
                                    rueckzahlungenQuittungMap.getOrDefault(
                                            finanzGruppeId,
                                            BigDecimal.ZERO
                                    )
                            );


                    // -----------------------------------------
                    // Ausgaben
                    // -----------------------------------------

                    FinanzSummen finanzen =
                            finanzenMap.getOrDefault(
                                    finanzGruppeId,
                                    new FinanzSummen(
                                            BigDecimal.ZERO,
                                            BigDecimal.ZERO
                                    )
                            );

                    BigDecimal ausgaben =
                            finanzen
                                    .kosten()
                                    .subtract(
                                            finanzen.einnahmen()
                                    );


                    // -----------------------------------------
                    // Fahrkosten
                    // -----------------------------------------

                    BigDecimal fahrkosten =
                            fahrkostenMap.getOrDefault(
                                    finanzGruppeId,
                                    BigDecimal.ZERO
                            );


                    // -----------------------------------------
                    // Finanzausgleich
                    //
                    // Die Finanzgruppe bekommt alle von ihr
                    // verauslagten Kosten erstattet.
                    // -----------------------------------------

                    BigDecimal finanzausgleich =
                            ausgaben
                                    .add(fahrkosten)
                                    .subtract(quittungen);


                    // -----------------------------------------
                    // DTO
                    // -----------------------------------------

                    FinanzausgleichDTO dto =
                            new FinanzausgleichDTO();

                    dto.setFinanzGruppeId(
                            finanzGruppeId
                    );

                    dto.setFinanzGruppeKuerzel(
                            finanzGruppe.getKuerzel()
                    );

                    dto.setTeilnehmerBeitraegeSoll(
                            teilnehmerBeitraegeSoll
                    );

                    dto.setTeilnehmerBeitraegeUeberweisung(
                            ueberweisungen
                    );

                    dto.setTeilnehmerBeitraegeQuittung(
                            quittungen
                    );

                    dto.setAusgaben(
                            ausgaben
                    );

                    dto.setFahrkosten(
                            fahrkosten
                    );

                    dto.setErstattungVomVK(
                            finanzausgleich
                    );

                    return dto;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public FinanzausgleichPruefungDTO pruefeTeilnehmerBeitraege(
            Long veranstaltungId
    ) {

        List<FinanzGruppe> finanzGruppen =
                finanzGruppeRepository
                        .findWithTeilnehmerByVeranstaltungId(
                                veranstaltungId
                        );

        if (finanzGruppen.isEmpty()) {
            return new FinanzausgleichPruefungDTO(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    FinanzausgleichGesamtstatus.OK,
                    List.of()
            );
        }

        Veranstaltung veranstaltung =
                finanzGruppen.getFirst()
                        .getVeranstaltung();

        /*
         * =========================================================
         * 1. Gesamt-Soll aller Teilnehmer
         *
         * Wichtig:
         * Teilnehmer ohne Finanzgruppe sind hier enthalten.
         * =========================================================
         */

        List<Teilnehmer> alleTeilnehmer =
                teilnehmerRepository.findAllWithPerson(
                        veranstaltungId
                );

        BigDecimal gesamtSoll =
                alleTeilnehmer.stream()
                        .map(teilnehmer ->
                                getSollBeitrag(
                                        veranstaltung,
                                        teilnehmer
                                )
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        /*
         * =========================================================
         * 2. Gesamt-Ist
         * =========================================================
         */

        BigDecimal gesamtUeberweisungen =
                safe(
                        zahlungsnachweisRepository
                                .sumBetragByVeranstaltungAndZahlungsweg(
                                        veranstaltungId,
                                        Zahlungsweg.UEBERWEISUNG
                                )
                );

        BigDecimal gesamtQuittungen =
                safe(
                        zahlungsnachweisRepository
                                .sumBetragByVeranstaltungAndZahlungsweg(
                                        veranstaltungId,
                                        Zahlungsweg.QUITTUNG
                                )
                );

        BigDecimal gesamtRueckzahlungen =
                safe(
                        zahlungsnachweisRepository
                                .sumRueckzahlungenByVeranstaltung(
                                        veranstaltungId)
                );

        BigDecimal gesamtIst =
                gesamtUeberweisungen
                        .add(gesamtQuittungen)
                        .subtract(gesamtRueckzahlungen);


        /*
         * =========================================================
         * 3. Tatsächliche Zahlungen je Finanzgruppe
         *
         * Die FG wird über den Teilnehmer bestimmt.
         * =========================================================
         */

        Map<Long, BigDecimal> ueberweisungenMap =
                toBigDecimalMap(
                        zahlungsnachweisRepository
                                .sumUeberweisungenByTeilnehmerFinanzGruppeGrouped(
                                        veranstaltungId
                                )
                );

        Map<Long, BigDecimal> ueberzahlungsUeberweisungenMap =
                toBigDecimalMap(
                        zahlungsnachweisRepository
                                .sumUeberzahlungsUeberweisungenByFinanzGruppeGrouped(
                                        veranstaltungId
                                )
                );

        Map<Long, BigDecimal> quittungenMap =
                toBigDecimalMap(
                        zahlungsnachweisRepository
                                .sumQuittungenByFinanzGruppeGrouped(
                                        veranstaltungId
                                )
                );

        Map<Long, BigDecimal> rueckzahlungenQuittungMap =
                toBigDecimalMap(
                        zahlungsnachweisRepository
                                .sumRueckzahlungenQuittungByFinanzGruppeGrouped(
                                        veranstaltungId
                                )
                );

        Map<Long, BigDecimal> rueckzahlungenUeberweisungMap =
                toBigDecimalMap(
                        zahlungsnachweisRepository
                                .sumRueckzahlungenUeberweisungByFinanzGruppeGrouped(
                                        veranstaltungId
                                )
                );
        /*
         * =========================================================
         * 4. Prüfung je Finanzgruppe
         * =========================================================
         */

        List<FinanzausgleichDTO> pruefungen =
                finanzGruppen.stream()
                        .map(finanzGruppe -> {

                            Long finanzGruppeId =
                                    finanzGruppe.getId();

                            BigDecimal soll =
                                    finanzGruppe
                                            .getTeilnehmer()
                                            .stream()
                                            .map(teilnehmer ->
                                                    getSollBeitrag(
                                                            veranstaltung,
                                                            teilnehmer
                                                    )
                                            )
                                            .reduce(
                                                    BigDecimal.ZERO,
                                                    BigDecimal::add
                                            );

                            BigDecimal ueberweisungen =
                                    ueberweisungenMap.getOrDefault(
                                            finanzGruppeId,
                                            BigDecimal.ZERO
                                    ).add(
                                            ueberzahlungsUeberweisungenMap.getOrDefault(
                                                    finanzGruppeId,
                                                    BigDecimal.ZERO
                                            ));

                            BigDecimal quittungen =
                                    quittungenMap.getOrDefault(
                                            finanzGruppeId,
                                            BigDecimal.ZERO);

                            BigDecimal rueckzahlungenQuittung =
                                    rueckzahlungenQuittungMap.getOrDefault(
                                            finanzGruppeId,
                                            BigDecimal.ZERO
                                    );

                            BigDecimal quittungenNetto =
                                    quittungen.subtract(rueckzahlungenQuittung);

                            BigDecimal rueckzahlungenUeberweisung =
                                    rueckzahlungenUeberweisungMap.getOrDefault(
                                            finanzGruppeId,
                                            BigDecimal.ZERO
                                    );

                            BigDecimal ueberweisungenNetto =
                                    ueberweisungen.subtract(rueckzahlungenUeberweisung);

                            BigDecimal ist =
                                    ueberweisungenNetto
                                            .add(quittungenNetto);

                            FinanzausgleichBeitragsstatus beitragsstatus =
                                    ist.compareTo(soll) == 0
                                            ? FinanzausgleichBeitragsstatus.OK
                                            : FinanzausgleichBeitragsstatus.ABWEICHUNG;

                            FinanzausgleichDTO dto =
                                    new FinanzausgleichDTO();

                            dto.setFinanzGruppeId(
                                    finanzGruppeId
                            );

                            dto.setFinanzGruppeKuerzel(
                                    finanzGruppe.getKuerzel()
                            );

                            dto.setTeilnehmerBeitraegeSoll(
                                    soll
                            );

                            dto.setTeilnehmerBeitraegeUeberweisung(
                                    ueberweisungenNetto
                            );

                            dto.setTeilnehmerBeitraegeQuittung(
                                    quittungenNetto
                            );

                            dto.setBeitragsstatus(
                                    beitragsstatus
                            );

                            return dto;
                        })
                        .toList();


        /*
         * =========================================================
         * 5. Gesamtstatus
         * =========================================================
         */

        FinanzausgleichGesamtstatus gesamtstatus;

        if (gesamtIst.compareTo(gesamtSoll) < 0) {

            gesamtstatus =
                    FinanzausgleichGesamtstatus.BEITRAEGE_FEHLEN;

        } else if (gesamtIst.compareTo(gesamtSoll) > 0) {

            gesamtstatus =
                    FinanzausgleichGesamtstatus.UEBERZAHLUNG;

        } else {

            boolean finanzgruppenStimmen =
                    pruefungen.stream()
                            .allMatch(dto ->
                                    dto.getBeitragsstatus()
                                            == FinanzausgleichBeitragsstatus.OK
                            );

            gesamtstatus =
                    finanzgruppenStimmen
                            ? FinanzausgleichGesamtstatus.OK
                            : FinanzausgleichGesamtstatus.FINANZGRUPPEN_ABWEICHUNG;
        }


        return new FinanzausgleichPruefungDTO(
                gesamtSoll,
                gesamtUeberweisungen,
                gesamtQuittungen,
                gesamtIst,
                gesamtstatus,
                pruefungen
        );
    }

    public FinanzausgleichDTO getFinanzausgleich(
            Long veranstaltungId,
            Long finanzGruppeId
    ) {

        return berechne(veranstaltungId)
                .stream()
                .filter(dto ->
                        dto.getFinanzGruppeId()
                                .equals(finanzGruppeId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                ErrorMessages.GRUPPE_NOT_IN_VERANSTALTUNG
                        )
                );
    }


    public Map<Long, BigDecimal> getSollBeitraegeByVeranstaltung(
            Long veranstaltungId
    ) {

        List<FinanzGruppe> finanzGruppen =
                finanzGruppeRepository
                        .findWithTeilnehmerByVeranstaltungId(
                                veranstaltungId
                        );

        if (finanzGruppen.isEmpty()) {
            return Map.of();
        }

        Veranstaltung veranstaltung =
                finanzGruppen.getFirst()
                        .getVeranstaltung();

        Map<Long, BigDecimal> result =
                new HashMap<>();

        for (FinanzGruppe finanzGruppe : finanzGruppen) {

            for (Teilnehmer teilnehmer :
                    finanzGruppe.getTeilnehmer()) {

                result.put(
                        teilnehmer.getId(),
                        getSollBeitrag(
                                veranstaltung,
                                teilnehmer
                        )
                );
            }
        }

        return result;
    }

    // =========================================================
    // HELPER
    // =========================================================

    private Map<Long, FinanzSummen> toFinanzSummenMap(
            List<Object[]> rows
    ) {

        Map<Long, FinanzSummen> result =
                new HashMap<>();

        for (Object[] row : rows) {

            Long finanzGruppeId =
                    (Long) row[0];

            BigDecimal einnahmen =
                    safe((BigDecimal) row[1]);

            BigDecimal kosten =
                    safe((BigDecimal) row[2]);

            result.put(
                    finanzGruppeId,
                    new FinanzSummen(
                            einnahmen,
                            kosten
                    )
            );
        }

        return result;
    }

    private BigDecimal getSollBeitrag(
            Veranstaltung veranstaltung,
            Teilnehmer teilnehmer
    ) {

        BigDecimal beitrag =
                teilnehmerBeitragService
                        .getSollBeitrag(
                                veranstaltung,
                                teilnehmer
                        );

        return safe(beitrag);
    }


    private Map<Long, BigDecimal> toBigDecimalMap(
            List<Object[]> rows
    ) {

        Map<Long, BigDecimal> result =
                new HashMap<>();

        for (Object[] row : rows) {

            Long finanzGruppeId =
                    (Long) row[0];

            BigDecimal betrag =
                    safe((BigDecimal) row[1]);

            result.put(
                    finanzGruppeId,
                    betrag
            );
        }

        return result;
    }


    private BigDecimal safe(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }

    private record FinanzSummen(
            BigDecimal einnahmen,
            BigDecimal kosten
    ) {}
}