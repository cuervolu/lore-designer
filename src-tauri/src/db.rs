use tauri_plugin_sql::{Builder as SqlBuilder, Migration, MigrationKind};

fn get_migrations() -> Vec<Migration> {
    vec![
        Migration {
            version: 1,
            description: "create_characters_table",
            sql: "CREATE TABLE Characters (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                Name TEXT NOT NULL,
                Description TEXT,
                Role TEXT CHECK(Role IN ('Primary', 'Secondary', 'Tertiary', 'Undefined')),
                ImageID TEXT,
                AdditionalNotes TEXT,
                CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (ImageID) REFERENCES Images(UUID) ON DELETE SET NULL
            );",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 2,
            description: "create_images_table",
            sql: "CREATE TABLE Images (
                UUID TEXT PRIMARY KEY,
                Path TEXT NOT NULL,
                CharacterID INTEGER,
                Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (CharacterID) REFERENCES Characters(ID) ON DELETE SET NULL
            );",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 3,
            description: "create_indexes",
            sql: "CREATE INDEX IF NOT EXISTS idx_characters_role ON Characters(Role);
                  CREATE INDEX IF NOT EXISTS idx_characters_created_at ON Characters(CreatedAt);
                  CREATE INDEX IF NOT EXISTS idx_characters_updated_at ON Characters(UpdatedAt);",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 4,
            description: "add_unique_constraint",
            sql: "CREATE UNIQUE INDEX IF NOT EXISTS idx_characters_name ON Characters(Name);",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 5,
            description: "update_characters_timestamps",
            sql: "CREATE TRIGGER update_characters_timestamp 
                    AFTER UPDATE ON Characters
                    BEGIN
                        UPDATE Characters SET UpdatedAt = CURRENT_TIMESTAMP WHERE ID = NEW.ID;
                    END;",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 6,
            description: "create_images_indexes",
            sql: "CREATE INDEX idx_images_path ON Images(Path);
                CREATE INDEX idx_images_character_id ON Images(CharacterID);
                CREATE INDEX idx_images_timestamp ON Images(Timestamp);",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 7,
            description: "update_images_timestamp_trigger",
            sql: "CREATE TRIGGER update_images_timestamp 
                    AFTER UPDATE ON Images
                    BEGIN
                        UPDATE Images SET Timestamp = CURRENT_TIMESTAMP WHERE UUID = NEW.UUID;
                    END;",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 8,
            description: "create_projects_table",
            sql: "CREATE TABLE Projects (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                Name TEXT NOT NULL UNIQUE,
                Description TEXT,
                ImageID TEXT,
                CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (ImageID) REFERENCES Images(UUID) ON DELETE SET NULL
            );",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 9,
            description: "create_project_character_relation",
            sql: "CREATE TABLE ProjectCharacters (
                ProjectID INTEGER,
                CharacterID INTEGER,
                PRIMARY KEY (ProjectID, CharacterID),
                FOREIGN KEY (ProjectID) REFERENCES Projects(ID) ON DELETE CASCADE,
                FOREIGN KEY (CharacterID) REFERENCES Characters(ID) ON DELETE CASCADE
            );",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 10,
            description: "update_projects_timestamp_trigger",
            sql: "CREATE TRIGGER update_projects_timestamp 
                AFTER UPDATE ON Projects
                BEGIN
                    UPDATE Projects SET UpdatedAt = CURRENT_TIMESTAMP WHERE ID = NEW.ID;
                END;",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 11,
            description: "create_projects_indexes",
            sql: "CREATE INDEX idx_projects_created_at ON Projects(CreatedAt);
                CREATE INDEX idx_projects_updated_at ON Projects(UpdatedAt);",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 12,
            description: "add_budget_to_projects",
            sql: "ALTER TABLE Projects ADD COLUMN Budget REAL;",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 13,
            description: "create_project_goals_table",
            sql: "CREATE TABLE ProjectGoals (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                ProjectID INTEGER,
                Description TEXT NOT NULL,
                IsCompleted BOOLEAN DEFAULT FALSE,
                DueDate DATE,
                CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (ProjectID) REFERENCES Projects(ID) ON DELETE CASCADE
            );",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 14,
            description: "create_project_resources_table",
            sql: "CREATE TABLE ProjectResources (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                ProjectID INTEGER,
                Name TEXT NOT NULL,
                Type TEXT NOT NULL,
                Description TEXT,
                Quantity INTEGER,
                CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (ProjectID) REFERENCES Projects(ID) ON DELETE CASCADE
            );",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 15,
            description: "create_project_assets_table",
            sql: "CREATE TABLE ProjectAssets (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                ProjectID INTEGER,
                Name TEXT NOT NULL,
                Type TEXT NOT NULL,
                Status TEXT CHECK(Status IN ('Pending', 'InProgress', 'Completed')) DEFAULT 'Pending',
                FilePath TEXT,
                CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (ProjectID) REFERENCES Projects(ID) ON DELETE CASCADE
            );",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 16,
            description: "create_project_notes_table",
            sql: "CREATE TABLE ProjectNotes (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                ProjectID INTEGER,
                Title TEXT NOT NULL,
                Content TEXT,
                CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (ProjectID) REFERENCES Projects(ID) ON DELETE CASCADE
            );",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 17,
            description: "create_project_metrics_table",
            sql: "CREATE TABLE ProjectMetrics (
                ID INTEGER PRIMARY KEY AUTOINCREMENT,
                ProjectID INTEGER,
                Name TEXT NOT NULL,
                Description TEXT,
                Target REAL,
                CurrentValue REAL,
                CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (ProjectID) REFERENCES Projects(ID) ON DELETE CASCADE
            );",
            kind: MigrationKind::Up,
        },
    ]
}

pub fn init_sql_plugin() -> tauri_plugin_sql::Builder {
    let migrations = get_migrations();
    SqlBuilder::default().add_migrations("sqlite:loredesigner.db", migrations)
}
