import { useEffect, useState, useCallback } from "react";
import keycloak from "@/auth/keycloak";
import { WarningDialog } from "@/components/common/WarningDialog";
import { getApiErrorMessage } from "@/api/utils/apiError";
import { ErrorDialog } from "@/components/common/ErrorDialog";
import {
  Box,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
} from "@mui/material";

import { getOnlineUsers } from "@/api/services/sessionApi";

import apiClient from "@/api/client/apiClient";

import { MenueHeader } from "@/components/layout/MenueHeader";
import { BottomActionBar } from "@/components/layout/BottomActionBar";

import { VeranstaltungTable } from "./VeranstaltungTable";
import { VeranstaltungFormView } from "./VeranstaltungFormView";
import { VeranstaltungCreateDialog } from "./VeranstaltungCreateDialog";

import {
  getVeranstaltungenPage,
  getVeranstaltung,
  createVeranstaltung,
  updateVeranstaltung,
  deleteVeranstaltung,
  setActiveVeranstaltung,
} from "@/api/services/veranstaltungApi";

import { VeranstaltungList } from "@/api/types/veranstaltung/VeranstaltungList";
import { VeranstaltungDetail } from "@/api/types/veranstaltung/VeranstaltungDetail";
import { VeranstaltungSave } from "@/api/types/veranstaltung/VeranstaltungSave";
import { VeranstaltungFormModel } from "@/api/types/veranstaltung/VeranstaltungFormModel";

import { useReloadAppContext } from "@/context/AppContextBridge";

/* =========================================================
   TYPES
   ========================================================= */

interface BeitragsstrukturDTO {
  id: number;
  name: string;
}

/* =========================================================
   COMPONENT
   ========================================================= */

