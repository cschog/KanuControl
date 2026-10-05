export interface RefBase {
  id: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
}

export type FetchPageFn<T> = (params: { search?: string }) => Promise<T[]>;
