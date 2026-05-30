// src/components/admin/PostalCodeImportButton.tsx

import { useState } from "react";
import { Button, CircularProgress } from "@mui/material";
import CloudDownloadIcon from "@mui/icons-material/CloudDownload";
import { importPostalCodes } from "@/api/services/postalCodeAdminApi";
import { CountryCode } from "@/api/enums/CountryCode";

export default function PostalCodeImportButton() {
  const [loading, setLoading] = useState(false);

  const countryCode: CountryCode = "DE";

  const handleImport = async () => {
    try {
      setLoading(true);

      await importPostalCodes(countryCode);

      alert("PLZ-Import erfolgreich gestartet");
    } catch (err) {
      console.error(err);

      alert("PLZ-Import fehlgeschlagen");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Button
      variant="contained"
      startIcon={loading ? <CircularProgress size={18} /> : <CloudDownloadIcon />}
      disabled={loading}
      onClick={handleImport}
    >
      Deutschland PLZ importieren
    </Button>
  );
}
