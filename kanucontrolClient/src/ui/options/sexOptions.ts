// src/ui/options/sexOptions.ts
import { Sex } from "../../api/enums/Sex";

export const SEX_OPTIONS: { value: Sex; label: string }[] = [
  { value: "MAENNLICH", label: "M" },
  { value: "WEIBLICH", label: "W" },
  { value: "DIVERS", label: "D" },
];