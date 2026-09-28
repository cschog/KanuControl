import { useEffect, useState } from "react";

import { Autocomplete, CircularProgress, TextField, Tooltip, Box } from "@mui/material";

import { suggestPostalCodes } from "@/api/services/postalCodeApi";
import { PostalCodeLookupResponse } from "@/api/types/PostalCodeLookup";

import type { FieldStatus } from "@/components/common/FormFeld";

type Props = {
  countryCode: string;
  postalCode?: string;

  onSelect: (item: PostalCodeLookupResponse) => void;

  disabled?: boolean;

  dataStatus?: FieldStatus;
  dataStatusMessage?: string;
};

export default function PostalCodeAutocomplete({
  countryCode,
  postalCode,
  onSelect,
  disabled,
  dataStatus,
  dataStatusMessage,
}: Props) {
  const [inputValue, setInputValue] = useState("");
  const [options, setOptions] = useState<PostalCodeLookupResponse[]>([]);
  const [loading, setLoading] = useState(false);

  /* =========================================================
     EXTERNEN STATE SYNCHRONISIEREN
     ========================================================= */

  useEffect(() => {
    setInputValue(postalCode ?? "");
  }, [postalCode]);

  /* =========================================================
     SEARCH
     ========================================================= */

  useEffect(() => {
    if (inputValue.trim().length < 2) {
      setOptions([]);
      return;
    }

    const timeout = setTimeout(async () => {
      try {
        setLoading(true);

        const result = await suggestPostalCodes(countryCode, inputValue);

        setOptions(result);
      } catch (err) {
        console.error(err);
        setOptions([]);
      } finally {
        setLoading(false);
      }
    }, 300);

    return () => clearTimeout(timeout);
  }, [countryCode, inputValue]);

  /* =========================================================
     RENDER
     ========================================================= */

  const field = (
    <Autocomplete
      fullWidth
      disabled={disabled}
      options={options}
      loading={loading}
      noOptionsText={
        inputValue.length >= 2
          ? "Keine Treffer gefunden"
          : "Mindestens 2 Zeichen einer PLZ oder eines Orts eingeben"
      }
      getOptionLabel={(option) => `${option.postalCode} ${option.city}`}
      isOptionEqualToValue={(a, b) => a.postalCode === b.postalCode && a.city === b.city}
      onChange={(_, value) => {
        if (!value) {
          return;
        }

        onSelect(value);
      }}
      inputValue={inputValue}
      onInputChange={(_, value, reason) => {
        if (reason === "input") {
          setInputValue(value);
        }
      }}
      renderInput={(params) => (
        <TextField
          {...params}
          label="PLZ oder Ort"
          size="small"
          error={dataStatus === "ERROR"}
          InputProps={{
            ...params.InputProps,
            endAdornment: (
              <>
                {loading ? <CircularProgress color="inherit" size={16} /> : null}

                {params.InputProps.endAdornment}
              </>
            ),
          }}
        />
      )}
    />
  );

  /* =========================================================
     STATUS TOOLTIP
     ========================================================= */

  if (!dataStatus || !dataStatusMessage) {
    return field;
  }

  return (
    <Tooltip
      title={dataStatusMessage}
      arrow
      placement="top"
      slotProps={{
        tooltip: {
          sx: {
            fontSize: "0.95rem",
            lineHeight: 1.4,
            maxWidth: 360,
            padding: "10px 14px",
          },
        },
        arrow: {
          sx: {
            fontSize: "1rem",
          },
        },
      }}
    >
      <Box
        sx={{
          width: "100%",

          ...(dataStatus === "ERROR" && {
            "& .MuiOutlinedInput-root": {
              backgroundColor: "rgba(209, 3, 3, 0.18)",
            },
            "& .MuiInputLabel-root": {
              color: "error.main",
            },
          }),

          ...(dataStatus === "WARNING" && {
            "& .MuiOutlinedInput-root": {
              backgroundColor: "rgba(244, 200, 4, 0.3)",
            },
            "& .MuiInputLabel-root": {
              color: "warning.main",
            },
          }),
        }}
      >
        {field}
      </Box>
    </Tooltip>
  );
}
