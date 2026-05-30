import apiClient from "@/api/client/apiClient";

export interface PostalCodeStatus {
  countryCode: string;
  count: number;
  lastImport: string | null;
  source: string | null;
  importStatus: "IDLE" | "RUNNING" | "SUCCESS" | "FAILED";
}

export async function getPostalCodeStatus(countryCode: string): Promise<PostalCodeStatus> {
  const res = await apiClient.get<PostalCodeStatus>(`/admin/postal-codes/status/${countryCode}`);

  return res.data;
}

export async function importPostalCodes(countryCode: string): Promise<void> {
  await apiClient.post(`/admin/postal-codes/import/${countryCode}`);
}

export async function getPostalCodeCountries(): Promise<string[]> {
  const res = await apiClient.get<string[]>("/admin/postal-codes/countries");
  return res.data;
}
