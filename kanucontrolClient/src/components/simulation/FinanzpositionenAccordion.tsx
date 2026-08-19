import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";

import { Fragment } from "react";

import { fontSize, padding } from "@/theme/ui";

import { FinanzKategorie, kategorieZuTyp } from "@/api/types/finanz";

type FinanzUebersichtsKategorie = FinanzKategorie | "FAHRKOSTEN";

interface PositionTableRow {
  kategorie: FinanzUebersichtsKategorie;
  betrag: number;
}

interface Props {
  title?: string;
  positionen: PositionTableRow[];
}

function formatEuro(value: number): string {
  return new Intl.NumberFormat("de-DE", {
    style: "currency",
    currency: "EUR",
  }).format(value);
}

function getFinanzTyp(kategorie: FinanzUebersichtsKategorie): "KOSTEN" | "EINNAHME" {
  if (kategorie === "FAHRKOSTEN") {
    return "KOSTEN";
  }

  return kategorieZuTyp[kategorie];
}

function getKategorieBezeichnung(kategorie: FinanzUebersichtsKategorie): string {
  if (kategorie === "FAHRKOSTEN") {
    return "Fahrkosten";
  }

  return kategorie;
}

export default function FinanzpositionenAccordion({ title, positionen }: Props) {
  const sortiertePositionen = [...positionen].sort((a, b) => {
    const typA = getFinanzTyp(a.kategorie);
    const typB = getFinanzTyp(b.kategorie);

    // Kosten vor Einnahmen
    if (typA !== typB) {
      return typA === "KOSTEN" ? -1 : 1;
    }

    // Fahrkosten innerhalb der Kosten nach vorne
    if (a.kategorie === "FAHRKOSTEN") {
      return -1;
    }

    if (b.kategorie === "FAHRKOSTEN") {
      return 1;
    }

    // Innerhalb der Gruppe alphabetisch
    return getKategorieBezeichnung(a.kategorie).localeCompare(
      getKategorieBezeichnung(b.kategorie),
      "de",
    );
  });

  return (
    <TableContainer component={Paper} sx={{ mt: 2 }}>
      <Typography
        variant="h5"
        sx={{
          p: 2,
          fontWeight: "bold",
          fontSize: fontSize.pageTitle,
        }}
      >
        {title}
      </Typography>

      <Table>
        <TableHead>
          <TableRow>
            <TableCell
              sx={{
                fontWeight: "bold",
                fontSize: fontSize.sectionTitle,
              }}
            >
              Kategorie
            </TableCell>

            <TableCell
              align="right"
              sx={{
                fontWeight: "bold",
                fontSize: fontSize.sectionTitle,
              }}
            >
              Betrag
            </TableCell>
          </TableRow>
        </TableHead>

        <TableBody>
          {sortiertePositionen.map((position, index) => {
            const typ = getFinanzTyp(position.kategorie);

            const vorherigerTyp =
              index === 0 ? undefined : getFinanzTyp(sortiertePositionen[index - 1].kategorie);

            return (
              <Fragment key={`${position.kategorie}-${index}`}>
                {typ !== vorherigerTyp && (
                  <TableRow>
                    <TableCell
                      colSpan={2}
                      sx={{
                        bgcolor: "grey.100",
                        fontWeight: "bold",
                        fontSize: fontSize.sectionTitle,
                      }}
                    >
                      {typ === "KOSTEN" ? "Kosten" : "Einnahmen"}
                    </TableCell>
                  </TableRow>
                )}

                <TableRow
                  sx={{
                    "& td": {
                      fontSize: fontSize.table,
                      py: padding.table,
                    },
                  }}
                >
                  <TableCell>{getKategorieBezeichnung(position.kategorie)}</TableCell>

                  <TableCell align="right">{formatEuro(position.betrag)}</TableCell>
                </TableRow>
              </Fragment>
            );
          })}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
