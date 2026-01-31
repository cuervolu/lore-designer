use serde::{Deserialize, Serialize};
use serde_json::Value;
use std::path::{Path, PathBuf};
use tokio::fs;

#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(rename_all = "camelCase")]
pub enum FieldType {
    Text,
    Number,
    Textarea,
    Select,
    Multiselect,
    Image,
    Date,
    CustomProperties,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(rename_all = "camelCase")]
pub struct FieldDefinition {
    pub key: String,
    pub label: String,
    #[serde(rename = "type")]
    pub field_type: FieldType,
    #[serde(default)]
    #[serde(skip_serializing_if = "std::ops::Not::not")]
    pub required: bool,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub default: Option<Value>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub options: Option<Vec<String>>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub placeholder: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    pub rows: Option<u32>,
}

// NUEVO: struct FormSection
#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(rename_all = "camelCase")]
pub struct FormSection {
    pub key: String,
    pub fields: Vec<FieldDefinition>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
#[serde(rename_all = "camelCase")]
pub struct FormConfig {
    pub sections: Vec<FormSection>,
}

fn get_config_path(workspace_path: &Path) -> PathBuf {
    workspace_path
        .join(".lore")
        .join("character_form_config.toml")
}

fn default_character_config() -> FormConfig {
    FormConfig {
        sections: vec![
            FormSection {
                key: "hero".to_string(),
                fields: vec![
                    FieldDefinition {
                        key: "name".to_string(),
                        label: "Name".to_string(),
                        field_type: FieldType::Text,
                        required: true,
                        default: Some(Value::String("New Character".to_string())),
                        options: None,
                        placeholder: Some("Character Name".to_string()),
                        rows: None,
                    },
                    FieldDefinition {
                        key: "aliases".to_string(),
                        label: "Aliases".to_string(),
                        field_type: FieldType::Multiselect,
                        required: false,
                        default: Some(Value::Array(vec![])),
                        options: None,
                        placeholder: Some("+ Add Alias".to_string()),
                        rows: None,
                    },
                ],
            },
            FormSection {
                key: "sidebar".to_string(),
                fields: vec![
                    FieldDefinition {
                        key: "avatar".to_string(),
                        label: "Avatar".to_string(),
                        field_type: FieldType::Image,
                        required: false,
                        default: None,
                        options: None,
                        placeholder: None,
                        rows: None,
                    },
                ],
            },
            FormSection {
                key: "grid".to_string(),
                fields: vec![
                    FieldDefinition {
                        key: "age".to_string(),
                        label: "Age:".to_string(),
                        field_type: FieldType::Number,
                        required: false,
                        default: None,
                        options: None,
                        placeholder: Some("15".to_string()),
                        rows: None,
                    },
                    FieldDefinition {
                        key: "family".to_string(),
                        label: "Family:".to_string(),
                        field_type: FieldType::Text,
                        required: false,
                        default: None,
                        options: None,
                        placeholder: Some("Select Family...".to_string()),
                        rows: None,
                    },
                    FieldDefinition {
                        key: "role".to_string(),
                        label: "Role:".to_string(),
                        field_type: FieldType::Text,
                        required: false,
                        default: None,
                        options: None,
                        placeholder: Some("e.g., protagonist".to_string()),
                        rows: None,
                    },
                    FieldDefinition {
                        key: "status".to_string(),
                        label: "Status:".to_string(),
                        field_type: FieldType::Select,
                        required: false,
                        default: Some(Value::String("Alive".to_string())),
                        options: Some(vec!["Alive".to_string(), "Deceased".to_string(), "Unknown".to_string()]),
                        placeholder: Some("Select status...".to_string()),
                        rows: None,
                    },
                    FieldDefinition {
                        key: "gender".to_string(),
                        label: "Gender:".to_string(),
                        field_type: FieldType::Text,
                        required: false,
                        default: None,
                        options: None,
                        placeholder: Some("Gender".to_string()),
                        rows: None,
                    },
                    FieldDefinition {
                        key: "species".to_string(),
                        label: "Species:".to_string(),
                        field_type: FieldType::Text,
                        required: false,
                        default: None,
                        options: None,
                        placeholder: Some("e.g., human, elf".to_string()),
                        rows: None,
                    },
                    FieldDefinition {
                        key: "occupation".to_string(),
                        label: "Occupation:".to_string(),
                        field_type: FieldType::Text,
                        required: false,
                        default: None,
                        options: None,
                        placeholder: Some("Occupation".to_string()),
                        rows: None,
                    },
                ],
            },
            FormSection {
                key: "custom".to_string(),
                fields: vec![
                    FieldDefinition {
                        key: "customProperties".to_string(),
                        label: "Add Property".to_string(),
                        field_type: FieldType::CustomProperties,
                        required: false,
                        default: Some(Value::Array(vec![])),
                        options: None,
                        placeholder: None,
                        rows: None,
                    },
                ],
            },
        ],
    }
}

pub async fn get_config(workspace_path: &PathBuf) -> Result<FormConfig, String> {
    let config_path = get_config_path(workspace_path);

    if !config_path.exists() {
        let default_config = default_character_config();

        if let Err(e) = save_config(workspace_path, &default_config).await {
            return Err(format!("Failed to create default config file: {}", e));
        }

        return Ok(default_config);
    }

    let content = fs::read_to_string(&config_path)
        .await
        .map_err(|e| e.to_string())?;

    let config: FormConfig = toml::from_str(&content)
        .map_err(|e| e.to_string())?;

    Ok(config)
}

pub async fn save_config(workspace_path: &PathBuf, config: &FormConfig) -> Result<(), String> {
    let config_path = get_config_path(workspace_path);

    if let Some(parent) = config_path.parent() {
        if !parent.exists() {
            fs::create_dir_all(parent)
                .await
                .map_err(|e| e.to_string())?;
        }
    }

    let toml_string = toml::to_string_pretty(&config)
        .map_err(|e| e.to_string())?;

    fs::write(&config_path, toml_string)
        .await
        .map_err(|e| e.to_string())?;

    Ok(())
}