import { BottomActionBar } from "@/components/common/BottomActionBar";

interface Props {
  editMode: boolean;
  onEdit: () => void;
  onCancelEdit: () => void;
  onSave: () => void;
  onDelete: () => void;
  onBack: () => void;
  onCsvImport?: () => void;
  disableEdit: boolean;
  disableDelete: boolean;
}

export function VereinActionBar(props: Props) {
  const {
    editMode,
    onEdit,
    onCancelEdit,
    onSave,
    onDelete,
    onBack,
    onCsvImport,
    disableEdit,
    disableDelete,
  } = props;

  return editMode ? (
    <BottomActionBar
      left={[
        ...(onCsvImport
          ? [
              {
                label: "CSV importieren",
                variant: "outlined" as const,
                onClick: onCsvImport,
              },
            ]
          : []),
        { label: "Speichern", onClick: onSave },
        { label: "Abbrechen", variant: "outlined", onClick: onCancelEdit },
      ]}
    />
  ) : (
    <BottomActionBar
      left={[
        { label: "Bearbeiten", variant: "outlined", onClick: onEdit, disabled: disableEdit },
        {
          label: "Löschen",
          variant: "outlined",
          color: "error",
          onClick: onDelete,
          disabled: disableDelete,
        },
        { label: "Zurück", onClick: onBack },
      ]}
    />
  );
}
