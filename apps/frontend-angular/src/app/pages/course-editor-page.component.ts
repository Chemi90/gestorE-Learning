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
          <button type="button" class="btn-secondary" (click)="goBack()">Back</button>
          
          <!-- BOTÓN DE GUARDADO - Forzamos el click y quitamos el disabled para debug -->
          <button
            *ngIf="!isReadOnly"
            type="button"
            class="btn-primary"
            (click)="saveCourse()"
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
              <input type="number" id="version" formControlName="version" [readonly]="isReadOnly" />
            </div>

            <div class="col" *ngIf="isNewCourse">
              <label for="organizationId">Organization ID</label>
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
    return role === 'STUDENT' || role === null;
  }

  ngOnInit() {
    this.initForm();

    this.route.paramMap.subscribe((params) => {
      const id = params.get('id');
      if (id === 'new') {
        this.isNewCourse = true;
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
      version: [1, Validators.required],
      organizationId: [this.authService.getOrganizationId() ?? '', Validators.required],
      modules: this.fb.array([]),
    });
  }

  loadCourseData(id: string) {
    forkJoin({
      tree: this.courseService.getCourseTree(id),
      course: this.courseService.getCourseById(id),
    }).subscribe({
      next: ({ tree, course }) => this.patchFormWithCourse(tree, course),
      error: (err: unknown) => console.error('Failed to load course tree', err),
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
      const unitsArray = this.getUnitsArray(modGroup);

      mod.units?.forEach((unit, uIndex) => {
        unitsArray.push(this.createUnitGroup(unit, uIndex));
      });

      modulesArray.push(modGroup);
    });
  }

  saveCourse() {
    // LOG INMEDIATO
    console.log('!!! SAVE COURSE FUNCTION CALLED !!!');
    console.log('Form Validity:', this.form.valid);
    console.log('Form Values:', this.form.value);

    if (this.form.invalid) {
      console.warn('Cannot proceed: Form is invalid.');
      this.findInvalidControls(this.form);
      return;
    }

    const payload = this.form.getRawValue() as CreateCourseBulkRequest;

    // RECALCULAR INDICES
    if (payload.modules) {
      payload.modules.forEach((mod: any, mIndex: number) => {
        mod.orderIndex = mIndex;
        if (mod.units) {
          mod.units.forEach((unit: any, uIndex: number) => {
            unit.orderIndex = uIndex;
            if (unit.elements) {
              unit.elements.forEach((el: any, eIndex: number) => {
                el.orderIndex = eIndex;
              });
            }
            if (unit.objectives) {
              unit.objectives.forEach((obj: any, oIndex: number) => {
                obj.orderIndex = oIndex;
              });
            }
          });
        }
      });
    }

    console.log('Payload to send:', payload);

    if (this.isNewCourse) {
      this.courseService.createFullCourse(payload).subscribe({
        next: () => this.router.navigate(['/courses']),
        error: (err: unknown) => console.error('Error creating course', err),
      });
    } else if (this.courseId) {
      this.courseService.updateCourseWithTree(this.courseId, payload).subscribe({
        next: () => this.router.navigate(['/courses']),
        error: (err: unknown) => console.error('Error updating course', err),
      });
    }
  }

  // Método auxiliar para debugear validaciones
  private findInvalidControls(form: FormGroup | FormArray) {
    const invalid = [];
    const controls = form.controls as any;
    for (const name in controls) {
      if (controls[name].invalid) {
        invalid.push(name);
        console.log('Invalid Control:', name, controls[name].errors);
      }
    }
    return invalid;
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

  private createObjectiveGroup(objective?: ObjectiveResponse, index = 0): FormGroup {
    return this.fb.group({
      description: [objective?.description ?? '', Validators.required],
      orderIndex: [objective?.orderIndex ?? index],
    });
  }

  private createElementGroup(element?: ElementResponse, index = 0): FormGroup {
    return this.fb.group({
      title: [element?.title ?? '', Validators.required],
      body: [element?.body ?? ''],
      resourceType: [element?.resourceType ?? ResourceType.TEXT, Validators.required],
      orderIndex: [element?.orderIndex ?? index],
    });
  }

  private createUnitGroup(unit?: UnitResponse, unitIndex = 0): FormGroup {
    const elementsArray = this.fb.array(
      (unit?.elements ?? []).map((el, i) => this.createElementGroup(el, i)),
    );
    const objectivesArray = this.fb.array(
      (unit?.objectives ?? []).map((obj, i) => this.createObjectiveGroup(obj, i)),
    );

    // If new unit, add one empty element
    if (!unit && elementsArray.length === 0) {
      elementsArray.push(this.createElementGroup());
    }

    return this.fb.group({
      title: [unit?.title ?? '', Validators.required],
      orderIndex: [unit?.orderIndex ?? unitIndex],
      elements: elementsArray,
      objectives: objectivesArray,
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
