import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { OrganizationResponse } from '../core/models/auth.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss'
})
export class LoginPageComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly organizations = signal<OrganizationResponse[]>([]);

  protected readonly backendPresets = [
    { label: 'Local (8090)', url: 'http://localhost:8090' },
    { label: 'Docker (8080)', url: 'http://localhost:8080' }
  ];

  protected readonly currentApiUrl = this.authService.apiBaseUrl;

  protected readonly loginForm = new FormGroup({
    email: new FormControl('admin@alpha.com', {
      nonNullable: true,
      validators: [Validators.required, Validators.email]
    }),
    password: new FormControl('password123', {
      nonNullable: true,
      validators: [Validators.required]
    }),
    organizationId: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    })
  });

  ngOnInit(): void {
    this.loadOrganizations();
  }

  protected setBackend(url: string): void {
    this.authService.setApiBaseUrl(url);
    this.loadOrganizations();
  }

  private loadOrganizations(): void {
    this.authService.getOrganizations().subscribe({
      next: (orgs) => this.organizations.set(orgs),
      error: () => {
        this.organizations.set([]);
        this.errorMessage.set(`Error al conectar con ${this.currentApiUrl()}`);
      }
    });
  }

  protected submit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const { email, password, organizationId } = this.loginForm.getRawValue();
    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.authService.login(email, password, organizationId).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.router.navigateByUrl('/courses'); // Cambiado a /courses para ir directo a la gestion
      },
      error: () => {
        this.isSubmitting.set(false);
        this.errorMessage.set('Credenciales invalidas o servicio no disponible');
      }
    });
  }
}
