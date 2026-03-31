import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormArray, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { ResourceType } from '../core/models/course.model';

@Component({
  selector: 'app-course-tree',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="tree-container" [formGroup]="form">
      <div class="tree-header">
        <h2>{{ isReadOnly ? 'Course Content' : 'Edit Course Modules' }}</h2>
        <button *ngIf="!isReadOnly" type="button" class="btn-primary" (click)="addModule()">
          + Add Module
        </button>
      </div>

      <div formArrayName="modules" class="modules-list">
        <div
          *ngFor="let moduleControl of modules.controls; let mIndex = index"
          [formGroupName]="mIndex"
          class="module-node"
        >
          <div class="node-header module-header">
            <span class="node-type">Module</span>
            <div class="node-content" *ngIf="!isReadOnly">
              <input formControlName="title" placeholder="Module Title" class="input-title" />
              <input formControlName="summary" placeholder="Module Summary" class="input-summary" />
            </div>
            <div class="node-content-readonly" *ngIf="isReadOnly">
              <strong>{{ moduleControl.get('title')?.value }}</strong>
              <p>{{ moduleControl.get('summary')?.value }}</p>
            </div>

            <div class="node-actions" *ngIf="!isReadOnly">
              <button type="button" class="btn-sm btn-danger" (click)="removeModule(mIndex)">
                Delete
              </button>
            </div>
          </div>

          <div class="units-container">
            <div class="tree-subheader">
              <h4>Units</h4>
              <button
                *ngIf="!isReadOnly"
                type="button"
                class="btn-sm btn-secondary"
                (click)="addUnit(mIndex)"
              >
                + Add Unit
              </button>
            </div>

            <div formArrayName="units" class="units-list">
              <div
                *ngFor="let unitControl of getUnits(mIndex).controls; let uIndex = index"
                [formGroupName]="uIndex"
                class="unit-node"
              >
                <div class="node-header unit-header">
                  <span class="node-type">Unit</span>
                  <div class="node-content" *ngIf="!isReadOnly">
                    <input formControlName="title" placeholder="Unit Title" class="input-title" />
                  </div>
                  <div class="node-content-readonly" *ngIf="isReadOnly">
                    <strong>{{ unitControl.get('title')?.value }}</strong>
                  </div>

                  <div class="node-actions" *ngIf="!isReadOnly">
                    <button
                      type="button"
                      class="btn-sm btn-danger"
                      (click)="removeUnit(mIndex, uIndex)"
                    >
                      Delete
                    </button>
                  </div>
                </div>

                <!-- Elements of the unit -->
                <div formArrayName="elements" class="elements-wrapper">
                  <div class="tree-subheader">
                    <h5>Elements</h5>
                    <button
                      *ngIf="!isReadOnly"
                      type="button"
                      class="btn-sm btn-outline"
                      (click)="addElement(mIndex, uIndex)"
                    >
                      + Add Element
                    </button>
                  </div>

                  <div
                    *ngFor="let elControl of getElements(mIndex, uIndex).controls; let eIndex = index"
                    [formGroupName]="eIndex"
                    class="element-container"
                  >
                    <div class="node-content" *ngIf="!isReadOnly">
                      <div class="element-header">
                        <input formControlName="title" placeholder="Element Title" class="input-title" />
                        <button type="button" class="btn-sm btn-danger" (click)="removeElement(mIndex, uIndex, eIndex)">x</button>
                      </div>
                      <input formControlName="summary" placeholder="Element Summary (Required)" class="input-summary" />
                      <textarea formControlName="body" placeholder="Element Body" rows="3"></textarea>
                      <select formControlName="resourceType">
                        <option *ngFor="let type of resourceTypes" [value]="type">{{ type }}</option>
                      </select>
                    </div>

                    <div class="node-content-readonly" *ngIf="isReadOnly">
                      <strong>{{ elControl.get('title')?.value }}</strong>
                      <p><em>{{ elControl.get('summary')?.value }}</em></p>
                      <p>Type: {{ elControl.get('resourceType')?.value }}</p>
                      <p *ngIf="elControl.get('body')?.value">{{ elControl.get('body')?.value }}</p>
                    </div>
                  </div>
                </div>

                <!-- Objectives of the unit -->
                <div class="objectives-container" formArrayName="objectives">
                  <div class="tree-subheader">
                    <h5>Objectives</h5>
                    <button
                      *ngIf="!isReadOnly"
                      type="button"
                      class="btn-sm btn-outline"
                      (click)="addObjective(mIndex, uIndex)"
                    >
                      + Add Objective
                    </button>
                  </div>

                  <div class="objectives-list">
                    <div
                      *ngFor="let objControl of getObjectives(mIndex, uIndex).controls; let oIndex = index"
                      [formGroupName]="oIndex"
                      class="objective-node"
                    >
                      <div class="node-content" *ngIf="!isReadOnly">
                        <input formControlName="description" placeholder="Objective Description" />
                      </div>
                      <div class="node-content-readonly" *ngIf="isReadOnly">
                        - {{ objControl.get('description')?.value }}
                      </div>
                      <button
                        *ngIf="!isReadOnly"
                        type="button"
                        class="btn-sm btn-danger"
                        (click)="removeObjective(mIndex, uIndex, oIndex)"
                      >
                        x
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .tree-container {
        margin-top: 2rem;
        border-top: 1px solid #ddd;
        padding-top: 1rem;
      }
      .tree-header,
      .tree-subheader {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1rem;
      }
      .module-node {
        border: 1px solid #ccc;
        border-radius: 6px;
        margin-bottom: 1.5rem;
        padding: 1rem;
        background: #fafafa;
      }
      .unit-node {
        border: 1px solid #eee;
        border-left: 4px solid #007bff;
        border-radius: 4px;
        margin-left: 1.5rem;
        margin-bottom: 1rem;
        padding: 1rem;
        background: white;
      }
      .elements-wrapper {
        margin-left: 1.5rem;
        margin-bottom: 1rem;
        border-left: 2px dashed #ccc;
        padding-left: 1rem;
      }
      .element-container {
        margin-bottom: 1rem;
        padding: 0.5rem;
        background: #f9f9f9;
        border-radius: 4px;
      }
      .element-header {
        display: flex;
        justify-content: space-between;
        gap: 0.5rem;
      }
      .objective-node {
        display: flex;
        gap: 0.5rem;
        align-items: center;
        margin-left: 1.5rem;
        margin-bottom: 0.5rem;
      }

      .node-header {
        display: flex;
        align-items: flex-start;
        gap: 1rem;
        margin-bottom: 1rem;
      }
      .node-type {
        font-weight: bold;
        font-size: 0.8rem;
        text-transform: uppercase;
        color: #666;
        min-width: 60px;
      }
      .node-content {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        flex: 1;
      }
      .node-content-readonly {
        flex: 1;
      }
      .node-content-readonly p {
        margin: 0.2rem 0;
        color: #555;
        font-size: 0.9rem;
      }

      input,
      select,
      textarea {
        padding: 0.4rem;
        border: 1px solid #ccc;
        border-radius: 4px;
        font-family: inherit;
      }
      textarea {
        resize: vertical;
      }
      .input-title {
        font-weight: bold;
        flex: 1;
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
        padding: 0.4rem 0.8rem;
        border-radius: 4px;
        cursor: pointer;
      }
      .btn-outline {
        background: transparent;
        color: #007bff;
        border: 1px solid #007bff;
        padding: 0.3rem 0.6rem;
        border-radius: 4px;
        cursor: pointer;
      }
      .btn-danger {
        background: #dc3545;
        color: white;
        border: none;
        padding: 0.3rem 0.6rem;
        border-radius: 4px;
        cursor: pointer;
      }
      .btn-sm {
        font-size: 0.8rem;
      }
    `,
  ],
})
export class CourseTreeComponent {
  @Input() form!: FormGroup;
  @Input() isReadOnly = false;

  private readonly fb = inject(FormBuilder);
  readonly resourceTypes = Object.values(ResourceType);

  get modules(): FormArray {
    return this.form.get('modules') as FormArray;
  }

  getUnits(moduleIndex: number): FormArray {
    return this.modules.at(moduleIndex).get('units') as FormArray;
  }

  getElements(moduleIndex: number, unitIndex: number): FormArray {
    return this.getUnits(moduleIndex).at(unitIndex).get('elements') as FormArray;
  }

  getObjectives(moduleIndex: number, unitIndex: number): FormArray {
    return this.getUnits(moduleIndex).at(unitIndex).get('objectives') as FormArray;
  }

  addModule() {
    const currentLength = this.modules.length;
    const moduleGroup = this.fb.group({
      title: ['', Validators.required],
      summary: [''],
      orderIndex: [currentLength],
      units: this.fb.array([]),
    });
    this.modules.push(moduleGroup);
  }

  removeModule(index: number) {
    this.modules.removeAt(index);
  }

  addUnit(moduleIndex: number) {
    const unitsArray = this.getUnits(moduleIndex);
    const unitGroup = this.fb.group({
      title: ['', Validators.required],
      orderIndex: [unitsArray.length],
      elements: this.fb.array([this.createElementGroup()]),
      objectives: this.fb.array([]),
    });
    unitsArray.push(unitGroup);
  }

  removeUnit(moduleIndex: number, unitIndex: number) {
    this.getUnits(moduleIndex).removeAt(unitIndex);
  }

  addElement(moduleIndex: number, unitIndex: number) {
    this.getElements(moduleIndex, unitIndex).push(this.createElementGroup(this.getElements(moduleIndex, unitIndex).length));
  }

  removeElement(moduleIndex: number, unitIndex: number, eIndex: number) {
    this.getElements(moduleIndex, unitIndex).removeAt(eIndex);
  }

  addObjective(moduleIndex: number, unitIndex: number) {
    const objectivesArray = this.getObjectives(moduleIndex, unitIndex);
    const objectiveGroup = this.fb.group({
      description: ['', Validators.required],
      orderIndex: [objectivesArray.length],
    });
    objectivesArray.push(objectiveGroup);
  }

  removeObjective(moduleIndex: number, unitIndex: number, objIndex: number) {
    this.getObjectives(moduleIndex, unitIndex).removeAt(objIndex);
  }

  private createElementGroup(orderIndex: number = 0): FormGroup {
    return this.fb.group({
      title: ['', Validators.required],
      summary: ['', Validators.required],
      body: [''],
      resourceType: [ResourceType.TEXT, Validators.required],
      orderIndex: [orderIndex],
    });
  }
}
