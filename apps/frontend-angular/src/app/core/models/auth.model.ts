import { UserRole } from '../types/user-role.type';

export interface OrganizationResponse {
  id: string;
  name: string;
}

export interface LoginRequest {
  email: string;
  password: string;
  organizationId: string;
}

export interface LoginResponse {
  accessToken: string;
  role: UserRole;
}

export interface AuthUserResponse {
  id: string;
  email: string;
  role: UserRole;
}