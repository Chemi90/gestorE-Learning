import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginResponse, OrganizationResponse } from '../core/models/auth.model';
import { UserRole } from '../core/types/user-role.type';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);

  public readonly token = signal<string | null>(localStorage.getItem('token'));
  public readonly role = signal<UserRole | null>(localStorage.getItem('role') as UserRole);
  public readonly organizationId = signal<string | null>(localStorage.getItem('organizationId'));
  
  // URL Dinámica para desarrollo
  public readonly apiBaseUrl = signal<string>(sessionStorage.getItem('apiBaseUrl') ?? environment.API_BASE_URL);

  setApiBaseUrl(url: string): void {
    this.apiBaseUrl.set(url);
    sessionStorage.setItem('apiBaseUrl', url);
    // Al cambiar de backend, la sesion anterior ya no es valida
    this.logout();
  }

  getOrganizations(): Observable<OrganizationResponse[]> {
    return this.http.get<OrganizationResponse[]>(`${this.apiBaseUrl()}/auth/api/v1/auth/organizations`);
  }

  login(email: string, password: string, organizationId: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${this.apiBaseUrl()}/auth/api/v1/auth/login`, { email, password, organizationId })
      .pipe(
        tap((response) => {
          this.token.set(response.accessToken);
          this.role.set(response.role);
          this.organizationId.set(organizationId);
          
          localStorage.setItem('token', response.accessToken);
          localStorage.setItem('role', response.role);
          localStorage.setItem('organizationId', organizationId);
        })
      );
  }

  getToken(): string | null {
    return this.token();
  }

  getRole(): UserRole | null {
    return this.role();
  }

  getOrganizationId(): string | null {
    return this.organizationId();
  }

  logout(): void {
    this.token.set(null);
    this.role.set(null);
    this.organizationId.set(null);
    localStorage.clear();
  }
}
