import { Button, Stack } from "@mui/material";

interface BtnStoreCancelProps {
  createUpdate: () => Promise<boolean>;
  onAbbruch: () => void;
}

export function BtnStoreCancel({
  createUpdate,
  onAbbruch,
}: Readonly<BtnStoreCancelProps>) {

  const handleSave = async () => {
    try {
      const success = await createUpdate();
      if (!success) {
        console.error("Failed to save changes");
      }
    } catch (err) {
      console.error("Save threw error", err);
    }
  };

  return (
    <Stack direction="row" spacing={2}>
      <Button
        variant="contained"
        color="success"
        onClick={handleSave}
      >
        Speichern
      </Button>

      <Button
        variant="outlined"
        color="inherit"
        onClick={onAbbruch}
      >
        Abbruch
      </Button>
    </Stack>
  );
}