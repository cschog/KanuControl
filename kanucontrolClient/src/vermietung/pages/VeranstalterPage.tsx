import { useEffect, useState } from "react";
import { Alert, Box, Button, CircularProgress, Paper, Stack, Typography } from "@mui/material";
import type { SortingState } from "@tanstack/react-table";
import VeranstalterTable from "@/vermietung/components/veranstalter/VeranstalterTable";
import AddIcon from "@mui/icons-material/Add";
import SearchField from "@/core/components/common/SearchField";
import type { Veranstalter } from "@/vermietung/api/veranstalterApi";
import type Verein from "@/core/api/types/verein/VereinFormModel";
import { VereinBaseForm } from "@/core/components/verein/form/VereinBaseForm";
import { useVereinForm } from "@/core/components/verein/hooks/useVereinForm";
import { useBackNavigation } from "@/core/context/BackNavigationContext";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import { searchVeranstalter, saveVeranstalter } from "@/vermietung/api/veranstalterApi";

export default function VeranstalterPage() {
  const [search, setSearch] = useState("");
  const [sorting, setSorting] = useState<SortingState>([{ id: "name", desc: false }]);
  const [data, setData] = useState<Veranstalter[]>([]);
  const [selected, setSelected] = useState<Verein | null>(null);
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [refresh, setRefresh] = useState(0);
  const { form, update, buildSavePayload, isValid } = useVereinForm(selected);
  const { setDetailViewOpen } = useBackNavigation();
  useEffect(() => {
    setDetailViewOpen(selected !== null);
    return () => setDetailViewOpen(false);
  }, [selected, setDetailViewOpen]);
  useEffect(() => {
    if (selected) return;
    let active = true;
    setLoading(true);
    const timer = setTimeout(() => {
      searchVeranstalter({ search }).then((result) => {
        if (active) { setData(result.filter((veranstalter) => veranstalter.buchungen > 0)); setError(""); }
      }).catch((e: unknown) => { if (active) setError(getApiErrorMessage(e)); })
        .finally(() => { if (active) setLoading(false); });
    }, 250);
    return () => { active = false; clearTimeout(timer); };
  }, [search, selected, refresh]);
  const back = () => { setSelected(null); setEditing(false); setError(""); };
  const save = async () => {
    const payload = buildSavePayload();
    if (!payload || !isValid || saving) return;
    setSaving(true);
    try {
      // Whitelist shared fields; no KJFP fields enter the request.
      const { id, name, abk, strasse, plz, ort, countryCode, bankName, iban, bic, kontoinhaberId } = payload;
      const saved = await saveVeranstalter(id, { name, abk, strasse, plz, ort, countryCode, bankName, iban, bic, kontoinhaberId });
      setSelected(saved); setEditing(false); setError(""); setRefresh((n) => n + 1);
    } catch (e: unknown) { setError(getApiErrorMessage(e)); }
    finally { setSaving(false); }
  };
  return <Box sx={{ minWidth: 0, overflowWrap: "anywhere" }}>
    <Stack direction={{ xs: "column", sm: "row" }} alignItems={{ xs: "stretch", sm: "center" }} justifyContent="space-between" gap={1} sx={{ mb: 2 }}>
      <Typography variant="h4" sx={{ fontWeight: 600, fontSize: { xs: "1.5rem", md: "2rem" } }}>Veranstalterverwaltung</Typography>
      {!selected && <Button startIcon={<AddIcon />} variant="contained" disabled={loading || saving}
        onClick={() => { setSelected({ name: "", abk: "", countryCode: "DE" }); setEditing(true); }}>Neuer Veranstalter</Button>}
    </Stack>
    <Paper variant="outlined" sx={{ p: 2, minWidth: 0 }}>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {!selected ? <Stack spacing={2}>
        <SearchField value={search} onChange={setSearch} placeholder="Name oder Abkürzung" />
        {loading ? <CircularProgress aria-label="Veranstalter werden geladen" /> : <VeranstalterTable
          data={data} sorting={sorting} onSortingChange={setSorting}
          onSelect={(v) => { setSelected(v); setEditing(false); }} />}

      </Stack> : <>
        <Box display="grid" gridTemplateColumns={{ xs: "1fr", sm: "repeat(2, 1fr)", lg: "repeat(4, 1fr)" }} gap={2}>
          {form && <VereinBaseForm form={form} editMode={editing && !saving} mode="edit" veranstalter onChange={update} />}
        </Box>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={1} sx={{ mt: 3 }}>
          <Button disabled={saving} onClick={back}>Zurück zur Suche</Button>
          {editing ? <>
            <Button disabled={saving} onClick={() => { if (!selected.id) back(); else { setSelected({ ...selected }); setEditing(false); } }}>Abbrechen</Button>
            <Button variant="contained" disabled={!isValid || saving} onClick={save}>Speichern</Button>
          </> : <Button variant="contained" onClick={() => setEditing(true)}>Bearbeiten</Button>}
        </Stack>
      </>}
    </Paper>

  </Box>;
}
