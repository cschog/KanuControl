import { Component } from "react";
import { MenueHeader } from "@/components/layout/MenueHeader";
import { PersonTable } from "@/components/person/PersonTable";
import { PersonFormView } from "@/components/person/PersonFormView";
import { PersonEditForm } from "@/components/person/PersonEditForm";
import { Person } from "@/api/types/Person";
import { renderLoadingOrError } from "@/components/common/loadingOnErrorUtils";
import { navigateToStartMenu } from "@/components/layout/navigateToStartMenue";
import { MessageSavingEmptyPerson } from "@/api/services/MessageSavingEmptyPerson"; 

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
    this.setState({
      btnLöschenIsDisabled: true,
      btnÄndernIsDisabled: true,
    });
  
    const { modusNeuePerson, selectedPerson } = this.state;
  
    const isValid =
      person.name.trim() !== "" && person.vorname.trim() !== "";
  
    if (modusNeuePerson && !isValid) {
      MessageSavingEmptyPerson();
      return;
    }
  
    if (!modusNeuePerson && !selectedPerson) {
      throw new Error("No selected person");
    }
  
    try {
      let response: Person;
  
      if (modusNeuePerson) {
        response = await dbCreatePerson(person);
      } else {
        const personToSave: Person = {
          ...person,
          id: selectedPerson!.id,
        };
        response = await dbReplacePerson(personToSave);
      }
  
      await this.fetchPersonenData();
  
      this.setState({
        personFormEditMode: false,
        selectedPerson: response,
      });
    } catch (error) {
      console.error("Error saving person:", error);
      alert("Failed to save changes");
    }
  };

  btnNeuePerson = () => {
    // Create a new empty person
    const newPerson: Person = {
      name: "",
      vorname: "",
      sex: "WEIBLICH", // oder Default deiner Wahl
      geburtsdatum: "",
      aktiv: true,
      strasse: "",
      plz: "",
      ort: "",
      countryCode: "",
      telefon: "",
      telefonFestnetz: "",
      bankName: "",
      iban: ""
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
      btnLöschenIsDisabled: true,
      btnÄndernIsDisabled: true,
      modusNeuePerson: false,
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
          data: prevState.data.filter(
            (person) => person.id !== selectedPerson.id
          ),
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

    return (
      <div>
        <MenueHeader headerText={`${personAnz} Personen`} />
        {renderLoadingOrError({ loading, error })}

        <PersonTable
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
    onÄndernPerson={this.editPerson}
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