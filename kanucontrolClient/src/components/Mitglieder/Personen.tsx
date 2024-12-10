import { Component } from "react";
import { MenueHeader } from "../MenueHeader";
import { PersonTable } from "./PersonTable";
import { PersonFormView } from "./PersonFormView";
import { PersonEditForm } from "./PersonEditForm";
import { Person } from "../interfaces/Person";
import { renderLoadingOrError } from "../../services/loadingOnErrorUtils";
import { navigateToStartMenu } from "../../services/navigateToStartMenue";
import { MessageSavingEmptyPerson } from "../../services/MessageSavingEmptyPerson";

import {
  getAllPersonen as dbGetAllPersonen,
  deletePerson as dbDeletePerson,
  createPerson as dbCreatePerson,
  replacePerson as dbReplacePerson,
} from "../../services/personen";

interface PersonenProps {}

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

class Personen extends Component<PersonenProps, PersonenState> {
  state: PersonenState = {
    data: [],
    selectedPerson: null,
    loading: true,
    error: null,
    personFormEditMode: false,
    modusNeuePerson: true,
    btnLöschenIsDisabled: true,
    btnÄndernIsDisabled: true,
    btnNeuePersonIsDisabled: false,
  };

  btnNeuePersonIsDisabled: boolean = false;
  btnÄndernIsDisabled: boolean = false;
  btnLöschenIsDisabled: boolean = false;

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
    } catch (error) {
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
    console.log("Saving person:", person); // Log the person object
    this.setState({
      btnLöschenIsDisabled: true,
      btnÄndernIsDisabled: true,
    });
  
    const { modusNeuePerson, selectedPerson } = this.state;
  
    try {

      let response;
      q
      if (
        (modusNeuePerson &&
          person.name.trim() !== "" &&
          person.vorname.trim() !== "") ||
        (!modusNeuePerson && selectedPerson)
      ) {
        if (modusNeuePerson) {
          response =  await dbCreatePerson(person); // Calls the API to create a new person
        } else if (selectedPerson) {
          person.id = selectedPerson.id; // Ensure the ID is set for updating
          response = await dbReplacePerson(person); // Calls the API to update the existing person
        } else {
          throw new Error("No selected Person found.");
        }
        console.log("API response:", response); // Log API response
        this.fetchPersonenData(); // Refresh data from the server
  
        if (!modusNeuePerson && selectedPerson) {
          this.setState({
            selectedPerson: {
              ...selectedPerson,
              ...person, // Merge updated data
            },
          });
        }
  
        this.setState({
          personFormEditMode: false,
        });
      } else {
        MessageSavingEmptyPerson(); // Handle the case where mandatory fields are empty
      }
    } catch (error) {
      console.error("Error saving person:", error);
      alert("Failed to save changes");
    }
  };

  btnNeuePerson = () => {
    // Create a new empty person
    const newPerson = {
      name: "",
      vorname: "",
      strasse: "",
      plz: "",
      ort: "",
      telefon: "",
      bankName: "",
      iban: "",
      bic: "",
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
    if (selectedPerson && selectedPerson.id !== undefined) {
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

  handleRowSelect = (event: { data: Person }) => {
    this.setState(
      {
        btnLöschenIsDisabled: false,
        btnÄndernIsDisabled: false,
        personFormEditMode: false,
        selectedPerson: event.data,
      },
      () => {
        //
      }
    );
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
          handleRowSelect={this.handleRowSelect}
        />

        <br />
        <div>
          {this.state.personFormEditMode ? (
            <PersonEditForm
              onSpeichern={this.btnSpeichern}
              onAbbruch={this.btnAbbruch}
              person={selectedPerson || undefined}
            />
          ) : (
            <PersonFormView
              onNeuePerson={this.btnNeuePerson}
              btnNeuePerson={this.btnNeuePersonIsDisabled}
              onÄndernPerson={this.editPerson}
              btnÄndernPerson={this.btnÄndernIsDisabled} // Correctly pass this prop
              onDeletePerson={this.deletePerson}
              btnLöschenPerson={this.btnLöschenIsDisabled}
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
