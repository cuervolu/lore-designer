use crate::image_manager::{BulkUpdateResult, ImageRegistry, ValidationReport};
use crate::WorkspaceError;
use serde::Serialize;
use tracing::{error, info};

#[derive(Serialize)]
pub struct ImageRegistrationResponse {
    pub success: bool,
    pub message: String,
}

/// Register a new image in the workspace index
#[tauri::command]
pub async fn register_image(
    workspace_path: String,
    relative_path: String,
    used_by_file: String,
) -> Result<ImageRegistrationResponse, WorkspaceError> {
    info!(
        "Registering image '{}' for file '{}'",
        relative_path, used_by_file
    );

    let registry = ImageRegistry::new(&workspace_path)
        .map_err(WorkspaceError::Creation)?;

    registry
        .register_image(relative_path.clone(), used_by_file)
        .await
        .map_err(|e| {
            error!("Failed to register image: {}", e);
            WorkspaceError::Creation(e)
        })?;

    Ok(ImageRegistrationResponse {
        success: true,
        message: format!("Image '{}' registered successfully", relative_path),
    })
}

/// Update image references for a specific file
#[tauri::command]
pub async fn update_image_references(
    workspace_path: String,
    file_path: String,
    image_paths: Vec<String>,
) -> Result<ImageRegistrationResponse, WorkspaceError> {
    info!(
        "Updating image references for '{}' with {} images",
        file_path,
        image_paths.len()
    );

    let registry = ImageRegistry::new(&workspace_path)
        .map_err(WorkspaceError::Creation)?;

    registry
        .update_image_references(file_path.clone(), image_paths)
        .map_err(|e| {
            error!("Failed to update image references: {}", e);
            WorkspaceError::Creation(e)
        })?;

    Ok(ImageRegistrationResponse {
        success: true,
        message: format!("Image references updated for '{}'", file_path),
    })
}

/// Validate the image index and return a report
#[tauri::command]
pub async fn validate_image_index(
    workspace_path: String,
) -> Result<ValidationReport, WorkspaceError> {
    info!("Validating image index for workspace '{}'", workspace_path);

    let registry = ImageRegistry::new(&workspace_path)
        .map_err(WorkspaceError::Creation)?;

    let report = registry.validate_index().map_err(|e| {
        error!("Failed to validate image index: {}", e);
        WorkspaceError::Creation(e)
    })?;

    info!(
        "Validation complete: {} total, {} missing, {} orphaned",
        report.total_images,
        report.missing_images.len(),
        report.orphaned_images.len()
    );

    Ok(report)
}

/// Find candidate images by hash for relocated images
#[tauri::command]
pub async fn find_image_candidates(
    workspace_path: String,
    missing_hash: String,
) -> Result<Vec<String>, WorkspaceError> {
    info!("Finding candidates for hash '{}'", missing_hash);

    let registry = ImageRegistry::new(&workspace_path)
        .map_err(WorkspaceError::Creation)?;

    let candidates = registry
        .find_image_candidates(missing_hash)
        .await
        .map_err(|e| {
            error!("Failed to find image candidates: {}", e);
            WorkspaceError::Creation(e)
        })?;

    info!("Found {} candidates", candidates.len());
    Ok(candidates)
}

/// Bulk update image path after move/rename
#[tauri::command]
pub async fn bulk_update_image_path(
    workspace_path: String,
    old_path: String,
    new_path: String,
) -> Result<BulkUpdateResult, WorkspaceError> {
    info!("Bulk updating image path from '{}' to '{}'", old_path, new_path);

    let registry = ImageRegistry::new(&workspace_path)
        .map_err(WorkspaceError::Creation)?;

    let result = registry
        .bulk_update_image_path(old_path, new_path)
        .map_err(|e| {
            error!("Failed to bulk update image path: {}", e);
            WorkspaceError::Creation(e)
        })?;

    info!(
        "Bulk update complete: {} updated, {} failed",
        result.updated_files.len(),
        result.failed_files.len()
    );

    Ok(result)
}