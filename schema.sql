-- CREATE SCHEMA IF NOT EXISTS ih_pm;
-- SET search_path TO ih_pm;

-- Drop tables if they exist (optional, use with caution)
-- DROP TABLE IF EXISTS ih_pm.documents;
-- DROP TABLE IF EXISTS ih_pm.tasks;
-- DROP TABLE IF EXISTS ih_pm.team_members;
-- DROP TABLE IF EXISTS ih_pm.teams;
-- DROP TABLE IF EXISTS ih_pm.project_members;
-- DROP TABLE IF EXISTS ih_pm.projects;

-- Table: projects
CREATE TABLE ih_pm.projects (
    id BIGINT PRIMARY KEY,
    project_uuid VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    note TEXT,
    status VARCHAR(50) NOT NULL,
    budget_token BIGINT NOT NULL,
    reward_token BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    assignee_id BIGINT NOT NULL,
    deliverable_description TEXT,
    deliverable_link VARCHAR(1000),
    completion_comment TEXT,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    -- AuditEntity fields
    created_at BIGINT NOT NULL,
    updated_at BIGINT,
    created_by VARCHAR(50),
    updated_by VARCHAR(100),
    version INTEGER DEFAULT 0
);

-- Table: project_members
CREATE TABLE ih_pm.project_members (
    id BIGINT PRIMARY KEY,
    user_id BIGINT,
    role VARCHAR(100),
    status VARCHAR(50),
    project_id BIGINT,
    -- AuditEntity fields
    created_at BIGINT NOT NULL,
    updated_at BIGINT,
    created_by VARCHAR(50),
    updated_by VARCHAR(100),
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_project_members_project FOREIGN KEY (project_id) REFERENCES ih_pm.projects(id)
);

-- Table: teams
CREATE TABLE ih_pm.teams (
    id BIGINT PRIMARY KEY,
    team_uuid VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    note TEXT,
    status VARCHAR(50) NOT NULL,
    budget_token BIGINT NOT NULL,
    reward_token BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    assignee_id BIGINT NOT NULL,
    deliverable_description TEXT,
    deliverable_link VARCHAR(1000),
    completion_comment TEXT,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    project_id BIGINT,
    -- AuditEntity fields
    created_at BIGINT NOT NULL,
    updated_at BIGINT,
    created_by VARCHAR(50),
    updated_by VARCHAR(100),
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_teams_project FOREIGN KEY (project_id) REFERENCES ih_pm.projects(id)
);

-- Table: team_members
CREATE TABLE ih_pm.team_members (
    id BIGINT PRIMARY KEY,
    user_id BIGINT,
    status VARCHAR(50),
    team_id BIGINT,
    -- AuditEntity fields
    created_at BIGINT NOT NULL,
    updated_at BIGINT,
    created_by VARCHAR(50),
    updated_by VARCHAR(100),
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES ih_pm.teams(id)
);

-- Table: tasks
CREATE TABLE ih_pm.tasks (
    id BIGINT PRIMARY KEY,
    task_uuid VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    note TEXT,
    status VARCHAR(50) NOT NULL,
    reward_token BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    assignee_id BIGINT NOT NULL,
    deliverable_description TEXT,
    deliverable_link VARCHAR(1000),
    completion_comment TEXT,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    team_id BIGINT,
    project_id BIGINT,
    -- AuditEntity fields
    created_at BIGINT NOT NULL,
    updated_at BIGINT,
    created_by VARCHAR(50),
    updated_by VARCHAR(100),
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_tasks_team FOREIGN KEY (team_id) REFERENCES ih_pm.teams(id),
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES ih_pm.projects(id)
);

-- Table: documents
CREATE TABLE ih_pm.documents (
    id BIGINT PRIMARY KEY,
    document_type VARCHAR(50),
    document_scope VARCHAR(50),
    entity_id BIGINT,
    file_url TEXT,
    file_name VARCHAR(255),
    -- AuditEntity fields
    created_at BIGINT NOT NULL,
    updated_at BIGINT,
    created_by VARCHAR(50),
    updated_by VARCHAR(100),
    version INTEGER DEFAULT 0
);
