import {
    Divider,
    Grid,
    Paper,
    Stack,
    Typography,
} from "@mui/material";
import { ReactNode } from "react";

interface SimulationSectionProps {
    title: string;
    icon?: ReactNode;
    children: ReactNode;
}

export default function SimulationSection({
    title,
    icon,
    children,
}: SimulationSectionProps) {

    return (
        <Grid size={12}>
            <Paper
                variant="outlined"
                sx={{
                    p: 2,
                    borderRadius: 2,
                    bgcolor: "grey.100",
                    borderColor: "divider",
                }}
            >
                <Stack
                    direction="row"
                    spacing={1}
                    alignItems="center"
                    sx={{ mb: 1 }}
                >
                    {icon}

                    <Typography
                        variant="subtitle1"
                        fontWeight={600}
                    >
                        {title}
                    </Typography>
                </Stack>

                <Divider sx={{ mb: 2 }} />

                <Grid
                    container
                    spacing={2}
                >
                    {children}
                </Grid>

            </Paper>
        </Grid>
    );
}