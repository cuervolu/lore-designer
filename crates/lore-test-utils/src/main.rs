use clap::Parser;
use indicatif::{ProgressBar, ProgressStyle};
use log::{debug, info};
use rand::{Rng, SeedableRng};
use rand_chacha::ChaCha8Rng; 
use std::{
    fs::{self},
    io::{self},
    path::{Path, PathBuf},
    time::Instant,
};
use tempfile::TempDir;

use fs_extra::dir as fs_extra_dir;
use fs_extra::file as fs_extra_file;
use rand::seq::IndexedRandom;
// const SPECIAL_FOLDERS: &[&str] = &["characters", "lore", "locations", "story", "notes"];

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
enum GeneratedFileType {
    Markdown,
    Canvas,
    Character,
    Location,
    Lore,
    DialogueMd,
    DialogueJson,
    Image,
    GenericJson,
}

impl GeneratedFileType {
    fn get_extension_and_name(&self, base_name: &str) -> (String, String) {
        match self {
            GeneratedFileType::Markdown => (format!("{}.md", base_name), "md".to_string()),
            GeneratedFileType::Canvas => (format!("{}.canvas.json", base_name), "canvas.json".to_string()),
            GeneratedFileType::Character => (format!("{}.character.md", base_name), "character.md".to_string()),
            GeneratedFileType::Location => (format!("{}.location.md", base_name), "location.md".to_string()),
            GeneratedFileType::Lore => (format!("{}.lore.md", base_name), "lore.md".to_string()),
            GeneratedFileType::DialogueMd => (format!("{}.dialogue.md", base_name), "dialogue.md".to_string()),
            GeneratedFileType::DialogueJson => (format!("{}.dialogue.json", base_name), "dialogue.json".to_string()),
            GeneratedFileType::Image => (format!("{}.png", base_name), "png".to_string()),
            GeneratedFileType::GenericJson => (format!("{}.json", base_name), "json".to_string()),
        }
    }

    fn needs_frontmatter(&self) -> bool {
        matches!(self, GeneratedFileType::Character | GeneratedFileType::Location | GeneratedFileType::Lore)
    }
}


#[derive(Parser, Debug)]
#[clap(author, version, about = "Generates a large, temporary workspace for Lore Designer performance testing.")]
struct Args {
    /// Name of the workspace to create (e.g., MyBigNovel).
    #[clap(long)]
    workspace_name: String,

    /// Number of files to generate in total.
    #[clap(long, default_value_t = 1000)]
    files: usize,

    /// Maximum depth of the directory structure.
    #[clap(long, default_value_t = 5)]
    max_depth: usize,

    /// Average size of each generated text file in kilobytes.
    #[clap(long, default_value_t = 10)]
    avg_size_kb: usize,

    /// Percentage chance to create a subdirectory instead of a file at a given level.
    #[clap(long, default_value_t = 30)]
    dir_chance_percent: u8,

    /// Optional path to persist the generated workspace for inspection.
    /// If not provided, the workspace is purely temporary.
    #[clap(long)]
    output_path: Option<PathBuf>,

    /// Base name for generated files and directories (e.g., chapter, scene).
    #[clap(long, default_value = "item")]
    base_name: String,

    /// App version to write into .lore/settings.json
    #[clap(long, default_value = "0.1.0-test")]
    app_version: String,

    /// Optional seed for the random number generator for reproducible results.
    #[clap(long)]
    seed: Option<u64>,
}

const LOREM_IPSUM: &[&str] = &[
    "lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit",
    "sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore", "et", "dolore",
    "magna", "aliqua", "enim", "ad", "minim", "veniam", "quis", "nostrud", "exercitation",
];

fn generate_lipsum_text(size_kb: usize, rng: &mut impl Rng) -> String {
    let target_bytes = size_kb * 1024;
    if target_bytes == 0 {
        return String::new();
    }

    let mut text = String::with_capacity(target_bytes + 100); 
    let mut current_sentence_len = 0;
    let mut capitalize_next = true;

    while text.len() < target_bytes {
        if let Some(word) = LOREM_IPSUM.choose(rng) {
            let mut current_word = word.to_string();
            if capitalize_next {
                if let Some(first_char) = current_word.chars().next() {
                    current_word = first_char.to_uppercase().to_string() + &current_word[first_char.len_utf8()..];
                }
                capitalize_next = false;
            }

            if !text.is_empty() {
                text.push(' ');
            }
            text.push_str(&current_word);
            current_sentence_len += 1;

            // Lógica simple para terminar frases
            if current_sentence_len >= rng.random_range(8..=20) { // Usa random_range de tu rand 0.9.1
                let punctuation = [".", ".", ".", "!", "?"]; // Más probabilidad de punto
                text.push_str(punctuation.choose(rng).unwrap_or(&"."));
                capitalize_next = true;
                current_sentence_len = 0;
            }
        } else {
            break; 
        }
    }
    
    if !text.is_empty() && !text.ends_with(['.', '!', '?']) {
        text.push('.');
    }
    
    if text.len() > target_bytes {
        text.truncate(target_bytes);
    }
    text
}

