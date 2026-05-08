import React from "react";
import { BottomActionBar } from "@/components/common/BottomActionBar";

interface Props {
  aktiv: boolean;
  editMode: boolean;

  onEdit: () => void;
  onCopy: () => void;
  onCancelEdit: () => void;
  onSave: () => void;
  onDelete: () => void; // ⭐ FEHLTE
  onBack: () => void;
  onActivate: () => void;

  disableEdit: boolean;
  disableDelete: boolean;
}

export const VeranstaltungActionBar: React.FC<Props> = ({
  aktiv,
  editMode,
  onEdit,
  onCopy,
  onCancelEdit,
  onSave,
  onDelete,
  onBack,
  onActivate,
  disableEdit,
  disableDelete,
}) => {
  if (editMode) {
    return (
      <BottomActionBar
        left={[
          { label: "Speichern", onClick: onSave, variant: "contained" },
          { label: "Abbrechen", onClick: onCancelEdit, variant: "outlined" },
        ]}
      />
    );
  }

  return (
    <BottomActionBar
      left={[
        {
          label: "Ändern",
          onClick: onEdit,
          variant: "contained",
          disabled: disableEdit,
        },
        {
          label: "Kopieren",
          variant: "outlined",
          onClick: onCopy,
        },
        {
          label: "Löschen",
          onClick: onDelete,
          variant: "outlined",
          disabled: disableDelete,
        },
        {
          label: aktiv ? "Aktiv" : "Aktiv setzen",
          onClick: onActivate,
          variant: aktiv ? "contained" : "outlined",
        },
        {
          label: "Zurück",
          onClick: onBack,
          variant: "outlined",
        },
      ]}
    />
  );
};
