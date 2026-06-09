import apiClient from "@/api/client/apiClient";
import { ValidationResult } from "@/api/types/ValidationResult";
import { PdfDokumentTyp } from "@/api/enums/PdfDokumentTyp";

export const validateDokument = async (
  veranstaltungId: number,
  typ: PdfDokumentTyp,
): Promise<ValidationResult> => {
  const { data } = await apiClient.get<ValidationResult>(
    `/veranstaltungen/${veranstaltungId}/dokumente/${typ}/validation`,
  );

  return data;
};
