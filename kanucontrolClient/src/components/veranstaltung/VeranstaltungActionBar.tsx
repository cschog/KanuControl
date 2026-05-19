import React from "react";
import { BottomActionBar } from "@/components/layout/BottomActionBar";

interface Props {
  aktiv: boolean;
  editMode: boolean;

  onEdit: () => void;
  onCopy: () => void;
  onCancelEdit: () => void;
  onSave: () => void;
  onDelete: () => void;
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
          {
            label: "Abbrechen",
            variant: "outlined",
            onClick: onCancelEdit,
          },

          ...(onCopy
            ? [
                {
                  label: "Kopieren",
                  variant: "outlined" as const,
                  onClick: onCopy,
                },
              ]
            : []),
        ]}
        right={[
          {
            label: "Speichern",
            onClick: onSave,
          },
        ]}
      />
    );
  }

  return (
    <BottomActionBar
      left={[
        {
          label: "Zurück",
          variant: "outlined",
          onClick: onBack,
        },

        {
          label: "Kopieren",
          variant: "outlined",
          onClick: onCopy,
        },

        {
          label: "Löschen",
          variant: "outlined",
          color: "error",
          disabled: disableDelete,
          onClick: onDelete,
        },
      ]}
      right={[
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
      ]}
    />
  );
};
