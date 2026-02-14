import React, { useEffect, useState } from "react";
import { Autocomplete, TextField, CircularProgress } from "@mui/material";
import { RefBase, FetchPageFn } from "./types";
import { useDebounced } from "./hooks";

interface Props<T extends RefBase> {
  label: string;

  value?: T;
  disabled?: boolean;

  fetch: FetchPageFn<T>;
  getLabel: (item: T) => string;

  onChange: (value?: T) => void;
}

export function EntityAutocomplete<T extends RefBase>({
  label,
  value,
  disabled,
  fetch,
  getLabel,
  onChange,
}: Props<T>) {
  const [options, setOptions] = useState<T[]>([]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);

  const debounced = useDebounced(input, 300);

  /* ================= LOAD ================= */

  useEffect(() => {
    let active = true;

    (async () => {
      setLoading(true);
      try {
        const res = await fetch({ search: debounced, page: 0, size: 20 });
        if (active) setOptions(res.content);
      } finally {
        if (active) setLoading(false);
      }
    })();

    return () => {
      active = false;
    };
  }, [debounced, fetch]);

  /* ================= RENDER ================= */

  return (
    <Autocomplete
      options={options}
      value={value ?? null}
      disabled={disabled}
      loading={loading}
      isOptionEqualToValue={(a, b) => a.id === b.id}
      getOptionLabel={(o) => getLabel(o)}
      onInputChange={(_, v) => setInput(v)}
      onChange={(_, v) => onChange(v ?? undefined)}
      renderInput={(params) => (
        <TextField
          {...params}
          label={label}
          InputProps={{
            ...params.InputProps,
            endAdornment: (
              <>
                {loading && <CircularProgress size={18} />}
                {params.InputProps.endAdornment}
              </>
            ),
          }}
        />
      )}
    />
  );
}
