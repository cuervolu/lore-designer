use serde::{Deserialize, Serialize};

pub const WORKSPACE_VERSION: u32 = 1;

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateWorkspaceRequest {
    pub name: String,
    #[serde(alias = "path")]
    pub parent_path: String,
    pub template_id: String,
    pub app_version: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct CreateWorkspaceResult {
    pub name: String,
    pub root_path: String,
    pub manifest_path: String,
    pub created_at: String,
    pub template_id: String,
    pub workspace_version: WorkspaceVersion,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct WorkspaceTemplateSummary {
    pub id: String,
    pub display_name: String,
    pub description: String,
    pub supports_creation: bool,
    pub is_builtin: bool,
    pub sort_order: u16,
}

#[derive(Debug, Clone, Copy, Serialize, Deserialize, PartialEq, Eq)]
#[serde(rename_all = "camelCase")]
pub struct WorkspaceVersion {
    pub major: u32,
}

impl Default for WorkspaceVersion {
    fn default() -> Self {
        Self {
            major: WORKSPACE_VERSION,
        }
    }
}
