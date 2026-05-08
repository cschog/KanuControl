import React, { Component } from "react";

import { Alert, Box, Grid, Paper } from "@mui/material";

import apiClient from "@/api/client/apiClient";

import axios from "axios";
import { useNavigate } from "react-router-dom";

import { MenueHeader } from "@/components/layout/MenueHeader";
import { renderLoadingOrError } from "@/components/common/loadingOnErrorUtils";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { VeranstaltungFormModel } from "@/api/types/VeranstaltungFormModel";
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

import { useReloadAppContext } from "@/context/AppContextBridge";

/* =========================================================
   TYPES
   ========================================================= */

interface BeitragsstrukturDTO {
  id: number;
  name: string;
}

interface Props {
  reloadContext: () => Promise<void>;
  navigate: (path: string) => void;
}

interface State {
  data: VeranstaltungList[];
  total: number;
  page: number;
  pageSize: number;
  selectedId: number | null;
  selectedVeranstaltung: VeranstaltungDetail | null;
  beitragsstrukturen: BeitragsstrukturDTO[];
  loading: boolean;
  error: string | null;
  createOpen: boolean;
  copyOpen: boolean;
  copyData?: Partial<VeranstaltungFormModel>;
  editMode: boolean;
  btnEditDisabled: boolean;
  btnDeleteDisabled: boolean;
}

/* =========================================================
   CLASS COMPONENT
   ========================================================= */

class VeranstaltungenScreen extends Component<Props, State> {
  state: State = {
    data: [],
    total: 0,
    page: 0,
    pageSize: 8,
    selectedId: null,
    selectedVeranstaltung: null,
    beitragsstrukturen: [],
    loading: true,
    error: null,
    createOpen: false,
    copyOpen: false,
    copyData: undefined,
    editMode: false,
    btnEditDisabled: true,
    btnDeleteDisabled: true,
  };

  /* =========================================================
     LIFECYCLE
     ========================================================= */

  componentDidMount() {
    this.fetchData();

    this.loadBeitragsstrukturen();
  }

  /* =========================================================
     LOAD BEITRAGSSTRUKTUREN
     ========================================================= */

  loadBeitragsstrukturen = async () => {
    try {
      const res = await apiClient.get<BeitragsstrukturDTO[]>("/beitragsstrukturen");

      this.setState({
        beitragsstrukturen: res.data,
      });
    } catch (err) {
      console.error(err);
    }
  };

  /* =========================================================
     DATA LOAD
     ========================================================= */

  fetchData = async () => {
    const { page, pageSize } = this.state;

    try {
      const res = await getVeranstaltungenPage(page, pageSize);

      this.setState({
        data: res,

        total: res.length,

        loading: false,

        error: null,
      });
    } catch {
      this.setState({
        loading: false,

        error: "Fehler beim Laden der Veranstaltungen",
      });
    }
  };

  /* =========================================================
     PAGING
     ========================================================= */

  handlePageChange = (page: number) => {
    this.setState(
      {
        page,
        loading: true,
      },
      this.fetchData,
    );
  };

  handlePageSizeChange = (pageSize: number) => {
    this.setState(
      {
        pageSize,
        page: 0,
        loading: true,
      },
      this.fetchData,
    );
  };

  /* =========================================================
     SELECT
     ========================================================= */

  handleSelect = async (row: VeranstaltungList | null) => {
    if (!row) {
      this.setState({
        selectedId: null,
        selectedVeranstaltung: null,
        btnEditDisabled: true,
        btnDeleteDisabled: true,
      });

      return;
    }

    const detail = await getVeranstaltung(row.id);

    this.setState({
      selectedId: row.id,
      selectedVeranstaltung: detail,
      editMode: false,
      btnEditDisabled: false,
      btnDeleteDisabled: false,
    });
  };

  /* =========================================================
     EDIT
     ========================================================= */

  handleEdit = () => {
    this.setState({
      editMode: true,
      btnEditDisabled: true,
      btnDeleteDisabled: true,
    });
  };

  handleCancelEdit = () => {
    this.setState({
      editMode: false,
    });
  };

  /* =========================================================
     SAVE
     ========================================================= */

  handleSave = async (payload: VeranstaltungSave) => {
    const { selectedVeranstaltung } = this.state;

    if (!selectedVeranstaltung?.id) {
      return;
    }

    try {
      /* ========================================
     FE VALIDIERUNG
     ======================================== */

      if (payload.individuelleGebuehren && !payload.beitragsstrukturId) {
        this.setState({
          error: "Bitte zuerst eine Beitragsstruktur auswählen.",
        });

        return;
      }

      const saved = await updateVeranstaltung(selectedVeranstaltung.id, payload);

      await this.fetchData();

      this.setState({
        selectedVeranstaltung: saved,
        editMode: false,
        btnEditDisabled: false,
        btnDeleteDisabled: false,
        error: null,
      });
    } catch (err: unknown) {
      console.error(err);

      if (axios.isAxiosError(err)) {
        this.setState({
          error: err.response?.data?.message ?? "Veranstaltung konnte nicht gespeichert werden.",
        });
      } else {
        this.setState({
          error: "Veranstaltung konnte nicht gespeichert werden.",
        });
      }
    }
  };

  /* =========================================================
     DELETE
     ========================================================= */

