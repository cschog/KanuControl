// api/types/SaveResponse.ts

export interface SaveResponse<T> {
  data: T;
  warnings: string[];
}
