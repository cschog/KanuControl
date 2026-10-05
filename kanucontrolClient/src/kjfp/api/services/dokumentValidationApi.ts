import apiClient from "@/core/api/client/apiClient";
import { ValidationResult } from "@/kjfp/types/ValidationResult";
import { PdfDokumentTyp } from "@/kjfp/api/enums/PdfDokumentTyp";

export const validateDokument = async (
  veranstaltungId: number,
  typ: PdfDokumentTyp,
): Promise<ValidationResult> => {
  const { data } = await apiClient.get<ValidationResult>(
    `/veranstaltungen/${veranstaltungId}/dokumente/${typ}/validation`,
  );

  return data;
};
