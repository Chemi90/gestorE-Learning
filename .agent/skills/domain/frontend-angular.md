# Skill: Frontend Angular

## Proposito
Patrones para componentes Angular 21, servicios, interceptores, rutas y testing con Vitest.

## Reglas Obligatorias

1. Componentes siempre **standalone** — no usar `NgModule`.
2. DI con `inject()` — no inyeccion por constructor.
3. Estado reactivo con `signal()` — no BehaviorSubject/Observable para estado local.
4. Formularios con Reactive Forms (`FormGroup`, `FormControl`, `Validators`).
5. Formato: Prettier con `printWidth=100`, `singleQuote=true`.
6. Testing: Vitest, NO Karma/Jasmine.

## Componente Standalone — Patron Canonico

```typescript
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MiService } from '../services/mi.service';

@Component({
  selector: 'app-mi-pagina',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './mi-pagina.component.html',
  styleUrl: './mi-pagina.component.scss',
})
export class MiPaginaComponent {
  private readonly router = inject(Router);
  private readonly miService = inject(MiService);

  form = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(8)]),
  });

  onSubmit() {
    if (this.form.invalid) return;
    const { email, password } = this.form.getRawValue();
    this.miService.login(email!, password!).subscribe({
      next: () => this.router.navigate(['/home']),
      error: (err) => console.error(err),
    });
  }
}
```

## Servicio — Patron Canonico

```typescript
import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  private readonly tokenSignal = signal<string | null>(localStorage.getItem('token'));

  get token() {
    return this.tokenSignal();
  }

  login(email: string, password: string) {
    return this.http.post<{ token: string }>('/api/v1/auth/login', { email, password }).pipe(
      tap((res) => {
        localStorage.setItem('token', res.token);
        this.tokenSignal.set(res.token);
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
    this.tokenSignal.set(null);
  }

  getToken(): string | null {
    return this.tokenSignal();
  }
}
```

## Interceptor HTTP — Patron Funcional

```typescript
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  if (token) {
    const cloned = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    });
    return next(cloned);
  }

  return next(req);
};
```

Registro en `app.config.ts`:

```typescript
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
  ],
};
```

## Rutas — Patron Canonico

```typescript
import { Routes } from '@angular/router';
import { LoginPageComponent } from './pages/login-page.component';
import { HomePageComponent } from './pages/home-page.component';

export const routes: Routes = [
  { path: 'login', component: LoginPageComponent },
  { path: 'home', component: HomePageComponent },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' },
];
```

## Estructura de Directorios del Frontend

```
apps/frontend-angular/src/
├── index.html
├── main.ts
├── styles.scss
├── environments/
└── app/
    ├── app.config.ts
    ├── app.html
    ├── app.routes.ts
    ├── interceptors/
    │   └── auth.interceptor.ts
    ├── pages/
    │   ├── login-page.component.{ts,html,scss}
    │   └── home-page.component.{ts,html,scss}
    └── services/
        ├── auth.service.ts
        └── gateway-api.service.ts
```

## Anti-patrones a Evitar

- Usar `NgModule` — siempre standalone
- Inyeccion por constructor — usar `inject()`
- `BehaviorSubject` para estado simple — usar `signal()`
- Interceptores basados en clase — usar `HttpInterceptorFn`
- Karma/Jasmine — el proyecto usa Vitest

## Referencias en el Repo

- `apps/frontend-angular/src/app/app.config.ts`
- `apps/frontend-angular/src/app/app.routes.ts`
- `apps/frontend-angular/src/app/interceptors/auth.interceptor.ts`
- `apps/frontend-angular/src/app/services/auth.service.ts`
- `apps/frontend-angular/src/app/pages/login-page.component.ts`
