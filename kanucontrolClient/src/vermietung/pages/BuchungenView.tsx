// src/vermietung/pages/BuchungenView.tsx

import { useCallback, useEffect, useState } from "react";
import { Box, Button, MenuItem, TextField, Typography } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";

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

const BuchungenView = () => {
  const { buchungen, selectedId, setSelectedId, sorting, setSorting, create, update, remove } =
    useBuchungen();

  const { mietobjekt } = useVermietungContext();

  const [mietobjekte, setMietobjekte] = useState<Mietobjekt[]>([]);
  const [filterMietobjektId, setFilterMietobjektId] = useState<number | "">("");
  const [filterInitialisiert, setFilterInitialisiert] = useState(false);

  const [showForm, setShowForm] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [selectedBuchung, setSelectedBuchung] = useState<Buchung | null>(null);
  const [neueBuchungsquelle, setNeueBuchungsquelle] = useState<Buchungsquelle>("DIREKT");
  const [saveError, setSaveError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const { registerBackHandler } = useBackNavigation();

  const handleSelect = (buchung: Buchung | null) => {
    setSelectedId(buchung?.id ?? null);
    setSelectedBuchung(buchung);
    setShowForm(!!buchung);
    setEditMode(false);
  };

  const handleNew = (buchungsquelle: Buchungsquelle) => {
    setSelectedId(null);
    setSelectedBuchung(null);
    setNeueBuchungsquelle(buchungsquelle);
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
      setEditMode(false);
    } catch (err: unknown) {
      console.error("Fehler beim Speichern der Buchung:", err);
      setSaveError(getApiErrorMessage(err));
    }
  };

 const handleConfirm = async () => {
   if (!selectedBuchung) return;

   setSaveError(null);

   try {
     const updated = await update(selectedBuchung.id, {
       anreise: selectedBuchung.anreise,
       abreise: selectedBuchung.abreise,
       mietobjektId: selectedBuchung.mietobjektId,
       mietbereichIds: selectedBuchung.mietbereichIds ?? [],
       positionen: (selectedBuchung.positionen ?? []).map((position) => ({
         mietbereichId: position.mietbereichId,
         anzahl: position.anzahl,
       })),
       buchungsquelle: selectedBuchung.buchungsquelle,
       mieterId: selectedBuchung.mieterId,
       veranstalterVereinId: selectedBuchung.veranstalterVereinId,
       status: "BESTAETIGT",
     });

     setSelectedBuchung(updated);
   } catch (err: unknown) {
     console.error("Fehler beim Bestätigen der Buchung:", err);
     setSaveError(getApiErrorMessage(err));
   }
 };

  const handleCancelBooking = async () => {
    if (!selectedBuchung) return;

    const updated = await update(selectedBuchung.id, {
      anreise: selectedBuchung.anreise,
      abreise: selectedBuchung.abreise,
      mietobjektId: selectedBuchung.mietobjektId,
      mietbereichIds: selectedBuchung.mietbereichIds ?? [],
      mieterId: selectedBuchung.mieterId,
      veranstalterVereinId: selectedBuchung.veranstalterVereinId,
      buchungsquelle: selectedBuchung.buchungsquelle,
      status: "STORNIERT",
      positionen: (selectedBuchung.positionen ?? []).map((position) => ({
        mietbereichId: position.mietbereichId,
        anzahl: position.anzahl,
      })),
    });

    setSelectedBuchung(updated);
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
  };

  const handleBack = useCallback(() => {
    setSelectedBuchung(null);
    setSelectedId(null);
    setShowForm(false);
    setEditMode(false);
  }, [setSelectedId]);

  const handleCancelEdit = () => {
    setEditMode(false);
  };

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

  const filteredBuchungen =
    filterMietobjektId === ""
      ? buchungen
      : buchungen.filter((buchung) => buchung.mietobjektId === filterMietobjektId);

if (showForm) {
  return (
    <Box sx={{ p: 3 }}>
      <BuchungFormView
        buchung={selectedBuchung}
        neueBuchungsquelle={neueBuchungsquelle}
        editMode={editMode}
        onEdit={() => setEditMode(true)}
        onCancelEdit={handleCancelEdit}
        onSave={handleSave}
        onConfirm={handleConfirm}
        onCancelBooking={handleCancelBooking}
        onDelete={handleDelete}
        onBack={handleBack}
        disableDelete={!selectedBuchung}
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
      <BuchungTable
        data={filteredBuchungen}
        selectedId={selectedId}
        onSelect={handleSelect}
        sorting={sorting}
        onSortingChange={setSorting}
      />

      <ErrorDialog open={!!error} message={error ?? ""} onClose={() => setError(null)} />
    </Box>
  );
};

export default BuchungenView;
