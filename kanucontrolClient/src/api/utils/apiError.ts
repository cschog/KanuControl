import axios from "axios";

export function getApiErrorMessage(
  error: unknown,
  fallback = "Ein Fehler ist aufgetreten.",
): string {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message ?? fallback;
  }

  if (error instanceof Error) {
    return error.message;
  }

  return fallback;
}
