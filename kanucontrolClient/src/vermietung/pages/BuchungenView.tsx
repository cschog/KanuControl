import AblesungBestaetigungDialog from "@/vermietung/components/buchung/AblesungBestaetigungDialog";
// src/vermietung/pages/BuchungenView.tsx

import { useCallback, useEffect, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { Box, Button, MenuItem, Tab, Tabs, TextField, Typography } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import { getBuchung } from "@/vermietung/api/buchungApi";

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
   selectedId,
   setSelectedId,
   sorting,
   setSorting,
   create,
   update,
   changeStatus,
   remove,
 } = useBuchungen();

  const { mietobjekt } = useVermietungContext();

  const [mietobjekte, setMietobjekte] = useState<Mietobjekt[]>([]);
  const [filterMietobjektId, setFilterMietobjektId] = useState<number | "">("");
  const [filterInitialisiert, setFilterInitialisiert] = useState(false);
  const [filterJahr, setFilterJahr] = useState<number | "">("");
  const [buchungsanzeige, setBuchungsanzeige] = useState<"STANDARD" | "ALLE">("STANDARD");
  const [ansicht, setAnsicht] = useState<"LISTE" | "KALENDER">("LISTE");

  const [showForm, setShowForm] = useState(false);
  const [initialZeitraum, setInitialZeitraum] = useState<{ anreiseDatum: string; abreiseDatum: string } | null>(null);
  const [editMode, setEditMode] = useState(false);
  const [selectedBuchung, setSelectedBuchung] = useState<Buchung | null>(null);
  const [neueBuchungsquelle, setNeueBuchungsquelle] = useState<Buchungsquelle>("DIREKT");
  const [ablesungBestaetigung, setAblesungBestaetigung] = useState<Buchung | null>(null);
  const [abschlussAktiv, setAbschlussAktiv] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

 const { registerBackHandler, setDetailViewOpen } = useBackNavigation();
 const location = useLocation();
 const navigate = useNavigate();
 const [searchParams] = useSearchParams();
 const buchungIdAusUrl = Number(searchParams.get("buchungId"));
 const returnTo = (location.state as { returnTo?: string } | null)?.returnTo;

  const handleStatusChange = async (status: Buchungsstatus) => {
    if (!selectedBuchung) return;

    setSaveError(null);

    try {
      const updated = await changeStatus(selectedBuchung.id, status);
      setSelectedBuchung(updated);
    } catch (err: unknown) {
      console.error("Fehler beim Ändern des Buchungsstatus:", err);
      setSaveError(getApiErrorMessage(err));
    }
  };
const handleReopen = async () => {
  if (!selectedBuchung) return;

  setSaveError(null);

  try {
    // Buchung wieder auf ABRECHENBAR setzen
    const updated = await changeStatus(selectedBuchung.id, "ABRECHENBAR");

    // Buchung erneut laden, damit auch die Verbrauchskosten-Vorschau
    // vom Backend berechnet und mitgeliefert wird.
    const aktuelleBuchung = await getBuchung(updated.id);

    setSelectedBuchung(aktuelleBuchung);
    setEditMode(true);
  } catch (err: unknown) {
    console.error("Fehler beim Wiederöffnen der Buchung:", err);
    setSaveError(getApiErrorMessage(err));
  }
};


  const handleReserve = () => handleStatusChange("RESERVIERT");

  const handleConfirm = () => handleStatusChange("BESTAETIGT");

  const handleReject = () => handleStatusChange("ABGELEHNT");

  const handleCancelBooking = () => handleStatusChange("STORNIERT");

  const abschliessen = async (buchung: Buchung, bestaetigt: boolean) => {
    const ablesungen = bestaetigt ? buchung.verbrauchskostenVorschau
      .filter(k => k.bestaetigungErforderlich)
      .map(k => ({
        zaehlerId: k.zaehlerId!, anreise: buchung.anreise, abreise: buchung.abreise,
        anfangsablesung: k.anfangsablesung!, endablesung: k.endablesung!,
        anfangsstand: k.anfangsstand!, endstand: k.endstand!,
      })) : [];
    const updated = await changeStatus(buchung.id, "ABGESCHLOSSEN", ablesungen);
    setSelectedBuchung(updated);
  };

  const handleComplete = async () => {
    if (!selectedBuchung || abschlussAktiv) return;
    setAbschlussAktiv(true);
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
        await abschliessen(aktuell, false);
      }
    } catch (err: unknown) {
      setSaveError(getApiErrorMessage(err));
    } finally {
      setAbschlussAktiv(false);
    }
  };

  const handleAblesungenBestaetigen = async () => {
    if (!ablesungBestaetigung || abschlussAktiv) return;
    setAbschlussAktiv(true);
    setSaveError(null);
    try {
      await abschliessen(ablesungBestaetigung, true);
    } catch (err: unknown) {
      setSaveError(getApiErrorMessage(err));
    } finally {
      setAblesungBestaetigung(null);
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

  const handleDelete = async () => {
    if (!selectedBuchung) {
      return;
    }

    await remove(selectedBuchung.id);

    setSelectedBuchung(null);
    setSelectedId(null);
    setShowForm(false);
    setEditMode(false);
    setInitialZeitraum(null);
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
    return passtZumMietobjekt && (buchungsanzeige === "ALLE" || !istAusgeblendet);
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
        onReserve={handleReserve}
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
        disableDelete={!selectedBuchung}
      />

      <AblesungBestaetigungDialog
        buchung={ablesungBestaetigung}
        busy={abschlussAktiv}
        onCancel={() => setAblesungBestaetigung(null)}
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
