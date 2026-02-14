import React, { Component } from "react";
import { MenueHeader } from "@/components/layout/MenueHeader";
import { renderLoadingOrError } from "@/components/common/loadingOnErrorUtils";
import { BottomActionBar } from "@/components/common/BottomActionBar";

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

interface Props {
  reloadContext: () => Promise<void>;
}

interface State {
  data: VeranstaltungList[];
  total: number;

  page: number;
  pageSize: number;

  selectedId: number | null;
  selectedVeranstaltung: VeranstaltungDetail | null;

  loading: boolean;
  error: string | null;

  createOpen: boolean;

  editMode: boolean;
  btnEditDisabled: boolean;
  btnDeleteDisabled: boolean;
}

/* =========================================================
   CLASS COMPONENT
   ========================================================= */

class Veranstaltungen extends Component<Props, State> {
  state: State = {
    data: [],
    total: 0,
    page: 0,
    pageSize: 8,

    selectedId: null,
    selectedVeranstaltung: null,

    loading: true,
    error: null,

    createOpen: false,

    editMode: false,
    btnEditDisabled: true,
    btnDeleteDisabled: true,
  };

  componentDidMount() {
    this.fetchData();
  }

  /* =========================================================
     DATA LOAD
     ========================================================= */

  fetchData = async () => {
    const { page, pageSize } = this.state;

    try {
      const res = await getVeranstaltungenPage(page, pageSize);
      this.setState({
        data: res.content,
        total: res.totalElements,
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

  handlePageChange = (page: number) => {
    this.setState({ page, loading: true }, this.fetchData);
  };

  handlePageSizeChange = (pageSize: number) => {
    this.setState({ pageSize, page: 0, loading: true }, this.fetchData);
  };

  /* =========================================================
     SELECT → DETAIL LOAD
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
    this.setState({ editMode: false });
  };

  /* =========================================================
     SAVE
     ========================================================= */

  handleSave = async (payload: VeranstaltungSave) => {
    const { selectedVeranstaltung } = this.state;
    if (!selectedVeranstaltung?.id) return;

    const saved = await updateVeranstaltung(selectedVeranstaltung.id, payload);

    await this.fetchData();

    this.setState({
      selectedVeranstaltung: saved,
      editMode: false,
      btnEditDisabled: false,
      btnDeleteDisabled: false,
    });
  };

  /* =========================================================
     DELETE
     ========================================================= */

  handleDelete = async () => {
    const { selectedVeranstaltung } = this.state;
    if (!selectedVeranstaltung?.id) return;

    await deleteVeranstaltung(selectedVeranstaltung.id);

    this.setState({
      selectedId: null,
      selectedVeranstaltung: null,
      btnEditDisabled: true,
      btnDeleteDisabled: true,
    });

    await this.fetchData();
    await this.props.reloadContext(); // ⭐ Context aktualisieren
  };

  /* =========================================================
     ACTIVATE
     ========================================================= */

  handleActivate = async () => {
    if (!this.state.selectedId) return;

    await setActiveVeranstaltung(this.state.selectedId);

    await this.fetchData();
    await this.props.reloadContext(); // ⭐ Statusleiste sofort aktualisieren
  };

  /* =========================================================
     CREATE
     ========================================================= */

  openCreate = () => this.setState({ createOpen: true });
  closeCreate = () => this.setState({ createOpen: false });

  handleCreate = async (payload: VeranstaltungSave) => {
    const saved = await createVeranstaltung(payload);

    await this.fetchData();
    await this.props.reloadContext(); // ⭐ BONUS: Context neu laden

    this.setState({
      createOpen: false,
      selectedVeranstaltung: saved,
      selectedId: saved.id,
      btnEditDisabled: false,
      btnDeleteDisabled: false,
    });
  };

  /* =========================================================
     NAV
     ========================================================= */

  handleBack = () => window.history.back();

  /* =========================================================
     RENDER
     ========================================================= */

  render() {
    const { data, total, loading, error, selectedId, selectedVeranstaltung, createOpen } =
      this.state;

    return (
      <>
        <MenueHeader headerText={`${total} Veranstaltungen`} />

        {renderLoadingOrError({ loading, error })}

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

        <VeranstaltungFormView
          veranstaltung={selectedVeranstaltung}
          editMode={this.state.editMode}
          onEdit={this.handleEdit}
          onCancelEdit={this.handleCancelEdit}
          onSave={this.handleSave}
          onDelete={this.handleDelete}
          onBack={this.handleBack}
          onActivate={this.handleActivate}
          disableEdit={this.state.btnEditDisabled}
          disableDelete={this.state.btnDeleteDisabled}
        />

        {!selectedVeranstaltung && (
          <BottomActionBar
            left={[
              { label: "Neue Veranstaltung", variant: "outlined", onClick: this.openCreate },
              { label: "Zurück", variant: "outlined", onClick: this.handleBack },
            ]}
          />
        )}

        <VeranstaltungCreateDialog
          open={createOpen}
          onClose={this.closeCreate}
          onCreate={this.handleCreate}
        />
      </>
    );
  }
}

/* =========================================================
   CONTEXT WRAPPER (HOOK → CLASS)
   ========================================================= */

function VeranstaltungenWithContext() {
  const reload = useReloadAppContext();
  return <Veranstaltungen reloadContext={reload} />;
}

export default VeranstaltungenWithContext;
