# Lore Designer

![Status](https://img.shields.io/badge/Status-Pre--Alpha-orange?style=flat-square)
![License](https://img.shields.io/badge/License-AGPL--3.0-blue?style=flat-square)

<div align="center">
  <img src="app_icon.webp" alt="Lore Designer Logo" width="128" height="128">

<h3>Design Narratives, Build Legends</h3>

  <p>
    <strong>A desktop application for building worlds, characters, relationships, and complex stories.</strong>
  </p>
</div>

## About

Lore Designer is a desktop application for writers, indie game developers, narrative designers, and worldbuilders working on stories that grow beyond a handful of notes.

It is designed to provide useful structure without forcing you to build your own organization system first. Characters, locations, factions, relationships, properties, and views can work together while the underlying project remains yours.

Lore Designer is not intended to be a generic Markdown editor, a publishing platform, or simply another personal knowledge management tool. Its goal is to help creators reason about and develop complex fictional works over time.

## Early Development

Lore Designer is currently in **pre-alpha** development.

The application is not ready for regular use yet. Features, file formats, and workflows may change while the core of the application is being built.

## Principles

### Your files belong to you

A Lore Designer workspace is a normal folder on your computer.

Documents are stored as Markdown, while project metadata uses open, human-readable formats such as TOML, JSON, and YAML. A project can be copied, moved, backed up, synchronized, or versioned without depending on a proprietary database as its source of truth.

Lore Designer may maintain local indexes and caches for performance, but these are derived data and can be rebuilt from the workspace.

### Structure without rigidity

Lore Designer provides useful concepts such as Characters, Locations, Factions, Properties, and Relationships without forcing every project into the same shape.

Built-in structures can be adapted, and creators will be able to define Types that better represent their own worlds.

### Meaningful relationships

Relationships are more than links between pages.

A character can belong to a faction, live in a location, fear another character, or reference any other meaningful concept defined by the project. These relationships can then be explored through different views of the same underlying content.

### Complexity stays out of the way

Lore Designer is designed primarily for creators, not for people who want to manage schemas, databases, IDs, or configuration files.

The technical structure exists to keep projects portable and reliable, but it should not become part of the normal creative workflow.

## Workspaces

Each Lore Designer project is a self-contained workspace.

A typical workspace may look like:

```text
My World/
├── project.lore
├── .loreignore
├── Characters/
│   └── Arin.md
├── Locations/
│   └── Aurelia.md
└── .lore/
    ├── types.json
    ├── templates/
    ├── views/
    └── boards/
```

`project.lore` identifies the workspace and stores its essential project-level information.

Narrative documents remain normal Markdown files. Structured information associated with those documents is stored alongside the content in a form that can still be inspected outside Lore Designer.

The `.lore/` directory contains persistent project metadata such as Types, templates, saved views, and boards. It is part of the workspace and should travel with it.

Caches, indexes, logs, and machine-specific application state are stored outside the workspace.

## Planned Direction

The first version of Lore Designer is focused on establishing the core workflow around:

* workspaces backed by real files and folders
* Markdown documents with structured properties
* customizable Types such as Characters, Locations, and Factions
* semantic relationships between documents
* templates for creating structured content
* dynamic views for exploring project information
* a modern visual Markdown editing experience
* safe handling of changes made outside Lore Designer

Additional features will be considered once this foundation is solid.

## Contributing

Lore Designer is still in an early stage and its internal architecture is actively evolving.

Contributions and feedback are welcome. A dedicated contribution guide with development setup, architecture notes, and contribution conventions will be added as the project becomes ready for broader collaboration.

## License

Lore Designer is licensed under the [GNU Affero General Public License v3.0](LICENSE).

## Special Thanks

**[@iriata18](https://instagram.com/iriata18)** for the Lore Designer application logo.