  handleDelete = async () => {
    const { selectedVeranstaltung } = this.state;

    if (!selectedVeranstaltung?.id) {
      return;
    }

    await deleteVeranstaltung(selectedVeranstaltung.id);

    this.setState({
      selectedId: null,
      selectedVeranstaltung: null,
      btnEditDisabled: true,
      btnDeleteDisabled: true,
    });

    await this.fetchData();

    await this.props.reloadContext();
  };

  /* =========================================================
     ACTIVATE
     ========================================================= */

  handleActivate = async () => {
    if (!this.state.selectedId) {
      return;
    }

    await setActiveVeranstaltung(this.state.selectedId);

    await this.fetchData();

    await this.props.reloadContext();
  };

  /* =========================================================
     CREATE
     ========================================================= */

  openCreate = () => {
    this.setState({
      createOpen: true,
    });
  };

  closeCreate = () => {
    this.setState({
      createOpen: false,
    });
  };

  handleCreate = async (payload: VeranstaltungSave) => {
    const saved = await createVeranstaltung(payload);

    await this.fetchData();

    await this.props.reloadContext();

    this.setState({
      createOpen: false,
      selectedVeranstaltung: saved,
      selectedId: saved.id,
      btnEditDisabled: false,
      btnDeleteDisabled: false,
    });
  };

  handleCopy = () => {
    const { selectedVeranstaltung } = this.state;

    if (!selectedVeranstaltung) {
      return;
    }

    this.setState({
      copyOpen: true,

      copyData: {
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
        geplanteTeilnehmerMaennlich: selectedVeranstaltung.geplanteTeilnehmerMaennlich,
        geplanteTeilnehmerWeiblich: selectedVeranstaltung.geplanteTeilnehmerWeiblich,
        geplanteTeilnehmerDivers: selectedVeranstaltung.geplanteTeilnehmerDivers,
        geplanteMitarbeiterMaennlich: selectedVeranstaltung.geplanteMitarbeiterMaennlich,
        geplanteMitarbeiterWeiblich: selectedVeranstaltung.geplanteMitarbeiterWeiblich,
        geplanteMitarbeiterDivers: selectedVeranstaltung.geplanteMitarbeiterDivers,
      },
    });
  };

  /* =========================================================
     NAV
     ========================================================= */

  handleBack = () => {
    this.props.navigate("/startmenue");
  };

  /* =========================================================
     RENDER
     ========================================================= */

  render() {
    const {
      data,
      total,
      loading,
      error,
      selectedId,
      selectedVeranstaltung,
      createOpen,
      beitragsstrukturen,
    } = this.state;

    return (
      <Box>
        <MenueHeader headerText={`${total} Veranstaltungen`} />

        {renderLoadingOrError({
          loading,
          error,
        })}

        {error && <Alert severity="error">{error}</Alert>}

        <Grid container spacing={2}>
          {/* ================= LEFT ================= */}

          <Grid size={{ xs: 12, md: 6 }}>
            <Paper sx={{ p: 2 }}>
              <VeranstaltungTable
                data={data}
                total={total}
                page={this.state.page}
                pageSize={this.state.pageSize}
                selectedId={selectedId}
                onSelect={this.handleSelect}
                onPageChange={this.handlePageChange}
                onPageSizeChange={this.handlePageSizeChange}
              />
            </Paper>
          </Grid>

          {/* ================= RIGHT ================= */}

          <Grid size={{ xs: 12, md: 6 }}>
            <Paper sx={{ p: 2 }}>
              <VeranstaltungFormView
                veranstaltung={selectedVeranstaltung}
                beitragsstrukturen={beitragsstrukturen}
                editMode={this.state.editMode}
                onEdit={this.handleEdit}
                onCopy={this.handleCopy}
                onCancelEdit={this.handleCancelEdit}
                onSave={this.handleSave}
                onDelete={this.handleDelete}
                onBack={this.handleBack}
                onActivate={this.handleActivate}
                disableEdit={this.state.btnEditDisabled}
                disableDelete={this.state.btnDeleteDisabled}
              />
            </Paper>
          </Grid>
        </Grid>

        {/* =====================================================
           ACTION BAR
           ===================================================== */}

        {!selectedVeranstaltung && (
          <BottomActionBar
            left={[
              {
                label: "Neue Veranstaltung",
                variant: "outlined",
                onClick: this.openCreate,
              },
              {
                label: "Zurück",
                variant: "outlined",
                onClick: this.handleBack,
              },
            ]}
          />
        )}

        {/* =====================================================
           CREATE DIALOG
           ===================================================== */}

        <VeranstaltungCreateDialog
          open={createOpen}
          onClose={this.closeCreate}
          onCreate={this.handleCreate}
          beitragsstrukturen={beitragsstrukturen}
        />

        <VeranstaltungCreateDialog
          open={this.state.copyOpen}
          onClose={() =>
            this.setState({
              copyOpen: false,
            })
          }
          onCreate={this.handleCreate}
          beitragsstrukturen={beitragsstrukturen}
          initialData={this.state.copyData}
        />
      </Box>
    );
  }
}

/* =========================================================
   CONTEXT WRAPPER
   ========================================================= */

function VeranstaltungenWithContext() {
  const reload = useReloadAppContext();

  const navigate = useNavigate();

  return <VeranstaltungenScreen reloadContext={reload} navigate={navigate} />;
}

export default VeranstaltungenWithContext;
