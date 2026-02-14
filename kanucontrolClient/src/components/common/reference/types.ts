export interface RefBase {
  id: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
}

export type FetchPageFn<T> = (params: {
  search?: string;
  page?: number;
  size?: number;
}) => Promise<Page<T>>;
