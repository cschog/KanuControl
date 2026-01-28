import { Component } from "react";
import { MenueHeader } from "@/components/layout/MenueHeader";
import { PersonTable } from "@/components/person/PersonTable";
import { PersonFormView } from "@/components/person/PersonFormView";
import { renderLoadingOrError } from "@/components/common/loadingOnErrorUtils";
import { navigateToStartMenu } from "@/components/layout/navigateToStartMenue";
import { PersonList, PersonSave, PersonDetail } from "@/api/types/Person";
import { getPersonById } from "@/api/services/personApi";
import apiClient from "@/api/client/apiClient";
import { toPersonSaveDTO } from "@/api/mappers/personMapper";

import {
  getAllPersonen as dbGetAllPersonen,
  deletePerson as dbDeletePerson,
  createPerson as dbCreatePerson,
  updatePerson as dbReplacePerson,
} from "@/api/services/personApi";

interface PersonenState {
  data: PersonList[];
  selectedPerson: PersonDetail | null; // 🔑 Detail im UI
  draftPerson: PersonSave | null; // 🔑 Formularzustand
  loading: boolean;
  error: null | string;
  personFormEditMode: boolean;
  modusNeuePerson: boolean;
  btnLöschenIsDisabled: boolean;
  btnÄndernIsDisabled: boolean;
  btnNeuePersonIsDisabled: boolean;
}

class Personen extends Component<Record<string, never>, PersonenState> {
  state: PersonenState = {
    data: [],
    selectedPerson: null,
    draftPerson: null,
    loading: true,
    error: null,
    personFormEditMode: false,
    modusNeuePerson: true,
    btnLöschenIsDisabled: true,
    btnÄndernIsDisabled: true,
    btnNeuePersonIsDisabled: false,
  };

  componentDidMount() {
    this.fetchPersonenData();
  }

  fetchPersonenData = async () => {
    try {
      const personen = await dbGetAllPersonen();
      this.setState({
        data: personen,
        loading: false,
        error: null,
      });
    } catch {
      this.setState({
        data: [],
        loading: false,
        error: "An error occurred while fetching data.",
      });
    }
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
    try {
      let saved: PersonDetail;

      if (this.state.modusNeuePerson) {
        saved = await dbCreatePerson(person);
      } else {
        if (!this.state.selectedPerson) {
          throw new Error("No selected person for update");
        }
        saved = await dbReplacePerson(this.state.selectedPerson.id, person);
      }

      await this.fetchPersonenData();

      this.setState({
        selectedPerson: saved, // 🔑 DAS ist der Fix
        personFormEditMode: false,
        modusNeuePerson: false,
        draftPerson: null,
        btnÄndernIsDisabled: false,
        btnLöschenIsDisabled: false,
      });
    } catch (e) {
      console.error(e);
      alert("Speichern fehlgeschlagen");
    }
  };

  btnNeuePerson = () => {
    const newPerson: PersonSave = {
      vorname: "",
      name: "",
      sex: "W",
      aktiv: true,
      geburtsdatum: undefined,
      ort: "",
      mitgliedschaften: [],
    };

    this.setState({
      modusNeuePerson: true,
      personFormEditMode: true,
      draftPerson: newPerson, // ✅ Save-Typ
      selectedPerson: null, // ✅ kein Detail
      btnLöschenIsDisabled: true,
      btnÄndernIsDisabled: true,
    });
  };

  editPerson = () => {
    this.setState({
      personFormEditMode: true,
      modusNeuePerson: false, // 🔑 EXPLIZIT
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

    this.setState({
      selectedPerson: fresh,
      draftPerson: toPersonSaveDTO(fresh),
    });
  };

  render() {
    const { data, selectedPerson } = this.state;
    const { loading, error } = this.state;
    const personAnz = data.length;

    // console.log("RENDER Personen – data:", data);

    return (
      <div>
        <MenueHeader headerText={`${personAnz} Personen`} />
        {renderLoadingOrError({ loading, error })}

        <PersonTable
          data={data}
          selectedPersonId={selectedPerson?.id ?? null}
          onSelectPerson={this.handleSelectPerson}
        />
        <br />
        <div>
          <PersonFormView
            personDetail={this.state.selectedPerson}
            draftPerson={this.state.draftPerson}
            editMode={this.state.personFormEditMode}
            onNeuePerson={this.btnNeuePerson}
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
        </div>
      </div>
    );
  }
}

export default Personen;
