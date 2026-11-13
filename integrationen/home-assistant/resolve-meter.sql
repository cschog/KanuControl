-- Read-only. Run separately on each target database before setting the binding.
BEGIN READ ONLY;
SELECT id, bezeichnung, zaehlernummer, verbrauchsart, mengeneinheit, aktiv
FROM ekc_eschweilerkanuclub.zaehler
WHERE zaehlernummer = '9728276';
COMMIT;
-- Expect exactly one active STROM/kWh row named Strom-Zähler Bootshaus.
-- Never substitute the physical meter number for id.
