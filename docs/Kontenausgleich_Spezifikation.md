# KontenKontenausgleichausgleich

## Zweck

Der **Kontenausgleich** einer Veranstaltung sorgt dafür, dass Ausgaben,
die von einzelnen Konton vorgestreckt wurden, korrekt mit den von
den zugeordneten Teilnehmern zu zahlenden Teilnehmerbeiträgen verrechnet
werden.

Der Kontenausgleich ist von der allgemeinen Veranstaltungsabrechnung zu
unterscheiden.

Die Veranstaltungsabrechnung ermittelt den **Eigenanteil des Vereins**.
Dieser Eigenanteil wird **nicht auf die Konton verteilt**.

Der Kontenausgleich ermittelt dagegen, **welchen Betrag der Verein an
die einzelnen Konton auszahlen muss**.

## 1. Konton

Für eine Veranstaltung können mehrere Konton verwendet werden.

Eine Konto ist beispielsweise eine Familie oder eine sonstige
Teilnehmergruppe.

### Kontontypen

Jede Konto besitzt einen Typ:

-   **SYSTEM**
-   **NORMAL**

### 1.1 System-Konto SYS

Das Konto **SYS** ist eine technische System-Konto der

Veranstaltung.

Sie dient zur Aufnahme von systemseitig erzeugten bzw. zentralen Finanzbuchungen.

Das SYS-Konto ist **kein Konto eines Teilnehmers** und nimmt **nicht am Kontenausgleich** teil.

Insbesondere erhält SYS keine Ausgleichszahlung.


---

### 1.2 Vereins-Konto VK

**VK** repräsentiert das **Vereinskonto innerhalb der Veranstaltung**.

Es dient insbesondere dazu, Zahlungsein- und abgänge auf dem Vereinskonto abzubilden.

Teilnehmerbeiträge, die per Überweisung auf das Vereinskonto eingehen, werden der VK zugeordnet.

VK nimmt **nicht am Kontenausgleich** teil. VK ist damit von normalen Konten zu unterscheiden:

- VK = Vereinskonto
- normales Konto = Konto, das Teilnehmer und/oder Ausgaben repräsentiert

VK wird für eine Veranstaltung automatisch angelegt.

---

### 1.3 Normale Konton

Normale Konton können beispielsweise Familien oder sonstige Teilnehmergruppen sein.

Eine normales Konto kann:

- Teilnehmer zugeordnet bekommen,
- Ausgaben für die Veranstaltung übernehmen,
- Teilnehmerbeiträge ihrer Teilnehmer überweisen,
- Teilnehmerbeiträge ihrer Teilnehmer per Quittung nachweisen,
- eine Ausgleichszahlung erhalten.

Ein normales Konto muss nicht zwingend Teilnehmer enthalten.

Für den Kontenausgleich ist es insbesondere dann relevant, wenn es Ausgaben für die Veranstaltung übernimmt.

Nur **normale Konton** können eine Ausgleichszahlung erhalten.

## 2. Zuordnung von Teilnehmern zu Konton

Teilnehmer werden bei einer Veranstaltung optional einem Konto zugeordnet.

Die Zuordnung ist insbesondere dann erforderlich, wenn ein Konto Ausgaben für die Veranstaltung übernimmt.

Teilnehmer, die keinem Konto zugeordnet sind, nehmen trotzdem ganz normal an der Veranstaltung teil und müssen ihren Teilnehmerbeitrag bezahlen. Ihre Beiträge werden jedoch nicht über eine Konto ausgeglichen.

### Beispiel

Bei einer Veranstaltung werden insgesamt 5.800 € Teilnehmerbeiträge
erhoben.

Davon gehören:

-   1.000 € zu Konto Hil,
-   1.100 € zu Konto Mo,
-   1.100 € zu Konto Rz,
-   1.500 € zu Konto Sg.

Damit sind 4.700 € Konton zugeordnet.

Die verbleibenden 1.100 € gehören zu Teilnehmern ohne Konto und verbleiben beim Verein.

## 3. Teilnehmerbeiträge

Der Teilnehmerbeitrag ergibt sich aus:

1.  dem Teilnehmer,
2.  seinem Alter,
3.  der für die Veranstaltung geltenden Beitragsstruktur.

Die Summe der Beiträge aller einem Konto zugeordneten Teilnehmer ergibt den Teilnehmerbeitrag für dieses Konto.

### Beispiel Konto Mo

  Teilnehmer          Beitrag
  ------------- -------------
  Erwachsener           400 €
  Erwachsener           400 €
  Kind                  200 €
  Kind                  100 €
  **Summe**       **1.100 €**

Das Konto Mo hat damit einen Teilnehmerbeitrag von insgesamt 1.100 €.

## 4. Zahlungsnachweise

