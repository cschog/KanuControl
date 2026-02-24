import apiClient from "@/api/client/apiClient";

export async function importCsv(
  vereinId: number,
  csvFile: File,
  mappingFile: File | null,
  dryRun: boolean,
) {
  const form = new FormData();
  form.append("csv", csvFile);

  if (mappingFile) {
    form.append("mapping", mappingFile);
  }

  form.append("dryRun", String(dryRun));

  const res = await apiClient.post(`/csv-import/verein/${vereinId}`, form, {
    headers: { "Content-Type": "multipart/form-data" },
  });

  return res.data;
}
