import { Component, inject, OnInit, signal } from '@angular/core';
import { MetaService, ServiceInfo } from './meta/meta.service';

@Component({
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App implements OnInit {
  private readonly meta = inject(MetaService);

  protected readonly info = signal<ServiceInfo | null>(null);
  protected readonly unreachable = signal(false);

  ngOnInit(): void {
    this.meta.info().subscribe({
      next: (info) => this.info.set(info),
      error: () => this.unreachable.set(true),
    });
  }
}
