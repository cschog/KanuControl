export interface Mietbereich {
  id: number;
  mietobjektId: number;

  bezeichnung: string;
  beschreibung?: string;

  mietbar: boolean;
  direktbuchungAktiv: boolean;
  airbnbAktiv: boolean;

  bestand: number;
  mengeneinheit?: string;
}
