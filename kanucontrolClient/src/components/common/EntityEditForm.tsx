import { ReactNode, useState } from "react";
import { Box, Typography, Snackbar, Alert } from "@mui/material";
import { BtnStoreCancel } from "@/components/common/BtnStoreCancel";

interface EntityEditFormProps {
  title: string;
  onSave: () => Promise<boolean>;
  onCancel: () => void;
  children: ReactNode;
}

export function EntityEditForm({ title, onSave, onCancel, children }: EntityEditFormProps) {
  const [error, setError] = useState<string | null>(null);

  const handleSave = async (): Promise<boolean> => {
    const ok = await onSave();

    if (!ok) {
      setError("Speichern fehlgeschlagen");
    }

    return ok;
  };

  return (
    <Box maxWidth="lg" mx="auto">
      <Typography variant="h5" align="center" gutterBottom>
        {title}
      </Typography>

      {children}

      <Box mt={4} display="flex" justifyContent="flex-end">
        <BtnStoreCancel createUpdate={handleSave} onAbbruch={onCancel} />
      </Box>

      <Snackbar open={!!error} autoHideDuration={4000} onClose={() => setError(null)}>
        <Alert severity="error" onClose={() => setError(null)}>
          {error}
        </Alert>
      </Snackbar>
    </Box>
  );
}
