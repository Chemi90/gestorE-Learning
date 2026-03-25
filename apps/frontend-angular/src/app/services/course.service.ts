import { inject, Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthService } from './auth.service';
import {
  CourseResponse,
  CourseTreeResponse,
  CreateCourseBulkRequest,
  CreateCourseRequest,
} from '../core/models/course.model';

@Injectable({
  providedIn: 'root',
})
export class CourseService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  
  // URL dinámica basada en el AuthService
  private readonly apiUrl = computed(() => `${this.authService.apiBaseUrl()}/content/api/v1/courses`);

  private readonly coursesSignal = signal<CourseResponse[]>([]);
  private readonly activeCourseTreeSignal = signal<CourseTreeResponse | null>(null);

  get courses() {
    return this.coursesSignal();
  }

  get activeCourseTree() {
    return this.activeCourseTreeSignal();
  }

  getCourses(): Observable<CourseResponse[]> {
    return this.http
      .get<CourseResponse[]>(this.apiUrl())
      .pipe(tap((courses) => this.coursesSignal.set(courses)));
  }

  getCourseById(id: string): Observable<CourseResponse> {
    return this.http.get<CourseResponse>(`${this.apiUrl()}/${id}`);
  }

  getCourseTree(id: string): Observable<CourseTreeResponse> {
    return this.http
      .get<CourseTreeResponse>(`${this.apiUrl()}/${id}/tree`)
      .pipe(tap((tree) => this.activeCourseTreeSignal.set(tree)));
  }

  createFullCourse(request: CreateCourseBulkRequest): Observable<CourseTreeResponse> {
    return this.http
      .post<CourseTreeResponse>(`${this.apiUrl()}/bulk`, request)
      .pipe(tap((tree) => this.activeCourseTreeSignal.set(tree)));
  }

  updateCourse(id: string, request: CreateCourseRequest): Observable<CourseResponse> {
    return this.http.put<CourseResponse>(`${this.apiUrl()}/${id}`, request);
  }

  updateCourseWithTree(id: string, request: CreateCourseBulkRequest): Observable<CourseResponse> {
    return this.http.put<CourseResponse>(`${this.apiUrl()}/${id}/tree`, request);
  }

  deleteCourse(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl()}/${id}`).pipe(
      tap(() => {
        const currentCourses = this.coursesSignal();
        this.coursesSignal.set(currentCourses.filter((c) => c.id !== id));
        if (this.activeCourseTreeSignal()?.courseId === id) {
          this.activeCourseTreeSignal.set(null);
        }
      }),
    );
  }

  clearActiveCourseTree(): void {
    this.activeCourseTreeSignal.set(null);
  }
}
