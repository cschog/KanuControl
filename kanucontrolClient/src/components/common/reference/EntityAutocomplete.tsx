import React, { useEffect, useState } from "react";
import { Autocomplete, TextField, CircularProgress } from "@mui/material";
import { RefBase, FetchPageFn } from "./types";
import { useDebounce } from "./hooks";

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

  const debounce = useDebounce(input, 300);

 function isPageResult(res: unknown): res is { content: unknown[] } {
   return (
     typeof res === "object" &&
     res !== null &&
     "content" in res &&
     Array.isArray((res as { content: unknown[] }).content)
   );
 }

  /* ================= LOAD ================= */

  useEffect(() => {
    let active = true;

    (async () => {
      setLoading(true);
      try {
        const res = await fetch({ search: debounce });

    if (active) {
      let safe: T[] = [];

      if (Array.isArray(res)) {
        safe = res;
      } else if (isPageResult(res)) {
        safe = (res as { content: T[] }).content;
      }

      setOptions(safe);
    }
      } finally {
        if (active) setLoading(false);
      }
    })();

    return () => {
      active = false;
    };
  }, [debounce, fetch]);

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
