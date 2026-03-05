import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { OrganizationResponse } from '../core/models/auth.model';

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.scss'
})
export class LoginPageComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly organizations = signal<OrganizationResponse[]>([]);

  protected readonly loginForm = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email]
    }),
    password: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    }),
    organizationId: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required]
    })
  });

  ngOnInit(): void {
    this.authService.getOrganizations().subscribe({
      next: (orgs) => this.organizations.set(orgs),
      error: () => this.errorMessage.set('Error al cargar las organizaciones')
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
        this.router.navigateByUrl('/home');
      },
      error: () => {
        this.isSubmitting.set(false);
        this.errorMessage.set('Credenciales invalidas o servicio no disponible');
      }
    });
  }
}
