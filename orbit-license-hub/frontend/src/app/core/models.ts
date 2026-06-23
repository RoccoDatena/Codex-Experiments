export type UserRole = 'SUPER_ADMIN' | 'TENANT_ADMIN' | 'LICENSE_ADMIN' | 'LICENSE_VIEWER';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  username: string;
  role: UserRole;
  tenantId: number | null;
}

export interface Tenant {
  id: number;
  name: string;
  purchasedLicenses: number;
  assignedLicenses: number;
  availableLicenses: number;
  status: 'ACTIVE' | 'DISABLED';
}

export interface ApplicationItem {
  id: number;
  tenantId: number;
  name: string;
  logoUrl: string;
  baseColor: string;
  environment: 'DEV' | 'PROD';
  retentionMinutes: number;
  publicKeyPem: string;
  allocatedLicenses: number;
  status: 'ACTIVE' | 'DISABLED';
}

export interface LicenseRecord {
  id: number;
  tenantId: number;
  applicationId: number;
  deviceCode: string;
  status: 'ASSIGNED' | 'FREE';
  lastUsedAt: string | null;
}

export interface UserItem {
  id: number;
  username: string;
  email: string;
  role: UserRole;
  tenantId: number | null;
  extraPermissions: string[];
  enabled: boolean;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
