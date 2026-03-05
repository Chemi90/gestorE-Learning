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

  private readonly token = signal<string | null>(null);
  private readonly role = signal<UserRole | null>(null);

  getOrganizations(): Observable<OrganizationResponse[]> {
    return this.http.get<OrganizationResponse[]>(`${environment.API_BASE_URL}/api/v1/auth/organizations`);
  }

  login(email: string, password: string, organizationId: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.API_BASE_URL}/api/v1/auth/login`, { email, password, organizationId })
      .pipe(
        tap((response) => {
          this.token.set(response.accessToken);
          this.role.set(response.role);
        })
      );
  }

  getToken(): string | null {
    return this.token();
  }

  getRole(): UserRole | null {
    return this.role();
  }

  logout(): void {
    this.token.set(null);
    this.role.set(null);
  }
}
