import {defineStore} from 'pinia'
import {ref, computed} from 'vue'
import {useDbStore} from './db.store'
import {useErrorHandler} from '~/composables/useErrorHandler'
import {DatabaseError} from '~/exceptions/db.error'
import {error} from "@tauri-apps/plugin-log"
import type {
  Project, ProjectRequest, ProjectGoal, ProjectGoalRequest,
  ProjectResource, ProjectResourceRequest, ProjectAsset, ProjectAssetRequest,
  ProjectNote, ProjectNoteRequest, ProjectMetric, ProjectMetricRequest
} from '~/interfaces'

export const useProjectStore = defineStore('project', () => {
  const dbStore = useDbStore()
  const {handleError} = useErrorHandler()
  const projects = ref<Project[]>([])
  const totalProjects = ref(0)
  const lastFetchTimestamp = ref(0)
  const CACHE_DURATION = 5 * 60 * 1000 // 5 minutes

  const isCacheValid = computed(() => {
    return Date.now() - lastFetchTimestamp.value < CACHE_DURATION
  })

  // Project CRUD operations
  const getProjects = async (page: number = 1, pageSize: number = 20, forceRefresh: boolean = false): Promise<{
    projects: Project[],
    total: number
  }> => {
    if (!forceRefresh && isCacheValid.value && projects.value.length > 0) {
      return {projects: projects.value, total: totalProjects.value}
    }
    try {
      const offset = (page - 1) * pageSize
      const result = await dbStore.select<any[]>(
          `SELECT p.*, i.Path as ImagePath
           FROM Projects p
                    LEFT JOIN Images i ON p.ImageID = i.UUID
           ORDER BY p.CreatedAt DESC
           LIMIT $1 OFFSET $2`,
          [pageSize, offset]
      )
      const fetchedProjects = result.map(row => ({
        id: row.ID,
        name: row.Name,
        description: row.Description,
        imageID: row.ImageID,
        budget: row.Budget,
        createdAt: row.CreatedAt,
        updatedAt: row.UpdatedAt,
        imagePath: row.ImagePath
      }))
      const countResult = await dbStore.select<[{
        count: number
      }]>('SELECT COUNT(*) as count FROM Projects')
      const total = countResult[0].count
      projects.value = fetchedProjects
      totalProjects.value = total
      lastFetchTimestamp.value = Date.now()
      return {projects: fetchedProjects, total}
    } catch (e) {
      await error(`Error fetching projects: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to fetch projects',
        cause: e
      }))
      return {projects: [], total: 0}
    }
  }

  const getProjectById = async (id: number): Promise<Project | null> => {
    try {
      const result = await dbStore.select<any[]>(
          `SELECT p.*, i.Path as ImagePath
           FROM Projects p
                    LEFT JOIN Images i ON p.ImageID = i.UUID
           WHERE p.ID = $1`,
          [id]
      )
      if (result.length === 0) return null
      const row = result[0]
      return {
        id: row.ID,
        name: row.Name,
        description: row.Description,
        imageID: row.ImageID,
        budget: row.Budget,
        createdAt: row.CreatedAt,
        updatedAt: row.UpdatedAt,
        imagePath: row.ImagePath
      }
    } catch (e) {
      await error(`Error fetching project by ID: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to fetch project by ID',
        cause: e
      }))
      return null
    }
  }

  const createProject = async (project: ProjectRequest): Promise<number | null> => {
    try {
      const result = await dbStore.executeQuery(
          'INSERT INTO Projects (Name, Description, ImageID, Budget) VALUES ($1, $2, $3, $4)',
          [project.name, project.description, project.imageID, project.budget]
      )
      const id = result.lastInsertId as number
      lastFetchTimestamp.value = 0 // Invalidate cache
      return id
    } catch (e) {
      await error(`Error creating project: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to create project',
        cause: e
      }))
      return null
    }
  }

  const updateProject = async (id: number, project: Partial<ProjectRequest>): Promise<boolean> => {
    try {
      const setClause = Object.keys(project).map((key, index) => `${key} = $${index + 2}`).join(', ')
      const values = Object.values(project)
      await dbStore.executeQuery(
          `UPDATE Projects
           SET ${setClause}
           WHERE ID = $1`,
          [id, ...values]
      )
      lastFetchTimestamp.value = 0 // Invalidate cache
      return true
    } catch (e) {
      await error(`Error updating project: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to update project',
        cause: e
      }))
      return false
    }
  }

  const deleteProject = async (id: number): Promise<boolean> => {
    try {
      await dbStore.executeQuery('DELETE FROM Projects WHERE ID = $1', [id])
      lastFetchTimestamp.value = 0 // Invalidate cache
      return true
    } catch (e) {
      await error(`Error deleting project: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to delete project',
        cause: e
      }))
      return false
    }
  }

  // Project Goals CRUD operations
  const getProjectGoals = async (projectID: number): Promise<ProjectGoal[]> => {
    try {
      const result = await dbStore.select<any[]>(
          'SELECT * FROM ProjectGoals WHERE ProjectID = $1 ORDER BY CreatedAt DESC',
          [projectID]
      )
      return result.map(row => ({
        id: row.ID,
        projectID: row.ProjectID,
        description: row.Description,
        isCompleted: row.IsCompleted === 1,
        dueDate: row.DueDate,
        createdAt: row.CreatedAt,
        updatedAt: row.UpdatedAt
      }))
    } catch (e) {
      await error(`Error fetching project goals: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to fetch project goals',
        cause: e
      }))
      return []
    }
  }

  const createProjectGoal = async (goal: ProjectGoalRequest): Promise<number | null> => {
    try {
      const result = await dbStore.executeQuery(
          'INSERT INTO ProjectGoals (ProjectID, Description, IsCompleted, DueDate) VALUES ($1, $2, $3, $4)',
          [goal.projectID, goal.description, goal.isCompleted ? 1 : 0, goal.dueDate]
      )
      return result.lastInsertId as number
    } catch (e) {
      await error(`Error creating project goal: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to create project goal',
        cause: e
      }))
      return null
    }
  }

  const updateProjectGoal = async (id: number, goal: Partial<ProjectGoalRequest>): Promise<boolean> => {
    try {
      const setClause = Object.keys(goal).map((key, index) => `${key} = $${index + 2}`).join(', ')
      const values = Object.values(goal).map(value => value === 'isCompleted' ? (value ? 1 : 0) : value)
      await dbStore.executeQuery(
          `UPDATE ProjectGoals
           SET ${setClause}
           WHERE ID = $1`,
          [id, ...values]
      )
      return true
    } catch (e) {
      await error(`Error updating project goal: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to update project goal',
        cause: e
      }))
      return false
    }
  }

  const deleteProjectGoal = async (id: number): Promise<boolean> => {
    try {
      await dbStore.executeQuery('DELETE FROM ProjectGoals WHERE ID = $1', [id])
      return true
    } catch (e) {
      await error(`Error deleting project goal: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to delete project goal',
        cause: e
      }))
      return false
    }
  }

  // Project Resources CRUD operations
  const getProjectResources = async (projectID: number): Promise<ProjectResource[]> => {
    try {
      const result = await dbStore.select<any[]>(
          'SELECT * FROM ProjectResources WHERE ProjectID = $1 ORDER BY CreatedAt DESC',
          [projectID]
      )
      return result.map(row => ({
        id: row.ID,
        projectID: row.ProjectID,
        name: row.Name,
        type: row.Type,
        description: row.Description,
        quantity: row.Quantity,
        createdAt: row.CreatedAt,
        updatedAt: row.UpdatedAt
      }))
    } catch (e) {
      await error(`Error fetching project resources: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to fetch project resources',
        cause: e
      }))
      return []
    }
  }

  const createProjectResource = async (resource: ProjectResourceRequest): Promise<number | null> => {
    try {
      const result = await dbStore.executeQuery(
          'INSERT INTO ProjectResources (ProjectID, Name, Type, Description, Quantity) VALUES ($1, $2, $3, $4, $5)',
          [resource.projectID, resource.name, resource.type, resource.description, resource.quantity]
      )
      return result.lastInsertId as number
    } catch (e) {
      await error(`Error creating project resource: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to create project resource',
        cause: e
      }))
      return null
    }
  }

  const updateProjectResource = async (id: number, resource: Partial<ProjectResourceRequest>): Promise<boolean> => {
    try {
      const setClause = Object.keys(resource).map((key, index) => `${key} = $${index + 2}`).join(', ')
      const values = Object.values(resource)
      await dbStore.executeQuery(
          `UPDATE ProjectResources
           SET ${setClause}
           WHERE ID = $1`,
          [id, ...values]
      )
      return true
    } catch (e) {
      await error(`Error updating project resource: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to update project resource',
        cause: e
      }))
      return false
    }
  }

  const deleteProjectResource = async (id: number): Promise<boolean> => {
    try {
      await dbStore.executeQuery('DELETE FROM ProjectResources WHERE ID = $1', [id])
      return true
    } catch (e) {
      await error(`Error deleting project resource: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to delete project resource',
        cause: e
      }))
      return false
    }
  }

  // Project Assets CRUD operations
  const getProjectAssets = async (projectID: number): Promise<ProjectAsset[]> => {
    try {
      const result = await dbStore.select<any[]>(
          'SELECT * FROM ProjectAssets WHERE ProjectID = $1 ORDER BY CreatedAt DESC',
          [projectID]
      )
      return result.map(row => ({
        id: row.ID,
        projectID: row.ProjectID,
        name: row.Name,
        type: row.Type,
        status: row.Status,
        filePath: row.FilePath,
        createdAt: row.CreatedAt,
        updatedAt: row.UpdatedAt
      }))
    } catch (e) {
      await error(`Error fetching project assets: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to fetch project assets',
        cause: e
      }))
      return []
    }
  }

  const createProjectAsset = async (asset: ProjectAssetRequest): Promise<number | null> => {
    try {
      const result = await dbStore.executeQuery(
          'INSERT INTO ProjectAssets (ProjectID, Name, Type, Status, FilePath) VALUES ($1, $2, $3, $4, $5)',
          [asset.projectID, asset.name, asset.type, asset.status || 'Pending', asset.filePath]
      )
      return result.lastInsertId as number
    } catch (e) {
      await error(`Error creating project asset: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to create project asset',
        cause: e
      }))
      return null
    }
  }

  const updateProjectAsset = async (id: number, asset: Partial<ProjectAssetRequest>): Promise<boolean> => {
    try {
      const setClause = Object.keys(asset).map((key, index) => `${key} = $${index + 2}`).join(', ')
      const values = Object.values(asset)
      await dbStore.executeQuery(
          `UPDATE ProjectAssets
           SET ${setClause}
           WHERE ID = $1`,
          [id, ...values]
      )
      return true
    } catch (e) {
      await error(`Error updating project asset: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to update project asset',
        cause: e
      }))
      return false
    }
  }

  const deleteProjectAsset = async (id: number): Promise<boolean> => {
    try {
      await dbStore.executeQuery('DELETE FROM ProjectAssets WHERE ID = $1', [id])
      return true
    } catch (e) {
      await error(`Error deleting project asset: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to delete project asset',
        cause: e
      }))
      return false
    }
  }

  const getProjectNotes = async (projectID: number): Promise<ProjectNote[]> => {
    try {
      const result = await dbStore.select<any[]>(
          'SELECT * FROM ProjectNotes WHERE ProjectID = $1 ORDER BY CreatedAt DESC',
          [projectID]
      )
      return result.map(row => ({
        id: row.ID,
        projectID: row.ProjectID,
        title: row.Title,
        content: row.Content,
        createdAt: row.CreatedAt,
        updatedAt: row.UpdatedAt
      }))
    } catch (e) {
      await error(`Error fetching project notes: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to fetch project notes',
        cause: e
      }))
      return []
    }
  }

  const createProjectNote = async (note: ProjectNoteRequest): Promise<number | null> => {
    try {
      const result = await dbStore.executeQuery(
          'INSERT INTO ProjectNotes (ProjectID, Title, Content) VALUES ($1, $2, $3)',
          [note.projectID, note.title, note.content]
      )
      return result.lastInsertId as number
    } catch (e) {
      await error(`Error creating project note: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to create project note',
        cause: e
      }))
      return null
    }
  }

  const updateProjectNote = async (id: number, note: Partial<ProjectNoteRequest>): Promise<boolean> => {
    try {
      const setClause = Object.keys(note).map((key, index) => `${key} = $${index + 2}`).join(', ')
      const values = Object.values(note)
      await dbStore.executeQuery(
          `UPDATE ProjectNotes
           SET ${setClause}
           WHERE ID = $1`,
          [id, ...values]
      )
      return true
    } catch (e) {
      await error(`Error updating project note: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to update project note',
        cause: e
      }))
      return false
    }
  }

  const deleteProjectNote = async (id: number): Promise<boolean> => {
    try {
      await dbStore.executeQuery('DELETE FROM ProjectNotes WHERE ID = $1', [id])
      return true
    } catch (e) {
      await error(`Error deleting project note: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to delete project note',
        cause: e
      }))
      return false
    }
  }
  // Project Metrics CRUD operations
  const getProjectMetrics = async (projectID: number): Promise<ProjectMetric[]> => {
    try {
      const result = await dbStore.select<any[]>(
          'SELECT * FROM ProjectMetrics WHERE ProjectID = $1 ORDER BY CreatedAt DESC',
          [projectID]
      )
      return result.map(row => ({
        id: row.ID,
        projectID: row.ProjectID,
        name: row.Name,
        description: row.Description,
        target: row.Target,
        currentValue: row.CurrentValue,
        createdAt: row.CreatedAt,
        updatedAt: row.UpdatedAt
      }))
    } catch (e) {
      await error(`Error fetching project metrics: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to fetch project metrics',
        cause: e
      }))
      return []
    }
  }

  const createProjectMetric = async (metric: ProjectMetricRequest): Promise<number | null> => {
    try {
      const result = await dbStore.executeQuery(
          'INSERT INTO ProjectMetrics (ProjectID, Name, Description, Target, CurrentValue) VALUES ($1, $2, $3, $4, $5)',
          [metric.projectID, metric.name, metric.description, metric.target, metric.currentValue]
      )
      return result.lastInsertId as number
    } catch (e) {
      await error(`Error creating project metric: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to create project metric',
        cause: e
      }))
      return null
    }
  }

  const updateProjectMetric = async (id: number, metric: Partial<ProjectMetricRequest>): Promise<boolean> => {
    try {
      const setClause = Object.keys(metric).map((key, index) => `${key} = $${index + 2}`).join(', ')
      const values = Object.values(metric)
      await dbStore.executeQuery(
          `UPDATE ProjectMetrics
           SET ${setClause}
           WHERE ID = $1`,
          [id, ...values]
      )
      return true
    } catch (e) {
      await error(`Error updating project metric: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to update project metric',
        cause: e
      }))
      return false
    }
  }

  const deleteProjectMetric = async (id: number): Promise<boolean> => {
    try {
      await dbStore.executeQuery('DELETE FROM ProjectMetrics WHERE ID = $1', [id])
      return true
    } catch (e) {
      await error(`Error deleting project metric: ${e}`)
      handleError(new DatabaseError({
        name: 'DB_QUERY_ERROR',
        message: 'Failed to delete project metric',
        cause: e
      }))
      return false
    }
  }

  return {
    getProjectById,
    getProjects,
    createProject,
    updateProject,
    deleteProject,
    getProjectGoals,
    createProjectGoal,
    deleteProjectAsset,
    updateProjectGoal,
    deleteProjectGoal,
    getProjectResources,
    createProjectResource,
    updateProjectResource,
    deleteProjectResource,
    getProjectAssets,
    createProjectAsset,
    updateProjectAsset,
    getProjectNotes,
    createProjectNote,
    updateProjectNote,
    deleteProjectNote,
    getProjectMetrics,
    createProjectMetric,
    updateProjectMetric,
    deleteProjectMetric,
  }
})