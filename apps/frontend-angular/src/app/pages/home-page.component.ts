import { JsonPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { GatewayApiService, ProtectedResponse } from '../services/gateway-api.service';

@Component({
  selector: 'app-home-page',
  imports: [JsonPipe, RouterLink],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.scss'
})
export class HomePageComponent {
  private readonly authService = inject(AuthService);
  private readonly gatewayApiService = inject(GatewayApiService);
  private readonly router = inject(Router);

  protected readonly result = signal<ProtectedResponse | null>(null);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly isLoading = signal(false);

  protected get role(): string {
    return this.authService.getRole() ?? 'NO_AUTH';
  }

  protected testProtectedEndpoint(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.gatewayApiService.testProtectedEndpoint().subscribe({
      next: (response) => {
        this.result.set(response);
        this.isLoading.set(false);
      },
      error: () => {
        this.errorMessage.set('No autorizado o fallo de red');
        this.isLoading.set(false);
      }
    });
  }

  protected logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
