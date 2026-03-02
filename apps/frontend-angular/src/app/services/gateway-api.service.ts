import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface PingResponse {
  service: string;
  status: string;
  time: string;
}

@Injectable({
  providedIn: 'root'
})
export class GatewayApiService {
  private readonly http = inject(HttpClient);

  pingContentService(): Observable<PingResponse> {
    return this.http.get<PingResponse>(`${environment.API_BASE_URL}/content/api/v1/ping`);
  }
}
