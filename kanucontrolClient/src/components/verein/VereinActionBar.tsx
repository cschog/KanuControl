import { BottomActionBar } from "@/components/layout/BottomActionBar";

interface Props {
  editMode: boolean;

  onEdit: () => void;

  onCancelEdit: () => void;

  onSave: () => void;

  onDelete: () => void;

  onBack: () => void;

  onCsvImport?: () => void;

  onChangeKontoinhaber?: () => void;

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
    /* ===================================================== */
    /* EDIT MODE */
    /* ===================================================== */

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

        ...(onCsvImport
          ? [
              {
                label: "CSV importieren",

                variant: "outlined" as const,

                onClick: onCsvImport,
              },
            ]
          : []),

        ...(props.onChangeKontoinhaber
          ? [
              {
                label: "Kontoinhaber ändern",

                variant: "outlined" as const,

                onClick: props.onChangeKontoinhaber,
              },
            ]
          : []),
      ]}
    />
  ) : (
    /* ===================================================== */
    /* VIEW MODE */
    /* ===================================================== */

    <BottomActionBar
      left={[
        {
          label: "Bearbeiten",
          variant: "outlined",
          onClick: onEdit,
          disabled: disableEdit,
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
}
