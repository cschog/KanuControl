import React, { ReactNode } from "react";
import { BtnEditDeleteBack } from "@/components/common/BtnEditDeleteBack";
import { Button } from "primereact/button";
import { ConfirmDialog } from "primereact/confirmdialog";
import { Verein } from "@/api/types/VereinFormModel";

interface ButtonNeuerVereinProps {
  onNeuerVerein: () => void;
  btnNeuerVerein: boolean;
  visible: boolean;
  setVisible: React.Dispatch<React.SetStateAction<boolean>>;
  selectedVerein: Verein | null;
  accept: () => void;
  reject: () => void;
  onÄndernVerein: () => void;
  btnÄndernVerein: boolean;
  btnLöschenVerein: boolean;
  onStartMenue: () => void;
}

export function buttonNeuerVerein({
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
}: ButtonNeuerVereinProps): ReactNode {
  return (
    <div>
      <Button
        label="Neuer Verein"
        className="p-button-outlined m-2"
        onClick={onNeuerVerein}
        disabled={btnNeuerVerein}
      />
      <ConfirmDialog
        visible={visible}
        onHide={() => setVisible(false)}
        message={selectedVerein ? "Soll der " + selectedVerein.name + " gelöscht werden?" : ""}
        header={selectedVerein ? "Löschen des " + selectedVerein.name + "?" : ""}
        icon="pi pi-exclamation-triangle"
        accept={accept}
        reject={reject}
      />
      <BtnEditDeleteBack
        onÄndern={onÄndernVerein}
        btnÄndern={btnÄndernVerein}
        setVisible={setVisible}
        btnLöschen={btnLöschenVerein}
        onStartMenue={onStartMenue}
      />
    </div>
  );
}
