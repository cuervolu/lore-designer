use chrono::{DateTime, Utc};

#[derive(Debug, Clone)]
pub struct Character {
    pub id: i32,
    pub name: String,
    pub description: Option<String>,
    pub role: Option<Role>,
    pub image_url: Option<String>,
    pub additional_notes: Option<String>,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}

#[derive(Debug, Clone, PartialEq)]
pub enum Role {
    Primary,
    Secondary,
    Tertiary,
    Undefined,
}