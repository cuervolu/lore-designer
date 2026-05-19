use lore_workspaces::{
    CreateWorkspaceRequest, CreateWorkspaceResult, WorkspaceTemplateSummary,
    create_workspace as create_workspace_impl, list_workspace_templates as list_templates_impl,
};

#[tauri::command]
pub fn list_workspace_templates() -> Result<Vec<WorkspaceTemplateSummary>, String> {
    Ok(list_templates_impl())
}

#[tauri::command]
pub fn create_workspace(request: CreateWorkspaceRequest) -> Result<CreateWorkspaceResult, String> {
    create_workspace_impl(request).map_err(|error| error.to_string())
}
