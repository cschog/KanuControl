import { formatPersonName } from "@/core/utils/personName";
import { useCallback, useEffect, useState } from "react";
import { Alert, Accordion, AccordionDetails, AccordionSummary, Box, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { useAppContext } from "@/core/components/context/AppContext";
import { useLocation, useNavigate } from "react-router-dom";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import { getGemeinsameTeilnehmer } from "@/kjfp/api/services/teilnehmerApi";
import type { TeilnehmerList } from "@/kjfp/types/TeilnehmerList";
import { addBoot, addCrew, createFahrt, createGewaesser, createLizenz, getFahrten, getGewaesser, getKanus, getKilometer, getPrivateKanus, removeCrew, saveKanu, updateKanu } from "@/kjfp/api/services/fahrtenApi";
import type { FahrtInfo, GewaesserInfo, KanuInfo, KilometerInfo } from "@/kjfp/api/services/fahrtenApi";

export default function FahrtenPage() {
  const { active } = useAppContext();
  const location = useLocation();
  const navigate = useNavigate();
  const [fahrten, setFahrten] = useState<FahrtInfo[]>([]);
  const [kanus, setKanus] = useState<KanuInfo[]>([]);
  const [gewaesser, setGewaesser] = useState<GewaesserInfo[]>([]);
  const [kilometer, setKilometer] = useState<KilometerInfo[]>([]);
  const [teilnehmer, setTeilnehmer] = useState<TeilnehmerList[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [tab, setTab] = useState<"fahrten" | "kanus" | "gewaesser" | "auswertung">("fahrten");
  const [rideOpen, setRideOpen] = useState(false);
  const [boatOpen, setBoatOpen] = useState(false);
  const [watersName, setWatersName] = useState("");
  const [selectedRide, setSelectedRide] = useState<number | null>(null);
  const [selectedBoat, setSelectedBoat] = useState<number | null>(null);
  const [selectedCanoe, setSelectedCanoe] = useState<number | null>(null);
  const [licenseRequired, setLicenseRequired] = useState(false);
  const [newRide, setNewRide] = useState({ datum: new Date().toISOString().slice(0, 10), beginnZeit: "", endeZeit: "", gewaesserId: "", startort: "", zielort: "", kilometer: "", streckenangabe: "", bemerkung: "" });
  const [newBoat, setNewBoat] = useState({ kennzeichnung: "", bezeichnung: "", typ: "KAJAK" as "CANADIER" | "KAJAK", platzanzahl: "1", privat: false });
  const [licenseOpen, setLicenseOpen] = useState(false);
  const [license, setLicense] = useState({ lizenznummer: "", gueltigVon: "", gueltigBis: "", kosten: "" });

  const load = useCallback(async () => {
    try {
      setError(null);
      const [fleet, waters] = await Promise.all([getKanus(), getGewaesser()]);
      const eventData = active?.id && active.fahrtenAktiv
        ? await Promise.all([getFahrten(active.id), getPrivateKanus(active.id), getKilometer(active.id), getGemeinsameTeilnehmer(active.id)])
        : null;
      setFahrten(eventData?.[0] ?? []);
      setKanus([...fleet, ...(eventData?.[1] ?? [])]);
      setGewaesser(waters);
      setKilometer(eventData?.[2] ?? []);
      setTeilnehmer(eventData?.[3] ?? []);
    } catch (e: unknown) { setError(getApiErrorMessage(e)); }
  }, [active?.id, active?.fahrtenAktiv]);

  useEffect(() => { void load(); }, [load]);
  useEffect(() => {
    setTab(location.pathname.endsWith("/kanus") ? "kanus" : location.pathname.endsWith("/gewaesser") ? "gewaesser" : "fahrten");
  }, [location.pathname]);

  const fahrtenAktiv = Boolean(active?.id && active.fahrtenAktiv);
  const pageTitle = tab === "kanus" ? "Kanus" : tab === "gewaesser" ? "Flüsse" : tab === "auswertung" ? "Kilometer-Auswertung" : "Fahrten";

  const submitRide = async () => {
    if (!active || !newRide.datum || !newRide.gewaesserId || !newRide.kilometer) return;
    try {
      await createFahrt(active.id, { ...newRide, kilometer: Number(newRide.kilometer), gewaesserId: Number(newRide.gewaesserId), beginnZeit: newRide.beginnZeit || undefined, endeZeit: newRide.endeZeit || undefined });
      setRideOpen(false); setNewRide((v) => ({ ...v, startort: "", zielort: "", kilometer: "", streckenangabe: "", bemerkung: "" })); await load();
    } catch (e: unknown) { setError(getApiErrorMessage(e)); }
  };
  const submitBoat = async () => {
    try { await saveKanu({ ...newBoat, platzanzahl: Number(newBoat.platzanzahl), aktiv: true }, newBoat.privat && fahrtenAktiv ? active!.id : undefined); setBoatOpen(false); setNewBoat({ kennzeichnung: "", bezeichnung: "", typ: "KAJAK", platzanzahl: "1", privat: false }); await load(); }
    catch (e: unknown) { setError(getApiErrorMessage(e)); }
  };
  const submitWater = async () => {
    if (!watersName.trim()) return;
    try { await createGewaesser(watersName); setWatersName(""); await load(); }
    catch (e: unknown) { setError(getApiErrorMessage(e)); }
  };
  const submitLicense = async () => {
    if (!selectedBoat) return;
    try { await createLizenz(selectedBoat, { ...license, kosten: license.kosten ? Number(license.kosten) : undefined }); setLicenseOpen(false); setLicense({ lizenznummer: "", gueltigVon: "", gueltigBis: "", kosten: "" }); await load(); }
    catch (e: unknown) { setError(getApiErrorMessage(e)); }
  };

  return <Box>
    <Typography variant="h5" fontWeight={600} sx={{ mb: 2 }}>{fahrtenAktiv ? `${pageTitle} · ${active!.name}` : pageTitle}</Typography>
    {error && <Alert severity="error" onClose={() => setError(null)} sx={{ mb: 2 }}>{error}</Alert>}
    <Stack direction="row" spacing={1} sx={{ mb: 2 }}>
      <Button variant={tab === "kanus" ? "contained" : "outlined"} onClick={() => navigate("/fahrten/kanus")}>Kanus</Button>
      <Button variant={tab === "gewaesser" ? "contained" : "outlined"} onClick={() => navigate("/fahrten/gewaesser")}>Flüsse</Button>
      <Button variant={tab === "fahrten" ? "contained" : "outlined"} disabled={!fahrtenAktiv} onClick={() => navigate("/fahrten/liste")}>Fahrten</Button>
      {fahrtenAktiv && <Button variant={tab === "auswertung" ? "contained" : "outlined"} onClick={() => setTab("auswertung")}>Kilometer</Button>}
    </Stack>

    {fahrtenAktiv && tab === "fahrten" && <>
      <Button variant="contained" sx={{ mb: 2 }} onClick={() => setRideOpen(true)}>Fahrt anlegen</Button>
      {fahrten.map((f) => <Accordion key={f.id}>
        <AccordionSummary expandIcon={<ExpandMoreIcon />}><Typography fontWeight={600}>{f.datum} · {f.gewaesser.name} · {f.startort || "Start offen"} → {f.zielort || "Ziel offen"} · {f.kilometer} km</Typography></AccordionSummary>
        <AccordionDetails>
          <Stack spacing={1}>
            {f.bemerkung && <Typography>{f.bemerkung}</Typography>}
            {f.kanus.map((b) => <Paper variant="outlined" sx={{ p: 1.5 }} key={b.fahrtKanuId}>
              <Stack direction="row" alignItems="center" spacing={1} flexWrap="wrap">
                <Typography fontWeight={600}>{b.kanu.kennzeichnung} · {b.kanu.bezeichnung}</Typography>
                {b.lizenzErforderlich && <ChipLabel missing={!b.lizenzVorhanden} />}
                <Button size="small" onClick={() => { setSelectedBoat(b.kanu.id); setLicenseOpen(true); }}>Lizenz erfassen</Button>
                <TextField select size="small" label="Teilnehmer hinzufügen" value="" sx={{ minWidth: 230 }} onChange={async (e) => { try { await addCrew(active!.id, f.id, b.fahrtKanuId, Number(e.target.value)); await load(); } catch (err: unknown) { setError(getApiErrorMessage(err)); } }}>
                  {teilnehmer.filter((t) => !f.kanus.some((boat) => boat.besatzung.some((c) => c.teilnehmerId === t.id))).map((t) => <MenuItem key={t.id} value={t.id}>{formatPersonName(t.person?.name, t.person?.vorname, "first")}</MenuItem>)}
                </TextField>
              </Stack>
              <Stack direction="row" spacing={1} sx={{ mt: 1 }} flexWrap="wrap">
                {b.besatzung.map((c) => <Button size="small" variant="outlined" key={c.id} onClick={async () => { try { await removeCrew(active!.id, f.id, c.id); await load(); } catch (err: unknown) { setError(getApiErrorMessage(err)); } }}>{formatPersonName(c.name, c.vorname, "first")} ×</Button>)}
                <Typography variant="caption" color="text.secondary">{b.besatzung.length}/{b.kanu.platzanzahl} Plätze</Typography>
              </Stack>
            </Paper>)}
            <Stack direction="row" spacing={1} alignItems="center">
              <TextField select size="small" label="Kanu zuordnen" value={selectedRide === f.id ? selectedCanoe ?? "" : ""} sx={{ minWidth: 240 }} onChange={(e) => { setSelectedRide(f.id); setSelectedCanoe(Number(e.target.value)); }}>
                {kanus.filter((k) => k.aktiv && !f.kanus.some((b) => b.kanu.id === k.id)).map((k) => <MenuItem key={k.id} value={k.id}>{k.kennzeichnung} · {k.bezeichnung}{k.privat ? " (privat)" : ""}</MenuItem>)}
              </TextField>
              <FormControlLabel control={<Checkbox checked={licenseRequired} onChange={(e) => setLicenseRequired(e.target.checked)} />} label="Lizenz erforderlich" />
              <Button disabled={selectedRide !== f.id || !selectedCanoe} onClick={async () => { try { await addBoot(active!.id, f.id, selectedCanoe!, licenseRequired); setSelectedCanoe(null); setLicenseRequired(false); await load(); } catch (err: unknown) { setError(getApiErrorMessage(err)); } }}>Kanu hinzufügen</Button>
            </Stack>
          </Stack>
        </AccordionDetails>
      </Accordion>)}
      {fahrten.length === 0 && <Typography color="text.secondary">Noch keine Fahrten erfasst.</Typography>}
    </>}

    {tab === "kanus" && <Stack spacing={2}>
      <Box><Button variant="contained" onClick={() => setBoatOpen(true)}>Kanu erfassen</Button></Box>
      <Paper sx={{ p: 2 }}><Typography variant="h6">Kanuinventar</Typography>{kanus.map((k) => <Stack key={k.id} direction="row" spacing={1} alignItems="center"><Typography sx={{ flex: 1 }}>{k.kennzeichnung} · {k.bezeichnung} · {k.typ} · {k.platzanzahl} Plätze{k.privat ? " · Veranstaltungsboot" : ""}{!k.aktiv ? " · archiviert" : ""}</Typography>{!k.privat && <Button size="small" color={k.aktiv ? "warning" : "primary"} onClick={async () => { try { await updateKanu(k.id, { ...k, aktiv: !k.aktiv, eigentuemerId: k.eigentuemerId }); await load(); } catch (e: unknown) { setError(getApiErrorMessage(e)); } }}>{k.aktiv ? "Archivieren" : "Reaktivieren"}</Button>}</Stack>)}{kanus.length === 0 && <Typography color="text.secondary">Keine Kanus erfasst.</Typography>}</Paper>
    </Stack>}

    {tab === "gewaesser" && <Stack spacing={2}>
      <Paper sx={{ p: 2 }}><Typography variant="h6">Gewässer</Typography><Stack direction="row" spacing={1} sx={{ my: 1 }}><TextField size="small" label="Gewässername" value={watersName} onChange={(e) => setWatersName(e.target.value)} /><Button onClick={submitWater}>Hinzufügen</Button></Stack>{gewaesser.map((g) => <Typography key={g.id}>{g.name}</Typography>)}</Paper>
    </Stack>}

    {tab === "auswertung" && <Paper sx={{ p: 2 }}><Typography variant="h6">Kilometer nach Teilnehmer und Gewässer</Typography>{kilometer.map((k) => <Typography key={`${k.personId}-${k.gewaesser}`}>{formatPersonName(k.name, k.vorname, "first")} · {k.gewaesser}: {k.kilometer} km</Typography>)}</Paper>}

    <Dialog open={rideOpen} onClose={() => setRideOpen(false)} fullWidth maxWidth="sm"><DialogTitle>Fahrt anlegen</DialogTitle><DialogContent><Stack spacing={1.5} sx={{ pt: 1 }}>
      <TextField type="date" label="Datum" InputLabelProps={{ shrink: true }} value={newRide.datum} onChange={(e) => setNewRide({ ...newRide, datum: e.target.value })} />
      <TextField select label="Gewässer" value={newRide.gewaesserId} onChange={(e) => setNewRide({ ...newRide, gewaesserId: e.target.value })}>{gewaesser.map((g) => <MenuItem key={g.id} value={g.id}>{g.name}</MenuItem>)}</TextField>
      <Stack direction="row" spacing={1}><TextField label="Startort" value={newRide.startort} onChange={(e) => setNewRide({ ...newRide, startort: e.target.value })} /><TextField label="Zielort" value={newRide.zielort} onChange={(e) => setNewRide({ ...newRide, zielort: e.target.value })} /></Stack>
      <Stack direction="row" spacing={1}><TextField type="time" label="Beginn" InputLabelProps={{ shrink: true }} value={newRide.beginnZeit} onChange={(e) => setNewRide({ ...newRide, beginnZeit: e.target.value })} /><TextField type="time" label="Ende" InputLabelProps={{ shrink: true }} value={newRide.endeZeit} onChange={(e) => setNewRide({ ...newRide, endeZeit: e.target.value })} /><TextField type="number" label="Kilometer" value={newRide.kilometer} onChange={(e) => setNewRide({ ...newRide, kilometer: e.target.value })} /></Stack>
      <TextField label="Weitere Streckenangabe" value={newRide.streckenangabe} onChange={(e) => setNewRide({ ...newRide, streckenangabe: e.target.value })} /><TextField label="Bemerkung" multiline value={newRide.bemerkung} onChange={(e) => setNewRide({ ...newRide, bemerkung: e.target.value })} />
    </Stack></DialogContent><DialogActions><Button onClick={() => setRideOpen(false)}>Abbrechen</Button><Button onClick={submitRide} variant="contained">Speichern</Button></DialogActions></Dialog>
    <Dialog open={boatOpen} onClose={() => setBoatOpen(false)} fullWidth maxWidth="sm"><DialogTitle>Kanu erfassen</DialogTitle><DialogContent><Stack spacing={1.5} sx={{ pt: 1 }}>
      {fahrtenAktiv && <FormControlLabel control={<Checkbox checked={newBoat.privat} onChange={(e) => setNewBoat({ ...newBoat, privat: e.target.checked })} />} label="Privates Kanu dieser Veranstaltung" />}
      <TextField label="Kennzeichnung" value={newBoat.kennzeichnung} onChange={(e) => setNewBoat({ ...newBoat, kennzeichnung: e.target.value })} /><TextField label="Bezeichnung" value={newBoat.bezeichnung} onChange={(e) => setNewBoat({ ...newBoat, bezeichnung: e.target.value })} />
      <TextField select label="Typ" value={newBoat.typ} onChange={(e) => setNewBoat({ ...newBoat, typ: e.target.value as "CANADIER" | "KAJAK" })}><MenuItem value="CANADIER">Canadier</MenuItem><MenuItem value="KAJAK">Kajak</MenuItem></TextField>
      <TextField type="number" label="Plätze (1–4)" inputProps={{ min: 1, max: 4 }} value={newBoat.platzanzahl} onChange={(e) => setNewBoat({ ...newBoat, platzanzahl: e.target.value })} />
    </Stack></DialogContent><DialogActions><Button onClick={() => setBoatOpen(false)}>Abbrechen</Button><Button onClick={submitBoat} variant="contained">Speichern</Button></DialogActions></Dialog>
    <Dialog open={licenseOpen} onClose={() => setLicenseOpen(false)} fullWidth maxWidth="sm"><DialogTitle>Kanu-Lizenz erfassen</DialogTitle><DialogContent><Stack spacing={1.5} sx={{ pt: 1 }}><TextField label="Lizenznummer" value={license.lizenznummer} onChange={(e) => setLicense({ ...license, lizenznummer: e.target.value })} /><TextField type="date" label="Gültig von" InputLabelProps={{ shrink: true }} value={license.gueltigVon} onChange={(e) => setLicense({ ...license, gueltigVon: e.target.value })} /><TextField type="date" label="Gültig bis" InputLabelProps={{ shrink: true }} value={license.gueltigBis} onChange={(e) => setLicense({ ...license, gueltigBis: e.target.value })} /><TextField type="number" label="Kosten" value={license.kosten} onChange={(e) => setLicense({ ...license, kosten: e.target.value })} /></Stack></DialogContent><DialogActions><Button onClick={() => setLicenseOpen(false)}>Abbrechen</Button><Button onClick={submitLicense} variant="contained">Speichern</Button></DialogActions></Dialog>
  </Box>;
}

function ChipLabel({ missing }: { missing: boolean }) {
  return <Typography variant="caption" color={missing ? "error" : "success.main"}>{missing ? "Lizenz fehlt" : "Lizenz vorhanden"}</Typography>;
}
