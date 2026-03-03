import { Routes } from '@angular/router';
import { HomePageComponent } from './pages/home-page.component';
import { LoginPageComponent } from './pages/login-page.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginPageComponent },
  { path: 'home', component: HomePageComponent },
  { path: '**', redirectTo: 'login' }
];
