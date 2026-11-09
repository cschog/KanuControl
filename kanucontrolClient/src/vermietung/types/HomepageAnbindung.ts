export interface HomepageAnbindung {
  version: number | null;
  aktiv: boolean;
  technischerClientId: string | null;
  bezeichnung: string;
  homepageUrl: string | null;
  datenschutzUrl: string | null;
  hausregelnUrl: string | null;
  antwortTestEmail?: string | null;
  antwortCcEmail?: string | null;
  hausmeisterEmail?: string | null;
  empfaengerEmail: string | null;
  gastBestaetigung: boolean;
  verwaltungsBenachrichtigung: boolean;
  teilnehmerPflicht: boolean;
  erlaubteMietbereichIds: number[];
}
