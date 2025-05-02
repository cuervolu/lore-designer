use super::{
    Editor, EditorError, EditorManager, EditorState, FileContent, FileSystemWatcher, FileTreeItem,
    FileType, IndexManager, IndexingProgress, SaveFileRequest, TabInfo, WorkspaceInfo,
};
use crate::templates::get_template_content;
use std::path::{Path, PathBuf};

#[tauri::command]
pub async fn open_workspace_in_editor(
    app: tauri::AppHandle,
    workspace_path: String,
) -> Result<WorkspaceInfo, EditorError> {
    EditorManager::open_workspace(&app, Path::new(&workspace_path))
}

#[tauri::command]
pub async fn get_workspace_file_tree(
    workspace_path: String,
) -> Result<Vec<FileTreeItem>, EditorError> {
    IndexManager::build_file_tree(Path::new(&workspace_path))
}

#[tauri::command]
pub async fn get_indexing_progress(workspace_path: String) -> Option<IndexingProgress> {
    IndexManager::get_indexing_progress(Path::new(&workspace_path))
}

#[tauri::command]
pub async fn get_file_content(
    workspace_path: String,
    file_path: String,
) -> Result<FileContent, EditorError> {
    EditorManager::get_file_content(workspace_path, file_path)
}

#[tauri::command]
pub async fn save_file_content(
    workspace_path: String,
    file_path: String,
    content: SaveFileRequest,
) -> Result<(), EditorError> {
    EditorManager::save_file_content(workspace_path, file_path, content)
}

#[tauri::command]
pub async fn load_editor_state(workspace_path: String) -> Result<EditorState, EditorError> {
    EditorState::load_or_create(Path::new(&workspace_path))
}

#[tauri::command]
pub async fn save_editor_state(state: EditorState) -> Result<(), EditorError> {
    state.save()
}

#[tauri::command]
pub async fn open_file_in_editor(
    workspace_path: String,
    file_path: String,
) -> Result<TabInfo, EditorError> {
    let mut state = EditorState::load_or_create(Path::new(&workspace_path))?;

    let file_path_buf = PathBuf::from(&file_path);
    let workspace_path_buf = PathBuf::from(&workspace_path);

    Editor::open_file(&workspace_path_buf, &file_path_buf, &mut state)
}

#[tauri::command]
pub async fn create_new_file(
    workspace_path: String,
    parent_dir: String,
    file_name: String,
    initial_content: String,
) -> Result<String, EditorError> {
    let workspace_path_buf = PathBuf::from(&workspace_path);
    let parent_dir_buf = PathBuf::from(&parent_dir);

    let file_path = Editor::create_file(
        &workspace_path_buf,
        &parent_dir_buf,
        &file_name,
        &initial_content,
    )?;

    let rel_path = file_path
        .strip_prefix(&workspace_path_buf)
        .map_err(|_| EditorError::InvalidPath(file_path.display().to_string()))?;

    Ok(rel_path.display().to_string())
}

#[tauri::command]
pub async fn get_welcome_text(workspace_name: String) -> String {
    Editor::get_welcome_text(&workspace_name)
}

#[tauri::command]
pub async fn stop_watching_workspace(workspace_path: String) -> Result<(), EditorError> {
    FileSystemWatcher::stop(Path::new(&workspace_path)).map_err(EditorError::Operation)
}

#[tauri::command]
pub async fn refresh_file_tree(workspace_path: String) -> Result<Vec<FileTreeItem>, EditorError> {
    // Reindex the workspace first
    IndexManager::start_indexing(Path::new(&workspace_path))?;

    // Then build and return the file tree
    IndexManager::build_file_tree(Path::new(&workspace_path))
}

#[tauri::command]
pub async fn create_file_from_template(
    workspace_path: String,
    parent_dir: String,
    file_name: String,
    file_type: FileType,
) -> Result<String, EditorError> {
    let workspace_path_buf = PathBuf::from(&workspace_path);
    let parent_dir_buf = PathBuf::from(&parent_dir);

    let template_content = get_template_content(&file_type, &file_name);

    let file_path = Editor::create_file(
        &workspace_path_buf,
        &parent_dir_buf,
        &file_name,
        &template_content,
    )?;

    let rel_path = file_path
        .strip_prefix(&workspace_path_buf)
        .map_err(|_| EditorError::InvalidPath(file_path.display().to_string()))?;

    Ok(rel_path.display().to_string())
}
