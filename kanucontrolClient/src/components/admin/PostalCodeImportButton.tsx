import { useState } from "react";
import { Button, CircularProgress } from "@mui/material";
import CloudDownloadIcon from "@mui/icons-material/CloudDownload";

import { importPostalCodes } from "@/api/services/postalCodeAdminApi";
import { CountryCode } from "@/api/enums/CountryCode";
import { ErrorDialog } from "@/components/common/ErrorDialog";
import { getApiErrorMessage } from "@/api/utils/apiError";

export default function PostalCodeImportButton() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const countryCode: CountryCode = "DE";

  const handleImport = async () => {
    try {
      setLoading(true);
      setError(null);

      await importPostalCodes(countryCode);

      alert("PLZ-Import erfolgreich gestartet");
    } catch (err: unknown) {
      console.error("PLZ-Import fehlgeschlagen", err);

      setError(getApiErrorMessage(err, "PLZ-Import fehlgeschlagen."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Button
        variant="contained"
        startIcon={loading ? <CircularProgress size={18} /> : <CloudDownloadIcon />}
        disabled={loading}
        onClick={handleImport}
      >
        Deutschland PLZ importieren
      </Button>

      <ErrorDialog
        open={error !== null}
        message={error ?? ""}
        title="PLZ-Import fehlgeschlagen"
        onClose={() => setError(null)}
      />
    </>
  );
}
