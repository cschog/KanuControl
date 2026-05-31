import apiClient from "@/api/client/apiClient";

export interface PostalCodeStatus {
  countryCode: string;
  count: number;
  lastImport: string | null;
  source: string | null;
  importStatus: "IDLE" | "RUNNING" | "SUCCESS" | "FAILED";
}

export interface PostalCodeCountry {
  countryCode: string;
  enabled: boolean;
  autoImport: boolean;
  lastImport: string | null;
  nextImport: string | null;
}

export async function getPostalCodeStatus(countryCode: string): Promise<PostalCodeStatus> {
  const res = await apiClient.get<PostalCodeStatus>(`/admin/postal-codes/status/${countryCode}`);

  return res.data;
}

export async function importPostalCodes(countryCode: string): Promise<void> {
  await apiClient.post(`/admin/postal-codes/import/${countryCode}`);
}

export async function getPostalCodeCountries(): Promise<PostalCodeCountry[]> {
  const res = await apiClient.get<PostalCodeCountry[]>("/admin/postal-codes/countries");

  return res.data;
}

export async function updatePostalCodeCountry(
  countryCode: string,
  enabled: boolean,
  autoImport: boolean,
): Promise<void> {
  await apiClient.put(`/admin/postal-codes/countries/${countryCode}`, {
    enabled,
    autoImport,
  });
}
