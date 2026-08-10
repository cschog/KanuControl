package com.kcserver.service;

import com.kcserver.enumtype.Zahlungsstatus;
import com.kcserver.repository.abrechnung.TeilnehmerZahlungSumme;
import com.kcserver.repository.abrechnung.ZahlungsnachweisRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ZahlungsstatusService {

    private final ZahlungsnachweisRepository zahlungsnachweisRepository;

    public ZahlungsstatusService(
            ZahlungsnachweisRepository zahlungsnachweisRepository
    ) {
        this.zahlungsnachweisRepository = zahlungsnachweisRepository;
    }

    public Map<Long, BigDecimal> getGezahlteBetraege(
            Long veranstaltungId
    ) {
        return zahlungsnachweisRepository
                .summeZahlungenByVeranstaltung(veranstaltungId)
                .stream()
                .collect(Collectors.toMap(
                        TeilnehmerZahlungSumme::getTeilnehmerId,
                        TeilnehmerZahlungSumme::getGezahlterBetrag
                ));
    }

    public Zahlungsstatus getStatus(
            BigDecimal sollBeitrag,
            BigDecimal gezahlterBetrag
    ) {
        if (gezahlterBetrag == null
                || gezahlterBetrag.compareTo(BigDecimal.ZERO) == 0) {
            return Zahlungsstatus.ROT;
        }

        if (gezahlterBetrag.compareTo(sollBeitrag) < 0) {
            return Zahlungsstatus.GELB;
        }

        return Zahlungsstatus.GRUEN;
    }
}