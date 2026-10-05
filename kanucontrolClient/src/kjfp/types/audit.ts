// src/types/audit.ts

export interface AuditSession {
  id: number;
  username: string;
  fullName: string;
  email: string;
  tenant: string;
  loginTime: string;
  lastSeen: string;
  ipAddress: string;
  userAgent: string;
}

export interface AuditDashboard {
  activeSessions: number;
  loginsToday: number;
  externalSessions: number;
  activeTenants: number;
}
