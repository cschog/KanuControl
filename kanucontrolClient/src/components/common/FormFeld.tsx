import { InputText } from "primereact/inputtext";

interface FormFeldProps {
  value: string;
  label: string;
  disabled: boolean;
  onChange: (value: string) => void;
  className?: string; // Optional className prop for custom styling
}

export const FormFeld: React.FC<FormFeldProps> = ({
  value,
  label,
  disabled,
  onChange,
  className = "", // Default to an empty string
}) => (
  <div className={`p-float-label ${className}`}>
    <InputText
      id={label}
      value={value}
      disabled={disabled}
      onChange={(e) => onChange(e.target.value)}
      className="w-full" // Ensure input spans the width of its container
    />
    <label htmlFor={label}>{label}</label>
  </div>
);