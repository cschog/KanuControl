-- Folgemigration: bereits vergebene Nummern und Reservierungen bleiben erhalten.
-- Die vorherige Migration bleibt unverändert, damit ausgeführte Changesets gültig bleiben.
CREATE TABLE belegnummer_konto_zaehler (
    finanz_gruppe_id bigint PRIMARY KEY REFERENCES finanz_gruppe(id) ON DELETE CASCADE,
    letzter_wert integer NOT NULL CHECK (letzter_wert >= 0)
);
INSERT INTO belegnummer_konto_zaehler
SELECT g.id, coalesce(max(substring(r.belegnummer from '_([0-9]+)$')::integer), 0)
FROM finanz_gruppe g
LEFT JOIN belegnummer_register r
  ON r.veranstaltung_id = g.veranstaltung_id
 AND left(r.belegnummer, length(g.kuerzel) + 1) = g.kuerzel || '_'
 AND substring(r.belegnummer from length(g.kuerzel) + 2) ~ '^[0-9]+$'
GROUP BY g.id;
DROP TABLE belegnummer_zaehler;
ALTER TABLE belegnummer_konto_zaehler RENAME TO belegnummer_zaehler;
