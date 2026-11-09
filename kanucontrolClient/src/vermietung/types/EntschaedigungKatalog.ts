export interface EntschaedigungKatalog {
  id: number;
  version: number;
  bezeichnung: string;
  einzelpreis: number;
  sortierung: number;
  aktiv: boolean;
}
export type EntschaedigungKatalogSave = Omit<EntschaedigungKatalog, "id" | "version"> & {
  version: number | null;
};
