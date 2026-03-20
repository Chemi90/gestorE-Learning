import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CourseService } from '../services/course.service';
import { AuthService } from '../services/auth.service';
import {
  CourseLevel,
  CourseResponse,
  CourseTreeResponse,
  CreateCourseBulkRequest,
  ElementResponse,
  ModuleResponse,
  ObjectiveResponse,
  ResourceType,
  UnitResponse,
} from '../core/models/course.model';
import { CourseTreeComponent } from '../components/course-tree.component';

@Component({
  selector: 'app-course-editor-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, CourseTreeComponent],
  template: `
    <div class="editor-container">
      <header class="page-header">
        <h1>
          {{ isNewCourse ? 'Create New Course' : isReadOnly ? 'View Course' : 'Edit Course' }}
        </h1>
        <div class="header-actions">
          <button class="btn-secondary" (click)="goBack()">Back</button>
          <button
            *ngIf="!isReadOnly"
            class="btn-primary"
            (click)="saveCourse()"
            [disabled]="form.invalid"
          >
            Save Course
          </button>
        </div>
      </header>

      <form [formGroup]="form" class="course-form">
        <div class="form-section">
          <h2>Course Details</h2>

          <div class="form-group">
            <label for="title">Title</label>
            <input id="title" formControlName="title" [readonly]="isReadOnly" />
          </div>

          <div class="form-group">
            <label for="description">Description</label>
            <textarea
              id="description"
              formControlName="description"
              [readonly]="isReadOnly"
              rows="3"
            ></textarea>
          </div>

          <div class="form-group row-group">
            <div class="col">
              <label for="level">Level</label>
              <select id="level" formControlName="level" [attr.disabled]="isReadOnly ? true : null">
                <option *ngFor="let lvl of levels" [value]="lvl">{{ lvl }}</option>
              </select>
            </div>

            <div class="col">
              <label for="version">Version</label>
              <input id="version" formControlName="version" [readonly]="isReadOnly" />
            </div>

            <div class="col" *ngIf="isNewCourse">
              <label for="organizationId">Organization ID</label>
              <!-- En un caso real esto se tomaria del usuario autenticado -->
              <input id="organizationId" formControlName="organizationId" />
            </div>
          </div>
        </div>

        <app-course-tree [form]="form" [isReadOnly]="isReadOnly"> </app-course-tree>
      </form>
    </div>
  `,
  styles: [
    `
      .editor-container {
        padding: 2rem;
        max-width: 1000px;
        margin: 0 auto;
      }
      .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 2rem;
      }
      .header-actions {
        display: flex;
        gap: 1rem;
      }
      .form-section {
        background: white;
        padding: 1.5rem;
        border-radius: 8px;
        border: 1px solid #ddd;
        margin-bottom: 2rem;
      }
      .form-group {
        margin-bottom: 1rem;
        display: flex;
        flex-direction: column;
      }
      .row-group {
        flex-direction: row;
        gap: 1rem;
      }
      .col {
        flex: 1;
        display: flex;
        flex-direction: column;
      }
      label {
        font-weight: bold;
        margin-bottom: 0.5rem;
        color: #444;
      }
      input,
      textarea,
      select {
        padding: 0.5rem;
        border: 1px solid #ccc;
        border-radius: 4px;
        font-family: inherit;
      }
      input[readonly],
      textarea[readonly] {
        background-color: #f5f5f5;
        border-color: #e0e0e0;
        color: #555;
      }
      select[disabled] {
        background-color: #f5f5f5;
        border-color: #e0e0e0;
        color: #555;
      }

      .btn-primary {
        background: #007bff;
        color: white;
        border: none;
        padding: 0.5rem 1.5rem;
        border-radius: 4px;
        cursor: pointer;
        font-weight: bold;
      }
      .btn-primary:disabled {
        background: #a0cbfc;
        cursor: not-allowed;
      }
      .btn-secondary {
        background: #6c757d;
        color: white;
        border: none;
        padding: 0.5rem 1.5rem;
        border-radius: 4px;
        cursor: pointer;
      }
    `,
  ],
})
export class CourseEditorPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly courseService = inject(CourseService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  form!: FormGroup;
  isNewCourse = false;
  courseId: string | null = null;
  levels = Object.values(CourseLevel);

  get isReadOnly(): boolean {
    const role = this.authService.getRole();
    return role === 'STUDENT' || role === null; // Students can only view
  }

  ngOnInit() {
    this.initForm();

    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id === 'new') {
        this.isNewCourse = true;
        // Auto-assign org if needed
      } else if (id) {
        this.courseId = id;
        this.isNewCourse = false;
        this.loadCourseData(id);
      }
    });
  }

  initForm() {
    this.form = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      level: [CourseLevel.BEGINNER, Validators.required],
      version: ['1.0.0', Validators.required],
      organizationId: [this.authService.getOrganizationId() ?? '', Validators.required],
      modules: this.fb.array([]),
    });

    if (this.isReadOnly) {
      // It is handled by templates mostly, but we can also disable the form to prevent any dirty state
    }
  }

  loadCourseData(id: string) {
    forkJoin({
      tree: this.courseService.getCourseTree(id),
      course: this.courseService.getCourseById(id),
    }).subscribe({
      next: ({ tree, course }) => this.patchFormWithCourse(tree, course),
      error: (err) => console.error('Failed to load course tree', err),
    });
  }

  patchFormWithCourse(tree: CourseTreeResponse, course: CourseResponse) {
    const modulesArray = this.modulesArray;
    modulesArray.clear();

    this.form.patchValue({
      title: tree.title,
      description: course.description ?? '',
      level: tree.level,
      version: tree.version,
      organizationId: tree.organizationId,
    });

    tree.modules?.forEach((mod, mIndex) => {
      const modGroup = this.createModuleGroup(mod, mIndex);

      mod.units?.forEach((unit, uIndex) => {
        this.getUnitsArray(modGroup).push(this.createUnitGroup(unit, uIndex));
      });

      modulesArray.push(modGroup);
    });
  }

  saveCourse() {
    if (this.form.invalid) {
      console.warn('Form is invalid', this.form.errors);
      return;
    }

    const payload = this.form.getRawValue() as CreateCourseBulkRequest;

    // RECALCULAR INDICES: Asegura que siempre sean secuenciales (1, 2, 3...) y sin duplicados
    if (payload.modules) {
      payload.modules.forEach((mod: any, mIndex: number) => {
        mod.orderIndex = mIndex + 1;
        if (mod.units) {
          mod.units.forEach((unit: any, uIndex: number) => {
            unit.orderIndex = uIndex + 1;
          });
        }
      });
    }

    console.log('Saving course with payload:', JSON.stringify(payload, null, 2));

    if (this.isNewCourse) {
      console.log('Calling createFullCourse...');
      this.courseService.createFullCourse(payload).subscribe({
        next: (res) => {
          console.log('Course created successfully:', res);
          this.router.navigate(['/courses']);
        },
        error: (err) => console.error('Error creating course', err),
      });
    } else if (this.courseId) {
      console.log('Calling updateCourse for ID:', this.courseId);
      this.courseService.updateCourse(this.courseId, payload).subscribe({
        next: (res) => {
          console.log('Course updated successfully:', res);
          this.router.navigate(['/courses']);
        },
        error: (err) => console.error('Error updating course', err),
      });
    }
  }

  goBack() {
    this.router.navigate(['/courses']);
  }

  private get modulesArray(): FormArray {
    return this.form.get('modules') as FormArray;
  }

  private getUnitsArray(moduleGroup: FormGroup): FormArray {
    return moduleGroup.get('units') as FormArray;
  }

  private createObjectiveGroup(objective?: ObjectiveResponse): FormGroup {
    return this.fb.group({
      description: [objective?.description ?? '', Validators.required],
    });
  }

  private createElementGroup(element?: ElementResponse): FormGroup {
    const objectives = this.fb.array(
      (element?.objectives ?? []).map((objective) => this.createObjectiveGroup(objective)),
    );

    return this.fb.group({
      title: [element?.title ?? '', Validators.required],
      body: [element?.body ?? ''],
      resourceType: [element?.resourceType ?? ResourceType.TEXT, Validators.required],
      objectives,
    });
  }

  private createUnitGroup(unit?: UnitResponse, unitIndex = 0): FormGroup {
    return this.fb.group({
      title: [unit?.title ?? '', Validators.required],
      orderIndex: [unit?.orderIndex ?? unitIndex],
      element: this.createElementGroup(unit?.element),
    });
  }

  private createModuleGroup(module?: ModuleResponse, moduleIndex = 0): FormGroup {
    return this.fb.group({
      title: [module?.title ?? '', Validators.required],
      summary: [module?.summary ?? ''],
      orderIndex: [module?.orderIndex ?? moduleIndex],
      units: this.fb.array([]),
    });
  }
}
