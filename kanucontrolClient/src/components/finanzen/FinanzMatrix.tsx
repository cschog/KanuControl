import React, { useMemo } from "react";
import {
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TableFooter,
  Typography,
  Paper,
} from "@mui/material";
import Money from "@/components/common/Money";

/* =========================================================
   Dummy Kategorien
   ========================================================= */

const kategorien = [
  "UNTERKUNFT",
  "VERPFLEGUNG",
  "HONORARE",
  "FAHRTKOSTEN",
  "PFAND",
  "TEILNEHMERBEITRAG",
];

/* =========================================================
   Dummy Buchungen
   ========================================================= */

const buchungen = [
  {
    id: 1,
    person: "AB",
    datum: "01.05.2025",
    bemerkung: "Jugendherberge",
    kategorie: "UNTERKUNFT",
    betrag: -800,
  },
  {
    id: 2,
    person: "AB",
    datum: "02.05.2025",
    bemerkung: "Essen",
    kategorie: "VERPFLEGUNG",
    betrag: -600,
  },
  {
    id: 3,
    person: "MS",
    datum: "03.05.2025",
    bemerkung: "Teilnehmerbeitrag",
    kategorie: "TEILNEHMERBEITRAG",
    betrag: 1200,
  },
  {
    id: 4,
    person: "MS",
    datum: "03.05.2025",
    bemerkung: "Pfand",
    kategorie: "PFAND",
    betrag: 200,
  },
];

/* =========================================================
   Komponente
   ========================================================= */

const FinanzMatrix = () => {
  /* ================= Sortierung ================= */

  const sorted = useMemo(() => {
    return [...buchungen].sort((a, b) => a.person.localeCompare(b.person));
  }, []);

  /* ================= Gruppierung ================= */

  const grouped = useMemo(() => {
    return sorted.reduce((acc, row) => {
      if (!acc[row.person]) {
        acc[row.person] = [];
      }
      acc[row.person].push(row);
      return acc;
    }, {} as Record<string, typeof buchungen>);
  }, [sorted]);

  /* ================= Spaltensummen ================= */

  const categoryTotals = useMemo(() => {
    const totals: Record<string, number> = {};
    kategorien.forEach((k) => (totals[k] = 0));

    sorted.forEach((r) => {
      totals[r.kategorie] += r.betrag;
    });

    return totals;
  }, [sorted]);

  /* ========================================================= */

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Finanzüberblick (Matrix)
      </Typography>

      <Paper elevation={2}>
        <TableContainer
          sx={{
            overflowX: "auto",
            maxHeight: 600,
          }}
        >
          <Table size="small" stickyHeader>
            {/* ================= HEADER ================= */}

            <TableHead>
              <TableRow>
                <TableCell
                  sx={{
                    position: "sticky",
                    left: 0,
                    zIndex: 5,
                    backgroundColor: "background.paper",
                    minWidth: 80,
                  }}
                >
                  Person
                </TableCell>

                <TableCell
                  sx={{
                    position: "sticky",
                    left: 80,
                    zIndex: 5,
                    backgroundColor: "background.paper",
                    minWidth: 110,
                  }}
                >
                  Datum
                </TableCell>

                <TableCell
                  sx={{
                    position: "sticky",
                    left: 190,
                    zIndex: 5,
                    backgroundColor: "background.paper",
                    minWidth: 180,
                  }}
                >
                  Bemerkung
                </TableCell>

                {kategorien.map((cat) => (
                  <TableCell
                    key={cat}
                    align="center"
                    sx={{
                      writingMode: "vertical-rl",
                      transform: "rotate(180deg)",
                      whiteSpace: "nowrap",
                      minWidth: 45,
                      fontSize: 12,
                      verticalAlign: "bottom",
                      pb: 2,
                    }}
                  >
                    {cat}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>

            {/* ================= BODY ================= */}

            <TableBody>
              {Object.entries(grouped).map(([person, rows]) => {
                const personTotals: Record<string, number> = {};
                kategorien.forEach((k) => (personTotals[k] = 0));

                rows.forEach((r) => {
                  personTotals[r.kategorie] += r.betrag;
                });

                return (
                  <React.Fragment key={person}>
                    {rows.map((r) => (
                      <TableRow key={r.id} hover>
                        <TableCell
                          sx={{
                            position: "sticky",
                            left: 0,
                            backgroundColor: "background.paper",
                            zIndex: 3,
                          }}
                        >
                          {r.person}
                        </TableCell>

                        <TableCell
                          sx={{
                            position: "sticky",
                            left: 80,
                            backgroundColor: "background.paper",
                            zIndex: 3,
                          }}
                        >
                          {r.datum}
                        </TableCell>

                        <TableCell
                          sx={{
                            position: "sticky",
                            left: 190,
                            backgroundColor: "background.paper",
                            zIndex: 3,
                          }}
                        >
                          {r.bemerkung}
                        </TableCell>

                        {kategorien.map((cat) => (
                          <TableCell key={cat} align="right">
                            {r.kategorie === cat && <Money value={r.betrag} colorize />}
                          </TableCell>
                        ))}
                      </TableRow>
                    ))}

                    {/* ===== Zwischensumme Person ===== */}

                    <TableRow sx={{ backgroundColor: "grey.100" }}>
                      <TableCell colSpan={3}>
                        <strong>Summe {person}</strong>
                      </TableCell>

                      {kategorien.map((cat) => (
                        <TableCell key={cat} align="right">
                          {personTotals[cat] !== 0 && <Money value={personTotals[cat]} colorize />}
                        </TableCell>
                      ))}
                    </TableRow>
                  </React.Fragment>
                );
              })}
            </TableBody>

            {/* ================= FOOTER ================= */}

            <TableFooter>
              <TableRow
                sx={{
                  position: "sticky",
                  bottom: 0,
                  backgroundColor: "grey.200",
                  zIndex: 4,
                }}
              >
                <TableCell colSpan={3}>
                  <strong>Gesamtsumme</strong>
                </TableCell>

                {kategorien.map((cat) => (
                  <TableCell key={cat} align="right">
                    {categoryTotals[cat] !== 0 && <Money value={categoryTotals[cat]} colorize />}
                  </TableCell>
                ))}
              </TableRow>
            </TableFooter>
          </Table>
        </TableContainer>
      </Paper>
    </Box>
  );
};

export default FinanzMatrix;
