export interface Mietbereich {
  id: number;
  mietobjektId?: number;

  bezeichnung: string;
  beschreibung?: string;

  mietbar: boolean;
}
