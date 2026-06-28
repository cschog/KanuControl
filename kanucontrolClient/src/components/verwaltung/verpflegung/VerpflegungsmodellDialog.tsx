// src/components/verwaltung/unterkunft/UnterkunftsartDialog.tsx

import { useEffect, useState } from "react";
import { Checkbox, FormControlLabel, Stack } from "@mui/material";

import GenericCrudDialog from "@/components/common/GenericCrudDialog";
import FormFeld from "@/components/common/FormFeld";
import MoneyField from "@/components/common/MoneyField";

import { VerpflegungsmodellDTO } from "@/api/types/verpflegung/VerpflegungsmodellDTO";
import { VerpflegungsmodellCreateUpdateDTO } from "@/api/types/verpflegung/VerpflegungsmodellCreateUpdateDTO";

interface VerpflegungsmodellDialogProps {
    open: boolean;
    verpflegungsmodell?: VerpflegungsmodellDTO | null;

    onClose: () => void;
    onSave: (dto: VerpflegungsmodellCreateUpdateDTO) => void;
}

const createEmptyDto = (): VerpflegungsmodellCreateUpdateDTO => ({
    bezeichnung: "",
    preisProPersonUndTag: 0,
    bemerkung: "",
    aktiv: true,
});

const VerpflegungsmodellDialog = ({
    open,
    verpflegungsmodell,
    onClose,
    onSave,
}: VerpflegungsmodellDialogProps) => {
    const [form, setForm] = useState<VerpflegungsmodellCreateUpdateDTO>(
        createEmptyDto(),
    );

    const updateField = <K extends keyof VerpflegungsmodellCreateUpdateDTO>(
        field: K,
        value: VerpflegungsmodellCreateUpdateDTO[K],
    ) => {
        setForm((prev) => ({
            ...prev,
            [field]: value,
        }));
    };

    useEffect(() => {
        if (!open) return;

        if (verpflegungsmodell) {
            setForm({
                bezeichnung: verpflegungsmodell.bezeichnung,
                preisProPersonUndTag: verpflegungsmodell.preisProPersonUndTag,
                bemerkung: verpflegungsmodell.bemerkung ?? "",
                aktiv: verpflegungsmodell.aktiv,
            });
        } else {
            setForm(createEmptyDto());
        }
    }, [open, verpflegungsmodell]);

    const handleSave = () => {
        onSave(form);
    };

    return (
        <GenericCrudDialog
            open={open}
            title={
                verpflegungsmodell
                    ? "Verpflegungsmodell bearbeiten"
                    : "Verpflegungsmodell anlegen"
            }
            onClose={onClose}
            onSave={handleSave}
            disableSave={
                !form.bezeichnung.trim() ||
                form.preisProPersonUndTag < 0
            }
        >
            <Stack spacing={2} sx={{ mt: 1 }}>
                <FormFeld
                    label="Bezeichnung"
                    value={form.bezeichnung}
                    onChange={(value) => updateField("bezeichnung", value)}
                    maxLength={100}
                    autoFocus
                />

                <MoneyField
                    label="Preis pro Person und Tag"
                    value={form.preisProPersonUndTag}
                    onChange={(value) =>
                        updateField("preisProPersonUndTag", Number(value))
                    }
                />

                <FormFeld
                    label="Bemerkung"
                    value={form.bemerkung}
                    onChange={(value) => updateField("bemerkung", value)}
                    multiline
                    rows={3}
                    maxLength={255}
                />

            </Stack>
        </GenericCrudDialog>
    );
};

export default VerpflegungsmodellDialog;