Für Teilnehmerbeiträge wird ein `Zahlungsnachweis` geführt. Neben dem Betrag ist der **Zahlungsweg** wichtig.

Der Zahlungsweg unterscheidet:

-   **UEBERWEISUNG**
-   **QUITTUNG**

Ein Zahlungsnachweis kann sich auf einen oder mehrere Teilnehmer beziehen. Zusätzlich wird ein Zahlungsnachweis einem **Konto** zugeordnet.

Dabei gilt:

> Ein Zahlungsnachweis gehört höchstens zu einer Konto.

Die Zuordnung zur Konto und der Zahlungsweg haben unterschiedliche Bedeutungen.

### 4.1 Überweisung

Bei einer **Überweisung** ist das Geld tatsächlich auf dem Vereinskonto eingegangen.

Der Zahlungseingang wird deshalb **VK** (Vereinskonto) zugeordnet.

### 4.2 Quittung

Mit einer Quittung wird ein Teilnehmerbeitrag nachgewiesen. Das Geld verbleibt bei dem Konto, das dem Zahlungsnachweis zugeordnet ist.

Eine Quittung ist immer einem Konto zugeordnet.

### Beispiel Hil

Die Familie Hil hat einen Teilnehmerbeitrag von 1.000 €.

Davon:

-   200 € wurden auf das Vereinskonto überwiesen,
-   800 € wurden bar gegen Quittung bezahlt.

  Zahlungsweg          Betrag
  ------------- -------------
  Überweisung           200 €
  Quittung              800 €
  **Gesamt**      **1.000 €**

Die 800 € Quittung werden bei der Berechnung des Ausgleichsbetrags berücksichtigt, weil dieses Geld nicht zusätzlich vom Verein an Hil ausgezahlt werden muss.

### Zahlungsnachweis und Konto

Für einen Zahlungsnachweis gelten folgende Regeln:

**Überweisung**
```
Zahlungsnachweis
│
├── Zahlungspositionen → Teilnehmer
│
└── Konto → VK
``` 
Das Geld ist auf dem Vereinskonto eingegangen.


**Quittung**
```
Zahlungsnachweis
│
├── Zahlungspositionen → Teilnehmer
│
└── Konto → normales Konto
```
Das Geld ist nicht auf dem Vereinskonto eingegangen.

## 5. Ausgaben eines Kontos

Ausgaben werden dem Konto zugeordnet, das die Ausgaben tatsächlich übernommen hat. Dabei handelt es sich grundsätzlich um **normale Konten**.

Beispiele:

-   Supermarkteinkauf,
-   Bäckerei,
-   sonstige Verpflegung,
-   Verbrauchsmaterial,
-   Fahrkosten.

Auch **Fahrkosten** sind normale Veranstaltungskosten. Die Fahrkosten werden dem Konto des jeweiligen Fahrers zugeordnet. Wenn das Konto noch nicht besteht, wird es bei Anlage der Fahrkostenabrechnung mit angelegt.

### Beispiel Mo

Mo hat übernommen:

-   Verpflegung: 536,00 €
-   Fahrkosten: 1.530,30 €

Gesamtausgaben: **2.066,30 €**

## 6. Berechnung der Ausgleichszahlung

Für jede normale Konto wird genau **eine Ausgleichszahlung je Veranstaltung** berechnet.

Grundsätzlich gilt:

> Das Konto soll nach dem Kontenausgleich nur den Teilnehmerbeitrag ihrer zugeordneten Teilnehmer bzw. von ihr selbst vereinnahmte sonstige Beträge tragen (i.d.R. sind das die Quittungen).

Ausgaben, die darüber hinaus von dem Konto vorgestreckt wurden, werden vom Verein ausgeglichen.

Für die konkrete Berechnung ist entscheidend, welcher Teil der Teilnehmerbeiträge bereits beim Verein eingegangen ist und welcher Teil
nur per Quittung nachgewiesen wurde.

Vereinfacht:

**Ausgleichszahlung = Ausgaben des Kontos − beim Konto verbliebene Beträge**

Dabei sind insbesondere quittierte Teilnehmerbeiträge zu berücksichtigen.

### Beispiel Mo

Mo hat:

-   Ausgaben: 2.066,30 €
-   Teilnehmerbeiträge: 1.100 €
-   davon Überweisung an den Verein: 1.100 €
-   davon Quittung: 0 €

Da die gesamten 1.100 € bereits beim Verein eingegangen sind, werden die
Ausgaben von Mo vollständig vom Verein ausgeglichen:

**Ausgleich Mo = 2.066,30 €**

Nach dem Ausgleich bleibt bei Mo der Teilnehmerbeitrag von 1.100 € als eigene Belastung.

### Beispiel Hil

Hil hat:

-   Ausgaben: 3.501,90 €
-   Teilnehmerbeiträge: 1.000 €
-   davon Überweisung: 200 €
-   davon Quittung: 800 €

