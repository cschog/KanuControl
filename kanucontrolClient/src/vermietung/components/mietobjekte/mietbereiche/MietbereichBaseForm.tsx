import { useEffect, useState } from "react";
import { Alert, Checkbox, ListItemText, MenuItem, TextField } from "@mui/material";

import { getZaehler } from "@/vermietung/api/zaehlerApi";
import type { Zaehler } from "@/vermietung/api/zaehlerApi";

import FormFeld from "@/core/components/common/FormFeld";

import type { MietbereichSave } from "@/vermietung/types/MietbereichSave";

import { Berechnungsart, BERECHNUNGSART_LABELS } from "@/vermietung/enums/Berechnungsart";

interface Props {
  form: MietbereichSave;
  editMode: boolean;
  mietobjektId: number;

  onChange: <K extends keyof MietbereichSave>(key: K, value: MietbereichSave[K]) => void;
}

export const MietbereichBaseForm: React.FC<Props> = ({
  form,
  editMode,
  mietobjektId,
  onChange,
}) => {
  const [zaehler, setZaehler] = useState<Zaehler[]>([]);
  const [zaehlerFehler, setZaehlerFehler] = useState<string | null>(null);

  useEffect(() => {
    let aktiv = true;

    const laden = async () => {
      try {
        setZaehlerFehler(null);

        const daten = await getZaehler(mietobjektId);

        if (aktiv) {
          setZaehler(daten);
        }
      } catch (error) {
        console.error("Fehler beim Laden der Zähler:", error);

        if (aktiv) {
          setZaehlerFehler("Die Zähler konnten nicht geladen werden.");
        }
      }
    };

    void laden();

    return () => {
      aktiv = false;
    };
  }, [mietobjektId]);

  const handleAirbnbAktivChange = (aktiv: boolean) => {
    onChange("airbnbAktiv", aktiv);

    if (!aktiv) {
      onChange("airbnbInserat", false);
    }
  };

  // Der bisherige return-Block folgt hier.

  return (
    <>
      <FormFeld
        label="Bezeichnung"
        value={form.bezeichnung}
        disabled={!editMode}
        onChange={(v) => onChange("bezeichnung", v)}
      />

      <FormFeld
        label="Beschreibung"
        value={form.beschreibung}
        disabled={!editMode}
        onChange={(v) => onChange("beschreibung", v)}
      />

      <TextField
        select
        fullWidth
        size="small"
        label="Mietbar"
        value={form.mietbar ? "ja" : "nein"}
        disabled={!editMode}
        onChange={(e) => onChange("mietbar", e.target.value === "ja")}
      >
        <MenuItem value="ja">Ja</MenuItem>
        <MenuItem value="nein">Nein</MenuItem>
      </TextField>

      <TextField
        fullWidth
        size="small"
        type="number"
        label="Bestand"
        value={form.bestand}
        disabled={!editMode}
        slotProps={{
          htmlInput: { min: 1, step: 1 },
        }}
        onChange={(e) => onChange("bestand", Number(e.target.value))}
        helperText="Anzahl verfügbarer Einheiten"
      />

      <FormFeld
        label="Mengeneinheit"
        value={form.mengeneinheit}
        disabled={!editMode}
        onChange={(v) => onChange("mengeneinheit", v)}
      />

      <TextField
        select
        fullWidth
        size="small"
        label="Berechnungsart"
        value={form.berechnungsart}
        disabled={!editMode}
        onChange={(e) => onChange("berechnungsart", e.target.value as Berechnungsart)}
        helperText="Legt fest, wie der Mietpreis berechnet wird."
      >
        {Object.values(Berechnungsart).map((art) => (
          <MenuItem key={art} value={art}>
            {BERECHNUNGSART_LABELS[art]}
          </MenuItem>
        ))}
      </TextField>

      <TextField
        select
        fullWidth
        size="small"
        label="Zähler"
        value={form.zaehlerIds}
        disabled={!editMode}
        slotProps={{
          select: {
            multiple: true,
            renderValue: (selected) => {
              const ids = selected as number[];

              return zaehler
                .filter((item) => ids.includes(item.id))
                .map((item) => item.bezeichnung)
                .join(", ");
            },
          },
        }}
        onChange={(e) => {
          const value = e.target.value;

          const ids =
            typeof value === "string" ? value.split(",").map(Number) : (value as number[]);

          onChange("zaehlerIds", ids);
        }}
        helperText="Alle für diesen Mietbereich benötigten Zähler auswählen."
      >
        {zaehler
          .filter((item) => item.aktiv || form.zaehlerIds.includes(item.id))
          .map((item) => (
            <MenuItem key={item.id} value={item.id}>
              <Checkbox checked={form.zaehlerIds.includes(item.id)} />
              <ListItemText
                primary={item.bezeichnung}
                secondary={`${item.verbrauchsart}${
                  item.zaehlernummer ? ` · Nr. ${item.zaehlernummer}` : ""
                }`}
              />
            </MenuItem>
          ))}
      </TextField>

      {zaehlerFehler && <Alert severity="error">{zaehlerFehler}</Alert>}

      {zaehlerFehler && <Alert severity="error">{zaehlerFehler}</Alert>}

      <TextField
        select
        fullWidth
        size="small"
        label="Direktbuchung aktiv"
        value={form.direktbuchungAktiv ? "ja" : "nein"}
        disabled={!editMode}
        onChange={(e) => onChange("direktbuchungAktiv", e.target.value === "ja")}
      >
        <MenuItem value="ja">Ja</MenuItem>
        <MenuItem value="nein">Nein</MenuItem>
      </TextField>

      <TextField
        select
        fullWidth
        size="small"
        label="Airbnb aktiv"
        value={form.airbnbAktiv ? "ja" : "nein"}
        disabled={!editMode}
        onChange={(e) => handleAirbnbAktivChange(e.target.value === "ja")}
      >
        <MenuItem value="ja">Ja</MenuItem>
        <MenuItem value="nein">Nein</MenuItem>
      </TextField>

      <TextField
        select
        fullWidth
        size="small"
        label="Airbnb-Inserat"
        value={form.airbnbInserat ? "ja" : "nein"}
        disabled={!editMode || !form.airbnbAktiv}
        onChange={(e) => onChange("airbnbInserat", e.target.value === "ja")}
        helperText="Kennzeichnet den Mietbereich, der dem Airbnb-Inserat entspricht."
      >
        <MenuItem value="ja">Ja</MenuItem>
        <MenuItem value="nein">Nein</MenuItem>
      </TextField>
    </>
  );
};
