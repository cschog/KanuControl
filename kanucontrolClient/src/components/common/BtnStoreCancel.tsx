import { useState } from "react";
import { Button, Stack } from "@mui/material";

import { ErrorDialog } from "@/components/common/ErrorDialog";
import { getApiErrorMessage } from "@/api/utils/apiError";


interface BtnStoreCancelProps {
  createUpdate: () => Promise<void>;
  onAbbruch: () => void;
}

export function BtnStoreCancel({ createUpdate, onAbbruch }: Readonly<BtnStoreCancelProps>) {
  const [error, setError] = useState<string | null>(null);

  const handleSave = async () => {
    try {
      setError(null);

      await createUpdate();
    } catch (err: unknown) {
      console.error("Fehler beim Speichern", err);

      setError(getApiErrorMessage(err));
    }
  };

  return (
    <>
      <Stack direction="row" spacing={2}>
        <Button variant="contained" color="success" onClick={handleSave}>
          Speichern
        </Button>

        <Button variant="outlined" color="inherit" onClick={onAbbruch}>
          Abbruch
        </Button>
      </Stack>

      <ErrorDialog open={!!error} message={error ?? ""} onClose={() => setError(null)} />
    </>
  );
}
