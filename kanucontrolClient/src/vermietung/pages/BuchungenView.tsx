import AblesungBestaetigungDialog from "@/vermietung/components/buchung/AblesungBestaetigungDialog";
// src/vermietung/pages/BuchungenView.tsx

import { useCallback, useEffect, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, MenuItem, Tab, Tabs, TextField, Typography } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import { brauchtAbrechnungsLoeschbestaetigung, getBuchung } from "@/vermietung/api/buchungApi";

import { ErrorDialog } from "@/core/components/common/ErrorDialog";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";

import { BuchungTable } from "@/vermietung/components/buchung/BuchungTable";
import { BuchungFormView } from "@/vermietung/components/buchung/BuchungFormView";

import { useBuchungen } from "@/vermietung/hooks/useBuchungen";
import { getMietobjekte } from "@/vermietung/api/mietobjektApi";
import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";

import type { Buchung } from "@/vermietung/types/Buchung";
import type { BuchungSave } from "@/vermietung/types/BuchungSave";
import { useVermietungContext } from "@/vermietung/context/VermietungContext";
import { useBackNavigation } from "@/core/context/BackNavigationContext";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";
import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";
import Buchungskalender from "@/vermietung/components/buchung/Buchungskalender";

const BuchungenView = () => {
 const {
   buchungen,
   reload,
   selectedId,
   setSelectedId,
   sorting,
   setSorting,
   create,
   update,
   changeStatus,
   changeBuchungsnummer,
   remove,
 } = useBuchungen();

  const { mietobjekt } = useVermietungContext();
  const [searchParams, setSearchParams] = useSearchParams();
  // Buchung.mieterId refers to Person.id, not the separate Mieter entity ID.
  const mieterParam = Number(searchParams.get("mieterId"));
  const mieterIdFilter = Number.isSafeInteger(mieterParam) && mieterParam > 0 ? mieterParam : null;

  const [mietobjekte, setMietobjekte] = useState<Mietobjekt[]>([]);
  const [filterMietobjektId, setFilterMietobjektId] = useState<number | "">("");
  const [filterInitialisiert, setFilterInitialisiert] = useState(mieterIdFilter !== null);
  const [filterJahr, setFilterJahr] = useState<number | "">("");
  const [buchungsanzeige, setBuchungsanzeige] = useState<"STANDARD" | "ALLE">(mieterIdFilter ? "ALLE" : "STANDARD");
  const [ansicht, setAnsicht] = useState<"LISTE" | "KALENDER">("LISTE");

  const [showForm, setShowForm] = useState(false);
  const [initialZeitraum, setInitialZeitraum] = useState<{ anreiseDatum: string; abreiseDatum: string } | null>(null);
  const [editMode, setEditMode] = useState(false);
  const [selectedBuchung, setSelectedBuchung] = useState<Buchung | null>(null);
  const [neueBuchungsquelle, setNeueBuchungsquelle] = useState<Buchungsquelle>("DIREKT");
  const [ablesungBestaetigung, setAblesungBestaetigung] = useState<Buchung | null>(null);
  const [abschlussSendEmail, setAbschlussSendEmail] = useState(false);
  const [abschlussFingerprint, setAbschlussFingerprint] = useState<string | undefined>();
  const [abschlussAktiv, setAbschlussAktiv] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [loeschbestaetigung, setLoeschbestaetigung] = useState<{ id: number; nummer: string; message: string } | null>(null);
  const [loeschenAktiv, setLoeschenAktiv] = useState(false);
  const [error, setError] = useState<string | null>(null);

 const { registerBackHandler, setDetailViewOpen } = useBackNavigation();
 const location = useLocation();
 const navigate = useNavigate();
 const buchungIdAusUrl = Number(searchParams.get("buchungId"));
 const returnTo = (location.state as { returnTo?: string } | null)?.returnTo;

  const refreshSelectedBuchung = async (id: number) => {
    try { setSelectedBuchung(await getBuchung(id)); } catch { /* retain the original error */ }
  };

  const handleStatusChange = async (status: Buchungsstatus, sendEmail = false, fingerprint?: string) => {
    if (!selectedBuchung) return;

    setSaveError(null);

    try {
      const updated = await changeStatus(selectedBuchung.id, status, [], sendEmail, fingerprint);
      setSelectedBuchung(updated);
      // Die Status-API liefert nicht in jedem Fall die berechnete
      // Verbrauchskosten-Vorschau mit. Für die Detailansicht deshalb die
      // vollständige Buchung nach dem Statuswechsel erneut laden.
      await refreshSelectedBuchung(updated.id);
    } catch (err: unknown) {
      console.error("Fehler beim Ändern des Buchungsstatus:", err);
      await refreshSelectedBuchung(selectedBuchung.id);
      setSaveError(getApiErrorMessage(err));
      throw err;
    }
  };
const handleReopen = async (sendEmail = false, fingerprint?: string) => {
  if (!selectedBuchung) return;

  setSaveError(null);

  try {
    // Buchung wieder auf ABRECHENBAR setzen
    const updated = await changeStatus(selectedBuchung.id, "ABRECHENBAR", [], sendEmail, fingerprint);

    // Buchung erneut laden, damit auch die Verbrauchskosten-Vorschau
    // vom Backend berechnet und mitgeliefert wird.
    const aktuelleBuchung = await getBuchung(updated.id);

    setSelectedBuchung(aktuelleBuchung);
    setEditMode(true);
  } catch (err: unknown) {
    console.error("Fehler beim Wiederöffnen der Buchung:", err);
    await refreshSelectedBuchung(selectedBuchung.id);
    setSaveError(getApiErrorMessage(err));
      throw err;
  }
};


  const handleReserve = (sendEmail: boolean, fingerprint?: string) => handleStatusChange("RESERVIERT", sendEmail, fingerprint);

  const handleConfirm = (sendEmail: boolean, fingerprint?: string) => handleStatusChange("BESTAETIGT", sendEmail, fingerprint);

  const handleReject = (sendEmail: boolean, fingerprint?: string) => handleStatusChange("ABGELEHNT", sendEmail, fingerprint);

  const handleCancelBooking = (sendEmail: boolean, fingerprint?: string) => handleStatusChange("STORNIERT", sendEmail, fingerprint);

  const abschliessen = async (buchung: Buchung, bestaetigt: boolean, sendEmail = false, fingerprint?: string) => {
    const ablesungen = bestaetigt ? buchung.verbrauchskostenVorschau
      .filter(k => k.bestaetigungErforderlich)
      .map(k => ({
        zaehlerId: k.zaehlerId!, anreise: buchung.anreise, abreise: buchung.abreise,
        anfangsablesung: k.anfangsablesung!, endablesung: k.endablesung!,
        anfangsstand: k.anfangsstand!, endstand: k.endstand!,
      })) : [];
    const updated = await changeStatus(buchung.id, "ABGESCHLOSSEN", ablesungen, sendEmail, fingerprint);
    setSelectedBuchung(updated);
    await refreshSelectedBuchung(updated.id);
  };

  const handleComplete = async (sendEmail: boolean, fingerprint?: string) => {
    if (!selectedBuchung || abschlussAktiv) return;
    setAbschlussAktiv(true);
    setAbschlussSendEmail(sendEmail);
    setAbschlussFingerprint(fingerprint);
    setSaveError(null);
    try {
      const aktuell = await getBuchung(selectedBuchung.id);
      setSelectedBuchung(aktuell);
      const vorschau = aktuell.verbrauchskostenVorschau ?? [];
      if (vorschau.some(k => !k.erfolgreich && !k.bestaetigungErforderlich)) {
        setSaveError("Die Verbrauchskosten sind unvollständig. Bitte die Hinweise bei den Verbrauchswerten prüfen.");
      } else if (vorschau.some(k => k.bestaetigungErforderlich)) {
        setAblesungBestaetigung(aktuell);
      } else {
        await abschliessen(aktuell, false, sendEmail, fingerprint);
      }
    } catch (err: unknown) {
      if (selectedBuchung) await refreshSelectedBuchung(selectedBuchung.id);
      setSaveError(getApiErrorMessage(err));
      throw err;
    } finally {
      setAbschlussAktiv(false);
    }
  };

  const handleAblesungenBestaetigen = async () => {
    if (!ablesungBestaetigung || abschlussAktiv) return;
    setAbschlussAktiv(true);
    setSaveError(null);
    try {
      await abschliessen(ablesungBestaetigung, true, abschlussSendEmail, abschlussFingerprint);
    } catch (err: unknown) {
      await refreshSelectedBuchung(ablesungBestaetigung.id);
      setSaveError(getApiErrorMessage(err));
    } finally {
      setAblesungBestaetigung(null);
      setAbschlussSendEmail(false);
      setAbschlussFingerprint(undefined);
      setAbschlussAktiv(false);
    }
  };

  const handleSelect = useCallback(async (buchung: Buchung | null) => {
    setInitialZeitraum(null);
    setSelectedId(buchung?.id ?? null);
    setSelectedBuchung(null);
    setShowForm(!!buchung);
    setEditMode(false);

    if (!buchung) {
      return;
    }

    try {
      const aktuelleBuchung = await getBuchung(buchung.id);
      setSelectedBuchung(aktuelleBuchung);
    } catch (err: unknown) {
      console.error("Fehler beim Laden der Buchung:", err);
      setError(getApiErrorMessage(err));
      setShowForm(false);
    }
  }, [setSelectedId]);

  useEffect(() => {
    if (!Number.isFinite(buchungIdAusUrl) || buchungIdAusUrl <= 0 || buchungen.length === 0) return;
    if (showForm && selectedId === buchungIdAusUrl) return;

    const zielBuchung = buchungen.find((buchung) => buchung.id === buchungIdAusUrl);
    if (zielBuchung) void handleSelect(zielBuchung);
  }, [buchungIdAusUrl, buchungen, handleSelect, selectedId, showForm]);

  const handleNew = (
    buchungsquelle: Buchungsquelle,
    zeitraum?: { anreiseDatum: string; abreiseDatum: string },
  ) => {
    setSelectedId(null);
    setSelectedBuchung(null);
    setNeueBuchungsquelle(buchungsquelle);
    setInitialZeitraum(zeitraum ?? null);
    setShowForm(true);
    setEditMode(true);
  };


  const handleSave = async (payload: BuchungSave) => {
    setSaveError(null);

    try {
      if (selectedBuchung) {
        const updated = await update(selectedBuchung.id, payload);

        setSelectedBuchung(updated);
        // Die Update-Antwort enthält nicht immer die berechnete
        // Verbrauchskosten-Vorschau. Nach dem Speichern die Detaildaten neu laden.
        await refreshSelectedBuchung(updated.id);
        setEditMode(false);
        return;
      }

      if (!mietobjekt) {
        return;
      }

      const created = await create({
        ...payload,
        mietobjektId: mietobjekt.id,
        buchungsquelle: neueBuchungsquelle,
      });

      setSelectedBuchung(created);
      setSelectedId(created.id);
      setInitialZeitraum(null);
      setEditMode(false);
    } catch (err: unknown) {
      console.error("Fehler beim Speichern der Buchung:", err);
      setSaveError(getApiErrorMessage(err));
    }
  };

  const loeschen = async (buchung: { id: number; nummer: string }, bestaetigt = false) => {
    if (loeschenAktiv) return;
    setLoeschenAktiv(true);
    setSaveError(null);
    try {
      await remove(buchung.id, bestaetigt);
      setLoeschbestaetigung(null);
      setSelectedBuchung(null);
      setSelectedId(null);
      setShowForm(false);
      setEditMode(false);
      setInitialZeitraum(null);
    } catch (err: unknown) {
      if (brauchtAbrechnungsLoeschbestaetigung(err)) {
        setLoeschbestaetigung({ ...buchung, message: getApiErrorMessage(err) });
      } else {
        setLoeschbestaetigung(null);
        setSaveError(getApiErrorMessage(err));
      }
    } finally {
      setLoeschenAktiv(false);
    }
  };

  const handleDelete = async () => {
    if (selectedBuchung) {
      await loeschen({ id: selectedBuchung.id, nummer: selectedBuchung.buchungsnummer });
    }
  };

  const handleBack = useCallback(() => {
    if (returnTo) {
      navigate(returnTo);
      return;
    }

    setSelectedBuchung(null);
    setSelectedId(null);
    setInitialZeitraum(null);
    setShowForm(false);
    setEditMode(false);
  }, [navigate, returnTo, setSelectedId]);

  const handleCancelEdit = () => {
    setEditMode(false);
  };

  useEffect(() => {
    setDetailViewOpen(showForm);

    return () => {
      setDetailViewOpen(false);
    };
  }, [showForm, setDetailViewOpen]);

  useEffect(() => {
    if (showForm) {
      registerBackHandler(handleBack);
    } else {
      registerBackHandler(null);
    }

    return () => {
      registerBackHandler(null);
    };
  }, [showForm, handleBack, registerBackHandler]);

  useEffect(() => {
    const loadMietobjekte = async () => {
      try {
        const data = await getMietobjekte();
        setMietobjekte(data);
      } catch (error) {
        console.error("Fehler beim Laden der Mietobjekte:", error);
      }
    };

    void loadMietobjekte();
  }, []);

  useEffect(() => {
    if (mietobjekt && !filterInitialisiert) {
      setFilterMietobjektId(mietobjekt.id);
      setFilterInitialisiert(true);
    }
  }, [mietobjekt, filterInitialisiert]);

  const buchungsjahre = [...new Set(
    buchungen.map((buchung) => Number(buchung.anreise.slice(0, 4)))
      .filter((jahr) => Number.isInteger(jahr) && jahr > 0),
  )].sort((a, b) => b - a);

  const gefilterteBuchungen = buchungen.filter((buchung) => {
    const passtZumMietobjekt = filterMietobjektId === "" || buchung.mietobjektId === filterMietobjektId;
    const istAusgeblendet = buchung.status === "ABGELEHNT" || buchung.status === "STORNIERT";
    return (mieterIdFilter === null || buchung.mieterId === mieterIdFilter) && passtZumMietobjekt && (buchungsanzeige === "ALLE" || !istAusgeblendet);
  });

  // Später beginnende Buchungen stehen oben; fehlende/ungültige Termine kommen ans Ende.
  const filteredBuchungen = [...gefilterteBuchungen].sort((a, b) => {
    const beginnA = Date.parse(a.anreise ?? "");
    const beginnB = Date.parse(b.anreise ?? "");
    const istBeginnAGueltig = Number.isFinite(beginnA);
    const istBeginnBGueltig = Number.isFinite(beginnB);

    if (!istBeginnAGueltig || !istBeginnBGueltig) {
      return Number(istBeginnBGueltig) - Number(istBeginnAGueltig);
    }

    return beginnB - beginnA;
  });

if (showForm) {
  return (
    <Box sx={{ p: 3 }}>
      <BuchungFormView
        buchung={selectedBuchung}
        initialZeitraum={initialZeitraum}
        neueBuchungsquelle={neueBuchungsquelle}
        editMode={editMode}
        onEdit={() => setEditMode(true)}
        onCancelEdit={handleCancelEdit}
        onSave={handleSave}
        onRefresh={async () => { if (selectedBuchung) await refreshSelectedBuchung(selectedBuchung.id); await reload(); }}
        onReserve={handleReserve}
        onBuchungsnummerChange={async nummer => {
          if (!selectedBuchung) return;
          const result = await changeBuchungsnummer(selectedBuchung.id, nummer, selectedBuchung.buchungsnummer);
          setSelectedBuchung(current => current?.id === result.id ? { ...current, buchungsnummer: result.buchungsnummer } : current);
        }}
        onConfirm={handleConfirm}
        onReopen={handleReopen}
        onReject={handleReject}
        onCancelBooking={handleCancelBooking}
        onComplete={handleComplete}
        onDelete={handleDelete}
        onBack={handleBack}
        onOpenFinanzen={() => {
          if (!selectedBuchung) return;
          navigate(`/vermietung/abrechnung?buchungId=${selectedBuchung.id}`, {
            state: {
              returnTo: `${location.pathname}${location.search}`,
              returnToState: location.state ?? null,
            },
          });
        }}
        disableDelete={!selectedBuchung || loeschenAktiv || !!loeschbestaetigung}
      />

      <Dialog open={!!loeschbestaetigung} onClose={() => { if (!loeschenAktiv) setLoeschbestaetigung(null); }}
        aria-labelledby="abrechnungen-loeschen-titel" aria-describedby="abrechnungen-loeschen-hinweis">
        <DialogTitle id="abrechnungen-loeschen-titel">Buchung {loeschbestaetigung?.nummer} samt Abrechnungen löschen?</DialogTitle>
        <DialogContent>
          <DialogContentText id="abrechnungen-loeschen-hinweis">{loeschbestaetigung?.message}</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button disabled={loeschenAktiv} onClick={() => setLoeschbestaetigung(null)}>Abbrechen</Button>
          <Button color="error" disabled={loeschenAktiv} onClick={() => { if (loeschbestaetigung) void loeschen(loeschbestaetigung, true); }}>
            {loeschenAktiv ? "Wird gelöscht …" : "Buchung und alle Abrechnungen endgültig löschen"}
          </Button>
        </DialogActions>
      </Dialog>

      <AblesungBestaetigungDialog
        buchung={ablesungBestaetigung}
        busy={abschlussAktiv}
        onCancel={() => { setAblesungBestaetigung(null); setAbschlussSendEmail(false); }}
        onConfirm={() => void handleAblesungenBestaetigen()}
      />

      <ErrorDialog
        open={!!saveError}
        message={saveError ?? ""}
        onClose={() => setSaveError(null)}
      />
    </Box>
  );
}

  return (
    <Box sx={{ p: 3 }}>
      {mieterIdFilter !== null && <Alert severity="info" sx={{ mb: 2 }} action={<Button color="inherit" onClick={() => {
        setSearchParams(current => { const next = new URLSearchParams(current); next.delete("mieterId"); return next; });
      }}>Mieterfilter aufheben</Button>}>
        {(() => { const b = buchungen.find(item => item.mieterId === mieterIdFilter);
          return b ? `Buchungen von ${b.mieterVorname} ${b.mieterName}` : "Buchungen des ausgewählten Mieters"; })()}
      </Alert>}
      {/* Header */}
      <Box
        sx={{
          display: "flex",
          flexDirection: { xs: "column", sm: "row" },
          justifyContent: "space-between",
          alignItems: { xs: "stretch", sm: "center" },
          mb: 2,
          gap: 2,
        }}
      >
        <Typography variant="h5">Buchungen</Typography>

        <Box
          sx={{
            display: "flex",
            flexDirection: { xs: "column", sm: "row" },
            gap: 1,
            flexWrap: "wrap",
            width: { xs: "100%", sm: "auto" },
          }}
        >
          <TextField
            select
            size="small"
            label="Mietobjekt"
            value={
              filterMietobjektId !== "" &&
              mietobjekte.some((objekt) => objekt.id === filterMietobjektId)
                ? filterMietobjektId
                : ""
            }
            onChange={(e) =>
              setFilterMietobjektId(e.target.value === "" ? "" : Number(e.target.value))
            }
            sx={{ width: { xs: "100%", sm: 240 } }}
          >
            <MenuItem value="">Alle Mietobjekte</MenuItem>

            {mietobjekte.map((objekt) => (
              <MenuItem key={objekt.id} value={objekt.id}>
                {objekt.bezeichnung}
              </MenuItem>
            ))}
          </TextField>

          {ansicht === "LISTE" && (
            <TextField
              select
              size="small"
              label="Jahr"
              value={filterJahr}
              onChange={(event) =>
                setFilterJahr(event.target.value === "" ? "" : Number(event.target.value))
              }
              sx={{ width: { xs: "100%", sm: 150 } }}
            >
              <MenuItem value="">Alle Jahre</MenuItem>
              {buchungsjahre.map((jahr) => (
                <MenuItem key={jahr} value={jahr}>{jahr}</MenuItem>
              ))}
            </TextField>
          )}

          <TextField
            select
            size="small"
            label="Buchungen"
            value={buchungsanzeige}
            onChange={(event) => setBuchungsanzeige(event.target.value as "STANDARD" | "ALLE")}
            sx={{ width: { xs: "100%", sm: 230 } }}
          >
            <MenuItem value="STANDARD">Ohne abgelehnte und stornierte</MenuItem>
            <MenuItem value="ALLE">Alle Buchungen</MenuItem>
          </TextField>

          <Box
            sx={{
              display: "flex",
              gap: 1,
              width: { xs: "100%", sm: "auto" },
            }}
          >
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => handleNew("DIREKT")}
              sx={{ flex: 1, whiteSpace: "nowrap" }}
            >
              Neu
            </Button>

            {mietobjekt?.airbnbAktiv && (
              <Button
                variant="contained"
                startIcon={<AddIcon />}
                onClick={() => handleNew("AIRBNB")}
                sx={{ flex: 1, whiteSpace: "nowrap" }}
              >
                Airbnb
              </Button>
            )}
          </Box>
        </Box>
      </Box>

      {/* Tabelle unterhalb des Headers */}
      <Tabs value={ansicht} onChange={(_, value) => setAnsicht(value)} sx={{ mb: 2 }}>
        <Tab value="LISTE" label="Liste" />
        <Tab value="KALENDER" label="Kalender" />
      </Tabs>

      {ansicht === "LISTE" ? (
        <BuchungTable
          data={filteredBuchungen.filter((buchung) =>
            filterJahr === "" || Number(buchung.anreise.slice(0, 4)) === filterJahr,
          )}
          selectedId={selectedId}
          onSelect={handleSelect}
          sorting={sorting}
          onSortingChange={setSorting}
        />
      ) : (
        <Buchungskalender
          objekte={mietobjekte.filter(o => filterMietobjektId === "" || o.id === filterMietobjektId)}
          buchungen={filteredBuchungen}
          onSelect={(buchung) => void handleSelect(buchung)}
          canCreateRange={filterMietobjektId !== "" && filterMietobjektId === mietobjekt?.id}
          onCreateRange={(anreiseDatum, abreiseDatum) => {
            const buchungsquelle = mietobjekt?.direktbuchungAktiv ? "DIREKT" : "AIRBNB";
            handleNew(buchungsquelle, { anreiseDatum, abreiseDatum });
          }}
        />
      )}

      <ErrorDialog open={!!error} message={error ?? ""} onClose={() => setError(null)} />
    </Box>
  );
};

export default BuchungenView;
