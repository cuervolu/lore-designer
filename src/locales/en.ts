export default {
  home: "Home",
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
  },
  characters: {
    title: 'Characters',
    createCharacter: 'Create Character',
    createCharacterDescription: 'Fill out the form to create a new character',
    name: 'Name',
    namePlaceholder: 'Character Name',
    nameDescription: 'The name of the character.',
    rolePlaceholder: 'Select Role',
    descriptionPlaceholder: 'Write a brief description of the character.',
    role: {
      name: "Role",
      primary: "Primary",
      secondary: "Secondary",
      tertiary: "Tertiary",
      undefined: "Undefined",
      description: "The role of the character in the story."
    },
    description: 'Description',
    textAreaDescription: "A brief description of the character.",
    additionalNotes: 'Additional Notes',
    additionalNotesPlaceholder: 'Additional Notes or Additional Description',
    additionalNotesDescription: 'Is there anything else we should know about this character? (Optional)',
    isSubmitting: 'Saving...',
    image: {
      title: 'Character Image',
      description: 'Upload an image to represent the character. (Optional)',
      click: "Click here to upload an image",
      size: "PNG, JPG, GIF up to 10 MB",
      upload: "Upload Image",
      change: 'Change Image',
      noPhoto: 'No Photo',
      altText: 'Character Image'
    },
    noCharacters: "No characters found.",
    editCharacter: 'Edit Character',
    editCharacterDescription: 'Edit the character information',
    deleteCharacter: 'Delete Character',
    updateCharacter: 'Update Character',
    cancel: 'Cancel',
    createSuccess: 'Character created successfully.',
    createSuccessDescription: 'The character has been successfully created and added to the project.',
    createError: 'Error creating character.',
    createErrorDescription: 'Failed to create character. Please try again.',
    updateSuccess: 'Character updated successfully.',
    updateSuccessDescription: 'The character has been successfully updated and saved.',
    updateError: 'Error updating character.',
    updateErrorDescription: 'Failed to update character. Please try again.',
    confirmDelete: {
      title: 'Are you sure you want to delete this character?',
      description: 'This action cannot be undone.',
      cancel: 'Cancel',
      confirm: 'Delete'
    },
    deleteSuccess: 'Character deleted successfully.',
    deleteSuccessDescription: "{ name } has been successfully removed from your character list and no longer exists in the project.",
    deleteError: 'Error deleting character.',
    deleteErrorDescription: 'Failed to delete character. Please try again.',
    noDescription: 'No description available',
    noAdditionalNotes: 'No additional notes available',
    noRole: "Undefined",
    noName: "Unnamed Character"
  },
  imageUploader: {
    title: "Character Image",
    description: "Upload an image for your character",
    click: "Click to upload",
    size: "PNG, JPG or GIF (MAX. 800x400px)",
    upload: "Upload Image",
    change: "Change Image",
    defaultAlt: "Character Image",
    successTitle: "Image Uploaded",
    successDescription: "Image has been successfully uploaded.",
    errorTitle: "Error",
    errorDescription: "Failed to upload image. Please try again."
  },
  time: {
    created: "Created {time}",
    updated: "Updated {time}",
    justNow: "just now"
  },
  about: {
    version: "Version {version}",
    showMore: "View Full Changelog",
    links: "Useful Links",
    documentation: "Documentation",
    madeBy: "Made with ❤️ by ",
    errorFetchingReleaseNotes: "Unable to fetch release notes. Please try again later."
  },
  help: {
    title: "Help & Support",
    searchPlaceholder: "Search for help topics...",
    usage: {
      title: "How to Use Lore Designer",
      intro: "Here are the basic steps to get started with Lore Designer:",
      steps: {
        1: "Create a new project or open an existing one",
        2: "Use the character creator to design your protagonists",
        3: "Build your world using the world-building tools",
        4: "Create your story structure in the plot outliner",
        5: "Write your scenes in the distraction-free writing mode"
      },
      moreInfo: "For more detailed instructions, check out our {0}.",
      userGuide: "comprehensive user guide"
    },
    reportBug: {
      title: "Report a Bug",
      intro: "If you encounter a bug, please follow these steps to report it:",
      steps: {
        1: "Describe the issue in detail",
        2: "Note the steps to reproduce the bug",
        3: "Include the system information and app version (provided below)",
        4: "Attach any relevant screenshots or error messages",
        5: "Submit the report through our GitHub issues page"
      },
      sysInfo: {
        title: "System Information",
        description: "Please include this information when reporting a bug",
        appVersion: "App Version",
        logDirectory: "Log Directory"
      },
      buttons: {
        copy: "Copy Info",
        openLog: "Open Log Directory",
        report: "Report on GitHub"
      }
    },
    faq: {
      title: "Frequently Asked Questions",
      questions: {
        updateFrequency: {
          q: "How often is Lore Designer updated?",
          a: "Lore Designer is developed by a single person, so updates are released when this person has available time. We strive to provide updates as frequently as possible, but the schedule may vary."
        },
        offlineUse: {
          q: "Can I use Lore Designer offline?",
          a: "Yes, 99% of Lore Designer's features are designed to work locally, using a local SQLite database. As of this version, the only function that uses the internet is checking the changelogs for each release."
        },
        contributions: {
          q: "Do you accept contributions?",
          a: "Yes, we welcome contributions! If you're interested in contributing to Lore Designer, please check our GitHub repository for more information on how to get involved."
        },
        dataSecurity: {
          q: "How does Lore Designer handle the privacy and security of my data?",
          a: "We don't handle any of your data. Everything runs within your computer and we have no way of knowing anything about what you do."
        },
        projectTypes: {
          q: "What types of projects is Lore Designer useful for?",
          a: "Lore Designer is useful for RPG games, visual novels, or linear stories. It's also great for game planning."
        },
        platforms: {
          q: "On which platforms is Lore Designer available?",
          a: "Currently, as we don't own a Mac, Lore Designer is only available on Linux and Windows. There is a file to run it on Mac with each release, but it hasn't been tested at all."
        },
        gameEngineCompatibility: {
          q: "Is Lore Designer compatible with any specific game engine?",
          a: "Any engine that supports JSON is compatible with Lore Designer!"
        },
        characterLimit: {
          q: "Is there a limit to the number of characters I can create?",
          a: "Nope! You can go crazy with it."
        }
      }
    },
    tips: {
      title: "Tips and Tricks",
      list: [
        "Press Ctrl + P to open the command palette and navigate faster through the application",
        "Don't like the app's theme? You can change between light and dark mode, and even the global font of the app in settings",
        "Use the Kanban board to manage your project tasks"
      ]
    },
    moreHelp: {
      title: "Need More Help?",
      description: "If you couldn't find the answer you're looking for, feel free to reach out to our support team:",
      contact: {
        email: "Email: cuervolu＠protonmail.com",
        twitter: "Twitter: ＠cuervolu29",
      },
    },
    toasts: {
      copySuccess: {
        title: "Success",
        description: "System information copied to clipboard"
      },
      copyError: {
        title: "Error",
        description: "Failed to copy to clipboard"
      },
      openLogError: {
        title: "Error",
        description: "Failed to open log directory"
      },
      loadInfoError: {
        title: "Error",
        description: "Failed to load system information"
      }
    }
  },
  globalCommand: {
    searchPlaceholder: "Type a command or search...",
    noResults: "No results found.",
    characters: "Characters",
    navigation: "Navigation",
    language: "Change Language",
    actions: "Actions",
    external: "External Links",
    english: "English",
    spanish: "Spanish",
    relaunch: "Relaunch App",
    github: "GitHub Repository",
    documentation: "Documentation",
    fonts: "Fonts",
    loadMore: "Load more",
    exit: "Exit application"
  },
  updateNotification: {
    title: "Update Available",
    description: "A new version ({version}) of Lore Designer is available.",
    whatsNew: "What's new:",
    later: "Later",
    updating: "Updating...",
    updateNow: "Update Now"
  },
  projects: {
    title: "Projects",
    loading: "Loading projects...",
    noProjects: "No projects found. Create your first project!",
    name: "Project Name",
    namePlaceholder: "What's the name of your project?",
    description: "Project Description",
    descriptionPlaceholder: "Tell us about your project...",
    budget: "Budget",
    budgetPlaceholder: "How much is your budget?",
    image: "Project Image",
    update: "Update Project",
    noName: "Unnamed Project",
    noDescription: "No description available",
    created: "Created",
    lastUpdated: "Last updated",
    never: "Never",
    createProject: "Create New Project",
    editProject: "Edit Project",
    deleteProject: "Delete Project",
    confirmDelete: {
      title: "Are you sure you want to delete this project?",
      description: "This action cannot be undone.",
      confirm: "Delete",
      cancel: "Cancel"
    },
    createSuccess: "Project created successfully",
    updateSuccess: "Project updated successfully",
    updateSuccessDescription: "The project has been successfully updated and saved.",
    deleteSuccess: "Project deleted successfully",
    createError: "Error creating project",
    updateError: "Error updating project",
    updateErrorDescription: "Failed to update project. Please try again.",
    deleteError: "Error deleting project",
    imageUpdateError: "Error updating project image",
    imageUpdateErrorDescription: "Failed to update project image. Please try again.",
    dashboard: {
      download: "Download",
      tasksOverview: "Tasks Overview",
      tasksCompleted: "{completed} of {total} tasks completed",
      activeTasks: "active tasks",
      budget: "Budget",
      budgetTotal: "Total Budget",
      budgetRemaining: "${remaining} remaining",
      teamSize: "Team Size",
      activeMembers: "active members",
      nextDeadline: "Next Deadline",
      daysRemaining: "{days} days remaining",
      progressOverview: "Progress Overview",
      progressChartPlaceholder: "Progress chart will be displayed here",
      recentTasks: "Recent Tasks",
      recentTasksDescription: "Latest updates on project tasks",
      recentTasksPlaceholder: "Recent tasks will be listed here",
      noDeadline: "No deadline set",
    },
    tabs: {
      overview: "Overview",
      kanban: "Kanban Board",
      gdd: "GDD Editor",
      timeline: "Timeline",
      expenses: "Expenses"
    },
    content: {
      kanban: "Kanban board content will be displayed here.",
      gdd: "GDD editor will be integrated here.",
      timeline: "Project timeline will be displayed here.",
      expenses: "Expense tracking information will be displayed here."
    }
  },
  createProjectDialog: {
    title: "Create a New Project",
    description: "Enter the name for your new project.",
    name: {
      label: "Project Name",
      placeholder: "Enter project name",
      required_error: "Project name is required",
    },
    submit: "Create Project",
  },
  submitting: "Creating...",
  branchDialogue: {
    showMessage: {
      label: 'Show Message',
      placeholder: 'Enter dialogue...'
    },
    execute: 'Execute',
    wait: 'Wait',
    conditionBranch: 'Condition Branch',
    randomBranch: 'Random Branch',
    chanceBranch: 'Chance Branch',
    repeat: 'Repeat',
    setLocalVariable: 'Set Local Variable',
    comment: 'Comment',
    selectCharacter: 'Select a character',
    removeElementTitle: 'Remove Element',
    removeElementDescription: 'Are you sure you want to remove this {elementType}?',
    cancel: 'Cancel',
    remove: 'Remove',
    load: 'Load',
    save: 'Save',
    export: 'Export',
    variables: 'Variables',
    localVariables: 'Local Variables',
    addVariable: 'Add Variable',
    variableName: 'Variable name',
    variableType: {
      string: 'STRING',
      number: 'NUMBER',
      boolean: 'BOOLEAN'
    }
  },
};