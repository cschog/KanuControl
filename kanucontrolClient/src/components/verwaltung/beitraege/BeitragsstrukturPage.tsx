import {
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Stack,
  Typography,
  TextField,
  Chip,
  MenuItem,
  Grid,
} from "@mui/material";
import { ColumnDef } from "@tanstack/react-table";
import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";

import Money from "@/components/common/Money";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { useEffect, useState } from "react";
import apiClient from "@/api/client/apiClient";
import { TEILNEHMER_ROLLEN, TeilnehmerRolle } from "@/api/enums/TeilnehmerRolle";
import { getApiErrorMessage } from "@/api/utils/apiError";

/* =========================================================
   TYPES
   ========================================================= */

interface BeitragsregelDTO {
  id: number;
  alterVon?: number;
  alterBis?: number;
  rolle: TeilnehmerRolle;
  beitrag: number;
}

interface BeitragsstrukturDTO {
  id: number;
  name: string;
  template: boolean;
  regeln: BeitragsregelDTO[];
}

/* =========================================================
   COMPONENT
   ========================================================= */

const BeitragsstrukturTable = () => {
  const [data, setData] = useState<BeitragsstrukturDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedId, setExpandedId] = useState<number | null>(null);
  const [newName, setNewName] = useState("");
  const [form, setForm] = useState({
    alterBis: "",
    rolle: "" as TeilnehmerRolle | "",
    beitrag: "",
  });

  /* =========================================================
     LOAD
     ========================================================= */

  const load = async () => {
    try {
      setLoading(true);

      const res = await apiClient.get<BeitragsstrukturDTO[]>("/beitragsstrukturen");

      setData(res.data);
    } catch (err: unknown) {
      console.error("Fehler beim Laden der Beitragsstrukturen", err);
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const [editName, setEditName] = useState<Record<number, string>>({});

  const [editOpen, setEditOpen] = useState(false);

  const [editingRegel, setEditingRegel] = useState<BeitragsregelDTO | null>(null);

  useEffect(() => {
    load();
  }, []);

  /* =========================================================
     CREATE
     ========================================================= */
  const handleCreate = async () => {
    try {
      if (!newName.trim()) {
        return;
      }

      await apiClient.post("/beitragsstrukturen/create", null, {
        params: {
          name: newName,
        },
      });

      setNewName("");

      load();
    } catch (err: unknown) {
      console.error("Fehler beim Erstellen der Beitragsstruktur", err);
      setError(getApiErrorMessage(err));
    }
  };

 const handleRename = async (id: number) => {
   try {
     await apiClient.put(`/beitragsstrukturen/${id}`, {
       name: editName[id],
     });

     load();
   } catch (err: unknown) {
     console.error("Fehler beim Umbenennen der Beitragsstruktur", err);
     setError(getApiErrorMessage(err));
   }
 };

  const handleDelete = async (id: number) => {
    if (!window.confirm("Struktur wirklich löschen?")) {
      return;
    }

    try {
      await apiClient.delete(`/beitragsstrukturen/${id}`);

      load();
    } catch (err: unknown) {
      console.error("Fehler beim Löschen der Beitragsstruktur", err);
      setError(getApiErrorMessage(err));
    }
  };

  const handleChange = (field: string, value: string) => {
    setForm((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleAddRegel = async (strukturId: number, regeln: BeitragsregelDTO[]) => {
    try {
      const letzte = regeln[regeln.length - 1];
      const alterVon = regeln.length === 0 ? 0 : (letzte.alterBis ?? 0) + 1;

      await apiClient.post(`/beitragsstrukturen/${strukturId}/regeln`, {
        alterVon,

        alterBis: form.alterBis ? Number(form.alterBis) : null,

        rolle: form.rolle || null,

        beitrag: Number(form.beitrag),
      });

      // Reset
      setForm({
        alterBis: "",
        rolle: "",
        beitrag: "",
      });

      load();
    } catch (err: unknown) {
      console.error("Fehler beim Hinzufügen der Beitragsregel", err);
      setError(getApiErrorMessage(err));
    }
  };

  const handleDeleteRegel = async (regelId: number) => {
    if (!window.confirm("Regel wirklich löschen?")) {
      return;
    }

    try {
      await apiClient.delete(`/beitragsstrukturen/regeln/${regelId}`);

      load();
    } catch (err: unknown) {
      console.error("Fehler beim Löschen der Beitragsregel", err);
      setError(getApiErrorMessage(err));
    }
  };

  const openEditDialog = (regel: BeitragsregelDTO) => {
    setEditingRegel({ ...regel });

    setEditOpen(true);
  };

 const handleSaveRegel = async () => {
   if (!editingRegel?.id) {
     setError("Regel-ID fehlt.");
     return;
   }

   try {
     await apiClient.put(`/beitragsstrukturen/regeln/${editingRegel.id}`, editingRegel);

     setEditOpen(false);
     setEditingRegel(null);

     load();
   } catch (err: unknown) {
     console.error("Fehler beim Speichern der Beitragsregel", err);
     setError(getApiErrorMessage(err));
   }
 };

  const handleCopy = async (id: number, name: string) => {
    const neuerName = window.prompt("Name der Kopie:", `${name} Kopie`);

    if (!neuerName?.trim()) {
      return;
    }

    try {
      await apiClient.post(`/beitragsstrukturen/${id}/copy`, null, {
        params: {
          name: neuerName,
        },
      });

      load();
    } catch (err: unknown) {
      console.error("Fehler beim Kopieren der Beitragsstruktur", err);
      setError(getApiErrorMessage(err));
    }
  };

  /* =========================================================
     HELPER
     ========================================================= */

  const formatAlter = (r: BeitragsregelDTO) => {
    // Rollenregeln haben kein Alter
    if (r.rolle === "L" || r.rolle === "M") {
      return "—";
    }

    const von = r.alterVon ?? 0;
    const bis = r.alterBis ?? "∞";

    return `${von} - ${bis}`;
  };

  const getRolleLabel = (rolle: TeilnehmerRolle | null) => {
    if (!rolle) return "Teilnehmer";

    return TEILNEHMER_ROLLEN.find((r) => r.code === rolle)?.label ?? rolle;
  };

  const getRolleColor = (rolle: TeilnehmerRolle | null) => {
    if (rolle === "L") return "secondary";
    if (rolle === "M") return "default";
    return "primary";
  };

  const sortierteRegeln = (regeln: BeitragsregelDTO[]) => {
    const rollenRank = (rolle: TeilnehmerRolle | null) => {
      switch (rolle) {
        case "M":
          return 1;

        case "L":
          return 2;

        default:
          return 0;
      }
    };

    return [...regeln].sort((a, b) => {
      // zuerst Rolle
      const rolleCompare = rollenRank(a.rolle) - rollenRank(b.rolle);

      if (rolleCompare !== 0) {
        return rolleCompare;
      }

      // dann Alter
      return (a.alterVon ?? 0) - (b.alterVon ?? 0);
    });
  };

  const regelColumns: ColumnDef<BeitragsregelDTO>[] = [
    {
      id: "alter",

      header: "Alter",

      cell: ({ row }) => formatAlter(row.original),
    },

    {
      accessorKey: "rolle",

      header: "Rolle",

      cell: ({ row }) => (
        <Chip
          size="small"
          label={getRolleLabel(row.original.rolle)}
          color={getRolleColor(row.original.rolle)}
        />
      ),
    },

    {
      accessorKey: "beitrag",

      header: "Beitrag",

      meta: {
        align: "right",
      },

      cell: ({ row }) => (
        <Box
          sx={{
            display: "flex",

            justifyContent: "flex-end",

            width: "100%",
          }}
        >
          <Money value={row.original.beitrag} />
        </Box>
      ),
    },

    {
      id: "actions",

      header: "",

      size: 120,

      enableSorting: false,

      cell: ({ row }) => (
        <Stack direction="row" spacing={0.5} justifyContent="flex-end">
          <IconButton size="small" onClick={() => openEditDialog(row.original)}>
            <EditIcon fontSize="small" />
          </IconButton>

          <IconButton size="small" color="error" onClick={() => handleDeleteRegel(row.original.id)}>
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Stack>
      ),
    },
  ];

  /* =========================================================
     RENDER
     ========================================================= */

  if (loading) {
    return (
      <Box sx={{ p: 2 }}>
        <CircularProgress />
      </Box>
    );
  }

 return (
   <Stack spacing={2}>
     {error && <Alert severity="error">{error}</Alert>}
     {/* HEADER */}
     <Card>
       <CardContent>
         <Stack spacing={2}>
           <Typography variant="h6">Beitragsstrukturen</Typography>

           <TextField
             label="Name für neue Struktur"
             size="small"
             value={newName}
             onChange={(e) => setNewName(e.target.value)}
           />
           <Button variant="contained" onClick={handleCreate} disabled={!newName.trim()}>
             Neue Struktur
           </Button>
         </Stack>
       </CardContent>
     </Card>

     {/* LISTE */}
     {data.map((s) => (
       <Accordion
         key={s.id}
         expanded={expandedId === s.id}
         onChange={() => setExpandedId(expandedId === s.id ? null : s.id)}
       >
         {/* HEADER */}
         <AccordionSummary expandIcon={<ExpandMoreIcon />}>
           <Stack
             direction="row"
             alignItems="flex-start"
             justifyContent="space-between"
             spacing={2}
             sx={{
               width: "100%",
             }}
           >
             <TextField
               size="small"
               value={editName[s.id] ?? s.name}
               sx={{
                 "& input": {
                   fontWeight: "bold",
                   fontSize: {
                     xs: "1.rem",
                     md: "1.2rem",
                   },
                 },
               }}
               onChange={(e) =>
                 setEditName((prev) => ({
                   ...prev,
                   [s.id]: e.target.value,
                 }))
               }
             />

             <Stack
               direction={{
                 xs: "column",
                 sm: "row",
               }}
               spacing={1}
               alignItems="stretch"
               sx={{
                 minWidth: {
                   xs: 110,
                   sm: "auto",
                 },
               }}
             >
               <Typography
                 variant="body2"
                 color="text.secondary"
                 sx={{
                   display: "flex",
                   alignItems: "center",

                   fontSize: {
                     xs: "0.95rem",
                     md: "1rem",
                   },
                 }}
               >
                 {s.regeln.length} Regeln
               </Typography>

               <Button
                 variant="outlined"
                 onClick={() => handleCopy(s.id, s.name)}
                 sx={{
                   whiteSpace: "nowrap",
                 }}
               >
                 Kopieren
               </Button>

               <Button
                 variant="outlined"
                 onClick={() => handleRename(s.id)}
                 sx={{
                   whiteSpace: "nowrap",
                 }}
               >
                 Speichern
               </Button>

               <Button
                 color="error"
                 variant="outlined"
                 onClick={() => handleDelete(s.id)}
                 sx={{
                   whiteSpace: "nowrap",
                 }}
               >
                 Löschen
               </Button>
             </Stack>
           </Stack>
         </AccordionSummary>

         {/* DETAILS */}
         <AccordionDetails>
           <Stack spacing={2}>
             {/* REGELN */}
             <GenericTableTanstack<BeitragsregelDTO>
               data={sortierteRegeln(s.regeln)}
               columns={regelColumns}
               loading={false}
               height={340}
               mobileRenderRow={(row) => (
                 <Box>
                   <Box
                     sx={{
                       display: "flex",

                       justifyContent: "space-between",

                       alignItems: "center",

                       gap: 1,
                     }}
                   >
                     <Chip
                       size="small"
                       label={getRolleLabel(row.rolle)}
                       color={getRolleColor(row.rolle)}
                     />

                     <Typography
                       sx={{
                         whiteSpace: "nowrap",
                       }}
                     >
                       <Money value={row.beitrag} />
                     </Typography>
                   </Box>

                   <Typography
                     variant="body2"
                     color="text.secondary"
                     sx={{
                       mt: 0.5,
                     }}
                   >
                     Alter: {formatAlter(row)}
                   </Typography>

                   <Stack
                     direction="row"
                     spacing={1}
                     sx={{
                       mt: 1,
                     }}
                   >
                     <Button size="small" variant="outlined" onClick={() => openEditDialog(row)}>
                       Bearbeiten
                     </Button>

                     <Button
                       size="small"
                       variant="outlined"
                       color="error"
                       onClick={() => handleDeleteRegel(row.id)}
                     >
                       Löschen
                     </Button>
                   </Stack>
                 </Box>
               )}
             />

             {/* NEUE REGEL */}
             <Card variant="outlined" sx={{ p: 2 }}>
               <Grid container spacing={1}>
                 <Grid size={3}>
                   <TextField
                     label="Alter von"
                     size="small"
                     fullWidth
                     disabled
                     value={
                       form.rolle === "L" || form.rolle === "M"
                         ? "—"
                         : s.regeln.filter((r) => r.rolle === null).length === 0
                           ? 0
                           : (s.regeln
                               .filter((r) => r.rolle === null)
                               .sort((a, b) => (a.alterVon ?? 0) - (b.alterVon ?? 0))
                               .at(-1)?.alterBis ?? 0) + 1
                     }
                   />
                 </Grid>

                 <Grid size={3}>
                   <TextField
                     label="Alter bis"
                     size="small"
                     type="number"
                     fullWidth
                     value={form.alterBis}
                     onChange={(e) => handleChange("alterBis", e.target.value)}
                   />
                 </Grid>

                 <Grid size={3}>
                   <TextField
                     select
                     label="Rolle"
                     size="small"
                     fullWidth
                     value={form.rolle}
                     onChange={(e) => handleChange("rolle", e.target.value)}
                   >
                     <MenuItem value="">Alle</MenuItem>

                     {TEILNEHMER_ROLLEN.map((r) => (
                       <MenuItem key={r.code} value={r.code}>
                         {r.label}
                       </MenuItem>
                     ))}
                   </TextField>
                 </Grid>

                 <Grid size={3}>
                   <TextField
                     label="Beitrag (€)"
                     size="small"
                     type="number"
                     fullWidth
                     value={form.beitrag}
                     onChange={(e) => handleChange("beitrag", e.target.value)}
                   />
                 </Grid>

                 <Grid size={12}>
                   <Button
                     variant="contained"
                     onClick={() => handleAddRegel(s.id, s.regeln)}
                     disabled={!form.beitrag}
                   >
                     Regel hinzufügen
                   </Button>
                 </Grid>
               </Grid>
             </Card>

             <Dialog open={editOpen} onClose={() => setEditOpen(false)} maxWidth="sm" fullWidth>
               <DialogTitle>Beitragsregel bearbeiten</DialogTitle>

               <DialogContent>
                 <Stack spacing={2} sx={{ mt: 1 }}>
                   <TextField label="Alter von" value={editingRegel?.alterVon ?? ""} disabled />

                   <TextField
                     label="Alter bis"
                     type="number"
                     value={editingRegel?.alterBis ?? ""}
                     onChange={(e) =>
                       setEditingRegel((prev) =>
                         prev
                           ? {
                               ...prev,
                               alterBis: Number(e.target.value),
                             }
                           : prev,
                       )
                     }
                   />

                   <TextField
                     select
                     label="Rolle"
                     value={editingRegel?.rolle ?? ""}
                     onChange={(e) =>
                       setEditingRegel((prev) =>
                         prev
                           ? {
                               ...prev,
                               rolle: e.target.value as TeilnehmerRolle,
                             }
                           : prev,
                       )
                     }
                   >
                     <MenuItem value="">Teilnehmer</MenuItem>

                     {TEILNEHMER_ROLLEN.map((r) => (
                       <MenuItem key={r.code} value={r.code}>
                         {r.label}
                       </MenuItem>
                     ))}
                   </TextField>

                   <TextField
                     label="Beitrag"
                     type="number"
                     value={editingRegel?.beitrag ?? ""}
                     onChange={(e) =>
                       setEditingRegel((prev) =>
                         prev
                           ? {
                               ...prev,
                               beitrag: Number(e.target.value),
                             }
                           : prev,
                       )
                     }
                   />
                 </Stack>
               </DialogContent>

               <DialogActions>
                 <Button onClick={() => setEditOpen(false)}>Abbrechen</Button>

                 <Button variant="contained" onClick={handleSaveRegel}>
                   Speichern
                 </Button>
               </DialogActions>
             </Dialog>
           </Stack>
         </AccordionDetails>
       </Accordion>
     ))}
   </Stack>
 );
};

export default BeitragsstrukturTable;
