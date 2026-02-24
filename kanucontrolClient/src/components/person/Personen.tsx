import { Component } from "react";
import { MenueHeader } from "@/components/layout/MenueHeader";
import { PersonTable } from "@/components/person/PersonTable";
import { PersonFormView } from "@/components/person/PersonFormView";
import { renderLoadingOrError } from "@/components/common/loadingOnErrorUtils";
import { PersonList, PersonSave, PersonDetail } from "@/api/types/Person";
import { getPersonById } from "@/api/services/personApi";
import apiClient from "@/api/client/apiClient";
import { PersonCreateDialog } from "@/components/person/PersonCreateDialog";
import { Box } from "@mui/material";
import { VereinRef } from "@/api/types/VereinRef";
import { PersonFilterBar } from "@/components/person/PersonFilterBar";
import { BottomActionBar } from "@/components/common/BottomActionBar";

import {
  getPersonenPage,
  deletePerson as dbDeletePerson,
  createPerson as dbCreatePerson,
  updatePerson as dbReplacePerson,
} from "@/api/services/personApi";

/* ========================================================= */

export interface PersonFilterState {
  name?: string;
  vorname?: string;
  vereinId?: number;
  aktiv?: boolean;
}

export interface PersonenState {
  data: PersonList[];
  total: number;
  page: number;
  pageSize: number;

  sortField: string;
  sortDirection: "asc" | "desc";

  vereine: VereinRef[];
  filters: PersonFilterState;

  selectedPerson: PersonDetail | null;
  draftPerson: PersonSave | null;
  loading: boolean;
  error: null | string;

  personFormEditMode: boolean;
  btnLöschenIsDisabled: boolean;
  btnÄndernIsDisabled: boolean;
  btnNeuePersonIsDisabled: boolean;
  createDialogOpen: boolean;
}

/* ========================================================= */

class Personen extends Component<Record<string, never>, PersonenState> {
  state: PersonenState = {
    data: [],
    total: 0,
    page: 0,
    pageSize: 8,

    sortField: "name",
    sortDirection: "asc",

    vereine: [],
    filters: {},

    selectedPerson: null,
    draftPerson: null,
    loading: true,
    error: null,

    personFormEditMode: false,
    btnLöschenIsDisabled: true,
    btnÄndernIsDisabled: true,
    btnNeuePersonIsDisabled: false,
    createDialogOpen: false,
  };

  /* ========================================================= */

  async componentDidMount() {
    await Promise.all([this.fetchPersonenData(), this.fetchVereine()]);
  }

  fetchVereine = async () => {
    const res = await apiClient.get<VereinRef[]>("/verein");
    this.setState({ vereine: res.data });
  };

  /* ========================================================= */
  /* 🔎 LOAD DATA (Paging + Sorting + Filter) */
  /* ========================================================= */

  fetchPersonenData = async () => {
    const { page, pageSize, filters, sortField, sortDirection, selectedPerson } = this.state;

    try {
      const res = await getPersonenPage(page, pageSize, filters, sortField, sortDirection);

      const stillExists =
        selectedPerson && res.content.some((p: PersonList) => p.id === selectedPerson.id);

      this.setState({
        data: res.content,
        total: res.totalElements,
        loading: false,
        error: null,
        selectedPerson: stillExists ? selectedPerson : null,
      });
    } catch {
      this.setState({
        data: [],
        loading: false,
        error: "Fehler beim Laden der Personen",
      });
    }
  };

  /* ========================================================= */
  /* Paging */
  /* ========================================================= */

  handlePageChange = (page: number) => {
    this.setState({ page, loading: true }, this.fetchPersonenData);
  };

  handlePageSizeChange = (pageSize: number) => {
    this.setState({ pageSize, page: 0, loading: true }, this.fetchPersonenData);
  };

  /* ========================================================= */
  /* Sorting */
  /* ========================================================= */

  handleSortChange = (field: string, direction: "asc" | "desc") => {
    this.setState(
      {
        sortField: field,
        sortDirection: direction,
        page: 0,
        loading: true,
      },
      this.fetchPersonenData,
    );
  };

  /* ========================================================= */

  resetFilters = () => {
    this.setState({ filters: {}, page: 0, loading: true }, this.fetchPersonenData);
  };

  /* ========================================================= */
  /* Person Selection */
  /* ========================================================= */

  handleSelectPerson = async (row: PersonList | null) => {
    if (!row) {
      this.setState({
        selectedPerson: null,
        btnLöschenIsDisabled: true,
        btnÄndernIsDisabled: true,
      });
      return;
    }

    const detail = await getPersonById(row.id);

    this.setState({
      selectedPerson: detail,
      draftPerson: null,
      btnLöschenIsDisabled: false,
      btnÄndernIsDisabled: false,
      personFormEditMode: false,
    });
  };

