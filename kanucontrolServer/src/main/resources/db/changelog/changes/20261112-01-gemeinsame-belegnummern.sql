-- Der bisherige Geltungsbereich ist eine Veranstaltungsabrechnung, nicht ein Zähler je Präfix.
ALTER TABLE zahlungsnachweis ADD COLUMN belegnummer varchar(50);
ALTER TABLE reisekostenabrechnung ADD COLUMN belegnummer varchar(50);
ALTER TABLE reisekostenabrechnung ADD COLUMN finanz_gruppe_id bigint REFERENCES finanz_gruppe(id);
CREATE TABLE belegnummer_zaehler (
    veranstaltung_id bigint PRIMARY KEY REFERENCES veranstaltung(id) ON DELETE CASCADE,
    letzter_wert integer NOT NULL CHECK (letzter_wert >= 0)
);
-- Kein FK zum Dokument: Reservierungen bleiben auch nach dessen Löschung erhalten.
CREATE TABLE belegnummer_register (
    veranstaltung_id bigint NOT NULL REFERENCES veranstaltung(id) ON DELETE CASCADE,
    belegnummer varchar(50) NOT NULL,
    PRIMARY KEY (veranstaltung_id, belegnummer)
);
DO $$
BEGIN
    IF EXISTS (
        SELECT a.veranstaltung_id, b.beleg_nummer
        FROM abrechnung_beleg b JOIN abrechnung a ON a.id = b.abrechnung_id
        GROUP BY a.veranstaltung_id, b.beleg_nummer HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION 'Belegnummernkollision im Bestand. Migration abgebrochen; bitte vorhandene Belege prüfen.';
    END IF;
END $$;
INSERT INTO belegnummer_register
SELECT a.veranstaltung_id, b.beleg_nummer
FROM abrechnung_beleg b JOIN abrechnung a ON a.id = b.abrechnung_id;
INSERT INTO belegnummer_zaehler
SELECT a.veranstaltung_id,
       greatest(coalesce(max(b.lfd_nr), 0),
                coalesce(max(substring(b.beleg_nummer from '_([0-9]+)$')::integer), 0))
FROM abrechnung a LEFT JOIN abrechnung_beleg b ON b.abrechnung_id = a.id
GROUP BY a.veranstaltung_id;
-- Deterministische, einmalige Ergänzung vorhandener Quittungen. Fehlende Konten bleiben sichtbar offen.
DO $$
DECLARE q record; nr integer; nummer text;
BEGIN
    FOR q IN SELECT z.id, z.veranstaltung_id, g.kuerzel
             FROM zahlungsnachweis z JOIN finanz_gruppe g ON g.id = z.finanz_gruppe_id
             WHERE z.zahlungsweg = 'QUITTUNG' AND z.belegnummer IS NULL
             ORDER BY z.veranstaltung_id, z.datum, z.id
    LOOP
        INSERT INTO belegnummer_zaehler VALUES (q.veranstaltung_id, 1)
        ON CONFLICT (veranstaltung_id) DO UPDATE SET letzter_wert = belegnummer_zaehler.letzter_wert + 1
        RETURNING letzter_wert INTO nr;
        nummer := q.kuerzel || '_' || lpad(nr::text, greatest(3, length(nr::text)), '0');
        INSERT INTO belegnummer_register VALUES (q.veranstaltung_id, nummer);
        UPDATE zahlungsnachweis SET belegnummer = nummer WHERE id = q.id;
    END LOOP;
END $$;
CREATE UNIQUE INDEX uk_quittung_belegnummer ON zahlungsnachweis(veranstaltung_id, belegnummer);
CREATE UNIQUE INDEX uk_reisekosten_belegnummer ON reisekostenabrechnung(veranstaltung_id, belegnummer);
