export enum CourseLevel {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED',
}

export enum ResourceType {
  TEXT = 'TEXT',
  VIDEO = 'VIDEO',
  QUIZ = 'QUIZ',
  ASSIGNMENT = 'ASSIGNMENT',
}

export enum GenerationStatus {
  PENDING = 'PENDING',
  GENERATING = 'GENERATING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}

export interface ObjectiveResponse {
  id: string;
  description: string;
  createdAt: string;
}

export interface ElementResponse {
  id: string;
  resourceType: ResourceType;
  title: string;
  body: string | null;
  status: GenerationStatus;
  version: number;
  createdAt: string;
  objectives: ObjectiveResponse[];
}

export interface UnitResponse {
  id: string;
  title: string;
  orderIndex: number;
  createdAt: string;
  active: boolean;
  element: ElementResponse;
}

export interface ModuleResponse {
  id: string;
  title: string;
  summary: string | null;
  orderIndex: number;
  createdAt: string;
  active: boolean;
  units: UnitResponse[];
}

export interface CourseTreeResponse {
  courseId: string;
  organizationId: string;
  title: string;
  level: CourseLevel;
  version: string;
  createdAt: string;
  active: boolean;
  modules: ModuleResponse[];
}

export interface CourseResponse {
  id: string;
  organizationId: string;
  title: string;
  description: string;
  level: CourseLevel;
  version: string;
  createdAt: string;
  active: boolean;
}

export interface CreateObjectiveRequest {
  description: string;
}

export interface CreateElementRequest {
  resourceType: ResourceType;
  title: string;
  body: string;
  objectives: CreateObjectiveRequest[];
}

export interface CreateUnitRequest {
  title: string;
  orderIndex: number;
  element: CreateElementRequest;
}

export interface CreateModuleRequest {
  title: string;
  summary: string;
  orderIndex: number;
  units: CreateUnitRequest[];
}

export interface CreateCourseBulkRequest {
  title: string;
  description: string;
  level: CourseLevel;
  version: string;
  organizationId: string;
  modules: CreateModuleRequest[];
}
