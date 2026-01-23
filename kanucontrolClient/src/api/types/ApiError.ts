export interface ApiError {
  status: number;
  error: string;
  message?: string;
  fieldErrors?: Record<string, string> | null;
}
