// src/components/verwaltung/unterkunft/UnterkunftsartPage.tsx

import { useEffect, useState } from "react";
import { Alert, Box, Button, CircularProgress } from "@mui/material";

import CrudToolbar from "@/components/common/CrudToolbar";

import ConfirmDeleteDialog from "@/components/common/ConfirmDeleteDialog";
import UnterkunftsartTable from "./UnterkunftsartTable";

import { UnterkunftsartDTO } from "@/api/types/unterkunft/UnterkunftsartDTO";
import { UnterkunftsartCreateUpdateDTO } from "@/api/types/unterkunft/UnterkunftsartCreateUpdateDTO";
import {
    getUnterkunftsarten,
    createUnterkunftsart,
    updateUnterkunftsart,
    deleteUnterkunftsart,
} from "@/api/services/unterkunftsartApi";
import UnterkunftsartDialog from "@/components/verwaltung/unterkunft/UnterkunftsartDialog";

const UnterkunftsartPage = () => {
    const [data, setData] = useState<UnterkunftsartDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [dialogOpen, setDialogOpen] = useState(false);

    const [editing, setEditing] =
        useState<UnterkunftsartDTO | null>(null);

    const [selected, setSelected] =
        useState<UnterkunftsartDTO | null>(null);

    const [deleteOpen, setDeleteOpen] = useState(false);

    /* =========================================================
       LOAD
       ========================================================= */

    const load = async () => {
        try {
            setLoading(true);

            const result = await getUnterkunftsarten();

            setData(result);

            setError(null);
        } catch (err) {
            console.error(err);

            setError("Unterkunftsarten konnten nicht geladen werden.");
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
            await deleteUnterkunftsart(selected.id);

            setDeleteOpen(false);
            setSelected(null);

            await load();
        } catch (err) {
            console.error(err);
            setError("Unterkunftsart konnte nicht gelöscht werden.");
        }
    };

    const handleSave = async (
        dto: UnterkunftsartCreateUpdateDTO,
    ) => {
        try {
            if (editing) {
                await updateUnterkunftsart(editing.id, dto);
            } else {
                await createUnterkunftsart(dto);
            }

            setDialogOpen(false);
            setEditing(null);

            await load();
        } catch (err) {
            console.error(err);
            setError("Unterkunftsart konnte nicht gespeichert werden.");
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
                title="Unterkunftsarten"
                onAdd={handleAdd}
            />

            <UnterkunftsartTable
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

            <UnterkunftsartDialog
                open={dialogOpen}
                unterkunftsart={editing}
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

export default UnterkunftsartPage;