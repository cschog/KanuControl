import { Chip, Stack, Typography } from "@mui/material";

import { AbrechnungBeleg } from "@/api/types/abrechnung";
import { istSystemBeleg } from "@/api/utils/belegUtils";
import { fontSize } from "@/theme/ui";

interface Props {
  beleg: AbrechnungBeleg;
}

export default function BelegInfo({ beleg }: Props) {
  const system = istSystemBeleg(beleg);

  return (
    <Stack spacing={0.25} sx={{ minWidth: 0 }}>
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
              height: 22,
              "& .MuiChip-label": {
                px: 0.75,
                fontSize: "0.7rem",
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
