// src/api/services/planungApi.ts
import apiClient from "@/api/client/apiClient";
import { PlanungDetail } from "@/api/types/planung";

export async function getPlanung(
    veranstaltungId: number
): Promise<PlanungDetail> {

    const { data } = await apiClient.get(
        `/veranstaltungen/${veranstaltungId}/planung`
    );

    return data;
}

export async function einreichen(
    veranstaltungId: number
): Promise<void> {

    await apiClient.post(
        `/veranstaltungen/${veranstaltungId}/planung/einreichen`
    );
}

export async function wiederOeffnen(
    veranstaltungId: number
): Promise<void> {

    await apiClient.post(
        `/veranstaltungen/${veranstaltungId}/planung/wieder-oeffnen`
    );
}