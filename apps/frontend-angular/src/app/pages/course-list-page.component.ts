import { Component, inject, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { CourseService } from '../services/course.service';
import { AuthService } from '../services/auth.service';
import { CourseLevel } from '../core/models/course.model';
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-course-list-page',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  template: `
    <div class="course-list-container">
      <div class="header-actions">
        <h1>Courses</h1>
        <div class="btns">
          <button class="btn-secondary" (click)="loadCourses()">Refresh</button>
          <button *ngIf="canEdit" class="btn-primary" (click)="createCourse()">+ New Course</button>
        </div>
      </div>

      <div class="filters">
        <label>
          Filter by Level:
          <select [formControl]="levelFilterControl">
            <option value="">All Levels</option>
            <option *ngFor="let level of levels" [value]="level">{{ level }}</option>
          </select>
        </label>
      </div>

      <div class="course-grid">
        <div class="course-card" *ngFor="let course of filteredCourses()">
          <h3>{{ course.title }}</h3>
          <p class="course-level">Level: {{ course.level }}</p>
          <p class="course-version">Version: {{ course.version }}</p>
          <p class="course-desc" *ngIf="course.description">{{ course.description }}</p>
          
          <div class="card-actions">
            <button class="btn-secondary" (click)="viewTree(course.id)">View Tree</button>
            <button *ngIf="canEdit" class="btn-danger" (click)="deleteCourse(course.id)">Delete</button>
          </div>
        </div>
      </div>
      
      <div *ngIf="filteredCourses().length === 0" class="no-courses">
        <p>No courses found.</p>
      </div>
    </div>
  `,
  styles: [`
    .course-list-container {
      padding: 2rem;
    }
    .header-actions {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }
    .btns {
      display: flex;
      gap: 1rem;
    }
    .filters {
      margin-bottom: 2rem;
      max-width: 300px;

      label {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        font-weight: 600;
        font-size: 0.9rem;
        color: #4a3f35;
      }

      select {
        padding: 0.75rem 1rem;
        border: 1.5px solid #e0d7d0;
        border-radius: 8px;
        font-size: 1rem;
        background-color: #fff;
        transition: border-color 0.2s, box-shadow 0.2s;
        outline: none;
        cursor: pointer;
        appearance: none;
        background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%238c7365'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E");
        background-repeat: no-repeat;
        background-position: right 1rem center;
        background-size: 1.25rem;
        padding-right: 2.5rem;

        &:focus {
          border-color: #d9773e;
          box-shadow: 0 0 0 3px rgba(217, 119, 62, 0.1);
        }
      }
    }
    .course-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 1.5rem;
    }
    .course-card {
      border: 1px solid #ddd;
      border-radius: 8px;
      padding: 1.5rem;
      background: white;
      box-shadow: 0 2px 4px rgba(0,0,0,0.05);
    }
    .card-actions {
      margin-top: 1.5rem;
      display: flex;
      gap: 1rem;
    }
    .btn-primary {
      background: #007bff;
      color: white;
      border: none;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      cursor: pointer;
    }
    .btn-secondary {
      background: #6c757d;
      color: white;
      border: none;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      cursor: pointer;
    }
    .btn-danger {
      background: #dc3545;
      color: white;
      border: none;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      cursor: pointer;
    }
    .no-courses {
      text-align: center;
      color: #666;
      margin-top: 3rem;
    }
  `]
})
export class CourseListPageComponent implements OnInit {
  private readonly courseService = inject(CourseService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly levels = Object.values(CourseLevel);
  readonly levelFilterControl = new FormControl<string>('', { nonNullable: true });
  
  // Convertimos el valor del form control a un signal para usarlo en el computed
  private readonly selectedLevel = toSignal(this.levelFilterControl.valueChanges, { initialValue: '' });

  // Transformamos filteredCourses en un Signal computado
  readonly filteredCourses = computed(() => {
    const level = this.selectedLevel();
    const courses = this.courseService.courses; // Accede al Signal del servicio
    if (!level) return courses;
    return courses.filter(c => c.level === level);
  });

  get canEdit(): boolean {
    const role = this.authService.getRole();
    return role === 'ADMIN' || role === 'TEACHER';
  }

  ngOnInit() {
    this.loadCourses();
  }

  loadCourses() {
    this.courseService.getCourses().subscribe({
      next: (courses) => console.log('Courses loaded:', courses),
      error: (err) => console.error('Failed to load courses', err)
    });
  }

  createCourse() {
    this.router.navigate(['/courses/new']);
  }

  viewTree(id: string) {
    this.router.navigate(['/courses', id]);
  }

  deleteCourse(id: string) {
    if (confirm('Are you sure you want to delete this course?')) {
      this.courseService.deleteCourse(id).subscribe({
        error: (err) => console.error('Failed to delete course', err)
      });
    }
  }
}
