import apiClient from "@/api/client/apiClient";
import { PostalCodeLookupResponse } from "@/api/types/PostalCodeLookup";

/* =========================================================
   LOOKUP
   ========================================================= */

export async function lookupPostalCode(
  country: string,
  postalCode: string,
): Promise<PostalCodeLookupResponse | null> {
  try {
    const res = await apiClient.get<PostalCodeLookupResponse>("/postal-codes/lookup", {
      params: {
        country,
        postalCode,
      },
    });

    return res.data;
  } catch (err: unknown) {
    if (err instanceof Error) {
      console.error(err.message);
    } else {
      console.error("Unknown error", err);
    }

    return null;
  }
}

/* =========================================================
   SUGGEST
   ========================================================= */

export async function suggestPostalCodes(
  country: string,
  query: string,
): Promise<PostalCodeLookupResponse[]> {
  try {
    const res = await apiClient.get<PostalCodeLookupResponse[]>("/postal-codes/suggest", {
      params: {
        country,
        query,
      },
    });

    return res.data ?? [];
  } catch (err: unknown) {
    if (err instanceof Error) {
      console.error(err.message);
    } else {
      console.error("Unknown error", err);
    }

    return [];
  }
}
