import api from "@/api/client/apiClient";

export interface VersionDTO {
  version: string;
}

export async function getBackendVersion(): Promise<string> {
  const response = await api.get<VersionDTO>("/version");

  return response.data.version;
}
