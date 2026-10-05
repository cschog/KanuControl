import { Button, Paper, Stack, TextField } from "@mui/material";

interface Props {
  value: string;
  onChange: (value: string) => void;
  onCreate: () => void;
}

export default function KontoCreateForm({ value, onChange, onCreate }: Props) {
  return (
    <Paper sx={{ p: 2, mb: 3 }}>
      <Stack direction="row" spacing={2}>
        <TextField
          size="small"
          placeholder="Neues Konto"
          value={value}
          onChange={(event) => onChange(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === "Enter") {
              onCreate();
            }
          }}
        />

        <Button variant="contained" onClick={onCreate}>
          Hinzufügen
        </Button>
      </Stack>
    </Paper>
  );
}
