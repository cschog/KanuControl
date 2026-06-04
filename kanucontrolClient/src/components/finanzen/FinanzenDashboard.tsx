import {
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
 
} from "@mui/material";
import { useState } from "react";

import ExpandMoreIcon from "@mui/icons-material/ExpandMore";

import Money from "@/components/common/Money";
import { useNavigate } from "react-router-dom";
import { useAppContext } from "@/context/AppContext";
import { useTheme } from "@mui/material/styles";
import useMediaQuery from "@mui/material/useMediaQuery";

import { useFinanzenDashboard } from "@/hooks/finanzen/useFinanzenDashboard";

import { BottomActionBar } from "@/components/layout/BottomActionBar";

const FinanzenDashboard = () => {
  /* =========================================================
     FAKE DATEN
     ========================================================= */

  const navigate = useNavigate();

  const { active } = useAppContext();

  const { data, isLoading } = useFinanzenDashboard(active?.id);

  const planKosten = data?.planKosten ?? 0;
  const istKosten = data?.istKosten ?? 0;
  const planEinnahmen = data?.planEinnahmen ?? 0;
  const istEinnahmen = data?.istEinnahmen ?? 0;
  const planSaldo = data?.planSaldo ?? 0;
  const istSaldo = data?.istSaldo ?? 0;
  const diffKosten = planKosten - istKosten;
  const diffEinnahmen = istEinnahmen - planEinnahmen;
  const diffSaldo = istSaldo - planSaldo;
  const foerderung = data?.foerderung;
  const kosten = data?.kostenNachKategorie ?? [];
  const einnahmen = data?.einnahmenNachKategorie ?? [];
  const istKostenNachKategorie = data?.istKostenNachKategorie ?? [];
  const istEinnahmenNachKategorie = data?.istEinnahmenNachKategorie ?? [];
  const [selectedDetail, setSelectedDetail] = useState<string | null>(null);

  const toggleDetail = (detail: string) => {
    setSelectedDetail((current) => (current === detail ? null : detail));
  };

  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));
  const fontSizeLabel = isMobile ? "0.5rem" : "0.8rem";
  const fontSizeValue = isMobile ? "0.8rem" : "1.1rem";

  const labels = {
    planKosten: isMobile ? "Plan K." : "Plan Kosten",
    istKosten: isMobile ? "Ist K." : "Ist Kosten",
    planEinnahmen: isMobile ? "Plan E." : "Plan Einnahmen",
    istEinnahmen: isMobile ? "Ist E." : "Ist Einnahmen",
    diffKosten: isMobile ? "Δ Kosten" : "Abweichung Kosten",
    diffEinnahmen: isMobile ? "Δ Ein." : "Abweichung Einnahmen",
    planSaldo: isMobile ? "Δ Plan" : "Abweichung Plan Saldo",
    istSaldo: isMobile ? "Δ Ist" : "Abweichung Ist Saldo",
    diffSaldo: isMobile ? "Δ Saldo" : "Abweichung Saldo",
    foerderfaehigeTn: isMobile ? "geförd. TN" : "Förderfähige TN",
    foerdertage: isMobile ? "Tage" : "Fördertage",
    foerdersatz: isMobile ? "Satz" : "Fördersatz",
    gesamtfoerderung: isMobile ? "Förderung" : "Gesamtförderung",
  };

  const istSaldoColor =
    istSaldo > 0 ? "success.main" : istSaldo < 0 ? "error.main" : "primary.main";
  const diffSaldoColor =
    diffSaldo > 0 ? "success.main" : diffSaldo < 0 ? "error.main" : "primary.main";
  const diffKostenColor =
    diffKosten > 0 ? "success.main" : diffKosten < 0 ? "error.main" : "primary.main";

  const diffEinnahmenColor =
    diffEinnahmen > 0 ? "success.main" : diffEinnahmen < 0 ? "error.main" : "primary.main";

  if (isLoading) {
    return <div>Lade Dashboard...</div>;
  }

  /* ========================================================= */

  return (
    <Box sx={{ mt: 2 }}>
      {/* =====================================================
      FOERDERUNG
      ===================================================== */}

      {foerderung && (
        <Card sx={{ mb: 3 }}>
          <CardContent
            sx={{
              py: 1,
              "&:last-child": {
                pb: 1,
              },
            }}
          >
            <Box
              sx={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                gap: 1,
              }}
            >
              {/* Förderfähige TN */}

              <Box
                sx={{
                  flex: 1,
                  minWidth: 0,
                  textAlign: "center",
                }}
              >
                <Typography
                  color="text.secondary"
                  sx={{
                    fontSize: fontSizeLabel,
                    whiteSpace: "nowrap",
                    lineHeight: 1.1,
                  }}
                >
                  {labels.foerderfaehigeTn}
                </Typography>

                <Typography
                  sx={{
                    fontSize: fontSizeValue,
                    fontWeight: 600,
                    lineHeight: 1.1,
                  }}
                >
                  {foerderung.foerderfaehigeTeilnehmer}
                </Typography>
              </Box>

              {/* Fördertage */}

              <Box
                sx={{
                  flex: 1,
                  minWidth: 0,
                  textAlign: "center",
                }}
              >
                <Typography
                  color="text.secondary"
                  sx={{
                    fontSize: fontSizeLabel,
                    whiteSpace: "nowrap",
                    lineHeight: 1.1,
                  }}
                >
                  {labels.foerdertage}
                </Typography>

                <Typography
                  sx={{
                    fontSize: fontSizeValue,
                    fontWeight: 600,
                    lineHeight: 1.1,
                  }}
                >
                  {foerderung.foerdertage}
                </Typography>
              </Box>

              {/* Fördersatz */}

              <Box
                sx={{
                  flex: 1,
                  minWidth: 0,
                  textAlign: "center",
                }}
              >
                <Typography
                  color="text.secondary"
                  sx={{
                    fontSize: fontSizeLabel,
                    whiteSpace: "nowrap",
                    lineHeight: 1.1,
                  }}
                >
                  {labels.foerdersatz}
                </Typography>

                <Typography
                  sx={{
                    fontSize: fontSizeValue,
                    fontWeight: 600,
                    lineHeight: 1.1,
                  }}
                >
                  <Money
                    value={foerderung.foerdersatz}
                    sx={{
                      fontSize: fontSizeValue,
                      fontWeight: 600,
                    }}
                  />
                </Typography>
              </Box>

              {/* Gesamtförderung */}

              <Box
                sx={{
                  flex: 1,
                  minWidth: 0,
                  textAlign: "center",
                }}
              >
                <Typography
                  color="text.secondary"
                  sx={{
                    fontSize: fontSizeLabel,
                    whiteSpace: "nowrap",
                    lineHeight: 1.1,
                  }}
                >
                  {labels.gesamtfoerderung}
                </Typography>

                <Typography
                  sx={{
                    fontSize: fontSizeValue,
                    fontWeight: 600,
                    lineHeight: 1.1,
                  }}
                >
                  <Money
                    value={foerderung.gesamtfoerderung}
                    sx={{
                      fontSize: fontSizeValue,
                      fontWeight: 600,
                    }}
                  />
                </Typography>
              </Box>
            </Box>
          </CardContent>
        </Card>
      )}
      {/* =====================================================
    KOSTEN KPI
    ===================================================== */}

      <Grid container spacing={3}>
        {/* PLAN KOSTEN */}

        <Grid size={{ xs: 4 }}>
          <Card
            onClick={() => toggleDetail("planKosten")}
            sx={{
              borderLeft: 6,
              borderColor: "error.main",
              cursor: "pointer",
            }}
          >
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">{labels.planKosten}</Typography>

              <Money
                value={-planKosten}
                sx={{
                  fontSize: fontSizeValue,
                  fontWeight: 500,
                }}
                colorize
              />
            </CardContent>
          </Card>
        </Grid>

        {/* IST KOSTEN */}

        <Grid size={{ xs: 4 }}>
          <Card
            onClick={() => toggleDetail("istKosten")}
            sx={{ borderLeft: 6, borderColor: "error.main", cursor: "pointer" }}
          >
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">{labels.istKosten}</Typography>

              <Money
                value={-istKosten}
                sx={{
                  fontSize: fontSizeValue,
                  fontWeight: 500,
                }}
                colorize
              />
            </CardContent>
          </Card>
        </Grid>

        {/* ABWEICHUNG KOSTEN */}

        <Grid size={{ xs: 4 }}>
          <Card
            sx={{
              borderLeft: 6,
              borderColor: diffKostenColor,
            }}
          >
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">{labels.diffKosten}</Typography>

              <Money
                value={diffKosten}
                sx={{
                  fontSize: fontSizeValue,
                  fontWeight: 500,
                }}
                colorize
              />
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* =====================================================
    EINNAHMEN KPI
    ===================================================== */}

      <Grid container spacing={3} sx={{ mt: 0.5 }}>
        {/* PLAN EINNAHMEN */}

        <Grid size={{ xs: 4 }}>
          <Card
            onClick={() => toggleDetail("planEinnahmen")}
            sx={{ borderLeft: 6, borderColor: "success.main", cursor: "pointer" }}
          >
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">{labels.planEinnahmen}</Typography>

              <Money
                value={planEinnahmen}
                sx={{
                  fontSize: fontSizeValue,
                  fontWeight: 500,
                }}
                colorize
              />
            </CardContent>
          </Card>
        </Grid>

        {/* IST EINNAHMEN */}

        <Grid size={{ xs: 4 }}>
          <Card
            onClick={() => toggleDetail("istEinnahmen")}
            sx={{ borderLeft: 6, borderColor: "success.main", cursor: "pointer" }}
          >
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">{labels.istEinnahmen}</Typography>

              <Money
                value={istEinnahmen}
                sx={{
                  fontSize: fontSizeValue,
                  fontWeight: 500,
                }}
                colorize
              />
            </CardContent>
          </Card>
        </Grid>

        {/* ABWEICHUNG EINNAHMEN */}

        <Grid size={{ xs: 4 }}>
          <Card
            sx={{
              borderLeft: 6,
              borderColor: diffEinnahmenColor,
            }}
          >
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">{labels.diffEinnahmen}</Typography>

              <Money
                value={diffEinnahmen}
                sx={{
                  fontSize: fontSizeValue,
                  fontWeight: 500,
                }}
                colorize
              />
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* =====================================================
    SALDO KPI
    ===================================================== */}

      <Grid container spacing={3} sx={{ mt: 0.5 }}>
        {/* PLAN SALDO */}

        <Grid size={{ xs: 4 }}>
          <Card sx={{ borderLeft: 6, borderColor: "primary.main" }}>
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">{labels.planSaldo}</Typography>

              <Money
                value={planSaldo}
                sx={{
                  fontSize: fontSizeValue,
                  fontWeight: 500,
                }}
                colorize
              />
            </CardContent>
          </Card>
        </Grid>

        {/* IST SALDO */}

        <Grid size={{ xs: 4 }}>
          <Card sx={{ borderLeft: 6, borderColor: istSaldoColor }}>
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">{labels.istSaldo}</Typography>

              <Money
                value={istSaldo}
                sx={{
                  fontSize: fontSizeValue,
                  fontWeight: 500,
                }}
                colorize
              />
            </CardContent>
          </Card>
        </Grid>

        {/* ABWEICHUNG SALDO */}

        <Grid size={{ xs: 4 }}>
          <Card
            sx={{
              borderLeft: 6,
              borderColor: diffSaldoColor,
            }}
          >
            <CardContent sx={{ py: 2 }}>
              <Typography variant="subtitle2">{labels.diffSaldo}</Typography>

              <Money
                value={diffSaldo}
                sx={{
                  fontSize: fontSizeValue,
                  fontWeight: 500,
                }}
                colorize
              />
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* =====================================================
    DETAILBEREICHE
    ===================================================== */}

      {selectedDetail && (
        <Accordion expanded sx={{ mt: 2 }}>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="subtitle1">
              {selectedDetail === "planKosten" && labels.planKosten}
              {selectedDetail === "istKosten" && labels.istKosten}
              {selectedDetail === "planEinnahmen" && labels.planEinnahmen}
              {selectedDetail === "istEinnahmen" && labels.istEinnahmen}
            </Typography>
          </AccordionSummary>

          <AccordionDetails>
            {selectedDetail === "planKosten" &&
              kosten.map((k) => (
                <Box
                  key={k.name}
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    mb: 1.5,
                  }}
                >
                  <Typography>{k.name}</Typography>
                  <Money value={-k.betrag} colorize />
                </Box>
              ))}

            {selectedDetail === "istKosten" &&
              istKostenNachKategorie.map((k) => (
                <Box
                  key={k.name}
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    mb: 1.5,
                  }}
                >
                  <Typography>{k.name}</Typography>
                  <Money value={-k.betrag} colorize />
                </Box>
              ))}

            {selectedDetail === "planEinnahmen" &&
              einnahmen.map((e) => (
                <Box
                  key={e.name}
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    mb: 1.5,
                  }}
                >
                  <Typography>{e.name}</Typography>
                  <Money value={e.betrag} colorize />
                </Box>
              ))}

            {selectedDetail === "istEinnahmen" &&
              istEinnahmenNachKategorie.map((e) => (
                <Box
                  key={e.name}
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    mb: 1.5,
                  }}
                >
                  <Typography>{e.name}</Typography>
                  <Money value={e.betrag} colorize />
                </Box>
              ))}
          </AccordionDetails>
        </Accordion>
      )}

      <BottomActionBar
        left={[
          {
            label: "Zurück",
            onClick: () => navigate(-1),
            variant: "outlined",
          },
        ]}
      />
    </Box>
  );
};

export default FinanzenDashboard;
