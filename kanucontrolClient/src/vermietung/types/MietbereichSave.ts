export interface MietbereichSave {
  bezeichnung: string;
  beschreibung: string;

  mietbar: boolean;
  direktbuchungAktiv: boolean;
  airbnbAktiv: boolean;

  bestand: number;
  mengeneinheit: string;
}
