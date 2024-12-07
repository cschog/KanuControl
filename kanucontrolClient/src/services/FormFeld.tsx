import { InputText } from "primereact/inputtext";

interface FormFeldProps {
  value: string;
  label: string;
  disabled: boolean;
  onChange: (value: string) => void;
}

export const FormFeld: React.FC<FormFeldProps> = ({ value, label, disabled, onChange }) => (
  <div className="p-float-label">
    <InputText
      id={label}
      value={value}
      disabled={disabled}
      onChange={(e) => onChange(e.target.value)}
    />
    <label htmlFor={label}>{label}</label>
  </div>
);