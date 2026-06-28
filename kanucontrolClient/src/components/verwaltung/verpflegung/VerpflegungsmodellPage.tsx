// src/components/verwaltung/unterkunft/UnterkunftsartPage.tsx

import { useEffect, useState } from "react";
import { Alert, Box, Button, CircularProgress } from "@mui/material";

import CrudToolbar from "@/components/common/CrudToolbar";

import ConfirmDeleteDialog from "@/components/common/ConfirmDeleteDialog";
import VerpflegungsmodellTable from "@/components/verwaltung/verpflegung/VerpflegungsmodellTable";

import { UnterkunftsartDTO } from "@/api/types/unterkunft/UnterkunftsartDTO";
import { UnterkunftsartCreateUpdateDTO } from "@/api/types/unterkunft/UnterkunftsartCreateUpdateDTO";
import {
    getVerpflegungsmodelle,
    createVerpflegungsmodell,
    updateVerpflegungsmodell,
    deleteVerpflegungsmodell,
} from "@/api/services/verpflegungsmodellApi";
import VerpflegungsmodellDialog from "@/components/verwaltung/verpflegung/VerpflegungsmodellDialog";
import { VerpflegungsmodellDTO } from "@/api/types/verpflegung/VerpflegungsmodellDTO";
import { VerpflegungsmodellCreateUpdateDTO } from "@/api/types/verpflegung/VerpflegungsmodellCreateUpdateDTO";
import UnterkunftsartPage from "@/components/verwaltung/unterkunft/UnterkunftsartPage";

const VerpflegungsmodellPage = () => {
    const [data, setData] = useState<VerpflegungsmodellDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [dialogOpen, setDialogOpen] = useState(false);

    const [editing, setEditing] =
        useState<VerpflegungsmodellDTO | null>(null);

    const [selected, setSelected] =
        useState<VerpflegungsmodellDTO | null>(null);

    const [deleteOpen, setDeleteOpen] = useState(false);

    /* =========================================================
       LOAD
       ========================================================= */

    const load = async () => {
        try {
            setLoading(true);

            const result = await getVerpflegungsmodelle();

            setData(result);

            setError(null);
        } catch (err) {
            console.error(err);

            setError("Verpflegungsmodelle konnten nicht geladen werden.");
        } finally {
            setLoading(false);
        }
    };

    const handleAdd = () => {
        setEditing(null);
        setDialogOpen(true);
    };

    const handleEdit = () => {
        if (!selected) return;

        setEditing(selected);
        setDialogOpen(true);
    };

    const handleDelete = () => {
        if (!selected) {
            return;
        }

        setDeleteOpen(true);
    };

    const confirmDelete = async () => {
        if (!selected) {
            return;
        }

        try {
            await deleteVerpflegungsmodell(selected.id);

            setDeleteOpen(false);
            setSelected(null);

            await load();
        } catch (err) {
            console.error(err);
            setError("Verpflegungsmodell konnte nicht gelöscht werden.");
        }
    };

    const handleSave = async (
        dto: VerpflegungsmodellCreateUpdateDTO,
    ) => {
        try {
            if (editing) {
                await updateVerpflegungsmodell(editing.id, dto);
            } else {
                await createVerpflegungsmodell(dto);
            }

            setDialogOpen(false);
            setEditing(null);

            await load();
        } catch (err) {
            console.error(err);
            setError("Verpflegungsmodell konnte nicht gespeichert werden.");
        }
    };

    useEffect(() => {
        load();
    }, []);



    /* =========================================================
       RENDER
       ========================================================= */

    if (loading) {
        return (
            <Box sx={{ p: 3 }}>
                <CircularProgress />
            </Box>
        );
    }

    if (error) {
        return <Alert severity="error">{error}</Alert>;
    }

    return (
        <>
            <CrudToolbar
                title="Verpflegungsmodelle"
                onAdd={handleAdd}
            />

            <VerpflegungsmodellTable
                data={data}
                loading={loading}
                onEdit={(row) => {
                    setEditing(row);
                    setDialogOpen(true);
                }}
                onDelete={(row) => {
                    setSelected(row);
                    setDeleteOpen(true);
                }}
            />

            <VerpflegungsmodellDialog
                open={dialogOpen}
                verpflegungsmodell={editing}
                onClose={() => {
                    setDialogOpen(false);
                    setEditing(null);
                }}
                onSave={handleSave}
            />
            <ConfirmDeleteDialog
                open={deleteOpen}
                title="Unterkunftsart löschen"
                description={
                    <>
                        Möchten Sie die Unterkunftsart{" "}
                        <strong>"{selected?.bezeichnung}"</strong> wirklich löschen?
                        <br />
                        <br />
                        Dieser Vorgang kann nicht rückgängig gemacht werden.
                    </>
                }
                onClose={() => setDeleteOpen(false)}
                onConfirm={confirmDelete}
            />
        </>
    );
};

export default VerpflegungsmodellPage;