// src/api/types/common/DataStatus.ts

export type DataStatus = "OK" | "WARNING" | "ERROR";

export interface DataFieldStatus {
  status: DataStatus;
  message: string;
}
