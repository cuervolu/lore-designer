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
    ]
}

pub fn init_sql_plugin() -> tauri_plugin_sql::Builder {
    let migrations = get_migrations();
    SqlBuilder::default().add_migrations("sqlite:loredesigner.db", migrations)
}
