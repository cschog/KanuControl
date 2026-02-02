import { Component } from "react";
import { MenueHeader } from "@/components/layout/MenueHeader";
import { VereinTable } from "@/components/verein/VereinTable";
import { VereinFormView } from "@/components/verein/VereinFormView";
import { VereinCreateDialog } from "@/components/verein/VereinCreateDialog";
import Verein from "@/api/types/VereinFormModel";
import { renderLoadingOrError } from "@/components/common/loadingOnErrorUtils";
import { navigateToStartMenu } from "@/components/layout/navigateToStartMenue";
import { BottomActionBar } from "@/components/common/BottomActionBar";
import { VereinSave } from "@/api/types/VereinSave";
import { VereinCsvImportDialog } from "@/components/verein/import/VereinCsvImportDialog";

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
  btnLöschenIsDisabled: boolean;
  btnÄndernIsDisabled: boolean;

  createDialogOpen: boolean;
  csvImportOpen: boolean;   // ✅ HIER
}

class Vereine extends Component<Record<string, never>, VereineState> {
  state: VereineState = {
  data: [],
  selectedVerein: null,
  loading: true,
  error: null,

  vereinFormEditMode: false,
  btnLöschenIsDisabled: true,
  btnÄndernIsDisabled: true,

  createDialogOpen: false,
  csvImportOpen: false,     // ✅ HIER
};

  componentDidMount() {
    this.fetchVereineData();
  }

  fetchVereineData = async () => {
    try {
      const vereine = await dbGetAllVereine();
      this.setState({ data: vereine, loading: false, error: null });
    } catch {
      this.setState({
        data: [],
        loading: false,
        error: "An error occurred while fetching data.",
      });
    }
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
      vereinFormEditMode: false,
      btnLöschenIsDisabled: false,
      btnÄndernIsDisabled: false,
    });
  };

  editVerein = () => {
    this.setState({
      vereinFormEditMode: true,
      btnLöschenIsDisabled: true,
      btnÄndernIsDisabled: true,
    });
  };

  cancelEdit = () => {
    this.setState({
      vereinFormEditMode: false,
    });
  };

  saveVerein = async (verein: VereinSave) => {
    if (!this.state.selectedVerein?.id) return;

    const saved = await dbReplaceVerein(this.state.selectedVerein.id, verein);

    await this.fetchVereineData();

    this.setState({
      selectedVerein: saved,
      vereinFormEditMode: false,
      btnÄndernIsDisabled: false,
      btnLöschenIsDisabled: false,
    });
  };

  deleteVerein = async () => {
    const { selectedVerein } = this.state;
    if (!selectedVerein?.id) return;

    await dbDeleteVerein(selectedVerein.id);

    this.setState({
      selectedVerein: null,
      btnLöschenIsDisabled: true,
      btnÄndernIsDisabled: true,
    });

    await this.fetchVereineData();
  };


  render() {
    const { data, selectedVerein, loading, error, createDialogOpen } = this.state;
  

    return (
      <div>
        <MenueHeader headerText={`${data.length} Vereine`} />
        {renderLoadingOrError({ loading, error })}

        <VereinTable
          data={data}
          selectedVerein={selectedVerein}
          onSelectVerein={this.handleSelectVerein}
        />

        <br />

        <VereinFormView
          verein={selectedVerein}
          editMode={this.state.vereinFormEditMode}
          onEdit={this.editVerein}
          onCancelEdit={this.cancelEdit}
          onSave={this.saveVerein}
          onDelete={this.deleteVerein}
          onBack={navigateToStartMenu}
          onCsvImport={() => this.setState({ csvImportOpen: true })}
          disableEdit={this.state.btnÄndernIsDisabled}
          disableDelete={this.state.btnLöschenIsDisabled}
        />

        {!selectedVerein && (
          <BottomActionBar
            left={[
              {
                label: "Neuer Verein",
                variant: "outlined",
                onClick: () => this.setState({ createDialogOpen: true }),
              },
              {
                label: "Zurück",
                variant: "outlined",
                onClick: navigateToStartMenu,
              },
            ]}
          />
        )}

        {selectedVerein?.id !== undefined && (
          <VereinCsvImportDialog
            open={this.state.csvImportOpen}
            vereinId={selectedVerein.id}
            onClose={() => this.setState({ csvImportOpen: false })}
          />
        )}

        <VereinCreateDialog
          open={createDialogOpen}
          onClose={() => this.setState({ createDialogOpen: false })}
          onCreate={async (verein) => {
            const saved = await dbCreateVerein(verein);
            await this.fetchVereineData();

            this.setState({
              createDialogOpen: false,
              selectedVerein: saved,
              btnÄndernIsDisabled: false,
              btnLöschenIsDisabled: false,
            });
          }}
        />
      </div>
    );
  }
}

export default Vereine;
