// src/locales/en.ts

export default {
  welcome: "Welcome to",
  loreDeisgner: "Lore Designer",
  description: "It is an effective tool for game development that seeks to organize files and provide a centralized point to manage all aspects of the game creation process.",
  getStarted: "Get Started",
  selectLanguage: "Select Language",
  features: {
    gddEditor: {
      title: "GDD Editor",
      description: "The Game Design Document (GDD) Editor is a tool that allows you to create and edit GDDs for your game projects."
    },
    dialogueTreeEditor: {
      title: "Non linear branching dialogue tree editor",
      description: "The editor lets you create and edit branching dialogue trees for your game projects and export them to your game engine."
    },
    linearScriptEditor: {
      title: "Linear Script Editor",
      description: "A fan of linear stories? The Linear Script Editor lets you create and edit scripts for your game projects."
    },
    localFirst: {
      title: "Local First",
      description: "Your data is yours. We don't store it. We don't track it. We don't sell it. 100% private by design."
    },
    assetManager: {
      title: "Asset Manager",
      description: "Organize, tag, and manage all your game assets, including images, sounds, and models."
    },
    taskTracker: {
      title: "Task and Milestone Tracker",
      description: "Plan, assign, and track tasks and milestones for your game development project."
    }
  },
  sidebar: {
    projects: "Projects",
    dialogue: "Dialogue",
    characters: "Characters",
    about: "About",
    settings: "Settings",
    help: 'Help'
  },
  createProjectDialog: {
    title: "Create a new project",
    description: "Start creating your first project by filling out the form below.",
    name: {
      label: "Name",
      placeholder: "Project Name",
      required_error: "Project Name is required",

    },
    submit: "Save Changes",
  },
  settings: {
    title: "Settings",
    options: {
      general: "General",
      fontFamily: {
        title: "Font Family",
        description: "Select the font family for the application.",
        restore: "Restore Default Font"
      },
      language: {
        title: "Language",
        description: "Select the language for the application.",
        restore: "Restore Default Language"
      }
    }
  }
};