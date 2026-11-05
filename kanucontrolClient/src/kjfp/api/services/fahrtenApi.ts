import apiClient from "@/core/api/client/apiClient";

export type KanuTyp = "CANADIER" | "KAJAK";
export interface KanuInfo { id: number; kennzeichnung: string; bezeichnung: string; typ: KanuTyp; platzanzahl: number; aktiv: boolean; privat: boolean; veranstaltungId?: number; eigentuemerId?: number; eigentuemer?: string }
export interface GewaesserInfo { id: number; name: string }
export interface CrewInfo { id: number; teilnehmerId: number; personId: number; vorname: string; name: string }
export interface BootInfo { fahrtKanuId: number; kanu: KanuInfo; lizenzErforderlich: boolean; lizenzVorhanden: boolean; besatzung: CrewInfo[] }
export interface FahrtInfo { id: number; veranstaltungId: number; datum: string; beginnZeit?: string; endeZeit?: string; gewaesser: GewaesserInfo; startort?: string; zielort?: string; kilometer: number; streckenangabe?: string; bemerkung?: string; kanus: BootInfo[] }
export interface KilometerInfo { personId: number; vorname: string; name: string; gewaesser: string; kilometer: number }
export interface LizenzInfo { id: number; kanuId: number; lizenznummer: string; gueltigVon: string; gueltigBis: string; kosten?: number }

export const getKanus = async () => (await apiClient.get<KanuInfo[]>("/kanus")).data;
export const getPrivateKanus = async (eventId: number) => (await apiClient.get<KanuInfo[]>(`/veranstaltungen/${eventId}/kanus`)).data;
export const saveKanu = async (data: Partial<KanuInfo> & { kennzeichnung: string; bezeichnung: string; typ: KanuTyp; platzanzahl: number; aktiv: boolean; eigentuemerId?: number }, eventId?: number) => eventId
  ? (await apiClient.post<KanuInfo>(`/veranstaltungen/${eventId}/kanus`, data)).data
  : (await apiClient.post<KanuInfo>("/kanus", data)).data;
export const updateKanu = async (id: number, data: Partial<KanuInfo> & { kennzeichnung: string; bezeichnung: string; typ: KanuTyp; platzanzahl: number; aktiv: boolean; eigentuemerId?: number }) => (await apiClient.put<KanuInfo>(`/kanus/${id}`, data)).data;
export const getGewaesser = async () => (await apiClient.get<GewaesserInfo[]>("/gewaesser")).data;
export const createGewaesser = async (name: string) => (await apiClient.post<GewaesserInfo>("/gewaesser", { name })).data;
export const getFahrten = async (eventId: number) => (await apiClient.get<FahrtInfo[]>(`/veranstaltungen/${eventId}/fahrten`)).data;
export const createFahrt = async (eventId: number, data: Omit<FahrtInfo, "id" | "veranstaltungId" | "kanus" | "gewaesser"> & { gewaesserId: number }) => (await apiClient.post<FahrtInfo>(`/veranstaltungen/${eventId}/fahrten`, data)).data;
export const addBoot = async (eventId: number, fahrtId: number, kanuId: number, lizenzErforderlich: boolean) => (await apiClient.post<BootInfo>(`/veranstaltungen/${eventId}/fahrten/${fahrtId}/kanus`, { kanuId, lizenzErforderlich })).data;
export const addCrew = async (eventId: number, fahrtId: number, fahrtKanuId: number, teilnehmerId: number) => (await apiClient.post<FahrtInfo>(`/veranstaltungen/${eventId}/fahrten/${fahrtId}/kanus/${fahrtKanuId}/besatzung`, { teilnehmerId })).data;
export const removeCrew = async (eventId: number, fahrtId: number, crewId: number) => (await apiClient.delete(`/veranstaltungen/${eventId}/fahrten/${fahrtId}/besatzung/${crewId}`)).data;
export const getKilometer = async (eventId: number, von?: string, bis?: string) => (await apiClient.get<KilometerInfo[]>(`/veranstaltungen/${eventId}/kilometer`, { params: { von, bis } })).data;
export const getLizenzen = async (kanuId: number) => (await apiClient.get<LizenzInfo[]>(`/kanus/${kanuId}/lizenzen`)).data;
export const createLizenz = async (kanuId: number, data: Omit<LizenzInfo, "id" | "kanuId">) => (await apiClient.post<LizenzInfo>(`/kanus/${kanuId}/lizenzen`, data)).data;
