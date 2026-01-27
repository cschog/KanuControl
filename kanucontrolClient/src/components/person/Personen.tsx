import { Component } from "react";
import { MenueHeader } from "@/components/layout/MenueHeader";
import { PersonTable } from "@/components/person/PersonTable";
import { PersonFormView } from "@/components/person/PersonFormView";
import { PersonEditForm } from "@/components/person/PersonEditForm";
import { Person } from "@/api/types/Person";
import { renderLoadingOrError } from "@/components/common/loadingOnErrorUtils";
import { navigateToStartMenu } from "@/components/layout/navigateToStartMenue";

import {
  getAllPersonen as dbGetAllPersonen,
  deletePerson as dbDeletePerson,
  createPerson as dbCreatePerson,
  updatePerson as dbReplacePerson,
} from "@/api/services/personApi";

interface PersonenState {
  data: Person[];
  selectedPerson: Person | null;
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

  btnSpeichern = async (person: Person) => {
    try {
      let response: Person;

      if (person.id) {
        response = await dbReplacePerson(person); // ✏️ UPDATE
      } else {
        response = await dbCreatePerson(person); // ➕ CREATE
      }

      this.setState((prev) => {
        const exists = prev.data.some((p) => p.id === response.id);

        return {
          data: exists
            ? prev.data.map((p) => (p.id === response.id ? response : p))
            : [...prev.data, response],

          selectedPerson: response,
          personFormEditMode: false,
          modusNeuePerson: false, // 🔑 WICHTIG
          btnÄndernIsDisabled: false,
          btnLöschenIsDisabled: false,
        };
      });
    } catch (e) {
      console.error(e);
      alert("Speichern fehlgeschlagen");
    }
  };

  btnNeuePerson = () => {
    // Create a new empty person
    const newPerson: Person = {
      name: "",
      vorname: "",
      sex: "W", // oder Default deiner Wahl
      geburtsdatum: "",
      aktiv: true,
      strasse: "",
      plz: "",
      ort: "",
      countryCode: undefined, // ✅ FIX
      telefon: "",
      telefonFestnetz: "",
      bankName: "",
      iban: "",
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

  handleSelectPerson = (person: Person | null) => {
    if (!person) {
      this.setState({
        selectedPerson: null,
        btnLöschenIsDisabled: true,
        btnÄndernIsDisabled: true,
      });
      return;
    }

    this.setState({
      selectedPerson: person,
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
          key={data.map((p) => p.id).join("-")}
          data={data}
          selectedPerson={selectedPerson}
          onSelectPerson={this.handleSelectPerson}
        />
        <br />
        <div>
          {this.state.personFormEditMode && selectedPerson ? (
            <PersonEditForm
              person={selectedPerson}
              onSave={this.btnSpeichern}
              onCancel={this.btnAbbruch}
            />
          ) : (
            <PersonFormView
              onNeuePerson={this.btnNeuePerson}
              btnNeuePerson={this.state.btnNeuePersonIsDisabled}
              onÄndernPerson={this.btnSpeichern}
              btnÄndernPerson={this.state.btnÄndernIsDisabled}
              onDeletePerson={this.deletePerson}
              btnLöschenPerson={this.state.btnLöschenIsDisabled}
              onStartMenue={this.btnStartMenue}
              selectedPerson={selectedPerson}
            />
          )}
        </div>
      </div>
    );
  }
}

export default Personen;
