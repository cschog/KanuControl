import React from "react";
import { InputText } from "primereact/inputtext";

export function FormFeld(
  feldName: string,
  label: string,
  feldDisabled: boolean,
  setFunction: (value: string) => void
) {
  return (
    <div className=" col-fixed">
      <div className="p-0">
        <span className="p-float-label">
          <InputText
            id={feldName}
            value={feldName}
            disabled={feldDisabled}
            onChange={(e) => setFunction(e.target.value)}
          />
          <label htmlFor="in">{label}</label>
        </span>
      </div>
    </div>
  );
}
