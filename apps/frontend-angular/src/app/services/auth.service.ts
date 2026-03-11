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

  private readonly token = signal<string | null>(localStorage.getItem('token'));
  private readonly role = signal<UserRole | null>(localStorage.getItem('role') as UserRole);
  private readonly organizationId = signal<string | null>(localStorage.getItem('organizationId'));

  getOrganizations(): Observable<OrganizationResponse[]> {
    return this.http.get<OrganizationResponse[]>(`${environment.API_BASE_URL}/auth/api/v1/auth/organizations`);
  }

  login(email: string, password: string, organizationId: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.API_BASE_URL}/auth/api/v1/auth/login`, { email, password, organizationId })
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
