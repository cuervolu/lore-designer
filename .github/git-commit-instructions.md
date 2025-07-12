## Commit Format
Use the following format for all commits:
```
<type>(<scope>): <description>

[optional body]

[optional footer(s)]
```

## Types
- **feat**: A new feature
- **fix**: A bug fix  
- **docs**: Documentation only changes
- **style**: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **perf**: A code change that improves performance
- **test**: Adding missing tests or correcting existing tests
- **chore**: Changes to the build process or auxiliary tools and libraries such as documentation generation

## Scopes
Use the following scopes based on the affected crate or functionality:
- **workspace**: Changes to lore-workspaces crate
- **editor**: Changes to lore-editor crate  
- **test-utils**: Changes to lore-test-utils crate
- **ui**: User interface changes
- **api**: API changes
- **config**: Configuration changes
- **deps**: Dependency updates
- **ci**: Continuous integration changes

## Guidelines
- Use present tense ("add feature" not "added feature")
- Use imperative mood ("move cursor to..." not "moves cursor to...")
- Limit first line to 72 characters
- Reference issues and pull requests liberally after the first line
- Be descriptive but concise

## Examples

### Features
```
feat(editor): add syntax highlighting for dialogue files
feat(workspace): implement workspace manifest validation
feat(ui): add file type icons in file tree
```

### Bug Fixes  
```
fix(editor): resolve crash when opening corrupted canvas files
fix(workspace): handle invalid characters in workspace names
fix(api): correct file path resolution on Windows
```

### Chores
```
chore(deps): update tauri to v2.1.0
chore(workspace): update Cargo.lock dependencies
chore(ci): add automated testing workflow
```

### Documentation
```
docs(editor): add JSDoc comments to file type detection
docs(workspace): update README with new manifest format
docs(api): document FileType enum variants
```

### Refactoring
```
refactor(editor): extract file content parsing to separate module
refactor(workspace): simplify directory creation logic
refactor(test-utils): improve random file generation performance
```

### Performance
```
perf(editor): optimize file indexing for large workspaces  
perf(workspace): cache manifest parsing results
```

### Tests
```
test(editor): add unit tests for FileType detection
test(workspace): add integration tests for workspace creation
```

### Style
```
style(editor): format code with rustfmt
style(workspace): fix clippy warnings
```

