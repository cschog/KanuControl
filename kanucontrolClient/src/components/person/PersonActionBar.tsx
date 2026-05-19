import React from "react";

import { BottomActionBar } from "@/components/layout/BottomActionBar";

interface PersonActionBarProps {
  editMode: boolean;

  onEdit: () => void;

  onCancelEdit: () => void;

  onSave: () => Promise<void>;

  onDelete: () => void;

  onBack: () => void;

  onCopy?: () => void;

  onAddVerein: () => void;

  disableEdit: boolean;

  disableDelete: boolean;
}

export const PersonActionBar: React.FC<PersonActionBarProps> = ({
  editMode,

  onEdit,

  onCancelEdit,

  onSave,

  onDelete,

  onBack,

  onCopy,

  onAddVerein,

  disableEdit,

  disableDelete,
}) => {
  if (editMode) {
    return (
      /* =================================================== */
      /* EDIT MODE */
      /* =================================================== */

      <BottomActionBar
        left={[
          // ⭐ PRIMARY ACTION FIRST
          {
            label: "Speichern",

            onClick: onSave,
          },

          {
            label: "Abbrechen",

            variant: "outlined",

            onClick: onCancelEdit,
          },

          {
            label: "Verein zuordnen",

            variant: "outlined",

            onClick: onAddVerein,
          },
        ]}
      />
    );
  }

  return (
    /* ===================================================== */
    /* VIEW MODE */
    /* ===================================================== */

    <BottomActionBar
      left={[
        {
          label: "Ändern",

          variant: "outlined",

          disabled: disableEdit,

          onClick: onEdit,
        },

        {
          label: "Zurück",

          onClick: onBack,
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

        // ⭐ destructive action last
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
