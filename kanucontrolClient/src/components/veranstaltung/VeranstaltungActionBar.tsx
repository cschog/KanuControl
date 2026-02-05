// src/components/veranstaltung/VeranstaltungActionBar.tsx
import React from "react";
import { BottomActionBar } from "@/components/common/BottomActionBar";
import { BottomAction } from "@/components/common/BottomActionBar";

interface Props {
  aktiv: boolean;
  editMode: boolean;

  onEdit: () => void;
  onCancelEdit: () => void;
  onSave: () => void;
  onBeenden: () => void;
  onAktivieren: () => void;
  onBack: () => void;

  disableEdit?: boolean;
}

export const VeranstaltungActionBar: React.FC<Props> = ({
  editMode,
  onEdit,
  onCancelEdit,
  onSave,
  onBeenden,
  onAktivieren,
  onBack,
  disableEdit,
}) => {
  const left: BottomAction[] = editMode
    ? [{ label: "Abbrechen", variant: "outlined", onClick: onCancelEdit }]
    : [{ label: "Zurück", variant: "outlined", onClick: onBack }];

  const right: BottomAction[] = [];

  if (!editMode) {
    right.push({
      label: "Bearbeiten",
      variant: "outlined",
      onClick: onEdit,
      disabled: disableEdit,
    });
  }

  if (editMode) {
    right.push({
      label: "Speichern",
      variant: "contained",
      onClick: onSave,
    });
  }

  right.push(
    {
      label: "Beenden",
      variant: "outlined",
      onClick: onBeenden,
    },
    {
      label: "Aktivieren",
      variant: "outlined",
      onClick: onAktivieren,
    },
  );

  return <BottomActionBar left={left} right={right} />;
};
