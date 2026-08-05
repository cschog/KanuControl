import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import AddIcon from "@mui/icons-material/Add";

import { IconButton, Stack, Tooltip } from "@mui/material";

import { AbrechnungBeleg } from "@/api/types/abrechnung";
import { istEditierbarerBeleg } from "@/api/utils/belegUtils";

interface Props {
  beleg: AbrechnungBeleg;
  readOnly?: boolean;

  onEditBeleg: (beleg: AbrechnungBeleg) => void;
  onAddPosition: (beleg: AbrechnungBeleg) => void;
  onDeleteBeleg: (belegId: number) => void;
}

export default function BelegActions({
  beleg,
  readOnly = false,
  onEditBeleg,
  onAddPosition,
  onDeleteBeleg,
}: Props) {

  if (readOnly || !istEditierbarerBeleg(beleg)) {
    return null;
  }

  return (
    <Stack direction="row" spacing={0.5} justifyContent="flex-end">
      <Tooltip title="Beleg bearbeiten">
        <IconButton
          size="small"
          color="primary"
          onClick={(e) => {
            e.stopPropagation();
            onEditBeleg(beleg);
          }}
        >
          <EditIcon fontSize="small" />
        </IconButton>
      </Tooltip>

      <Tooltip title="Position hinzufügen">
        <IconButton
          size="small"
          color="success"
          onClick={(e) => {
            e.stopPropagation();
            onAddPosition(beleg);
          }}
        >
          <AddIcon fontSize="small" />
        </IconButton>
      </Tooltip>

      <Tooltip title="Beleg löschen">
        <IconButton
          size="small"
          color="error"
          onClick={(e) => {
            e.stopPropagation();
            onDeleteBeleg(beleg.id);
          }}
        >
          <DeleteIcon fontSize="small" />
        </IconButton>
      </Tooltip>
    </Stack>
  );
}
