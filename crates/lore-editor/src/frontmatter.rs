use yaml_rust2::YamlLoader;

pub fn extract_frontmatter(text: &str) -> (Option<String>, String) {
    // Check if the text starts with "---" (YAML frontmatter delimiter)
    if text.starts_with("---") {
        if let Some(end_index) = text[3..].find("---") {
            let frontmatter = text[3..(end_index + 3)].trim();
            let content = text[(end_index + 6)..].trim();

            match YamlLoader::load_from_str(frontmatter) {
                Ok(_) => return (Some(frontmatter.to_string()), content.to_string()),
                Err(_) => {}
            }
        }
    }

    (None, text.to_string())
}

pub fn combine_frontmatter_and_content(frontmatter: &str, content: &str) -> String {
    format!("---\n{}\n---\n\n{}", frontmatter, content)
}
