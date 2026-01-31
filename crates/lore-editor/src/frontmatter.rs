pub fn combine_frontmatter_and_content(frontmatter: &str, content: &str) -> String {
    if frontmatter.trim().is_empty() {
        content.to_string()
    } else {
        format!("---\n{}\n---\n\n{}", frontmatter.trim(), content.trim())
    }
}