Die 800 € sind bereits bei Hil verblieben.

Daher beträgt die Ausgleichszahlung:

**3.501,90 € − 800 € = 2.701,90 €**

Nach dem Ausgleich trägt Hil insgesamt:

**200 € Überweisung + 800 € Quittung = 1.000 €**

Damit entspricht die tatsächliche Belastung der Familie genau ihrem
Teilnehmerbeitrag.

## 7. Kontenausgleich und Eigenanteil des Vereins

Der Kontenausgleich darf nicht mit dem Eigenanteil des Vereins verwechselt werden.

Die allgemeine Veranstaltungsabrechnung lautet:
```
Gesamtausgaben
- Teilnehmerbeiträge
- KJFP
- sonstige Einnahmen
=
Eigenanteil des Vereins
```

Der Eigenanteil wird vollständig vom Verein getragen. Er wird **nicht
auf die Konten verteilt**.

Der KJFP-Zuschuss wird bei der hier beschriebenen Berechnung des Kontenausgleichs **nicht auf die normalen Konton verteilt**. Er gehört
ausschließlich zur allgemeinen Veranstaltungsabrechnung.

## 8. Rückspenden

Ausgleichszahlungen, insbesondere für Fahrkosten, können später ganz
oder teilweise wieder an den Verein zurückgespendet werden.

Diese Rückspenden können den tatsächlichen Verlust bzw. Eigenanteil des
Vereins nachträglich reduzieren.

Die Rückspenden sind jedoch **nicht Bestandteil der KanuControl-Abrechnung**.

Insbesondere werden sie nicht als negative Fahrkosten, als Korrektur einer Ausgleichszahlung oder als nachträgliche Änderung der Veranstaltungsabrechnung erfasst.

Die ursprüngliche Veranstaltung bleibt mit ihren tatsächlichen Kosten und Einnahmen dokumentiert.

Die spätere Rückspende ist ein davon unabhängiger Vorgang außerhalb von KanuControl.

## 9. Grundprinzip

Das Konto beantwortet folgende Frage:

> **Welche Ausgaben hat ein Konto für die Veranstaltung vorgestreckt und welcher Teil davon ist durch bereits beim Konto verbliebenen Einnahmen gedeckt?**

Der Verein gleicht anschließend den verbleibenden Betrag aus.

Dabei gilt:

- Teilnehmerbeiträge werden aus den Teilnehmern und der Beitragsstruktur ermittelt.
- Teilnehmer werden optional normalen Konten zugeordnet.
- Ausgaben werden dem Konto zugeordnet, das sie tatsächlich bezahlt hat.
- Fahrkosten werden buchhalterisch der SYS-FG zugeordnet.
- Für den Kontenausgleich wird bei Fahrkosten über den Fahrer und dessen Teilnehmer-Zuordnung das betroffene normale Konto ermittelt.
- Die Fahrkostenbuchung bleibt dabei in SYS und wird nicht in die normale Konto verschoben.
- Ausgaben mit der Vereins-Girokarte gehören zum Vereinskonto bzw. zur VK.
- Der Zahlungsweg eines Teilnehmerbeitrags ist entscheidend.
- UEBERWEISUNG bedeutet: Geld ist beim Verein eingegangen und wird der VK zugeordnet.
- QUITTUNG bedeutet: Geld ist nachgewiesen, aber nicht auf dem Vereinskonto eingegangen und verbleibt bei dem zugeordneten normalen Konto.
- Ein Zahlungsnachweis kann sich auf einen oder mehrere Teilnehmer beziehen.
- Ein Zahlungsnachweis kann höchstens einem Konto zugeordnet sein.
- Für jedes normales Konto gibt es höchstens eine Ausgleichszahlung je Veranstaltung.
- Das Systemkonto SYS nimmt nicht am Kontenausgleich teil.
- Das Vereinskonto VK nimmt nicht am Kontenausgleich teil.
- Nur normale Konten können eine Ausgleichszahlung erhalten.
- Der Vereins-Eigenanteil wird nicht auf die Konten verteilt.
- Spätere Rückspenden gehören nicht zur KanuControl-Abrechnung.


## 10. Vereins-Girokarte

Ein Einkauf kann direkt mit der Vereins-Girokarte bezahlt werden.

In diesem Fall wurde die Ausgabe **nicht von einem normalen Konto** vorgestreckt.

Die Ausgabe wird deshalb der **VK bzw. dem Vereinskonto** zugeordnet.

Beispiel:
```
Supermarkteinkauf        350,00 €
Zahlungsart              Vereins-Girokarte

VK:
- 350,00 €
```
Dieser Betrag wird nicht als Ausgabe eines normalen Konto beim Kontenausgleich berücksichtigt.