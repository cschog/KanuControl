import assert from "node:assert/strict";
import { test } from "node:test";
import { readFileSync } from "node:fs";
import ts from "typescript";

const source = readFileSync(new URL("../../src/vermietung/utils/verbrauchspreise.ts", import.meta.url), "utf8");
const { outputText } = ts.transpileModule(source, { compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.ESNext } });
const { waehleVerbrauchspreise } = await import(`data:text/javascript;base64,${Buffer.from(outputText).toString("base64")}`);
let id = 0;
function tarif(art, von, bis, preis) {
  return { id: ++id, mietobjektId: 1, bezeichnung: `${art} ${von}`, verbrauchsart: art,
    abrechnungsart: art === "WASSER" ? "TAGESPREIS" : "ZAEHLERSTAND", mengeneinheit: art === "STROM" ? "kWh" : "m3",
    preis: null, aktiv: true, preise: [{ gueltigVon: von, gueltigBis: bis, preis }] };
}
function historie() {
  return [tarif("STROM", "2026-07-01", null, 2), tarif("GAS", "2026-01-01", null, 3), tarif("WASSER", "2010-01-01", null, 10),
    tarif("STROM", "1999-01-01", "2009-12-31", .5), tarif("STROM", "2010-01-01", "2021-12-31", .75), tarif("STROM", "2022-01-01", "2026-06-30", 1),
    tarif("GAS", "1999-01-01", "2009-12-31", .5), tarif("GAS", "2010-01-01", "2021-12-31", 1), tarif("GAS", "2022-01-01", "2025-12-31", 2.5),
    tarif("WASSER", "1999-01-01", "2001-12-31", 4), tarif("WASSER", "2002-01-01", "2009-12-31", 5)];
}
test("Prod-Fall 2022: genau drei passende Preise statt Fehler je historische Zeile", () => {
  const auswahl = waehleVerbrauchspreise(historie(), "2022-10-14");
  assert.deepEqual(auswahl.map(a => a.preis), [1, 2.5, 10]);
  assert.ok(auswahl.every(a => a.hinweis === null));
});
test("Inklusive Grenzen und Preiswechsel", () => {
  const strom = historie().filter(k => k.verbrauchsart === "STROM");
  for (const [datum, preis] of [["2009-12-31", .5], ["2010-01-01", .75], ["2026-06-30", 1], ["2026-07-01", 2]]) {
    assert.equal(waehleVerbrauchspreise(strom, datum)[0].preis, preis);
  }
});
test("Aktiv-Altbestand und Reihenfolge ändern die Preiswahl nicht", () => {
  const daten = historie().reverse().map(k => ({ ...k, aktiv: false }));
  const auswahl = waehleVerbrauchspreise(daten, "2022-10-14");
  assert.equal(auswahl.find(a => a.konfiguration.verbrauchsart === "STROM").preis, 1);
  assert.ok(auswahl.every(a => a.hinweis === null));
});
test("Lücke liefert einen Hinweis je betroffener Verbrauchsart", () => {
  const daten = historie().filter(k => !(k.verbrauchsart === "STROM" && k.preise[0].gueltigVon === "2022-01-01"));
  const auswahl = waehleVerbrauchspreise(daten, "2022-10-14");
  assert.equal(auswahl.filter(a => a.hinweis !== null).length, 1);
  assert.match(auswahl[0].hinweis, /Kein gültiger Preis für Strom/);
  assert.equal(auswahl[0].preis, null);
});
test("Überschneidungen werden auch bei identischem Preis nicht still aufgelöst", () => {
  const daten = historie(); daten.push(tarif("STROM", "2022-10-14", null, 1));
  assert.match(waehleVerbrauchspreise(daten, "2022-10-14")[0].hinweis, /Mehrere gültige Preise für Strom/);
  const k = tarif("STROM", "2022-01-01", null, 1); k.preise.push({ gueltigVon: "2022-10-14", gueltigBis: null, preis: 1 });
  assert.match(waehleVerbrauchspreise([k], "2022-10-14")[0].hinweis, /Mehrere gültige Preise/);
});
test("Gemeinsame Historie bleibt unterstützt; ohne Datum keine willkürliche Auswahl", () => {
  const k = tarif("STROM", "2010-01-01", "2021-12-31", .75); k.preise.push({ gueltigVon: "2022-01-01", gueltigBis: null, preis: 1 });
  assert.equal(waehleVerbrauchspreise([k], "2022-10-14")[0].preis, 1);
  assert.match(waehleVerbrauchspreise(historie())[0].hinweis, /Anreisedatum/);
  assert.deepEqual(waehleVerbrauchspreise([], "2022-10-14"), []);
});
