// src/vermietung/pages/BuchungenView.tsx

import { useCallback, useEffect, useState } from "react";
import { Box, Button, MenuItem, TextField, Typography } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";

import { BuchungTable } from "@/vermietung/components/anmeldung/BuchungTable";
import { BuchungFormView } from "@/vermietung/components/anmeldung/BuchungFormView";

import { useBuchungen } from "@/vermietung/hooks/useBuchungen";
import { getMietobjekte } from "@/vermietung/api/mietobjektApi";
import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";

import type { Buchung } from "@/vermietung/types/Buchung";
import type { BuchungSave } from "@/vermietung/types/BuchungSave";
import { useVermietungContext } from "@/vermietung/context/VermietungContext";
import { useBackNavigation } from "@/core/context/BackNavigationContext";


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

  const { registerBackHandler } = useBackNavigation();

  const handleSelect = (buchung: Buchung | null) => {
    setSelectedId(buchung?.id ?? null);
    setSelectedBuchung(buchung);
    setShowForm(!!buchung);
    setEditMode(false);
  };

  const handleNew = () => {
    setSelectedId(null);
    setSelectedBuchung(null);
    setShowForm(true);
    setEditMode(true);
  };

  const handleSave = async (payload: BuchungSave) => {
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
    });

    setSelectedBuchung(created);
    setSelectedId(created.id);
    setEditMode(false);
  };

  const handleConfirm = async () => {
    if (!selectedBuchung) return;

    const updated = await update(selectedBuchung.id, {
      anreise: selectedBuchung.anreise,
      abreise: selectedBuchung.abreise,
      mietobjektId: selectedBuchung.mietobjektId,
      mietbereichIds: selectedBuchung.mietbereichIds ?? [],
      mieterId: selectedBuchung.mieterId,
      veranstalterVereinId: selectedBuchung.veranstalterVereinId,
      status: "BESTAETIGT",
    });

    setSelectedBuchung(updated);
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
      status: "STORNIERT",
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
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          mb: 2,
          gap: 2,
          flexWrap: "wrap",
        }}
      >
        <Typography variant="h5">Buchungen</Typography>

        <Box sx={{ display: "flex", gap: 2, alignItems: "center" }}>
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
            sx={{ minWidth: 240 }}
          >
            <MenuItem value="">Alle Mietobjekte</MenuItem>

            {mietobjekte.map((objekt) => (
              <MenuItem key={objekt.id} value={objekt.id}>
                {objekt.bezeichnung}
              </MenuItem>
            ))}
          </TextField>

          <Button variant="contained" startIcon={<AddIcon />} onClick={handleNew}>
            Neu
          </Button>
        </Box>
      </Box>

      <BuchungTable
        data={filteredBuchungen}
        selectedId={selectedId}
        onSelect={handleSelect}
        sorting={sorting}
        onSortingChange={setSorting}
      />
    </Box>
  );
};

export default BuchungenView;