  /* ========================================================= */
  /* CRUD */
  /* ========================================================= */

  btnSpeichern = async (person: PersonSave) => {
    if (!this.state.selectedPerson) return;

    const saved = await dbReplacePerson(this.state.selectedPerson.id, person);
    await this.fetchPersonenData();

    this.setState({
      selectedPerson: saved,
      personFormEditMode: false,
      btnÄndernIsDisabled: false,
      btnLöschenIsDisabled: false,
    });
  };

  deletePerson = async () => {
    const { selectedPerson } = this.state;
    if (!selectedPerson) return;

    await dbDeletePerson(selectedPerson.id);
    await this.fetchPersonenData();

    this.setState({
      selectedPerson: null,
      btnLöschenIsDisabled: true,
      btnÄndernIsDisabled: true,
    });
  };

  editPerson = () => {
    this.setState({
      personFormEditMode: true,
      btnLöschenIsDisabled: true,
      btnÄndernIsDisabled: true,
    });
  };

  cancelEdit = () => {
    this.setState({
      personFormEditMode: false,
      draftPerson: null,
    });
  };

  btnStartMenue = () => {
    window.location.href = "/startmenue";
  };

  deleteMitglied = async (mitgliedId: number) => {
    await apiClient.delete(`/mitglied/${mitgliedId}`);
    await this.reloadSelectedPerson();
  };

  setHauptverein = async (mitgliedId: number) => {
    await apiClient.put(`/mitglied/${mitgliedId}/hauptverein`);
    await this.reloadSelectedPerson();
  };

  reloadSelectedPerson = async () => {
    if (!this.state.selectedPerson) return;
    const fresh = await getPersonById(this.state.selectedPerson.id);
    this.setState({ selectedPerson: fresh });
  };

  /* ========================================================= */

  render() {
    const { data, total, selectedPerson, loading, error, createDialogOpen } = this.state;

    return (
      <div>
        <Box display="flex" justifyContent="space-between" gap={2} mb={2}>
          <MenueHeader headerText={`${total} Personen`} />

          <PersonFilterBar
            filters={this.state.filters}
            vereine={this.state.vereine}
            onChange={(filters) =>
              this.setState({ filters, page: 0, loading: true }, this.fetchPersonenData)
            }
            onReset={this.resetFilters}
          />
        </Box>

        {renderLoadingOrError({ loading, error })}

        <PersonTable
          data={data}
          total={this.state.total}
          page={this.state.page}
          pageSize={this.state.pageSize}
          onPageChange={this.handlePageChange}
          onPageSizeChange={this.handlePageSizeChange}
          selectedPersonId={selectedPerson?.id ?? null}
          onSelectPerson={this.handleSelectPerson}
          sortField={this.state.sortField}
          sortDirection={this.state.sortDirection}
          onSortChange={this.handleSortChange}
        />

        <br />

        <PersonFormView
          personDetail={this.state.selectedPerson}
          editMode={this.state.personFormEditMode}
          onEdit={this.editPerson}
          onCancelEdit={this.cancelEdit}
          onSpeichern={this.btnSpeichern}
          onDeletePerson={this.deletePerson}
          onDeleteMitglied={this.deleteMitglied}
          onSetHauptverein={this.setHauptverein}
          onStartMenue={this.btnStartMenue}
          onReloadPerson={this.reloadSelectedPerson}
          btnÄndernPerson={this.state.btnÄndernIsDisabled}
          btnLöschenPerson={this.state.btnLöschenIsDisabled}
        />
        {!selectedPerson && (
          <BottomActionBar
            left={[
              {
                label: "Neue Person",
                variant: "outlined",
                onClick: () => this.setState({ createDialogOpen: true }),
              },
              {
                label: "Zurück",
                variant: "outlined",
                onClick: this.btnStartMenue,
              },
            ]}
          />
        )}

        <PersonCreateDialog
          open={createDialogOpen}
          onClose={() => this.setState({ createDialogOpen: false })}
          onCreate={async (person) => {
            const saved = await dbCreatePerson(person);
            await this.fetchPersonenData();

            this.setState({
              createDialogOpen: false,
              selectedPerson: saved, // ⭐ exakt wie Verein
              btnÄndernIsDisabled: false,
              btnLöschenIsDisabled: false,
            });
          }}
        />
      </div>
    );
  }
}

export default Personen;
