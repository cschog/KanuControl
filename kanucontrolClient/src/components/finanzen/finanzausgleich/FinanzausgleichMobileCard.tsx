import { Box, Stack, Typography } from "@mui/material";

import Money from "@/components/common/Money";
import { FinanzausgleichRow } from "./finanzausgleichTypes";

interface Props {
  row: FinanzausgleichRow;
}

export default function FinanzausgleichMobileCard({ row }: Props) {
  return (
    <Box>
      {/* =====================================================
          KOPF
         ===================================================== */}

      <Typography variant="h6" fontWeight={700}>
        {row.kuerzel}
      </Typography>

      {/* =====================================================
          POSITIONEN
         ===================================================== */}

      <Stack spacing={0.75} sx={{ mt: 1.5 }}>
        <Stack direction="row" justifyContent="space-between">
          <Typography color="text.secondary">Soll-Beiträge</Typography>

          <Money value={row.teilnehmerBeitraegeSoll} />
        </Stack>

        <Stack direction="row" justifyContent="space-between">
          <Typography color="text.secondary">Überweisung</Typography>

          <Money value={row.teilnehmerBeitraegeUeberweisung} />
        </Stack>

        <Stack direction="row" justifyContent="space-between">
          <Typography color="text.secondary">Quittung</Typography>

          <Money value={row.teilnehmerBeitraegeQuittung} />
        </Stack>

        <Stack direction="row" justifyContent="space-between">
          <Typography color="text.secondary">Ausgaben</Typography>

          <Money value={row.ausgaben} />
        </Stack>

        <Stack direction="row" justifyContent="space-between">
          <Typography color="text.secondary">Fahrtkosten</Typography>

          <Money value={row.fahrkosten} />
        </Stack>
      </Stack>

      {/* =====================================================
          ERSTATTUNG
         ===================================================== */}

      <Box
        sx={{
          mt: 2,
          pt: 1.5,

          borderTop: 1,
          borderColor: "divider",
        }}
      >
        <Typography variant="body2" color="text.secondary">
          Erstattung vom VK
        </Typography>

        <Typography variant="h6" fontWeight={700}>
          <Money value={row.erstattungVomVK} />
        </Typography>
      </Box>
    </Box>
  );
}
