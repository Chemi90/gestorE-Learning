export enum CourseLevel {
  BEGINNER = 'BEGINNER',
  INTERMEDIATE = 'INTERMEDIATE',
  ADVANCED = 'ADVANCED'
}

export enum ResourceType {
  TEXT = 'TEXT',
  VIDEO = 'VIDEO',
  QUIZ = 'QUIZ',
  ASSIGNMENT = 'ASSIGNMENT'
}

export enum GenerationStatus {
  PENDING = 'PENDING',
  GENERATING = 'GENERATING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

export interface ObjectiveResponse {
  id: string;
  description: string;
}

export interface UnitResponse {
  id: string;
  title: string;
  contentPlaceholder: string;
  resourceType: ResourceType;
  orderIndex: number;
  status: GenerationStatus;
  objectives: ObjectiveResponse[];
}

export interface ModuleResponse {
  id: string;
  title: string;
  summary: string;
  orderIndex: number;
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

export interface CreateUnitRequest {
  title: string;
  contentPlaceholder: string;
  resourceType: ResourceType;
  objectives: CreateObjectiveRequest[];
}

export interface CreateModuleRequest {
  title: string;
  summary: string;
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

export interface CreateCourseRequest {
  title: string;
  description: string;
  level: CourseLevel;
  version: string;
  organizationId: string;
}
