//src/api/utils/apiError.ts
import axios from "axios";
import type { ApiError } from "@/api/types/ApiError";


export function getApiErrorMessage(
  error: unknown,
  fallback = "Ein Fehler ist aufgetreten.",
): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as ApiError | undefined;

    if (data?.message) {
      return data.message;
    }

    return fallback;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return fallback;
}
