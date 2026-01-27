import React, { useState, useEffect, useCallback } from "react";
import {
  Box,
  Typography,
  Button,
  Snackbar,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from "@mui/material";

import { BottomActionBar } from "@/components/common/BottomActionBar";

import api from "@/api/client/apiClient";
import { PersonEditForm } from "@/components/person/PersonEditForm";
import { createMitglied } from "@/api/services/mitgliedServices";

import { FormFeld } from "@/components/common/FormFeld";
import { Person } from "@/api/types/Person";
import { VereinFormModel } from "@/api/types/VereinFormModel";
import { VereinRef } from "@/api/types/VereinRef";

interface PersonFormViewProps {
  onNeuePerson: () => void;
  btnNeuePerson: boolean;

  // 🔑 FEHLTE
  onÄndernPerson: (person: Person) => Promise<void>;

  btnÄndernPerson: boolean;

  onDeletePerson: () => void;
  btnLöschenPerson: boolean;

  onStartMenue: () => void;
  selectedPerson: Person | null;
}

export const PersonFormView: React.FC<PersonFormViewProps> = ({
  onNeuePerson,
  btnNeuePerson,
  onÄndernPerson,
  btnÄndernPerson,
  onDeletePerson,
  btnLöschenPerson,
  onStartMenue,
  selectedPerson,
}) => {
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [snackbarMsg, setSnackbarMsg] = useState<string | null>(null);
  const [snackbarSeverity, setSnackbarSeverity] = useState<"success" | "warning">("success");

  const handleDeleteConfirm = useCallback(() => {
    if (!selectedPerson) return;

    onDeletePerson();
    setConfirmOpen(false);
    setSnackbarMsg(`${selectedPerson.name} wurde gelöscht`);
    setSnackbarSeverity("success");
  }, [onDeletePerson, selectedPerson]);

  const handleDeleteCancel = () => {
    setConfirmOpen(false);
    if (selectedPerson) {
      setSnackbarMsg(`${selectedPerson.name} wurde nicht gelöscht`);
      setSnackbarSeverity("warning");
    }
  };

  const [editOpen, setEditOpen] = useState(false);
  const [vereineRef, setVereineRef] = useState<VereinRef[]>([]);

  useEffect(() => {
    api.get<VereinFormModel[]>("/verein").then((res) => {
      const mapped: VereinRef[] = res.data.map(
        (v): VereinRef => ({
          id: v.id!, // bei GET immer vorhanden
          name: v.name,
        }),
      );
      setVereineRef(mapped);
    });
  }, []);

  console.log("Person für Edit:", selectedPerson);
  console.log("Mitgliedschaften:", selectedPerson?.mitgliedschaften);

  return (
    <>
      <Box maxWidth="lg" mx="auto" p={3} borderRadius={2} boxShadow={3} bgcolor="background.paper">
        <Typography variant="h6" gutterBottom align="center">
          Mitgliederdetails
        </Typography>

        {selectedPerson ? (
          <Box
            display="grid"
            gridTemplateColumns={{
              xs: "1fr",
              sm: "1fr 1fr",
              md: "1fr 1fr 1fr",
            }}
            gap={2}
          >
            <FormFeld label="Name" value={selectedPerson.name} disabled />
            <FormFeld label="Vorname" value={selectedPerson.vorname} disabled />
            <FormFeld label="Sex" value={selectedPerson.sex} disabled />
            <FormFeld label="Alter" value={selectedPerson.alter ?? "-"} disabled />
            <FormFeld label="Straße" value={selectedPerson.strasse} disabled />
            <FormFeld label="PLZ" value={selectedPerson.plz} disabled />
            <FormFeld label="Ort" value={selectedPerson.ort} disabled />
            <FormFeld label="Land" value={selectedPerson.countryCode} disabled />
            <FormFeld label="Telefon" value={selectedPerson.telefon} disabled />
            <FormFeld label="Festnetz" value={selectedPerson.telefonFestnetz} disabled />
            <FormFeld label="Bank" value={selectedPerson.bankName} disabled />
            <FormFeld label="IBAN" value={selectedPerson.iban} disabled />
            <FormFeld label="Aktiv" value={selectedPerson.aktiv} disabled />
          </Box>
        ) : (
          <Typography color="text.secondary" align="center" sx={{ fontStyle: "italic", mt: 2 }}>
            Bitte wählen Sie ein Mitglied aus der Tabelle aus.
          </Typography>
        )}
      </Box>

      {/* ❗ Delete Confirm Dialog */}
      <Dialog open={confirmOpen} onClose={handleDeleteCancel}>
        <DialogTitle>Löschen bestätigen</DialogTitle>
        <DialogContent>
          {selectedPerson && `Soll die Person "${selectedPerson.name}" wirklich gelöscht werden?`}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleDeleteCancel}>Abbruch</Button>
          <Button color="error" onClick={handleDeleteConfirm}>
            Löschen
          </Button>
        </DialogActions>
      </Dialog>

      {editOpen && selectedPerson && (
        <Dialog open onClose={() => setEditOpen(false)} maxWidth="lg" fullWidth>
          <DialogTitle>Person bearbeiten</DialogTitle>
          <DialogContent>
            <PersonEditForm
              person={selectedPerson}
              vereine={vereineRef}
              onSave={async (p) => {
                await onÄndernPerson(p); // 🔑 NUR hochreichen
                setEditOpen(false);
              }}
              onCreateMitglied={createMitglied}
              onCancel={() => setEditOpen(false)}
            />
          </DialogContent>
        </Dialog>
      )}

      <BottomActionBar
        left={[
          {
            label: "Neue Person",
            onClick: onNeuePerson,
            disabled: btnNeuePerson,
          },
          {
            label: "Bearbeiten",
            onClick: () => setEditOpen(true),
            disabled: btnÄndernPerson || !selectedPerson,
            variant: "outlined",
          },
          {
            label: "Löschen",
            onClick: () => setConfirmOpen(true),
            disabled: btnLöschenPerson || !selectedPerson,
            color: "error",
            variant: "outlined",
          },
          {
            label: "Zurück",
            onClick: onStartMenue,
          },
        ]}
      />

      {/* 🔔 Feedback */}
      <Snackbar open={!!snackbarMsg} autoHideDuration={3000} onClose={() => setSnackbarMsg(null)}>
        <Alert severity={snackbarSeverity}>{snackbarMsg}</Alert>
      </Snackbar>
    </>
  );
};
