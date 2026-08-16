#!/bin/bash
{
  echo "===== ENTITIES ====="
  find src/main/java/com/kcserver/entity -type f \
    \( -name "FinanzGruppe.java" \
    -o -name "Abrechnung.java" \
    -o -name "AbrechnungBeleg.java" \
    -o -name "AbrechnungBuchung.java" \
    -o -name "Zahlungsnachweis.java" \
    -o -name "ZahlungsPosition.java" \
    -o -name "Reisekostenabrechnung.java" \
    -o -name "Planung.java" \
    -o -name "PlanungPosition.java" \
    -o -name "FinanzGruppeService.java" \
    -o -name "Teilnehmer.java" \
    -o -name "BelegDokument.java" \
    -o -name "ZahlungsnachweisDokument.java" \
    -o -name "FahrtabschnittMitfahrer.java" \
    -o -name "Fahrtabschnitt.java" \) \
    -exec sh -c 'echo; echo "===== $1 ====="; cat "$1"' _ {} \;

  echo
  echo "===== REPOSITORIES ====="
  find src/main/java/com/kcserver/repository -type f \
    \( -name "FinanzGruppeRepository.java" \
    -o -name "AbrechnungRepository.java" \
    -o -name "AbrechnungBelegRepository.java" \
    -o -name "AbrechnungBuchungRepository.java" \
    -o -name "ZahlungsnachweisRepository.java" \
    -o -name "ReisekostenabrechnungRepository.java" \
    -o -name "FahrtabschnittRepository.java" \) \
    -exec sh -c 'echo; echo "===== $1 ====="; cat "$1"' _ {} \;
} > loeschanalyse.txt