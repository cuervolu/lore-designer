use crate::FileType;

pub fn get_template_content(file_type: &FileType, name: &str) -> String {
    match file_type {
        FileType::Character => {
            let current_time = chrono::Local::now().to_rfc3339();
            format!(
                "---
name: {}
alias:
role:
status: active
age:
gender:
species: human
occupation:

# Physical attributes
appearance:
height:
weight:
distinguishing_features:

# Metadata
created: {}
updated: {}
version: 1.0
tags: []
---

## Biography

Write character biography here...

## Personality

Describe personality traits...

## Motivations

What drives this character...

## Arc

Character development throughout the story...",
                name, current_time, current_time
            )
        }
        FileType::Location => {
            let current_time = chrono::Local::now().to_rfc3339();
            format!(
                "---
name: {}
type:
region:
climate:
population:
governing_entity:
tags: []
coordinates:
connections: []

# Metadata
created: {}
updated: {}
---

## Description

Detailed description of the location...

## Notable Features

List and describe notable features...

## History

Historical information about this location...",
                name, current_time, current_time
            )
        }
        FileType::Lore => {
            let current_time = chrono::Local::now().to_rfc3339();
            format!(
                "---
title: {}
category:
era:
importance: medium
related_entities: []
tags: []
created: {}
updated: {}
---

## Overview

General overview of this lore element...

## Details

More detailed information...

## Significance

Why this matters in the world...",
                name, current_time, current_time
            )
        }
        _ => format!("# {}\n\nContent goes here...", name),
    }
}
