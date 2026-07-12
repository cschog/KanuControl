// src/components/simulation/SimulationField.tsx

import { Paper } from "@mui/material";
import { ReactNode } from "react";

interface SimulationFieldProps {
    children: ReactNode;
}

export default function SimulationField({
    children,
}: SimulationFieldProps) {

    return (
        <Paper
            variant="outlined"
            sx={{
                p: 2,
                height: "100%",
                borderRadius: 2,
                bgcolor: "grey.200",
                display: "flex",
                alignItems: "center",
            }}
        >
            {children}
        </Paper>
    );
}