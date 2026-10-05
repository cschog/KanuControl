import React, { useEffect, useState } from "react";
import { Autocomplete, TextField, CircularProgress, Tooltip, Box } from "@mui/material";

import { RefBase, FetchPageFn } from "./types";
import { useDebounce } from "./hooks";
import type { FieldStatus } from "@/core/components/common/FormFeld";

interface Props<T extends RefBase> {
  label: string;

  value?: T;
  disabled?: boolean;

  size?: "small" | "medium";

  fetch: FetchPageFn<T>;
  getLabel: (item: T) => string;

  onChange: (value?: T) => void;

  dataStatus?: FieldStatus;
  dataStatusMessage?: string;
}

export function EntityAutocomplete<T extends RefBase>({
  label,
  value,
  disabled,
  fetch,
  getLabel,
  onChange,
  size,
  dataStatus,
  dataStatusMessage,
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
        if (active) {
          setLoading(false);
        }
      }
    })();

    return () => {
      active = false;
    };
  }, [debounce, fetch]);

  useEffect(() => {
    if (!value) {
      setInput("");
    }
  }, [value]);

  /* ================= FIELD ================= */

  const field = (
    <Autocomplete
      size={size ?? "small"}
      options={options}
      value={value ?? null}
      inputValue={input}
      disabled={disabled}
      loading={loading}
      isOptionEqualToValue={(a, b) => a.id === b.id}
      getOptionLabel={(o) => getLabel(o)}
      onInputChange={(_, v) => setInput(v)}
      onChange={(_, v) => {
        onChange(v ?? undefined);

        if (v) {
          setInput("");
        }
      }}
      renderInput={(params) => (
        <TextField
          {...params}
          label={label}
          size={size ?? "small"}
          error={dataStatus === "ERROR"}
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

  /* ================= STATUS ================= */

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