export default function VeranstaltungenScreen() {

  const reloadContext = useReloadAppContext();

  /* =========================================================
     STATE
     ========================================================= */

  const [data, setData] = useState<VeranstaltungList[]>([]);
  const [total, setTotal] = useState(0);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [selectedVeranstaltung, setSelectedVeranstaltung] = useState<VeranstaltungDetail | null>(
    null,
  );
  const [beitragsstrukturen, setBeitragsstrukturen] = useState<BeitragsstrukturDTO[]>([]);
  const [, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [copyOpen, setCopyOpen] = useState(false);
  const [copyData, setCopyData] = useState<Partial<VeranstaltungFormModel>>();
  const [editMode, setEditMode] = useState(false);
  const [btnEditDisabled, setBtnEditDisabled] = useState(true);
  const [btnDeleteDisabled, setBtnDeleteDisabled] = useState(true);
  const [sorting, setSorting] = useState<{ id: string; desc: boolean }[]>([
    {
      id: "beginnDatum",
      desc: true,
    },
  ]);

  const [confirmActivateOpen, setConfirmActivateOpen] = useState(false);
  const [otherOnlineUsers, setOtherOnlineUsers] = useState<string[]>([]);
  const [activating, setActivating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [warnings, setWarnings] = useState<string[]>([]);
  const [warningDialogOpen, setWarningDialogOpen] = useState(false);
  const [dialogTitle, setDialogTitle] = useState("");

  /* =========================================================
     LOAD DATA
     ========================================================= */

  const fetchData = useCallback(async () => {
    try {
      setLoading(true);

      const res = await getVeranstaltungenPage(sorting[0]?.id, sorting[0]?.desc ? "desc" : "asc");

      setData(res);
      setTotal(res.length);

      setError(null);
    } catch (err: unknown) {
      console.error("Fehler beim Laden der Veranstaltungen", err);
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [sorting]);

  /* =========================================================
     LOAD BEITRAGSSTRUKTUREN
     ========================================================= */

  const loadBeitragsstrukturen = async () => {
    try {
      const res = await apiClient.get<BeitragsstrukturDTO[]>("/beitragsstrukturen");

      setBeitragsstrukturen(res.data);
    } catch (err: unknown) {
      console.error("Fehler beim Laden der Beitragsstrukturen", err);
      setError(getApiErrorMessage(err));
    }
  };

  /* =========================================================
     INITIAL LOAD
     ========================================================= */

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  useEffect(() => {
    loadBeitragsstrukturen();
  }, []);

  /* =========================================================
     SELECT
     ========================================================= */

  const handleSelect = async (row: VeranstaltungList | null) => {
    if (!row) {
      setSelectedId(null);
      setSelectedVeranstaltung(null);
      setBtnEditDisabled(true);
      setBtnDeleteDisabled(true);
      return;
    }

    try {
      setError(null);
      const detail = await getVeranstaltung(row.id);

      setSelectedId(row.id);
      setSelectedVeranstaltung(detail);
      setEditMode(false);
      setBtnEditDisabled(false);
      setBtnDeleteDisabled(false);
    } catch (err: unknown) {
      console.error("Fehler beim Laden der Veranstaltung", err);
      setError(getApiErrorMessage(err));
    }
  };

  /* =========================================================
     EDIT
     ========================================================= */

  const handleEdit = () => {
    setEditMode(true);

    setBtnEditDisabled(true);

    setBtnDeleteDisabled(true);
  };

  const handleCancelEdit = async () => {
    if (!selectedId) {
      return;
    }

    try {
      setError(null);
      const detail = await getVeranstaltung(selectedId);

      setSelectedVeranstaltung(detail);
      setEditMode(false);
      setBtnEditDisabled(false);
      setBtnDeleteDisabled(false);
    } catch (err: unknown) {
      console.error("Fehler beim Laden der Veranstaltung", err);
      setError(getApiErrorMessage(err));
    }
  };

  /* =========================================================
     SAVE
     ========================================================= */

  const handleSave = async (payload: VeranstaltungSave) => {
    if (!selectedVeranstaltung?.id) {
      return;
    }

    try {
      setError(null);
      const response = await updateVeranstaltung(selectedVeranstaltung.id, payload);

      await fetchData();

      setSelectedVeranstaltung(response.data);

      if ((response.warnings ?? []).length > 0) {
        setDialogTitle("Hinweise");
        setWarnings(response.warnings);
        setWarningDialogOpen(true);
      }

      setError(null);
      setEditMode(false);
      setBtnEditDisabled(false);
      setBtnDeleteDisabled(false);
    } catch (err: unknown) {
      console.error("Fehler beim Speichern der Veranstaltung", err);

      setError(getApiErrorMessage(err));
    }
  };

  /* =========================================================
     DELETE
     ========================================================= */

  const handleDelete = async () => {
    if (!selectedVeranstaltung?.id) {
      return;
    }

    try {
      setError(null);
      await deleteVeranstaltung(selectedVeranstaltung.id);

      setSelectedId(null);
      setSelectedVeranstaltung(null);
      setBtnEditDisabled(true);
      setBtnDeleteDisabled(true);

      await fetchData();
      await reloadContext();
    } catch (err: unknown) {
      console.error("Fehler beim Löschen der Veranstaltung", err);

      setError(getApiErrorMessage(err));
    }
  };

  /* =========================================================
     ACTIVATE
     ========================================================= */

  const handleActivate = async () => {
    if (!selectedId) {
      return;
    }

    try {
      setError(null);
      const users = await getOnlineUsers();

      const otherUsers = users.filter((user) => user !== keycloak.tokenParsed?.preferred_username);

      if (otherUsers.length > 0) {
        setOtherOnlineUsers(otherUsers);
        setConfirmActivateOpen(true);
        return;
      }

      await doActivate();
    } catch (err: unknown) {
      console.error("Fehler beim Ermitteln der Online-Benutzer", err);
      setError(getApiErrorMessage(err));
    }
  };

  const doActivate = async () => {
    if (!selectedId) {
      return;
    }

    setActivating(true);

    try {
      setError(null);
      await setActiveVeranstaltung(selectedId);

      await fetchData();

      const updated = await getVeranstaltung(selectedId);

      setSelectedVeranstaltung(updated);

      await reloadContext();
    } catch (err: unknown) {
      console.error("Fehler beim Aktivieren der Veranstaltung", err);
      setError(getApiErrorMessage(err));
    } finally {
      setActivating(false);
    }
  };

  /* =========================================================
     CREATE
     ========================================================= */

  const handleCreate = async (payload: VeranstaltungSave) => {
    try {
      setError(null);
      const response = await createVeranstaltung(payload);

      console.log("CREATE RESPONSE:", response);

      await fetchData();
      await reloadContext();

      setCreateOpen(false);

      setSelectedVeranstaltung(response);
      setSelectedId(response.id);

      setBtnEditDisabled(false);
      setBtnDeleteDisabled(false);

      setError(null);
    } catch (err: unknown) {
      console.error("CREATE ERROR:", err);

      setError(getApiErrorMessage(err));
    }
  };

  /* =========================================================
     COPY
     ========================================================= */

  const handleCopy = () => {
    if (!selectedVeranstaltung) {
      return;
    }

    setCopyData({
      name: `${selectedVeranstaltung.name} Kopie`,
      typ: selectedVeranstaltung.typ,
      verein: selectedVeranstaltung.verein,
      leiter: selectedVeranstaltung.leiter,
      beginnDatum: selectedVeranstaltung.beginnDatum,
      endeDatum: selectedVeranstaltung.endeDatum,
      beginnZeit: selectedVeranstaltung.beginnZeit,
      endeZeit: selectedVeranstaltung.endeZeit,
      unterkunftsart: selectedVeranstaltung.unterkunftsart,
      verpflegungsmodell: selectedVeranstaltung.verpflegungsmodell,
      countryCode: selectedVeranstaltung.countryCode,
      plz: selectedVeranstaltung.plz,
      ort: selectedVeranstaltung.ort,
      beitragsstrukturId: selectedVeranstaltung.beitragsstrukturId,
    });

    setCopyOpen(true);
  };

  /* =========================================================
     RENDER
     ========================================================= */

  return (
    <Box>
      <MenueHeader headerText={`${total} Veranstaltungen`} />

      {/* ===================================================== */}
      {/* LIST VIEW */}
      {/* ===================================================== */}

      {!selectedVeranstaltung ? (
        <Paper sx={{ p: 2 }}>
          <VeranstaltungTable
            data={data}
            selectedId={selectedId}
            onSelect={handleSelect}
            sorting={sorting}
            onSortingChange={setSorting}
          />
        </Paper>
      ) : (
        /* =================================================== */
        /* DETAIL VIEW */
        /* =================================================== */

        <Paper sx={{ p: 2 }}>
          <VeranstaltungFormView
            veranstaltung={selectedVeranstaltung}
            beitragsstrukturen={beitragsstrukturen}
            editMode={editMode}
            onEdit={handleEdit}
            onCopy={handleCopy}
            onCancelEdit={handleCancelEdit}
            onSave={handleSave}
            onDelete={handleDelete}
            onBack={() => {
              setSelectedId(null);

              setSelectedVeranstaltung(null);

              setEditMode(false);
            }}
            onActivate={handleActivate}
            disableEdit={btnEditDisabled}
            disableDelete={btnDeleteDisabled}
          />
        </Paper>
      )}

      {/* ===================================================== */}
      {/* ACTION BAR */}
      {/* ===================================================== */}

      {!selectedVeranstaltung && (
        <BottomActionBar
          left={[
            {
              label: "Neue Veranstaltung",
              variant: "outlined",
              onClick: () => setCreateOpen(true),
            },
          ]}
        />
      )}

      {/* ===================================================== */}
      {/* CREATE */}
      {/* ===================================================== */}

      <VeranstaltungCreateDialog
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreate={handleCreate}
        beitragsstrukturen={beitragsstrukturen}
      />

      {/* ===================================================== */}
      {/* COPY */}
      {/* ===================================================== */}

      <VeranstaltungCreateDialog
        open={copyOpen}
        onClose={() => setCopyOpen(false)}
        onCreate={handleCreate}
        beitragsstrukturen={beitragsstrukturen}
        initialData={copyData}
      />

      <Dialog open={confirmActivateOpen} onClose={() => setConfirmActivateOpen(false)}>
        <DialogTitle>Achtung</DialogTitle>

        <DialogContent>
          <DialogContentText>
            Der Wechsel der aktiven Veranstaltung betrifft alle aktuell angemeldeten Benutzer dieses
            Vereins.
          </DialogContentText>

          <DialogContentText sx={{ mt: 2 }}>
            Folgende weitere Benutzer sind derzeit angemeldet:
          </DialogContentText>

          <Box sx={{ mt: 1 }}>
            {otherOnlineUsers.map((user) => (
              <div key={user}>• {user}</div>
            ))}
          </Box>
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setConfirmActivateOpen(false)}>Abbrechen</Button>

          <Button
            variant="contained"
            disabled={activating}
            onClick={async () => {
              setConfirmActivateOpen(false);
              await doActivate();
            }}
          >
            Wechseln
          </Button>
        </DialogActions>
      </Dialog>

      <WarningDialog
        open={warningDialogOpen}
        title={dialogTitle}
        warnings={warnings}
        onClose={() => setWarningDialogOpen(false)}
      />

      <ErrorDialog open={!!error} message={error ?? ""} onClose={() => setError(null)} />
    </Box>
  );
}
