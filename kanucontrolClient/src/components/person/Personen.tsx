import { Component } from "react";
import { MenueHeader } from "@/components/layout/MenueHeader";
import { PersonTable } from "@/components/person/PersonTable";
import { PersonFormView } from "@/components/person/PersonFormView";
import { renderLoadingOrError } from "@/components/common/loadingOnErrorUtils";
import { navigateToStartMenu } from "@/components/layout/navigateToStartMenue";
import { PersonList, PersonSave, PersonDetail } from "@/api/types/Person";
import { getPersonById } from "@/api/services/personApi";
import apiClient from "@/api/client/apiClient";
import { PersonCreateDialog } from "@/components/person/PersonCreateDialog";
import { Box } from "@mui/material";
import { BottomActionBar } from "@/components/common/BottomActionBar";
import { VereinRef } from "@/api/types/VereinRef";
import { PersonFilterBar } from "@/components/person/PersonFilterBar";

import {
  getPersonenPage,
  deletePerson as dbDeletePerson,
  createPerson as dbCreatePerson,
  updatePerson as dbReplacePerson,
} from "@/api/services/personApi";

export interface PersonenState {
  data: PersonList[];
  total: number;
  page: number;
  pageSize: number;

  vereine: VereinRef[]; // ✅ HIER
  filters: PersonFilterState; // ✅ HIER

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

export interface PersonFilterState {
  name?: string;
  vorname?: string;
  vereinId?: number;
  aktiv?: boolean;
}

class Personen extends Component<Record<string, never>, PersonenState> {
  state: PersonenState & { filters: PersonFilterState } = {
    data: [],
    total: 0,
    page: 0,
    pageSize: 20,

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

  async componentDidMount() {
    await Promise.all([this.fetchPersonenData(), this.fetchVereine()]);
  }

  fetchVereine = async () => {
    const res = await apiClient.get<VereinRef[]>("/verein");
    this.setState({ vereine: res.data });
  };

  fetchPersonenData = async () => {
    const { page, pageSize, filters } = this.state;

    try {
      const res = await getPersonenPage(page, pageSize, filters);

      this.setState({
        data: res.content,
        total: res.totalElements,
        loading: false,
        error: null,
      });
    } catch {
      this.setState({
        data: [],
        loading: false,
        error: "Fehler beim Laden der Personen",
      });
    }
  };

  handlePageChange = (page: number) => {
    this.setState({ page, loading: true }, this.fetchPersonenData);
  };

  handlePageSizeChange = (pageSize: number) => {
    this.setState({ pageSize, page: 0, loading: true }, this.fetchPersonenData);
  };

  // ↓↓↓ alle weiteren Methoden ganz normal ↓↓↓

  btnAbbruch = () => {
    this.setState({
      btnLöschenIsDisabled: true,
      personFormEditMode: false,
      selectedPerson: null,
    });
  };

  btnSpeichern = async (person: PersonSave) => {
    if (!this.state.selectedPerson) {
      throw new Error("No selected person for update");
    }

    const saved = await dbReplacePerson(this.state.selectedPerson.id, person);

    await this.fetchPersonenData();

    this.setState({
      selectedPerson: saved,
      personFormEditMode: false,
      btnÄndernIsDisabled: false,
      btnLöschenIsDisabled: false,
    });
  };

  btnNeuePerson = () => {
    this.setState({ createDialogOpen: true });
  };

  editPerson = () => {
    this.setState({
      personFormEditMode: true,
      btnLöschenIsDisabled: true,
      btnÄndernIsDisabled: true,
    });
  };

  deletePerson = async () => {
    const { selectedPerson } = this.state;
    if (selectedPerson?.id !== undefined) {
      // Check if id is defined
      try {
        await dbDeletePerson(selectedPerson.id);
        // Remove the deleted Person from the state's data array
        this.setState((prevState) => ({
          data: prevState.data.filter((person) => person.id !== selectedPerson.id),
          selectedPerson: null,
          btnLöschenIsDisabled: true,
          btnÄndernIsDisabled: true,
        }));
      } catch (error) {
        // Handle error
        console.error("Error deleting person:", error);
      }
    }
  };

  btnStartMenue = () => {
    navigateToStartMenu();
  };

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
      selectedPerson: detail, // PersonDetail
      draftPerson: null,
      btnLöschenIsDisabled: false,
      btnÄndernIsDisabled: false,
      personFormEditMode: false,
    });
  };

  setHauptverein = async (mitgliedId: number) => {
    await apiClient.put(`/mitglied/${mitgliedId}/hauptverein`);
    await this.reloadSelectedPerson();
  };

  deleteMitglied = async (mitgliedId: number) => {
    await apiClient.delete(`/mitglied/${mitgliedId}`);
    await this.reloadSelectedPerson();
  };

  cancelEdit = () => {
    this.setState({
      personFormEditMode: false,
      draftPerson: null,
    });
  };

  reloadSelectedPerson = async () => {
    const fresh = await getPersonById(this.state.selectedPerson!.id);
    this.setState({ selectedPerson: fresh });
  };

  render() {
    const { data, total, selectedPerson, loading, error, createDialogOpen } = this.state;

    const personAnz = typeof total === "number" ? total : 0;

    // console.log("RENDER Personen – data:", data);

    return (
      <div>
        <Box
          display="flex"
          alignItems="center"
          justifyContent="space-between"
          flexWrap="wrap"
          gap={2}
          mb={2}
        >
          <MenueHeader headerText={`${personAnz} Personen`} />
          <PersonFilterBar
            filters={this.state.filters}
            vereine={this.state.vereine}
            onChange={(filters: PersonFilterState) =>
              this.setState({ filters, page: 0, loading: true }, this.fetchPersonenData)
            }
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
        />
        <br />
        <div>
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
            btnÄndernPerson={this.state.btnÄndernIsDisabled}
            btnLöschenPerson={this.state.btnLöschenIsDisabled}
            onReloadPerson={this.reloadSelectedPerson}
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
          {/* ✅ CREATE DIALOG */}
          <PersonCreateDialog
            open={createDialogOpen}
            onClose={() => this.setState({ createDialogOpen: false })}
            onCreate={async (person) => {
              const saved = await dbCreatePerson(person);
              await this.fetchPersonenData();

              this.setState({
                createDialogOpen: false,
                selectedPerson: saved,
                personFormEditMode: false,
                btnÄndernIsDisabled: false,
                btnLöschenIsDisabled: false,
              });
            }}
          />
        </div>
      </div>
    );
  }
}

export default Personen;
