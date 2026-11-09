package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.BuchungsnummerCounter;
import com.kcserver.vermietung.entity.Buchung;
import com.kcserver.vermietung.repository.BuchungRepository;
import com.kcserver.vermietung.repository.BuchungsnummerCounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Objects;
import java.util.regex.Pattern;

@Service @RequiredArgsConstructor
public class BuchungsnummerService {
    private static final Pattern FORMAT=Pattern.compile("([1-9][0-9]{3})-([0-9]{1,10})");
    private final BuchungRepository buchungen;
    private final BuchungsnummerCounterRepository counters;
    public record Nummer(Long id,String buchungsnummer) {}

    @Transactional
    public Nummer aendern(Long id,String nummer,String bisherigeNummer) {
        String neueNummer=nummer==null?"":nummer.trim();
        var match=FORMAT.matcher(neueNummer);
        if(!match.matches())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Bitte eine Buchungsnummer im Format 2027-015 eingeben.");
        int jahr=Integer.parseInt(match.group(1));long wert=Long.parseLong(match.group(2));
        if(wert<1 || wert>=Integer.MAX_VALUE)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Die laufende Nummer muss positiv sein und Platz für die nächste automatische Nummer lassen.");
        int laufend=(int)wert;
        var b=buchungen.findLockedById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Buchung nicht gefunden."));
        if(!Objects.equals(b.getBuchungsnummer(),bisherigeNummer))throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Die Buchungsnummer wurde inzwischen geändert. Bitte die Buchung neu laden.");
        if(Objects.equals(b.getBuchungsnummer(),neueNummer))return new Nummer(id,b.getBuchungsnummer());
        try {
            // Derselbe Jahreszähler wie bei der automatischen Vergabe, unter Schreibsperre.
            var counter=counters.findByJahr(jahr).orElseGet(()->counters.saveAndFlush(new BuchungsnummerCounter(jahr,0)));
            if(buchungen.existsByBuchungsnummerAndIdNot(neueNummer,id))
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Diese Buchungsnummer ist bereits vergeben. Bitte eine andere Nummer wählen.");
            int letzteVorhandene=buchungen.findTopByBuchungsjahrOrderByLaufendeNummerDesc(jahr).map(Buchung::getLaufendeNummer).orElse(0);
            counter.setLetzteNummer(Math.max(counter.getLetzteNummer(),Math.max(letzteVorhandene,laufend)));
            counters.save(counter);
            b.setBuchungsjahr(jahr);b.setLaufendeNummer(laufend);b.setBuchungsnummer(neueNummer);
            // Die Datenbank-Unique-Constraint schützt zusätzlich bei gleichzeitigen Änderungen.
            buchungen.saveAndFlush(b);
            return new Nummer(id,b.getBuchungsnummer());
        } catch(DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Die Nummer konnte wegen einer gleichzeitigen Änderung nicht vergeben werden. Bitte neu laden und eine freie Nummer wählen.",e);
        }
    }
}
