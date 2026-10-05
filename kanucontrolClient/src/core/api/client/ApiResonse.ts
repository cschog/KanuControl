export interface ApiResponse<T> {
    data: T;
    warnings: string[];
}