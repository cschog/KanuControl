import React, { ReactNode } from "react";
import { Button } from "primereact/button";
import { ConfirmDialog } from "primereact/confirmdialog";
import  VereinDTO from "../interfaces/VereinDTO";

interface BtnNeuerVereinProps {
  readonly onNeuerVerein: () => void; // Callback to create a new Verein
  readonly btnNeuerVerein: boolean; // Disable or enable the "New" button
  readonly visible: boolean; // State to show/hide the confirmation dialog
  readonly setVisible: React.Dispatch<React.SetStateAction<boolean>>; // Setter for `visible`
  readonly selectedVerein: VereinDTO | null; // Currently selected Verein
  readonly accept: () => void; // Callback for confirming delete
  readonly reject: () => void; // Callback for canceling delete
  readonly onÄndernVerein: () => void; // Callback to edit a Verein
  readonly btnÄndernVerein: boolean; // Disable or enable the "Edit" button
  readonly btnLöschenVerein: boolean; // Disable or enable the "Delete" button
  readonly onStartMenue: () => void; // Callback to navigate back to the main menu
}

export function BtnNeuerVerein({
  onNeuerVerein,
  btnNeuerVerein,
  visible,
  setVisible,
  selectedVerein,
  accept,
  reject,
  onÄndernVerein,
  btnÄndernVerein,
  btnLöschenVerein,
  onStartMenue,
}: BtnNeuerVereinProps): ReactNode {
  return (
    <div className="btn-container">
      <Button
        label="Neuer Verein"
        className="p-button-outlined m-2"
        onClick={onNeuerVerein}
        disabled={btnNeuerVerein}
      />
      <Button
        label="Ändern"
        className="p-button-outlined p-button-warning m-2"
        onClick={onÄndernVerein}
        disabled={btnÄndernVerein}
      />
      <Button
        label="Löschen"
        className="p-button-outlined p-button-danger m-2"
        onClick={() => setVisible(true)}
        disabled={btnLöschenVerein}
      />
      <Button
        label="Zurück"
        className="p-button-outlined m-2"
        onClick={onStartMenue}
      />
      <ConfirmDialog
        visible={visible}
        onHide={() => setVisible(false)}
        message={
          selectedVerein
            ? `Möchten Sie den Verein ${selectedVerein.name} wirklich löschen?`
            : "Kein Verein ausgewählt."
        }
        header="Bestätigung erforderlich"
        icon="pi pi-exclamation-triangle"
        accept={accept}
        reject={reject}
      />
    </div>
  );
}