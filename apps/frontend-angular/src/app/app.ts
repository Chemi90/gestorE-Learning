import { JsonPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { GatewayApiService, PingResponse } from './services/gateway-api.service';

@Component({
  selector: 'app-root',
  imports: [JsonPipe],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  private readonly gatewayApiService = inject(GatewayApiService);

  protected readonly title = signal('Gestor e-Learning');
  protected readonly isLoading = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly pingResult = signal<PingResponse | null>(null);

  protected pingGateway(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.gatewayApiService.pingContentService().subscribe({
      next: (result) => {
        this.pingResult.set(result);
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        const message = error instanceof Error ? error.message : 'Unknown error while calling gateway';
        this.errorMessage.set(message);
        this.isLoading.set(false);
      }
    });
  }
}
