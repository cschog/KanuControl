import { useEffect, useState } from "react";
import { Alert, Box, Button, CircularProgress, Paper, Stack, Typography, MenuItem, TextField, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions } from "@mui/material";
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
import { searchVeranstalter, saveVeranstalter, deleteVeranstalter } from "@/vermietung/api/veranstalterApi";

export default function VeranstalterPage() {
  const [search, setSearch] = useState("");
  const [buchungsfilter, setBuchungsfilter] = useState<"MIT" | "ALLE" | "OHNE">("MIT");
  const [sorting, setSorting] = useState<SortingState>([{ id: "name", desc: false }]);
  const [data, setData] = useState<Veranstalter[]>([]);
  const [selected, setSelected] = useState<(Verein & { buchungen?: number }) | null>(null);
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState("");
  const [success, setSuccess] = useState("");
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
        if (active) { setData(result); setError(""); }
      }).catch((e: unknown) => { if (active) setError(getApiErrorMessage(e)); })
        .finally(() => { if (active) setLoading(false); });
    }, 250);
    return () => { active = false; clearTimeout(timer); };
  }, [search, selected, refresh]);
  const back = () => { if (deleting || saving) return; setSelected(null); setEditing(false); setError(""); };
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
  const remove = async () => {
    if (!selected?.id || selected.buchungen !== 0 || deleting) return;
    setDeleting(true); setDeleteError("");
    try {
      await deleteVeranstalter(selected.id);
      setSuccess(`Veranstalter ${selected.name} wurde gelöscht.`);
      setData(current => current.filter(v => v.id !== selected.id));
      setDeleteOpen(false); setSelected(null); setEditing(false); setError("");
      setRefresh(n => n + 1);
    } catch (e: unknown) { setDeleteError(getApiErrorMessage(e)); }
    finally { setDeleting(false); }
  };
  return <Box sx={{ minWidth: 0, overflowWrap: "anywhere" }}>
    <Stack direction={{ xs: "column", sm: "row" }} alignItems={{ xs: "stretch", sm: "center" }} justifyContent="space-between" gap={1} sx={{ mb: 2 }}>
      <Typography variant="h4" sx={{ fontWeight: 600, fontSize: { xs: "1.5rem", md: "2rem" } }}>Veranstalterverwaltung</Typography>
      {!selected && <Button startIcon={<AddIcon />} variant="contained" disabled={loading || saving}
        onClick={() => { setSuccess(""); setSelected({ name: "", abk: "", countryCode: "DE" }); setEditing(true); }}>Neuer Veranstalter</Button>}
    </Stack>
    {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
    <Paper variant="outlined" sx={{ p: 2, minWidth: 0 }}>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      {!selected ? <Stack spacing={2}>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
          <SearchField value={search} onChange={setSearch} placeholder="Name oder Abkürzung" />
          <TextField select label="Buchungsfilter" value={buchungsfilter}
            onChange={event => setBuchungsfilter(event.target.value as typeof buchungsfilter)}
            size="small" sx={{ minWidth: { sm: 260 } }}>
            <MenuItem value="MIT">Mit Buchungen</MenuItem>
            <MenuItem value="ALLE">Alle Veranstalter (auch ohne Buchung)</MenuItem>
            <MenuItem value="OHNE">Ohne Buchungen</MenuItem>
          </TextField>
        </Stack>
        {loading ? <CircularProgress aria-label="Veranstalter werden geladen" /> : <VeranstalterTable
          data={data.filter(v => buchungsfilter === "ALLE" || (buchungsfilter === "MIT" ? v.buchungen > 0 : v.buchungen === 0))} sorting={sorting} onSortingChange={setSorting}
          onSelect={(v) => { setSuccess(""); setSelected(v); setEditing(false); }} />}

      </Stack> : <>
        <Box display="grid" gridTemplateColumns={{ xs: "1fr", sm: "repeat(2, 1fr)", lg: "repeat(4, 1fr)" }} gap={2}>
          {form && <VereinBaseForm form={form} editMode={editing && !saving} mode="edit" veranstalter onChange={update} />}
        </Box>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={1} sx={{ mt: 3 }}>
          <Button disabled={saving || deleting} onClick={back}>Zurück zur Suche</Button>
          {editing ? <>
            <Button disabled={saving} onClick={() => { if (!selected.id) back(); else { setSelected({ ...selected }); setEditing(false); } }}>Abbrechen</Button>
            <Button variant="contained" disabled={!isValid || saving} onClick={save}>Speichern</Button>
          </> : <>
            <Button variant="contained" disabled={deleting} onClick={() => setEditing(true)}>Ändern</Button>
            {selected.id && <Button color="error" variant="outlined" disabled={deleting || selected.buchungen !== 0}
              onClick={() => { setDeleteError(""); setDeleteOpen(true); }}>Löschen</Button>}
          </>}

        </Stack>
        {!editing && (selected.buchungen ?? 0) > 0 && <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          Löschen ist erst möglich, wenn keine Buchungen mehr zugeordnet sind. Das gilt auch für stornierte und abgeschlossene Buchungen.
        </Typography>}
      </>}
    </Paper>

    <Dialog open={deleteOpen} onClose={() => { if (!deleting) setDeleteOpen(false); }} aria-labelledby="veranstalter-delete-title" maxWidth="sm" fullWidth>
      <DialogTitle id="veranstalter-delete-title">Veranstalter löschen?</DialogTitle>
      <DialogContent>
        <DialogContentText>Möchtest du {selected?.name} dauerhaft löschen?</DialogContentText>
        <DialogContentText sx={{ mt: 1 }}>Das Löschen ist nur ohne zugehörige Buchungen möglich. Bestehende Verknüpfungen zu Mitgliedern oder KJFP-Veranstaltungen verhindern das Löschen ebenfalls.</DialogContentText>
        {deleteError && <Alert severity="error" sx={{ mt: 2 }}>{deleteError}</Alert>}
      </DialogContent>
      <DialogActions>
        <Button disabled={deleting} onClick={() => setDeleteOpen(false)}>Abbrechen</Button>
        <Button color="error" variant="contained" disabled={deleting} onClick={() => void remove()}>{deleting ? "Wird gelöscht …" : "Veranstalter löschen"}</Button>
      </DialogActions>
    </Dialog>
  </Box>;
}
