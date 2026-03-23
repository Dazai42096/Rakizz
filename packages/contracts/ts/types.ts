export enum Role {
    STUDENT = "student",
    PARENT = "parent",
    ADMIN = "admin"
}

export enum AssignmentStatus {
    PENDING = "pending",
    COMPLETED = "completed"
}

export interface UserResponse {
    id: string;
    email: string;
    role: Role;
    created_at: string;
}

export interface MaterialCreate {
    title: string;
    source_url: string;
}

export interface MaterialResponse {
    id: string;
    owner_id: string;
    title: string;
    source_url: string;
    created_at: string;
}

export interface AssignmentCreate {
    student_id: string;
    description: string;
    due_date: string;
}

export interface AssignmentUpdateStatus {
    status: AssignmentStatus;
}

export interface AssignmentUpdateMetadata {
    description?: string;
    due_date?: string;
}

export interface AssignmentResponse {
    id: string;
    student_id: string;
    description: string;
    due_date: string;
    status: AssignmentStatus;
}
