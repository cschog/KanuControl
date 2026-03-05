import { Box, Typography, Grid, Card, CardContent, Button } from "@mui/material";
import KostenSummary from "@/components/finanzen/KostenSummary";
import KostenTable from "@/components/finanzen/KostenTable";
import KostenDialog from "@/components/finanzen/KostenDialog";
import { useState } from "react";
import DeleteConfirmDialog from "@/components/dialogs/DeleteConfirmDialog";

const KostenPage = () => {
  const [openDialog, setOpenDialog] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const handleDelete = (id: number) => {
    setDeleteId(id);
  };

  const confirmDelete = () => {
    if (deleteId) {
      console.log("delete booking", deleteId);
    }

    setDeleteId(null);
  };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Kosten (IST)
      </Typography>

      <KostenSummary />

      <Grid container spacing={3} mt={1}>
        <Grid size={{ xs: 12 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Buchungen
              </Typography>

              <Box
                sx={{
                  maxHeight: 400,
                  overflow: "auto",
                }}
              >
                <KostenTable onDelete={handleDelete} />
              </Box>

              <Button sx={{ mt: 2 }} variant="contained" onClick={() => setOpenDialog(true)}>
                + Buchung hinzufügen
              </Button>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Dialog */}

      <KostenDialog open={openDialog} onClose={() => setOpenDialog(false)} />
      <DeleteConfirmDialog
        open={deleteId !== null}
        title="Buchung löschen"
        message="Soll diese Buchung wirklich gelöscht werden?"
        onClose={() => setDeleteId(null)}
        onConfirm={confirmDelete}
      />
    </Box>
  );
};

export default KostenPage;
