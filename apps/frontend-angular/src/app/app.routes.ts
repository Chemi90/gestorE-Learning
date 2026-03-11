import { Routes } from '@angular/router';
import { HomePageComponent } from './pages/home-page.component';
import { LoginPageComponent } from './pages/login-page.component';
import { CourseListPageComponent } from './pages/course-list-page.component';
import { CourseEditorPageComponent } from './pages/course-editor-page.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', component: LoginPageComponent },
  { path: 'home', component: HomePageComponent },
  { path: 'courses', component: CourseListPageComponent },
  { path: 'courses/new', component: CourseEditorPageComponent },
  { path: 'courses/:id', component: CourseEditorPageComponent },
  { path: '**', redirectTo: 'login' }
];
