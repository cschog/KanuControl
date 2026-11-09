import VorlagenVerwaltung from "@/vermietung/components/verwaltung/VorlagenVerwaltung";
import EntschaedigungKatalogVerwaltung from "@/vermietung/components/verwaltung/EntschaedigungKatalogVerwaltung";
import AirbnbExportVerwaltung from "@/vermietung/components/verwaltung/AirbnbExportVerwaltung";
import SperrzeitenVerwaltung from "@/vermietung/components/verwaltung/SperrzeitenVerwaltung";
import { Box, Button, Typography, Tabs, Tab } from "@mui/material";
import { useSearchParams } from "react-router-dom";

import MietpreiseVerwaltung from "@/vermietung/components/verwaltung/MietpreiseVerwaltung";
import RabatteVerwaltung from "@/vermietung/components/verwaltung/RabatteVerwaltung";
import SortierungVerwaltung from "@/vermietung/components/verwaltung/SortierungVerwaltung";
import VerbrauchskostenVerwaltung from "@/vermietung/components/verwaltung/VerbrauchskostenVerwaltung";
import ZaehlerVerwaltung from "@/vermietung/components/verwaltung/ZaehlerVerwaltung";
import MietbereichAbhaengigkeitenVerwaltung from "@/vermietung/components/verwaltung/MietbereichAbhaengigkeitenVerwaltung";

import HomepageAnbindungVerwaltung from "@/vermietung/components/verwaltung/HomepageAnbindungVerwaltung";

const gruppen = [
  {
    id: "preise",
    label: "Preise & Kosten",
    bereiche: [
      { id: "mietpreise", label: "Mietpreise", component: MietpreiseVerwaltung },
      { id: "rabatte", label: "Rabatte", component: RabatteVerwaltung },
      { id: "verbrauchskosten", label: "Verbrauchskosten", component: VerbrauchskostenVerwaltung },
      { id: "entschaedigung", label: "Entschädigung", component: EntschaedigungKatalogVerwaltung },
    ],
  },
  {
    id: "mietobjekte",
    label: "Mietobjekte & Belegung",
    bereiche: [
      { id: "sortierung", label: "Anzeigereihenfolge", component: SortierungVerwaltung },
      { id: "abhaengigkeiten", label: "Mietbereichsregeln", component: MietbereichAbhaengigkeitenVerwaltung },
      { id: "zaehler", label: "Zählerverwaltung", component: ZaehlerVerwaltung },
      { id: "sperrzeiten", label: "Sperrzeiten", component: SperrzeitenVerwaltung },
    ],
  },
  {
    id: "vorlagen",
    label: "Vorlagen & Anbindungen",
    bereiche: [
      { id: "vorlagen", label: "Dokumentvorlagen", component: VorlagenVerwaltung },
      { id: "homepage", label: "Homepage-Anbindung", component: HomepageAnbindungVerwaltung },
      { id: "airbnb", label: "Airbnb-Export", component: AirbnbExportVerwaltung },
    ],
  },
];

const VerwaltungPage = () => {
  const [params, setParams] = useSearchParams();
  const bereichId = params.get("bereich");
  const gruppe = gruppen.find((item) => item.bereiche.some((bereich) => bereich.id === bereichId)) ?? gruppen[0];
  const bereich = gruppe.bereiche.find((item) => item.id === bereichId) ?? gruppe.bereiche[0];
  const Inhalt = bereich.component;

  const waehleBereich = (id: string) => {
    setParams((current) => {
      const next = new URLSearchParams(current);
      next.set("bereich", id);
      return next;
    });
  };

  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, minWidth: 0 }}>
      <Typography variant="h5" gutterBottom>
        Vermietung – Verwaltung
      </Typography>

      <Box
        role="group"
        aria-label="Verwaltungsgruppen"
        sx={{ display: "flex", flexWrap: "wrap", gap: 1, mb: 1.5 }}
      >
        {gruppen.map((item) => (
          <Button
            key={item.id}
            variant={item.id === gruppe.id ? "contained" : "outlined"}
            aria-pressed={item.id === gruppe.id}
            aria-controls="verwaltung-unterbereiche"
            onClick={() => {
              if (item.id !== gruppe.id) waehleBereich(item.bereiche[0].id);
            }}
            sx={{ textTransform: "none", flex: { xs: "1 1 100%", sm: "0 1 auto" } }}
          >
            {item.label}
          </Button>
        ))}
      </Box>

      <Tabs
        id="verwaltung-unterbereiche"
        aria-label={`Unterbereiche: ${gruppe.label}`}
        value={bereich.id}
        onChange={(_, value: string) => waehleBereich(value)}
        sx={{ mb: 2, borderBottom: 1, borderColor: "divider" }}
        variant="scrollable"
        scrollButtons="auto"
        allowScrollButtonsMobile
      >
        {gruppe.bereiche.map((item) => (
          <Tab
            key={item.id}
            id={`verwaltung-tab-${item.id}`}
            value={item.id}
            label={item.label}
            aria-controls={`verwaltung-inhalt-${item.id}`}
            sx={{ textTransform: "none" }}
          />
        ))}
      </Tabs>

      <Box
        key={bereich.id}
        role="tabpanel"
        id={`verwaltung-inhalt-${bereich.id}`}
        aria-labelledby={`verwaltung-tab-${bereich.id}`}
        tabIndex={0}
      >
        <Inhalt />
      </Box>
    </Box>
  );
};

export default VerwaltungPage;
