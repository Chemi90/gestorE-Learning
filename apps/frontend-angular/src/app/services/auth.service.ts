import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LoginResponse {
  accessToken: string;
  role: 'ADMIN' | 'TEACHER' | 'STUDENT';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly token = signal<string | null>(null);
  private readonly role = signal<LoginResponse['role'] | null>(null);

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${environment.API_BASE_URL}/api/v1/auth/login`, { email, password })
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

  getRole(): LoginResponse['role'] | null {
    return this.role();
  }

  logout(): void {
    this.token.set(null);
    this.role.set(null);
  }
}
