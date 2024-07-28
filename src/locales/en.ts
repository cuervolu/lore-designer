// src/locales/en.ts

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
      noPhoto: 'No Photo'
    },
    noCharacters: "No characters found.",
    editCharacter: 'Edit Character',
    editCharacterDescription: 'Edit the character information',
    updateCharacter: 'Update Character',
    cancel: 'Cancel',
    createSuccess: 'Character created successfully.',
    createSuccessDescription: 'The character has been successfully created and added to the project.',
    createError: 'Error creating character.',
    createErrorDescription: 'Failed to create character. Please try again.',
    updateSuccess: 'Character updated successfully.',
    updateSuccessDescription: 'The character has been successfully updated and saved.',
    updateError: 'Error updating character.',
    updateErrorDescription: 'Failed to update character. Please try again.'
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
  }

};