import { Component } from "react";
import { MenueHeader } from "@/components/layout/MenueHeader";
import { PersonTable } from "@/components/person/PersonTable";
import { PersonFormView } from "@/components/person/PersonFormView";
import { renderLoadingOrError } from "@/components/common/loadingOnErrorUtils";
import { navigateToStartMenu } from "@/components/layout/navigateToStartMenue";
import { PersonList } from "@/api/types/PersonList";
import { PersonDetail } from "@/api/types/PersonDetail";
import { getPersonById } from "@/api/services/personApi";

import {
  getAllPersonen as dbGetAllPersonen,
  deletePerson as dbDeletePerson,
  createPerson as dbCreatePerson,
  updatePerson as dbReplacePerson,
} from "@/api/services/personApi";

interface PersonenState {
  data: PersonList[];
  selectedPerson: PersonDetail | null;
  loading: boolean;
  error: null | string;
  personFormEditMode: boolean;
  modusNeuePerson: boolean;
  btnLöschenIsDisabled: boolean;
  btnÄndernIsDisabled: boolean;
  btnNeuePersonIsDisabled: boolean;
}

class Personen extends Component<Record<string, never>, PersonenState> {
  state = {
    data: [],
    selectedPerson: null,
    loading: true,
    error: null,
    personFormEditMode: false,
    modusNeuePerson: true,
    btnLöschenIsDisabled: true,
    btnÄndernIsDisabled: true,
    btnNeuePersonIsDisabled: false,
  } as PersonenState;

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

  btnAbbruch = () => {
    this.setState({
      btnLöschenIsDisabled: true,
      personFormEditMode: false,
      selectedPerson: null,
    });
  };

  btnSpeichern = async (person: PersonDetail) => {
    try {
      let response: PersonDetail;

      if (person.id) {
        response = await dbReplacePerson(person);
      } else {
        response = await dbCreatePerson(person);
      }

      await this.fetchPersonenData();

      this.setState({
        selectedPerson: response,
        personFormEditMode: false,
        modusNeuePerson: false,
        btnÄndernIsDisabled: false,
        btnLöschenIsDisabled: false,
      });
    } catch (e) {
      console.error(e);
      alert("Speichern fehlgeschlagen");
    }
  };

  btnNeuePerson = () => {
    const newPerson: PersonDetail = {
      vorname: "",
      name: "",
      sex: "W",
      aktiv: true,
      geburtsdatum: undefined,
      ort: "",
      mitgliedschaften: [], // 🔑 extrem wichtig
    };

    this.setState({
      modusNeuePerson: true,
      btnLöschenIsDisabled: true,
      btnÄndernIsDisabled: true,
      btnNeuePersonIsDisabled: false,
      personFormEditMode: true,
      selectedPerson: newPerson,
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
      selectedPerson: detail,
      btnLöschenIsDisabled: false,
      btnÄndernIsDisabled: false,
      personFormEditMode: false,
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
            selectedPerson={selectedPerson}
            onNeuePerson={this.btnNeuePerson}
            onÄndernPerson={this.btnSpeichern}
            btnÄndernPerson={this.state.btnÄndernIsDisabled}
            onDeletePerson={this.deletePerson}
            btnLöschenPerson={this.state.btnLöschenIsDisabled}
            onStartMenue={this.btnStartMenue}
          />
        </div>
      </div>
    );
  }
}

export default Personen;
