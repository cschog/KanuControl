import { Box, Chip, Stack } from "@mui/material";
import Money from "@/components/common/Money";
import BelegActions from "./BelegActions";
import BelegInfo from "./BelegInfo";

import { AbrechnungBeleg } from "@/api/types/abrechnung";
import { chip, fontSize, spacing } from "@/theme/ui";

interface Props {
  beleg: AbrechnungBeleg;
  summe: number;
  readOnly?: boolean;

  showBuchungsChip?: boolean;

  onEditBeleg: (beleg: AbrechnungBeleg) => void;
  onAddPosition: (beleg: AbrechnungBeleg) => void;
  onDeleteBeleg: (belegId: number) => void;
}

export default function BelegHeader({
  beleg,
  summe,
  readOnly = false,
  showBuchungsChip = false,
  onEditBeleg,
  onAddPosition,
  onDeleteBeleg,
}: Props) {

  return (
    <Box
      sx={{
        display: "flex",
        flexWrap: "wrap",
        alignItems: "flex-start",
        justifyContent: "space-between",
        gap: spacing.xs,
        width: "100%",
      }}
    >
      {/* =====================================================
          LINKE SEITE
          ===================================================== */}
      <Box
        sx={{
          flexGrow: 1,
          minWidth: 0,
          display: "flex",
          alignItems: "flex-start",
          justifyContent: "space-between",
          gap: 1,
        }}
      >
        <BelegInfo beleg={beleg} />

        <BelegActions
          beleg={beleg}
          readOnly={readOnly}
          onEditBeleg={onEditBeleg}
          onAddPosition={onAddPosition}
          onDeleteBeleg={onDeleteBeleg}
        />
      </Box>

      {/* =====================================================
          RECHTE SEITE
          ===================================================== */}
      <Stack
        spacing={spacing.compact}
        alignItems="flex-end"
        sx={{
          whiteSpace: "nowrap",
          minWidth: "fit-content",
        }}
      >
        <Money
          value={summe}
          colorize
          align="right"
          sx={{
            fontSize: fontSize.money,
            fontWeight: 700,
          }}
        />

        {showBuchungsChip && (
          <Chip
            label={`${beleg.positionen.length} Buchungen`}
            size="small"
            sx={{
              height: chip.height,
              borderRadius: chip.borderRadius,
              bgcolor: "action.hover",
              color: "text.secondary",
              "& .MuiChip-label": {
                px: chip.labelPadding,
                fontSize: fontSize.cardTitle,
              },
            }}
          />
        )}
      </Stack>
    </Box>
  );
}
