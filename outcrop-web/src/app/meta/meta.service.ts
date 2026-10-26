import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface ServiceInfo {
  name: string;
  version: string;
}

@Injectable({ providedIn: 'root' })
export class MetaService {
  private readonly http = inject(HttpClient);

  info(): Observable<ServiceInfo> {
    return this.http.get<ServiceInfo>('/api/meta');
  }
}
