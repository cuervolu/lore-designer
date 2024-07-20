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
            ImageURL TEXT,
            AdditionalNotes TEXT,
            CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
            UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP
        );",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 2,
            description: "create_indexes",
            sql: "CREATE INDEX idx_characters_name ON Characters(Name);
                CREATE INDEX idx_characters_role ON Characters(Role);
                CREATE INDEX idx_characters_created_at ON Characters(CreatedAt);
                CREATE INDEX idx_characters_updated_at ON Characters(UpdatedAt);",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 3,
            description: "add_unique_constraint",
            sql: "CREATE UNIQUE INDEX idx_characters_name ON Characters(Name);",
            kind: MigrationKind::Up,
        },
        Migration {
            version: 4,
            description: "update_characters_timestamps",
            sql: "CREATE TRIGGER update_characters_timestamp 
                    AFTER UPDATE ON Characters
                    BEGIN
                        UPDATE Characters SET UpdatedAt = CURRENT_TIMESTAMP WHERE ID = NEW.ID;
                    END;",
            kind: MigrationKind::Up,
        },
    ]
}


pub fn init_sql_plugin() -> tauri_plugin_sql::Builder {
    let migrations = get_migrations();
    SqlBuilder::default().add_migrations("sqlite:loredesigner.db", migrations)
}
