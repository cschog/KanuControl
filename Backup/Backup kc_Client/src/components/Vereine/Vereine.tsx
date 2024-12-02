import React, { Component } from "react";
import { MenueHeader } from "../MenueHeader";
import { VereinTable } from "./VereinTable";
import { VereinFormView } from "./VereinFormView";
import { VereinEditForm } from "./VereinEditForm";
import  VereinDTO  from "../interfaces/VereinDTO";
import { getAllVereine, createVerein, replaceVerein, deleteVerein } from "../../services/vereine";

interface VereineState {
  data: VereinDTO[];
  selectedVerein: VereinDTO | null;
  loading: boolean;
  error: string | null;
  formEditMode: boolean;
  isNewVerein: boolean;
}

class Vereine extends Component<{}, VereineState> {
  state: VereineState = {
    data: [],
    selectedVerein: null,
    loading: true,
    error: null,
    formEditMode: false,
    isNewVerein: true,
  };

  componentDidMount() {
    this.fetchVereine();
  }

  fetchVereine = async () => {
    this.setState({ loading: true, error: null });
    try {
      const data = await getAllVereine();
      this.setState({ data, loading: false });
    } catch (error) {
      this.setState({ error: "Failed to load Vereine.", loading: false });
    }
  };

  handleSave = async (verein: VereinDTO) => {
    try {
      if (this.state.isNewVerein) {
        await createVerein(verein);
      } else {
        await replaceVerein(verein);
      }
      this.fetchVereine();
      this.setState({ formEditMode: false, selectedVerein: null });
    } catch (error) {
      console.error("Failed to save Verein:", error);
    }
  };

  handleDelete = async () => {
    if (this.state.selectedVerein?.id) {
      try {
        await deleteVerein(this.state.selectedVerein.id);
        this.fetchVereine();
      } catch (error) {
        console.error("Failed to delete Verein:", error);
      }
    }
  };

  handleNew = () => {
    this.setState({
      formEditMode: true,
      isNewVerein: true,
      selectedVerein: {
        name: "",
        abk: "",
      } as VereinDTO,
    });
  };

  handleEdit = () => {
    this.setState({ formEditMode: true, isNewVerein: false });
  };

  handleCancel = () => {
    this.setState({ formEditMode: false, selectedVerein: null });
  };

  render() {
    const { data, selectedVerein, loading, error, formEditMode } = this.state;

    return (
      <div>
        <MenueHeader headerText="Vereine" />
        {loading && <p>Loading...</p>}
        {error && <p>{error}</p>}
        <VereinTable
          data={data}
          selectedVerein={selectedVerein}
          onRowSelect={(verein) => this.setState({ selectedVerein: verein })}
        />
        {formEditMode ? (
          <VereinEditForm
          verein={selectedVerein || undefined} // Convert `null` to `undefined`
          onSpeichern={this.handleSave}
          onAbbruch={this.handleCancel}
        />
        ) : (
          <VereinFormView
              onNeuerVerein={this.handleNew}
              onÄndernVerein={this.handleEdit}
              onDeleteVerein={this.handleDelete}
              selectedVerein={selectedVerein} btnNeuerVerein={false} btnÄndernVerein={false} btnLöschenVerein={false} onStartMenue={function (): void {
                throw new Error("Function not implemented.");
              } }          />
        )}
      </div>
    );
  }
}

export default Vereine;