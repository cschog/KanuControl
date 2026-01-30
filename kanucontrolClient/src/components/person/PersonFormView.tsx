import React, { useEffect, useState } from "react";
import {
  Typography 
} from "@mui/material";
import { PersonBaseForm } from "@/components/person/form/PersonBaseForm";
import { PersonMembershipsCard } from "./PersonMembershipsCard";
import { PersonActionBar } from "@/components/person/PersonActionBar";
import { ConfirmDeleteDialog } from "@/components/common/ConfirmDeleteDialog";
import { AddMembershipDialog } from "@/components/person/membership/AddMembershipDialog";
import { usePersonForm } from "@/components/person/hooks/usePersonForm";

import { PersonDetail, PersonSave } from "@/api/types/Person";
import apiClient from "@/api/client/apiClient";
import { VereinRef } from "@/api/types/VereinRef";

/* =========================================================
   Props
   ========================================================= */

interface PersonFormViewProps {
  personDetail: PersonDetail | null;

  editMode: boolean;

  onEdit: () => void;
  onCancelEdit: () => void;
  onSpeichern: (person: PersonSave) => Promise<void>;

  onDeletePerson: () => void;
  onDeleteMitglied: (mitgliedId: number) => Promise<void>;
  onSetHauptverein: (mitgliedId: number) => Promise<void>;

  onStartMenue: () => void;

  btnÄndernPerson: boolean;
  btnLöschenPerson: boolean;

  onReloadPerson: () => Promise<void>;
}


/* =========================================================
   Component
   ========================================================= */

export const PersonFormView: React.FC<PersonFormViewProps> = ({
  personDetail,
  editMode,
  onEdit,
  onCancelEdit,
  onSpeichern,
  onDeletePerson,
  onDeleteMitglied,
  onSetHauptverein,
  onStartMenue,
  btnÄndernPerson,
  btnLöschenPerson,
  onReloadPerson,
}) => {
 
  const [confirmOpen, setConfirmOpen] = useState(false);
  const { form, update, buildSavePayload } = usePersonForm(personDetail);

  const [addVereinOpen, setAddVereinOpen] = useState(false);

  const [vereine, setVereine] = useState<VereinRef[]>([]);

  const zugeordneteIds = new Set(personDetail?.mitgliedschaften.map((m) => m.verein.id));

  const verfügbareVereine = vereine.filter((v) => !zugeordneteIds?.has(v.id));

  useEffect(() => {
    apiClient.get<VereinRef[]>("/verein").then((res) => setVereine(res.data));
  }, []);

  if (!personDetail || !form) {
    return (
      <Typography align="center" sx={{ mt: 4 }} color="text.secondary">
        Bitte wählen Sie eine Person aus.
      </Typography>
    );
  }

  return (
    <>
      <PersonBaseForm form={form} editMode={editMode} mode="edit" onChange={update} />

      <PersonMembershipsCard
        person={personDetail}
        editMode={editMode}
        onSetHauptverein={onSetHauptverein}
        onDeleteMitglied={onDeleteMitglied}
      />

      <PersonActionBar
        editMode={editMode}
        onEdit={onEdit}
        onCancelEdit={onCancelEdit}
        onSave={async () => {
          const payload = buildSavePayload();
          if (payload) {
            await onSpeichern(payload);
          }
        }}
        onDelete={() => setConfirmOpen(true)}
        onBack={onStartMenue}
        onAddVerein={() => setAddVereinOpen(true)}
        disableEdit={btnÄndernPerson}
        disableDelete={btnLöschenPerson}
      />

      {/* ================= DELETE ================= */}
      <ConfirmDeleteDialog
        open={confirmOpen}
        onClose={() => setConfirmOpen(false)}
        onConfirm={async () => {
          await onDeletePerson();
          setConfirmOpen(false);
        }}
        description={`Soll die Person „${personDetail.name}“ wirklich gelöscht werden?`}
      />

      {/* ================= Add Verein ================= */}
      <AddMembershipDialog
        open={addVereinOpen}
        onClose={() => setAddVereinOpen(false)}
        personId={personDetail.id}
        availableVereine={verfügbareVereine}
        onAdded={onReloadPerson}
      />
    </>
  );
};
