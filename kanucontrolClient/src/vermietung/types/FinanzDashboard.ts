export interface FinanzDashboard {
  jahr: number; stand: string; umsatzgrenze: number; umsatz: number; vorjahresumsatz: number;
  verbleibend: number; geplantOffen: number; buchungsprognose: number;
  trendprognose: number | null; prognose: number; unbefristeteBuchungen: number;
  monate: { monat: number; umsatz: number; kumuliert: number; vorjahrKumuliert: number }[];
  buchungen: { id: number; nummer: string | null; mieter: string; mietobjekt: string; quelle: string; status: string; umsatz: number; geplantOffen: number }[];
  quellen: { quelle: string; umsatz: number }[];
}
