#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
# Auch gemeinsam genutzte Veranstaltungs-, Teilnehmer- und Finanzregressionen prüfen.
tests='*UseCaseTest,KjfpHttpSecurityTest,ModulfunktionenSecurityTest,*Planung*Test,*Simulation*Test,*Kjfp*Test,*Foerdersatz*Test,*Beitragsregel*Test,Teilnehmer*Test,FinanzGruppe*Test,FinanzServiceTest,AbrechnungServiceTest,AbrechnungControllerTest,AbrechnungSynchronisationsServiceTest,ZahlungsstatusServiceTest,Belegnummer*Test,*BelegnummerPdfTest,BelegAusrichtungPdfTest,Programmentwurf*Test,DokumentAusrichtung*Test,FinanzausgleichKategorien*Test,Veranstaltung*Test'
exec mvn test "-Dtest=$tests" "$@"
