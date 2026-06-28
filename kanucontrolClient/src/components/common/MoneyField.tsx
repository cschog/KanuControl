// src/components/common/MoneyField.tsx

// src/components/common/MoneyField.tsx

import InputAdornment from "@mui/material/InputAdornment";
import FormFeld, { FormFeldProps } from "@/components/common/FormFeld";

interface MoneyFieldProps extends Omit<FormFeldProps, "type"> {
  currency?: string;
}

const MoneyField = ({
  currency = "€",
  ...props
}: MoneyFieldProps) => {
  return (
    <FormFeld
      {...props}
      type="number"
      slotProps={{
        input: {
          endAdornment: (
            <InputAdornment position="end">
              {currency}
            </InputAdornment>
          ),
        },
      }}
    />
  );
};

export default MoneyField;