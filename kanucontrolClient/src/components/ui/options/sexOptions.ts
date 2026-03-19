// src/ui/options/sexOptions.ts
import { Sex } from "@/api/enums/Sex";

export const SEX_OPTIONS: { value: Sex; label: string }[] = [
  { value: "M", label: "Männlich" },
  { value: "W", label: "Weiblich" },
  { value: "D", label: "Divers" },
];