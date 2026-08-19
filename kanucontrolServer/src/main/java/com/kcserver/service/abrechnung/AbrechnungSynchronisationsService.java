package com.kcserver.service.abrechnung;

import com.kcserver.entity.*;

import com.kcserver.enumtype.BuchungsHerkunft;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.enumtype.Zahlungsweg;
import com.kcserver.repository.FinanzGruppeRepository;
import com.kcserver.repository.abrechnung.AbrechnungRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.repository.abrechnung.ZahlungsnachweisRepository;
import com.kcserver.service.FoerderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AbrechnungSynchronisationsService {

    private final AbrechnungBelegService abrechnungBelegService;
    private final TeilnehmerRepository teilnehmerRepository;
    private final AbrechnungRepository abrechnungRepository;
    private final FoerderService foerderService;
    private final FinanzGruppeRepository finanzGruppeRepository;
    private final ZahlungsnachweisRepository zahlungsnachweisRepository;

    public void synchronisieren(Long veranstaltungId) {

        Abrechnung abrechnung = abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Abrechnung nicht gefunden"
                        ));

        synchronisiereTeilnehmerbeitraege(abrechnung);
        synchronisiereKjfp(abrechnung);
    }

    private void synchronisiereKjfp(
            Abrechnung abrechnung
    ) {

        FinanzGruppe vk =
                finanzGruppeRepository
                        .findByVeranstaltungIdAndKuerzel(
                                abrechnung.getVeranstaltung().getId(),
                                "VK"
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Für die Veranstaltung ist kein VK-Konto eingerichtet."
                                )
                        );

        AbrechnungBeleg beleg =
                abrechnungBelegService.getOrCreateBeleg(
                        abrechnung,
                        vk,
                        BuchungsHerkunft.KJFP
                );

        beleg.removePositionenByHerkunft(
                BuchungsHerkunft.KJFP
        );

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findAllWithPerson(
                        abrechnung.getVeranstaltung().getId()
                );

        BigDecimal zuschuss =
                foerderService.berechneKjfpZuschuss(
                        abrechnung.getVeranstaltung(),
                        teilnehmer
                );

        if (zuschuss == null
                || zuschuss.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        addKjfp(
                beleg,
                abrechnung.getVeranstaltung(),
                zuschuss
        );
    }

    private void addKjfp(
            AbrechnungBeleg beleg,
            Veranstaltung veranstaltung,
            BigDecimal betrag
    ) {

        AbrechnungBuchung buchung = new AbrechnungBuchung();

        buchung.setKategorie(FinanzKategorie.KJFP_ZUSCHUSS);
        buchung.setHerkunft(BuchungsHerkunft.KJFP);
        buchung.setBetrag(betrag);

        buchung.setBeschreibung(
                "KJFP-Zuschuss " + veranstaltung.getName()
        );

        beleg.addPosition(buchung);
    }

    private void synchronisiereTeilnehmerbeitraege(
            Abrechnung abrechnung
    ) {

        FinanzGruppe vk =
                finanzGruppeRepository
                        .findByVeranstaltungIdAndKuerzel(
                                abrechnung.getVeranstaltung().getId(),
                                "VK"
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Für die Veranstaltung ist kein VK-Konto eingerichtet."
                                )
                        );

        AbrechnungBeleg beleg =
                abrechnungBelegService.getOrCreateBeleg(
                        abrechnung,
                        vk,
                        BuchungsHerkunft.TEILNEHMERBEITRAG
                );

        // Alte automatische Position entfernen
        beleg.removePositionenByHerkunft(
                BuchungsHerkunft.TEILNEHMERBEITRAG
        );

        BigDecimal ueberweisungen =
                zahlungsnachweisRepository
                        .sumBetragByVeranstaltungAndZahlungsweg(
                                abrechnung.getVeranstaltung().getId(),
                                Zahlungsweg.UEBERWEISUNG
                        );

        BigDecimal quittungen =
                zahlungsnachweisRepository
                        .sumBetragByVeranstaltungAndZahlungsweg(
                                abrechnung.getVeranstaltung().getId(),
                                Zahlungsweg.QUITTUNG
                        );

        BigDecimal betrag =
                (ueberweisungen == null
                        ? BigDecimal.ZERO
                        : ueberweisungen)
                        .add(
                                quittungen == null
                                        ? BigDecimal.ZERO
                                        : quittungen
                        );

        if (betrag.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        AbrechnungBuchung buchung = new AbrechnungBuchung();

        buchung.setKategorie(
                FinanzKategorie.TEILNEHMERBEITRAG
        );

        buchung.setHerkunft(
                BuchungsHerkunft.TEILNEHMERBEITRAG
        );

        buchung.setBetrag(betrag);

        buchung.setBeschreibung(
                "TN-Beiträge"
        );

        beleg.addPosition(buchung);
    }
}