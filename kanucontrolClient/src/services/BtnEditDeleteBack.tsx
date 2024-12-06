import React from "react";
import { Button } from "primereact/button";

interface BtnEditDeleteBackProps {
  onÄndern: () => void;
  btnÄndern: boolean;
  setVisible: React.Dispatch<React.SetStateAction<boolean>>;
  btnLöschen: boolean;
  onStartMenue: () => void;
}

export function BtnEditDeleteBack({
  onÄndern,
  btnÄndern,
  setVisible,
  btnLöschen,
  onStartMenue,
}: BtnEditDeleteBackProps) {
  return (
    <>
      <Button
        label="Ändern"
        className="p-button-outlined p-button-warning m-2"
        onClick={onÄndern}
        disabled={btnÄndern}
      />
      <Button
        onClick={() => setVisible(true)}
        className="p-button-outlined p-button-danger m-2"
        label="Löschen"
        disabled={btnLöschen}
      />
      <Button
        label="Zurück"
        className="p-button-outlined m-2"
        onClick={onStartMenue}
      />
    </>
  );
}
