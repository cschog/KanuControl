import React from "react";

import { BottomActionBar } from "@/core/components/layout/BottomActionBar";

interface Props {
  aktiv: boolean;
  editMode: boolean;

  onEdit: () => void;
  onCancelEdit: () => void;
  onSave: () => void;
  onDelete: () => void;
  onBack: () => void;
  onActivate: () => void;

  disableEdit: boolean;
  disableDelete: boolean;
}

export const MietobjektActionBar: React.FC<Props> = ({
  aktiv,
  editMode,
  onEdit,
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
          {
            label: "Speichern",
            variant: "contained",
            onClick: onSave,
          },
          {
            label: "Abbrechen",
            variant: "outlined",
            onClick: onCancelEdit,
          },
        ]}
      />
    );
  }

  return (
    <BottomActionBar
      left={[
        {
          label: "Ändern",
          variant: "outlined",
          disabled: disableEdit,
          onClick: onEdit,
        },
        {
          label: aktiv ? "Aktiv" : "Aktiv setzen",
          onClick: onActivate,
          variant: aktiv ? "contained" : "outlined",
        },
        {
          label: "Zurück",
          onClick: onBack,
        },
      ]}
      right={[
        {
          label: "Löschen",
          variant: "outlined",
          color: "error",
          disabled: disableDelete,
          onClick: onDelete,
        },
      ]}
    />
  );
};
