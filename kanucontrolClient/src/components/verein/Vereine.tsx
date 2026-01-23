import { Component } from "react";
import { MenueHeader } from "@/components/layout/MenueHeader";
import { VereinTable } from "@/components/verein/VereinTable";
import { VereinFormView } from "@/components/verein/VereinFormView";
import { VereinEditForm } from "@/components/verein/VereinEditForm";
import  Verein  from "@/api/types/VereinFormModel";
import { renderLoadingOrError } from "@/components/common/loadingOnErrorUtils";
import { navigateToStartMenu } from "@/components/layout/navigateToStartMenue";

import { MessageSavingEmptyVerein } from "@/api/services/MessageSavingEmptyVerein";

import {
  getAllVereine as dbGetAllVereine,
  deleteVerein as dbDeleteVerein,
  createVerein as dbCreateVerein,
  updateVerein as dbReplaceVerein,
} from "@/api/services/vereinApi";

interface VereineState {
  data: Verein[];
  selectedVerein: Verein | null;
  loading: boolean;
  error: null | string;
  vereinFormEditMode: boolean;
  modusNeuerVerein: boolean;
  btnLöschenIsDisabled: boolean;
  btnÄndernIsDisabled: boolean;
  btnNeuerVereinIsDisabled: boolean;
  //setModusNeuerVerein: boolean;
}

class Vereine extends Component<Record<string, never>, VereineState> {
  state: VereineState = {
    data: [],
    selectedVerein: null,
    loading: true,
    error: null,
    vereinFormEditMode: false,
    modusNeuerVerein: true,
    btnLöschenIsDisabled: true,
    btnÄndernIsDisabled: true,
    btnNeuerVereinIsDisabled: false,
    // setModusNeuerVerein: false,
    // setBtnLöschenIsDisabled: false,
    // setBtnÄndernIsDisabled: false,
    // setBtnNeuerVereinIsDisabled: true,
    // setVereinFormEditMode: false,
  };
  btnNeuerVereinIsDisabled: boolean = false;
  btnÄndernIsDisabled: boolean = false;
  btnLöschenIsDisabled: boolean = false;

  componentDidMount() {
    this.fetchVereineData();
  }

  fetchVereineData = async () => {
    try {
      const vereine = await dbGetAllVereine();
      this.setState({
        data: vereine,
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
      vereinFormEditMode: false,
      selectedVerein: null,
    });
  };

  btnSpeichern = async (verein: Verein) => {
    this.setState({
      btnLöschenIsDisabled: true,
      btnÄndernIsDisabled: true,
    });

    const { modusNeuerVerein, selectedVerein } = this.state;

    try {
      // Perform validation check here
      if (
        (modusNeuerVerein && verein.name.trim() !== "" && verein.abk.trim() !== "") ||
        (!modusNeuerVerein && selectedVerein)
      ) {
        if (modusNeuerVerein) {
          await dbCreateVerein(verein);
        } else if (selectedVerein) {
          verein.id = selectedVerein.id;
          await dbReplaceVerein(verein);
        } else {
          throw new Error("No selected Verein found.");
        }

        this.fetchVereineData();

        if (!modusNeuerVerein && selectedVerein) {
          this.setState({
            selectedVerein: {
              ...selectedVerein,
              ...verein,
            },
          });
        }

        this.setState({
          vereinFormEditMode: false,
        });
      } else {
        MessageSavingEmptyVerein();
      }
    } catch (error) {
      console.error("Error saving verein:", error);
    }
  };

  btnNeuerVerein = () => {
    // erzeuge einen neuen leeren Verein

    const newVerein = {
      name: "",
      abk: "",
      strasse: "",
      plz: "",
      ort: "",
      telefon: "",
      bankName: "",
      iban: "",
    };

    this.setState({
      modusNeuerVerein: true,
      btnLöschenIsDisabled: true,
      btnÄndernIsDisabled: true,
      btnNeuerVereinIsDisabled: false,
      vereinFormEditMode: true,
      selectedVerein: newVerein,
    });
  };

  handleSelectVerein = (verein: Verein | null) => {
    if (!verein) {
      this.setState({
        selectedVerein: null,
        btnLöschenIsDisabled: true,
        btnÄndernIsDisabled: true,
      });
      return;
    }

    this.setState({
      selectedVerein: verein,
      btnLöschenIsDisabled: false,
      btnÄndernIsDisabled: false,
    });
  };

  editVerein = () => {
    this.setState({
      vereinFormEditMode: true,
      btnLöschenIsDisabled: true,
      btnÄndernIsDisabled: true,
      modusNeuerVerein: false,
    });
  };

  deleteVerein = async () => {
    const { selectedVerein } = this.state;
    if (selectedVerein?.id !== undefined) {
      // Check if id is defined
      try {
        await dbDeleteVerein(selectedVerein.id);
        // Remove the deleted Verein from the state's data array
        this.setState((prevState) => ({
          data: prevState.data.filter((verein) => verein.id !== selectedVerein.id),
          selectedVerein: null,
          btnLöschenIsDisabled: true,
          btnÄndernIsDisabled: true,
        }));
      } catch (error) {
        // Handle error
        console.error("Error deleting verein:", error);
      }
    }
  };

  btnStartMenue = () => {
    navigateToStartMenu();
  };

  render() {
    const { data, selectedVerein } = this.state;
    const { loading, error } = this.state;
    const vereinAnz = data.length;

    return (
      <div>
        <MenueHeader headerText={`${vereinAnz} Vereine`} />
        {renderLoadingOrError({ loading, error })}

        <VereinTable
          data={data}
          selectedVerein={selectedVerein}
          onSelectVerein={this.handleSelectVerein}
        />

        <br />
        <div>
          {this.state.vereinFormEditMode ? (
            <VereinEditForm
              onSpeichern={this.btnSpeichern}
              onAbbruch={this.btnAbbruch}
              verein={selectedVerein || undefined}
            />
          ) : (
            <VereinFormView
              onNeuerVerein={this.btnNeuerVerein}
              btnNeuerVerein={this.btnNeuerVereinIsDisabled}
              onÄndernVerein={this.editVerein}
              btnÄndernVerein={this.btnÄndernIsDisabled}
              onDeleteVerein={this.deleteVerein}
              btnLöschenVerein={this.btnLöschenIsDisabled}
              onStartMenue={this.btnStartMenue}
              selectedVerein={selectedVerein}
            />
          )}
        </div>
      </div>
    );
  }
}

export default Vereine;