fn generate_frontmatter(title: &str, rng: &mut impl Rng) -> String {
    let tags = ["epic", "draft", "important", "plot-point", "character-arc", "world-building"];
    let num_tags = rng.random_range(1..=3);
    let selected_tags: Vec<&str> = tags.choose_multiple(rng, num_tags).copied().collect();

    let timestamp = chrono::Utc::now().to_rfc3339(); // Assuming chrono is in workspace deps
    
    let num_words_for_custom_field = rng.random_range(5..=15);
    let mut custom_value_parts = Vec::with_capacity(num_words_for_custom_field);
    for _ in 0..num_words_for_custom_field {
        if let Some(word) = LOREM_IPSUM.choose(rng) {
            custom_value_parts.push(*word);
        }
    }
    let custom_value = custom_value_parts.join(" ");

    format!(
        "---\n\
        title: \"{}\"\n\
        tags: [{}]\n\
        created_at: \"{}\"\n\
        custom_field: \"{}\"\n\
        ---\n",
        title,
        selected_tags.iter().map(|t| format!("\"{}\"", t)).collect::<Vec<_>>().join(", "),
        timestamp,
        custom_value // Now uses the DIY generated text
    )
}

fn create_initial_workspace_structure(
    workspace_root: &Path,
    _workspace_name: &str, 
    app_version: &str,
) -> io::Result<()> {
    let lore_dir = workspace_root.join(".lore");
    fs::create_dir_all(&lore_dir)?;

    let settings_path = lore_dir.join("settings.json");
    // Usamos serde_json del workspace
    let settings_content = serde_json::json!({
        "version": app_version,
        "properties": {
            "editorFontSize": 14,
            "theme": "system",
        },
        "lastOpened": chrono::Utc::now().to_rfc3339(), 
    });
    fs::write(
        settings_path,
        serde_json::to_string_pretty(&settings_content)
            .map_err(|e| io::Error::new(io::ErrorKind::Other, e))?, 
    )?;

    debug!("Created initial .lore structure in {}", workspace_root.display());
    Ok(())
}

fn generate_workspace_recursive(
    current_path: &Path,
    current_depth: usize,
    args: &Args,
    rng: &mut impl Rng,
    files_created_count: &mut usize,
    pb: &ProgressBar,
) -> io::Result<()> {
    if current_depth >= args.max_depth || *files_created_count >= args.files {
        return Ok(());
    }

    let max_items_here = if args.files - *files_created_count > 0 {
        rng.random_range(1..=std::cmp::min(10, (args.files - *files_created_count).max(1)))
    } else {
        0
    };

    for i in 0..max_items_here {
        if *files_created_count >= args.files {
            break;
        }

        let item_base_name = format!("{}_{}_{}", args.base_name, current_depth, i);

        if current_depth < args.max_depth - 1 && rng.random_range(0..100) < args.dir_chance_percent {
            let dir_name = item_base_name; // Simplificado, podrías reintroducir SPECIAL_FOLDERS aquí
            let new_dir_path = current_path.join(dir_name);
            fs::create_dir_all(&new_dir_path)?;
            generate_workspace_recursive(&new_dir_path, current_depth + 1, args, rng, files_created_count, pb)?;
        } else {
            let file_types = [
                GeneratedFileType::Markdown, GeneratedFileType::Canvas, GeneratedFileType::Character,
                GeneratedFileType::Location, GeneratedFileType::Lore, GeneratedFileType::DialogueMd,
            ];
            let chosen_file_type = file_types.choose(rng).unwrap_or(&GeneratedFileType::Markdown);
            let (file_name_with_ext, _) = chosen_file_type.get_extension_and_name(&item_base_name);
            let file_path = current_path.join(&file_name_with_ext);

            let mut file_content_parts = Vec::new();

            if chosen_file_type.needs_frontmatter() {
                file_content_parts.push(generate_frontmatter(&item_base_name, rng));
            }

            match chosen_file_type {
                GeneratedFileType::Canvas | GeneratedFileType::DialogueJson | GeneratedFileType::GenericJson => {
                    // Usamos uuid del workspace
                    let json_data = serde_json::json!({
                        "id": uuid::Uuid::new_v4().to_string(),
                        "type": format!("{:?}", chosen_file_type),
                        "nodes": [{"id": "node_1", "x": rng.random_range(0..800), "y": rng.random_range(0..600)}],
                        "payload": generate_lipsum_text(1, rng)
                    });
                    file_content_parts.push(serde_json::to_string_pretty(&json_data)
                        .map_err(|e| io::Error::new(io::ErrorKind::Other, e))?);
                }
                GeneratedFileType::Image => {
                    file_content_parts.push("Placeholder for a PNG image.".to_string());
                }
                _ => {
                    let text_size_kb = rng.random_range((args.avg_size_kb / 2).max(1)..=args.avg_size_kb + (args.avg_size_kb / 2));
                    file_content_parts.push(generate_lipsum_text(text_size_kb, rng));
                }
            }

            let final_content = file_content_parts.join("\n"); 
            fs::write(&file_path, final_content)?;
            *files_created_count += 1;
            pb.inc(1);
            pb.set_message(format!("Created {}", file_path.file_name().unwrap_or_default().to_string_lossy()));
        }
    }
    Ok(())
}

