// src/components/common/SliderNumberField.tsx

import {
    Box,
    Slider,
    TextField,
    Typography,
} from "@mui/material";
import InputAdornment from "@mui/material/InputAdornment";

interface SliderNumberFieldProps {

    label: string;

    value: number;

    onChange(
        value: number
    ): void;

    min: number;
    max: number;

    step?: number;

    suffix?: string;

    disabled?: boolean;
}

export default function SliderNumberField({

    label,

    value,

    onChange,

    min,
    max,

    step = 1,

    suffix,

    disabled = false,

}: SliderNumberFieldProps) {

    return (

        <Box>

            <Typography
                variant="body2"
                sx={{ mb: 1 }}
            >
                {label}
            </Typography>

            <Box
                display="flex"
                alignItems="center"
                gap={2}
            >

                <Slider
                    value={value}
                    min={min}
                    max={max}
                    step={step}
                    disabled={disabled}
                    valueLabelDisplay="auto"
                    sx={{ flex: 1 }}
                    onChange={(_, newValue) =>
                        onChange(newValue as number)
                    }
                />

                <TextField
                    type="number"
                    size="small"
                    disabled={disabled}
                    value={value}
                    sx={{ width: 110 }}
                    slotProps={{
                        input: {
                            endAdornment: suffix ? (
                                <InputAdornment position="end">
                                    {suffix}
                                </InputAdornment>
                            ) : undefined,
                        },
                    }}
                    onChange={(e) =>
                        onChange(Number(e.target.value))
                    }
                />

            </Box>

        </Box>

    );
}