import React from "react";
import { BottomActionBar } from "@/components/common/BottomActionBar";

interface PersonActionBarProps {
  editMode: boolean;

  onEdit: () => void;
  onCancelEdit: () => void;
  onSave: () => Promise<void>;

  onDelete: () => void;
  onBack: () => void;

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
  onAddVerein,
  disableEdit,
  disableDelete,
}) => {
  if (editMode) {
    return (
      <BottomActionBar
        left={[
          {
            label: "Verein zuordnen",
            variant: "outlined",
            onClick: onAddVerein,
          },
          {
            label: "Speichern",
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
          label: "Bearbeiten",
          variant: "outlined",
          disabled: disableEdit,
          onClick: onEdit,
        },
        {
          label: "Löschen",
          variant: "outlined",
          color: "error",
          disabled: disableDelete,
          onClick: onDelete,
        },
        {
          label: "Zurück",
          onClick: onBack,
        },
      ]}
    />
  );
};
