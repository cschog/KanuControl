import { Chip, Stack, Typography } from "@mui/material";

import { AbrechnungBeleg } from "@/api/types/abrechnung";
import { istSystemBeleg } from "@/api/utils/belegUtils";
import { chip, fontSize } from "@/theme/ui";

interface Props {
  beleg: AbrechnungBeleg;
}

export default function BelegInfo({ beleg }: Props) {
  const system = istSystemBeleg(beleg);
  const showKategorieChip = !system && beleg.positionen.length === 1;

  return (
    <Stack spacing={0.25} sx={{ minWidth: 0 }}>
      <Stack
        direction={{ xs: "column", md: "row" }}
        spacing={1}
        alignItems={{ xs: "flex-start", md: "center" }}
        sx={{ minWidth: 0 }}
      >
        <Typography
          noWrap
          sx={{
            fontSize: fontSize.belegTitle,
            fontWeight: 700,
            overflow: "hidden",
            textOverflow: "ellipsis",
          }}
        >
          {system ? beleg.beschreibung : `${beleg.belegnummer} (${beleg.kuerzel}) • ${beleg.datum}`}
        </Typography>

        {showKategorieChip && (
          <Chip
            label={beleg.positionen[0].kategorie.replaceAll("_", " ")}
            size="medium"
            sx={{
              height: chip.height,
              borderRadius: chip.borderRadius,
              "& .MuiChip-label": {
                px: chip.labelPadding,
                fontSize: fontSize.cardTitle,
                fontWeight: 500,
              },
            }}
          />
        )}
      </Stack>

      {system && (
        <Stack direction="row" spacing={1} alignItems="center">
          <Typography
            color="text.secondary"
            sx={{
              fontSize: fontSize.table,
            }}
          >
            {beleg.datum}
          </Typography>

          <Chip
            size="small"
            label="SYS"
            color="info"
            variant="outlined"
            sx={{
              height: chip.height,
              borderRadius: chip.borderRadius,
              "& .MuiChip-label": {
                px: chip.labelPadding,
                fontSize: chip.fontSize.small,
              },
            }}
          />
        </Stack>
      )}

      {!system && beleg.beschreibung && (
        <Typography variant="body2" color="text.secondary" noWrap>
          {beleg.beschreibung}
        </Typography>
      )}
    </Stack>
  );
}
