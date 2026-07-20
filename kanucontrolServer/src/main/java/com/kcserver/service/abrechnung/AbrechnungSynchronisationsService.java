package com.kcserver.service.abrechnung;

import com.kcserver.entity.*;

import com.kcserver.enumtype.BuchungsHerkunft;
import com.kcserver.enumtype.FinanzKategorie;
import com.kcserver.finanz.AbrechnungBelegService;
import com.kcserver.repository.AbrechnungRepository;
import com.kcserver.repository.TeilnehmerRepository;
import com.kcserver.service.FoerderService;
import com.kcserver.service.beitrag.TeilnehmerBeitragService;
import com.kcserver.service.reisekosten.ReisekostenabrechnungService;
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
    private final TeilnehmerBeitragService teilnehmerBeitragService;
    private final TeilnehmerRepository teilnehmerRepository;
    private final AbrechnungRepository abrechnungRepository;
    private final ReisekostenabrechnungService reisekostenabrechnungService;
    private final FoerderService foerderService;

    public void synchronisieren(Long veranstaltungId) {

        Abrechnung abrechnung = abrechnungRepository
                .findByVeranstaltungId(veranstaltungId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Abrechnung nicht gefunden"
                        ));

        // Einnahmen
        synchronisiereTeilnehmerbeitraege(abrechnung);
        synchronisiereKjfp(abrechnung);

        // Ausgaben
        synchronisiereReisekosten(abrechnung);
    }

    private void synchronisiereTeilnehmerbeitraege(
            Abrechnung abrechnung
    ) {

        AbrechnungBeleg beleg =
                abrechnungBelegService.getOrCreateSystemBeleg(
                        abrechnung,
                        BuchungsHerkunft.TEILNEHMERBEITRAG
                );

        // Alte automatisch erzeugte Teilnehmerbeiträge entfernen
        beleg.removePositionen(
                BuchungsHerkunft.TEILNEHMERBEITRAG
        );

        List<Teilnehmer> teilnehmer =
                teilnehmerRepository.findAllWithPerson(
                        abrechnung.getVeranstaltung().getId()
                );

        for (Teilnehmer t : teilnehmer) {

            // Nur tatsächlich bezahlte Beiträge buchen
            if (!teilnehmerBeitragService.isBezahlt(t)) {
                continue;
            }

            BigDecimal beitrag =
                    teilnehmerBeitragService.getEffektiverBeitrag(
                            abrechnung.getVeranstaltung(),
                            t
                    );

            if (beitrag == null || beitrag.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            addTeilnehmerbeitrag(
                    beleg,
                    t,
                    beitrag
            );
        }
    }

    private void synchronisiereReisekosten(
            Abrechnung abrechnung
    ) {

        AbrechnungBeleg beleg =
                abrechnungBelegService.getOrCreateSystemBeleg(
                        abrechnung,
                        BuchungsHerkunft.FAHRTKOSTEN
                );

        beleg.removePositionen(
                BuchungsHerkunft.FAHRTKOSTEN
        );

        List<Reisekostenabrechnung> abrechnungen =
                reisekostenabrechnungService.findByVeranstaltung(
                        abrechnung.getVeranstaltung().getId()
                );

        for (Reisekostenabrechnung rk : abrechnungen) {

            if (rk.getGesamtBetrag() == null
                    || rk.getGesamtBetrag().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            addReisekosten(
                    beleg,
                    rk
            );
        }
    }

    private void synchronisiereKjfp(
            Abrechnung abrechnung
    ) {

        AbrechnungBeleg beleg =
                abrechnungBelegService.getOrCreateSystemBeleg(
                        abrechnung,
                        BuchungsHerkunft.KJFP
                );

        beleg.removePositionen(
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

    private void addTeilnehmerbeitrag(
            AbrechnungBeleg beleg,
            Teilnehmer teilnehmer,
            BigDecimal betrag
    ) {
        AbrechnungBuchung buchung = new AbrechnungBuchung();

        buchung.setKategorie(FinanzKategorie.TEILNEHMERBEITRAG);
        buchung.setHerkunft(BuchungsHerkunft.TEILNEHMERBEITRAG);
        buchung.setTeilnehmer(teilnehmer);
        buchung.setBetrag(betrag);

        buchung.setBeschreibung(
                teilnehmer.getPerson().getVorname()
                        + " "
                        + teilnehmer.getPerson().getName()
        );

        beleg.addPosition(buchung);
    }

    private void addReisekosten(
            AbrechnungBeleg beleg,
            Reisekostenabrechnung abrechnung
    ) {

        AbrechnungBuchung buchung = new AbrechnungBuchung();

        buchung.setKategorie(FinanzKategorie.FAHRTKOSTEN);
        buchung.setHerkunft(BuchungsHerkunft.FAHRTKOSTEN);

        buchung.setReisekostenabrechnung(abrechnung);

        buchung.setBetrag(
                abrechnung.getGesamtBetrag()
        );

        buchung.setBeschreibung(
                "Fahrtkosten "
                        + abrechnung.getFahrer().getVorname()
                        + " "
                        + abrechnung.getFahrer().getName()
        );

        beleg.addPosition(buchung);
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
}