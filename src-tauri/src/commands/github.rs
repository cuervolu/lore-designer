use anyhow::Result;
use serde_json::Value;
use crate::error::AppError;

#[tauri::command]
pub async fn get_release_notes() -> Result<String, AppError> {
    let client = reqwest::Client::new();
    let res = client
        .get("https://api.github.com/repos/cuervolu/lore-designer/releases/latest")
        .header("User-Agent", "request")
        .send()
        .await
        .map_err(|e| AppError::GithubError(e.to_string()))?;

    let json: Value = res.json()
        .await
        .map_err(|e| AppError::GithubError(e.to_string()))?;

    let body = json["body"]
        .as_str()
        .unwrap_or("No release notes available.")
        .to_string();

    Ok(body)
}