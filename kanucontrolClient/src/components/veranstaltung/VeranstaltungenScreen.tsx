import { useEffect, useState, useCallback } from "react";

import { Alert, Box, Paper } from "@mui/material";

import axios from "axios";
import { useNavigate } from "react-router-dom";

import apiClient from "@/api/client/apiClient";

import { MenueHeader } from "@/components/layout/MenueHeader";
import { renderLoadingOrError } from "@/components/common/loadingOnErrorUtils";
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

import { VeranstaltungList } from "@/api/types/VeranstaltungList";
import { VeranstaltungDetail } from "@/api/types/VeranstaltungDetail";
import { VeranstaltungSave } from "@/api/types/VeranstaltungSave";
import { VeranstaltungFormModel } from "@/api/types/VeranstaltungFormModel";

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
  const navigate = useNavigate();

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

  const [loading, setLoading] = useState(true);

  const [error, setError] = useState<string | null>(null);

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
    } catch {
      setError("Fehler beim Laden der Veranstaltungen");
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
    } catch (err) {
      console.error(err);
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

    const detail = await getVeranstaltung(row.id);

    setSelectedId(row.id);

    setSelectedVeranstaltung(detail);

    setEditMode(false);

    setBtnEditDisabled(false);

    setBtnDeleteDisabled(false);
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

    const detail = await getVeranstaltung(selectedId);

    setSelectedVeranstaltung(detail);

    setEditMode(false);

    setBtnEditDisabled(false);
    setBtnDeleteDisabled(false);
  };

  /* =========================================================
     SAVE
     ========================================================= */

  const handleSave = async (payload: VeranstaltungSave) => {
    if (!selectedVeranstaltung?.id) {
      return;
    }

    try {
      if (payload.individuelleGebuehren && !payload.beitragsstrukturId) {
        setError("Bitte zuerst eine Beitragsstruktur auswählen.");

        return;
      }

      const saved = await updateVeranstaltung(selectedVeranstaltung.id, payload);

      await fetchData();

      setSelectedVeranstaltung(saved);

      setEditMode(false);

      setBtnEditDisabled(false);

      setBtnDeleteDisabled(false);

      setError(null);
    } catch (err: unknown) {
      console.error(err);

      if (axios.isAxiosError(err)) {
        setError(err.response?.data?.message ?? "Veranstaltung konnte nicht gespeichert werden.");
      } else {
        setError("Veranstaltung konnte nicht gespeichert werden.");
      }
    }
  };

  /* =========================================================
     DELETE
     ========================================================= */

  const handleDelete = async () => {
    if (!selectedVeranstaltung?.id) {
      return;
    }

    await deleteVeranstaltung(selectedVeranstaltung.id);

    setSelectedId(null);

    setSelectedVeranstaltung(null);

    setBtnEditDisabled(true);

    setBtnDeleteDisabled(true);

    await fetchData();

    await reloadContext();
  };

  /* =========================================================
     ACTIVATE
     ========================================================= */

  const handleActivate = async () => {
    if (!selectedId) {
      return;
    }

    await setActiveVeranstaltung(selectedId);
    await fetchData();
    const updated = await getVeranstaltung(selectedId);
    setSelectedVeranstaltung(updated);
    await reloadContext();
  };

  /* =========================================================
     CREATE
     ========================================================= */

  const handleCreate = async (payload: VeranstaltungSave) => {
    const saved = await createVeranstaltung(payload);

    await fetchData();

    await reloadContext();

    setCreateOpen(false);

    setSelectedVeranstaltung(saved);

    setSelectedId(saved.id);

    setBtnEditDisabled(false);

    setBtnDeleteDisabled(false);
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
      scope: selectedVeranstaltung.scope,
      verein: selectedVeranstaltung.verein,
      leiter: selectedVeranstaltung.leiter,
      beginnDatum: selectedVeranstaltung.beginnDatum,
      endeDatum: selectedVeranstaltung.endeDatum,
      beginnZeit: selectedVeranstaltung.beginnZeit,
      endeZeit: selectedVeranstaltung.endeZeit,
      artDerUnterkunft: selectedVeranstaltung.artDerUnterkunft,
      artDerVerpflegung: selectedVeranstaltung.artDerVerpflegung,
      countryCode: selectedVeranstaltung.countryCode,
      plz: selectedVeranstaltung.plz,
      ort: selectedVeranstaltung.ort,
      individuelleGebuehren: selectedVeranstaltung.individuelleGebuehren,
      standardGebuehr: selectedVeranstaltung.standardGebuehr,
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

      {renderLoadingOrError({
        loading,
        error,
      })}

      {error && <Alert severity="error">{error}</Alert>}

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
            {
              label: "Zurück",
              variant: "outlined",
              onClick: () => navigate("/startmenue"),
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
    </Box>
  );
}
