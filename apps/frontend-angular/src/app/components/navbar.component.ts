import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="navbar">
      <div class="nav-brand">Gestor E-Learning</div>
      <div class="nav-links" *ngIf="isLoggedIn()">
        <a routerLink="/courses" routerLinkActive="active" class="nav-link">Courses</a>
        <button class="btn-logout" (click)="logout()">Logout</button>
      </div>
      <div class="nav-links" *ngIf="!isLoggedIn()">
        <a routerLink="/login" routerLinkActive="active" class="nav-link">Login</a>
      </div>
    </nav>
  `,
  styles: [`
    .navbar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem 2rem;
      background-color: #333;
      color: white;
      box-shadow: 0 2px 5px rgba(0,0,0,0.1);
    }
    .nav-brand {
      font-size: 1.5rem;
      font-weight: bold;
    }
    .nav-links {
      display: flex;
      gap: 1.5rem;
      align-items: center;
    }
    .nav-link {
      color: white;
      text-decoration: none;
      font-weight: 500;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      transition: background-color 0.3s;
    }
    .nav-link:hover {
      background-color: #555;
    }
    .nav-link.active {
      background-color: #007bff;
    }
    .btn-logout {
      background-color: #dc3545;
      color: white;
      border: none;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      cursor: pointer;
      font-weight: 500;
      transition: background-color 0.3s;
    }
    .btn-logout:hover {
      background-color: #c82333;
    }
  `]
})
export class NavbarComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  isLoggedIn(): boolean {
    return !!this.authService.getToken();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
