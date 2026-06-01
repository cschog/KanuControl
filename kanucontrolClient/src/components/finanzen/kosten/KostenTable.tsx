import { Box, Typography } from "@mui/material";

import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";

import { kostenColumns } from "@/components/finanzen/kosten/kostenColumns";

import { KostenRow } from "@/api/types/KostenRow";

interface Props {
  onDelete: (id: number) => void;
}

const rows: KostenRow[] = [
  {
    id: 1,
    datum: "01.05.2025",
    person: "MS",
    kategorie: "Unterkunft",
    kommentar: "Jugendherberge",
    ausgabe: 800,
  },

  {
    id: 2,
    datum: "02.05.2025",
    person: "AB",
    kategorie: "Verpflegung",
    kommentar: "Essen",
    ausgabe: 600,
  },

  {
    id: 3,
    datum: "03.05.2025",
    person: "CK",
    kategorie: "Teilnehmerbeitrag",
    kommentar: "Überweisung",
    einnahme: 250,
  },
];

const KostenTable = ({ onDelete }: Props) => {
  const handleEdit = (id: number) => {
    console.log("Edit Buchung", id);
  };

  const columns = kostenColumns(handleEdit, onDelete);

  return (
    <GenericTableTanstack<KostenRow>
      data={rows}
      columns={columns}
      loading={false}
      sorting={[
        {
          id: "datum",
          desc: true,
        },
      ]}
      mobileRenderRow={(row) => (
        <Box>
          <Typography fontWeight={600}>{row.kategorie}</Typography>

          <Typography variant="body2" color="text.secondary">
            {row.datum}
            {" • "}
            {row.person}
          </Typography>

          <Typography variant="body2" sx={{ mt: 0.5 }}>
            {row.kommentar}
          </Typography>

          <Box sx={{ mt: 1 }}>
            {row.einnahme ? (
              <Typography color="success.main" fontWeight={600}>
                + {row.einnahme.toFixed(2)} €
              </Typography>
            ) : (
              <Typography color="error.main" fontWeight={600}>
                - {row.ausgabe?.toFixed(2)} €
              </Typography>
            )}
          </Box>
        </Box>
      )}
    />
  );
};

export default KostenTable;
