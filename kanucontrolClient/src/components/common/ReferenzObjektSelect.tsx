// src/components/common/ReferenzObjektSelect.tsx

import { FormControl, InputLabel, MenuItem, Select, type SelectChangeEvent } from "@mui/material";

import { ReferenzObjekt } from "@/api/enums/ReferenzObjekt";

interface Props {
  value: ReferenzObjekt;
  onChange: (value: ReferenzObjekt) => void;
  disabled?: boolean;
}

const REFERENZ_LABELS: Record<ReferenzObjekt, string> = {
  [ReferenzObjekt.DIN_A4]: "DIN A4",
  [ReferenzObjekt.DIN_A5]: "DIN A5",
  [ReferenzObjekt.DIN_A6]: "DIN A6",
  [ReferenzObjekt.DIN_A7]: "DIN A7",
};

export default function ReferenzObjektSelect({ value, onChange, disabled = false }: Props) {
  function handleChange(event: SelectChangeEvent<string>) {
    onChange(event.target.value as ReferenzObjekt);
  }

  return (
    <FormControl fullWidth size="small" disabled={disabled}>
      <InputLabel id="referenzobjekt-label">Dokumentformat</InputLabel>

      <Select
        labelId="referenzobjekt-label"
        value={value}
        label="Dokumentformat"
        onChange={handleChange}
      >
        {Object.values(ReferenzObjekt).map((referenz) => (
          <MenuItem key={referenz} value={referenz}>
            {REFERENZ_LABELS[referenz]}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
}
