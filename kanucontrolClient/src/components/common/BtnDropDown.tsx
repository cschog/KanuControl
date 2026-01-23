import { Autocomplete, TextField } from "@mui/material";

interface Item {
  name: string;
  code: string;
}

interface Props {
  label: string;
  labelPlaceholder: string;
  selectedItem: Item | null;
  items: Item[];
  setSelectedItem: (item: Item | null) => void;
}

export function BtnDropDown({
  label,
  labelPlaceholder,
  selectedItem,
  items,
  setSelectedItem,
}: Readonly<Props>) {
  return (
    <Autocomplete
      options={items}
      value={selectedItem}
      getOptionLabel={(option) => option.name}
      isOptionEqualToValue={(option, value) => option.code === value.code}
      onChange={(_, value) => setSelectedItem(value)}
      renderInput={(params) => (
        <TextField {...params} label={label} placeholder={labelPlaceholder} />
      )}
      sx={{ minWidth: 220 }}
    />
  );
}
