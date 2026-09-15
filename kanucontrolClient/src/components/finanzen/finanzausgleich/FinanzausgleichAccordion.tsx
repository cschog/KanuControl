import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Box,
  Stack,
  Typography,
} from "@mui/material";

import FinanzausgleichDokumentPanel from "./FinanzausgleichDokumentPanel";

import ExpandMoreIcon from "@mui/icons-material/ExpandMore";

import { fontSize } from "@/theme/ui";
import Money from "@/components/common/Money";
import FinanzGruppeBelege from "@/components/finanzen/konto/FinanzGruppeBelege";

import { FinanzausgleichRow } from "./finanzausgleichTypes";
import { FINANZAUSGLEICH_GRID_TEMPLATE } from "./finanzausgleichLayout";

interface Props {
  veranstaltungId: number;
  row: FinanzausgleichRow;
  expanded: boolean;
  onChange: (expanded: boolean) => void;
}

export default function FinanzausgleichAccordion({
  veranstaltungId,
  row,
  expanded,
  onChange,
}: Props) {

const isVK = row.kuerzel === "VK";
    
const beitragsAbweichung = !isVK && row.beitragsstatus === "ABWEICHUNG";

  return (
    <Accordion
      expanded={expanded}
      onChange={(_, isExpanded) => onChange(isExpanded)}
      disableGutters
      elevation={0}
      sx={{
        borderBottom: 1,
        borderColor: "divider",

        "&:before": {
          display: "none",
        },
      }}
    >
      <AccordionSummary
        sx={{
          p: 0,
          minHeight: 0,

          "&.Mui-expanded": {
            minHeight: 0,
          },

          "& .MuiAccordionSummary-content": {
            display: "block",
            width: "100%",
            minWidth: 0,
            flex: "1 1 auto",

            m: 0,

            "&.Mui-expanded": {
              m: 0,
            },
          },
        }}
      >
        <>
          {/* =====================================================
              DESKTOP
             ===================================================== */}

          <Box
            sx={{
              display: {
                xs: "none",
                md: "grid",
              },

              width: "100%",
              minWidth: 0,

              gridTemplateColumns: FINANZAUSGLEICH_GRID_TEMPLATE,

              px: 4,
              py: 1,

              alignItems: "center",
            }}
          >
            {/* KONTO */}

            <Box>
              <Typography
                component="span"
                fontWeight={700}
                fontSize="1.5rem"
                sx={{
                  color: beitragsAbweichung ? "error.main" : "success.main",
                }}
              >
                {row.kuerzel}
              </Typography>

              {beitragsAbweichung && (
                <Typography
                  variant="body2"
                  color="error.main"
                  sx={{
                    mt: 0.25,
                    lineHeight: 1.2,
                  }}
                ></Typography>
              )}
            </Box>

            {/* SOLL-BEITRÄGE */}

            <Box textAlign="right">
              {!isVK && (
                <Typography component="div" fontSize="1.5rem">
                  <Money value={row.teilnehmerBeitraegeSoll} />
                </Typography>
              )}
            </Box>

            {/* ÜBERWEISUNG */}

            <Box textAlign="right">
              <Typography component="div" fontSize="1.5rem">
                <Money value={row.teilnehmerBeitraegeUeberweisung} />
              </Typography>
            </Box>

            {/* QUITTUNG */}

            <Box textAlign="right">
              {!isVK && (
                <Typography component="div" fontSize="1.5rem">
                  <Money value={row.teilnehmerBeitraegeQuittung} />
                </Typography>
              )}
            </Box>

            {/* AUSGABEN */}

            <Box textAlign="right">
              <Typography component="div" fontSize="1.5rem">
                <Money value={row.ausgaben} />
              </Typography>
            </Box>

            {/* FAHRTKOSTEN */}

            <Box textAlign="right">
              {!isVK && (
                <Typography component="div" fontSize="1.5rem">
                  <Money value={row.fahrkosten} />
                </Typography>
              )}
            </Box>

            {/* BEWUSSTER ABSTAND */}

            <Box />

            {/* ERSTATTUNG VOM VK */}

            <Box textAlign="right">
              {!isVK && (
                <Typography component="div" fontSize="1.5rem" fontWeight={700}>
                  <Money value={row.erstattungVomVK} />
                </Typography>
              )}
            </Box>

            {/* PFEIL-SPALTE */}

            <Box
              sx={{
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
              }}
            >
              <ExpandMoreIcon
                sx={{
                  transition: "transform 150ms ease",
                  transform: expanded ? "rotate(180deg)" : "rotate(0deg)",
                }}
              />
            </Box>
          </Box>

          {/* =====================================================
              MOBILE
             ===================================================== */}

          <Box
            sx={{
              display: {
                xs: "block",
                md: "none",
              },

              width: "100%",
              px: 2,
              py: 1.5,
            }}
          >
            {/* KOPF */}

            <Stack direction="row" justifyContent="space-between" alignItems="center">
              <Box>
                <Typography
                  fontWeight={700}
                  fontSize={fontSize.finanzausgleich.konto}
                  sx={{
                    color: beitragsAbweichung ? "error.main" : "success.main",
                  }}
                >
                  {row.kuerzel}
                </Typography>

                {beitragsAbweichung && (
                  <Typography
                    variant="body2"
                    color="error.main"
                    sx={{
                      mt: 0.25,
                      lineHeight: 1.2,
                    }}
                  >
                    Teilnehmerbeiträge passen nicht zur Kontenzuordnung
                  </Typography>
                )}
              </Box>

              <ExpandMoreIcon
                sx={{
                  transition: "transform 150ms ease",
                  transform: expanded ? "rotate(180deg)" : "rotate(0deg)",
                }}
              />
            </Stack>

            {/* BEITRÄGE */}

            <Stack
              spacing={0.75}
              sx={{
                mt: 1.5,
                width: "100%",
              }}
            >
              {!isVK && (
                <FinanzausgleichPosition
                  label="Soll-Beiträge"
                  value={row.teilnehmerBeitraegeSoll}
                />
              )}

              <FinanzausgleichPosition
                label="Überweisung"
                value={row.teilnehmerBeitraegeUeberweisung}
              />

              {!isVK && (
                <FinanzausgleichPosition label="Quittung" value={row.teilnehmerBeitraegeQuittung} />
              )}

              <FinanzausgleichPosition label="Ausgaben" value={row.ausgaben} />

              {!isVK && <FinanzausgleichPosition label="Fahrtkosten" value={row.fahrkosten} />}
            </Stack>

            {/* ERSTATTUNG */}

            {!isVK && (
              <Box
                sx={{
                  mt: 2,
                  pt: 1.5,

                  borderTop: 1,
                  borderColor: "divider",
                }}
              >
                <FinanzausgleichPosition label="Ausgleich" value={row.erstattungVomVK} bold />
              </Box>
            )}
          </Box>
        </>
      </AccordionSummary>

      {/* =====================================================
          DETAILS
         ===================================================== */}
      <AccordionDetails
        sx={{
          pt: 0,
          pb: 3,
          px: {
            xs: 2,
            md: 3,
          },
        }}
      >
        <Stack spacing={3}>
          {!isVK && (
            <Box sx={{ pt: 2 }}>
              <FinanzausgleichDokumentPanel
                veranstaltungId={veranstaltungId}
                finanzGruppeId={row.finanzGruppeId}
              />
            </Box>
          )}

          <FinanzGruppeBelege
            veranstaltungId={veranstaltungId}
            finanzGruppeId={row.finanzGruppeId}
          />
        </Stack>
      </AccordionDetails>
    </Accordion>
  );
}

/* =========================================================
   MOBILE HELPER
   ========================================================= */

interface FinanzausgleichPositionProps {
  label: string;
  value: number;
  bold?: boolean;
}

function FinanzausgleichPosition({ label, value, bold = false }: FinanzausgleichPositionProps) {
  return (
    <Box
      sx={{
        display: "grid",
        gridTemplateColumns: "minmax(0, 1fr) max-content",
        alignItems: "center",
        columnGap: 1,
        width: "100%",
      }}
    >
      <Typography color="text.secondary" fontSize={fontSize.finanzausgleich.label}>
        {label}
      </Typography>

      <Box
        sx={{
          justifySelf: "end",
          whiteSpace: "nowrap",
          fontWeight: bold ? 700 : 400,
        }}
      >
        <Money value={value} />
      </Box>
    </Box>
  );
}
