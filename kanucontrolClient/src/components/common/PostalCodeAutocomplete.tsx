import { useEffect, useState } from "react";

import { Autocomplete, CircularProgress, TextField } from "@mui/material";

import { suggestPostalCodes } from "@/api/services/postalCodeApi";
import { PostalCodeLookupResponse } from "@/api/types/PostalCodeLookup";

type Props = {
  countryCode: string;

  postalCode?: string;
  city?: string;

  onSelect: (item: PostalCodeLookupResponse) => void;

  disabled?: boolean;
};

export default function PostalCodeAutocomplete({
  countryCode,
  postalCode,
  onSelect,
  disabled,
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
      } finally {
        setLoading(false);
      }
    }, 300);

    return () => clearTimeout(timeout);
  }, [countryCode, inputValue]);


  

  /* =========================================================
     RENDER
     ========================================================= */

  return (
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
}
