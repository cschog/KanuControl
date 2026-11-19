package com.kcserver.vermietung.service;

import com.kcserver.vermietung.entity.Verbrauchskosten;
import com.kcserver.vermietung.entity.Verbrauchspreis;
import com.kcserver.vermietung.enumtype.Verbrauchsart;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Ein eindeutiger Tarif je Verbrauchsart am Anreisetag, unabhängig von Zeile und Aktiv-Altbestand. */
public final class VerbrauchspreisAuswahl {
    private VerbrauchspreisAuswahl() {}

    public record Auswahl(Verbrauchskosten basis, Verbrauchskosten konfiguration, String hinweis) {}

    public static List<Auswahl> zumDatum(List<Verbrauchskosten> positionen, LocalDate datum) {
        Map<Verbrauchsart, List<Verbrauchskosten>> gruppen = new LinkedHashMap<>();
        for (var position : positionen) {
            gruppen.computeIfAbsent(position.getVerbrauchsart(), art -> new ArrayList<>()).add(position);
        }
        List<Auswahl> ergebnis = new ArrayList<>();
        for (var gruppe : gruppen.entrySet()) {
            List<Verbrauchskosten> passende = new ArrayList<>();
            int anzahlPreise = 0;
            for (var position : gruppe.getValue()) {
                long treffer = position.getPreise().stream().filter(p -> gilt(p, datum)).count();
                anzahlPreise += Math.toIntExact(treffer);
                if (treffer > 0) passende.add(position);
            }
            var basis = gruppe.getValue().getFirst();
            String art = switch (gruppe.getKey()) { case STROM -> "Strom"; case GAS -> "Gas"; case WASSER -> "Wasser"; };
            String tag = datum.format(DateTimeFormatter.ofPattern("dd.MM.uuuu"));
            if (anzahlPreise == 0) {
                ergebnis.add(new Auswahl(basis, null, "Kein gültiger Preis für " + art + " am Anreisetag " + tag + ". Bitte den Gültigkeitszeitraum ergänzen."));
            } else if (anzahlPreise > 1) {
                String namen = String.join(", ", passende.stream().map(Verbrauchskosten::getBezeichnung).toList());
                ergebnis.add(new Auswahl(basis, null, "Mehrere gültige Preise für " + art + " am Anreisetag " + tag + " (" + namen + "). Bitte überschneidende Zeiträume korrigieren."));
            } else {
                var position = passende.getFirst();
                ergebnis.add(new Auswahl(position, position.zumDatum(datum), null));
            }
        }
        return List.copyOf(ergebnis);
    }

    private static boolean gilt(Verbrauchspreis preis, LocalDate datum) {
        return !datum.isBefore(preis.getGueltigVon())
                && (preis.getGueltigBis() == null || !datum.isAfter(preis.getGueltigBis()));
    }
}