fn main() -> io::Result<()> {
    env_logger::init();

    let args = Args::parse();
    let start_time = Instant::now();
    
    let mut thread_rng_instance = rand::rng(); 
    let mut rng: ChaCha8Rng = if let Some(seed) = args.seed {
        SeedableRng::seed_from_u64(seed)
    } else {
        SeedableRng::from_rng(&mut thread_rng_instance) 
    };


    let (workspace_base_path, _temp_dir_guard): (PathBuf, Option<TempDir>) =
        if let Some(ref output_path_arg) = args.output_path {
            let abs_output_path = if output_path_arg.is_absolute() {
                output_path_arg.clone()
            } else {
                std::env::current_dir()?.join(output_path_arg)
            };

            if abs_output_path.exists() {
                info!("Output path {:?} already exists. Removing it.", abs_output_path);
                if abs_output_path.is_dir() {
                    fs_extra_dir::remove(&abs_output_path)
                        .map_err(|e| io::Error::new(io::ErrorKind::Other, e))?;
                } else {
                    fs_extra_file::remove(&abs_output_path)
                        .map_err(|e| io::Error::new(io::ErrorKind::Other, e))?;
                }
            }
            fs::create_dir_all(&abs_output_path)?;
            (abs_output_path, None)
        } else {
            let temp_dir = TempDir::new()?;
            (temp_dir.path().to_path_buf(), Some(temp_dir))
        };

    let actual_workspace_path = workspace_base_path.join(&args.workspace_name);
    fs::create_dir_all(&actual_workspace_path)?;

    info!(
        "Generating workspace '{}' (target {} files, max depth {}, avg size {}KB).",
        args.workspace_name, args.files, args.max_depth, args.avg_size_kb
    );
    if let Some(seed) = args.seed {
        info!("Using RNG seed: {}", seed);
    }
    info!("Workspace location: {:?}", actual_workspace_path.display());

    let pb = ProgressBar::new(args.files as u64);
    pb.set_style(ProgressStyle::default_bar()
        .template("{spinner:.green} [{elapsed_precise}] [{bar:40.cyan/blue}] {pos}/{len} ({eta}) {msg}")
        .unwrap()
        .progress_chars("=>-"));

    let mut files_actually_created = 0;

    create_initial_workspace_structure(&actual_workspace_path, &args.workspace_name, &args.app_version)?;

    generate_workspace_recursive(
        &actual_workspace_path,
        0,
        &args,
        &mut rng, 
        &mut files_actually_created,
        &pb,
    )?;

    pb.finish_with_message(format!("Generated {} files.", files_actually_created));

    if args.output_path.is_none() {
        info!("\nWorkspace generated in temporary directory: {:?}", actual_workspace_path.display());
        info!("This directory will be automatically deleted when this program exits.");
        info!("To inspect, re-run with --output-path /path/to/inspect_dir");
    } else {
        info!("\nWorkspace generated at: {:?}", actual_workspace_path.display());
    }

    info!("Total generation time: {:?}", start_time.elapsed());
    Ok(())
}
