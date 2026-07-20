import { Paper, Stack } from "@mui/material";
import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";
import { buchungColumns } from "@/components/finanzen/buchung/buchungColumns";
import { AbrechnungBeleg, Buchung } from "@/api/types/abrechnung";
import BelegInfo from "@/components/finanzen/buchung/BelegInfo";
import BelegActions from "@/components/finanzen/buchung/BelegActions";
import BuchungRow from "@/components/finanzen/buchung/BuchungRow";

interface Props {
  beleg: AbrechnungBeleg;
  readOnly?: boolean;

  onEditBeleg: (beleg: AbrechnungBeleg) => void;
  onAddPosition: (beleg: AbrechnungBeleg) => void;
  onEditPosition: (beleg: AbrechnungBeleg, buchung: Buchung) => void;
  onDeletePosition: (belegId: number, buchungId: number) => void;
  onDeleteBeleg: (belegId: number) => void;
}

export default function BelegCard({
  beleg,
  readOnly = false,
  onEditBeleg,
  onAddPosition,
  onEditPosition,
  onDeletePosition,
  onDeleteBeleg,
}: Props) {
  const columns = buchungColumns({
    onEdit: (buchung) => onEditPosition(beleg, buchung),

    onDelete: (buchungId) => onDeletePosition(beleg.id, buchungId),
  });

  return (
    <Paper
      variant="outlined"
      sx={{
        p: 2,
        mb: 3,
      }}
    >
      {/* =====================================================
          HEADER
         ===================================================== */}

      <Stack
        direction={{ xs: "column", md: "row" }}
        justifyContent="space-between"
        alignItems={{ xs: "flex-start", md: "center" }}
        spacing={2}
        mb={2}
      >
        <BelegInfo beleg={beleg} />
        <BelegActions
          beleg={beleg}
          readOnly={readOnly}
          onEditBeleg={onEditBeleg}
          onAddPosition={onAddPosition}
          onDeleteBeleg={onDeleteBeleg}
        />
      </Stack>

      {/* =====================================================
          TABLE
         ===================================================== */}

      <GenericTableTanstack<Buchung>
        data={beleg.positionen}
        columns={columns}
        loading={false}
        height={350}
        mobileRenderRow={(row) => (
          <BuchungRow
            beleg={beleg}
            buchung={row}
            readOnly={readOnly}
            stopPropagation
            onEdit={onEditPosition}
            onDelete={onDeletePosition}
          />
        )}
      />
    </Paper>
  );
}
