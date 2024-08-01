export default {
  home: "Inicio",
  welcome: "Bienvenido a",
  loreDeisgner: "Lore Designer",
  description: "Es una herramienta efectiva para el desarrollo de juegos que busca organizar archivos y proporcionar un punto centralizado para gestionar todos los aspectos del proceso de creación de juegos.",
  getStarted: "Comenzar",
  selectLanguage: "Seleccionar Idioma",
  features: {
    gddEditor: {
      title: "Editor de GDD",
      description: "El Editor de Documento de Diseño de Juego (GDD) es una herramienta que te permite crear y editar GDDs para tus proyectos de juegos."
    },
    dialogueTreeEditor: {
      title: "Editor de árbol de diálogo no lineal",
      description: "El editor te permite crear y editar árboles de diálogo ramificados para tus proyectos de juegos y exportarlos a tu motor de juegos."
    },
    linearScriptEditor: {
      title: "Editor de Guiones Lineales",
      description: "¿Fan de las historias lineales? El Editor de Guiones Lineales te permite crear y editar guiones para tus proyectos de juegos."
    },
    localFirst: {
      title: "Local Primero",
      description: "Tus datos son tuyos. No los almacenamos. No los rastreamos. No los vendemos. 100% privado por diseño."
    },
    assetManager: {
      title: "Gestor de Activos",
      description: "Organiza, etiqueta y gestiona todos tus activos de juego, incluyendo imágenes, sonidos y modelos."
    },
    taskTracker: {
      title: "Seguimiento de Tareas e Hitos",
      description: "Planifica, asigna y realiza un seguimiento de las tareas e hitos de tu proyecto de desarrollo de juegos."
    }
  },
  sidebar: {
    projects: "Proyectos",
    dialogue: "Diálogo",
    characters: "Personajes",
    about: "Acerca de",
    settings: "Ajustes",
    help: 'Ayuda'
  },
  createProjectDialog: {
    title: "Crear un nuevo proyecto",
    description: "Comienza a crear tu primer proyecto llenando el formulario a continuación.",
    name: {
      label: "Nombre",
      placeholder: "Nombre del Proyecto",
      required_error: "El Nombre del Proyecto es requerido",
    },
    submit: "Guardar Cambios",
  },
  settings: {
    title: "Ajustes",
    options: {
      general: "General",
      fontFamily: {
        title: "Familia de Fuentes",
        description: "Selecciona la familia de fuentes para la aplicación.",
        restore: "Restaurar Fuente Predeterminada"
      },
      language: {
        title: "Lenguaje",
        description: "Selecciona el idioma de la aplicación.",
        restore: "Restaurar Idioma Predeterminado"
      }
    }
  },
  characters: {
    title: 'Personajes',
    createCharacter: 'Crear Personaje',
    createCharacterDescription: 'Rellena el formulario para crear un nuevo personaje',
    name: 'Nombre',
    namePlaceholder: 'Nombre del personaje',
    nameDescription: 'El nombre del personaje',
    rolePlaceholder: 'Selecciona Rol',
    descriptionPlaceholder: 'Escribe una breve descripción del personaje',
    role: {
      name: "Rol",
      primary: "Principal",
      secondary: "Secundario",
      tertiary: "Terciario",
      undefined: "Indefinido",
      description: "El rol del personaje en la historia."
    },
    description: 'Descripción',
    textAreaDescription: "Una breve descripción del personaje.",
    additionalNotes: 'Notas Adicionales',
    additionalNotesPlaceholder: 'Notas Adicionales o Descripción Adicional',
    additionalNotesDescription: '¿Hay algo más que debamos saber sobre este personaje? (Opcional)',
    isSubmitting: 'Guardando...',
    image: {
      title: 'Imagen del Personaje',
      description: 'Sube una imagen para representar al personaje. (Opcional)',
      click: "Haz clic aquí para subir una imagen",
      size: "PNG, JPG, GIF hasta 10 MB",
      upload: "Subir Imagen",
      change: 'Cambiar Imagen',
      noPhoto: 'Sin Foto',
      altText: 'Imagen del Personaje'
    },
    noCharacters: "No se encontraron personajes.",
    editCharacter: 'Editar el personaje',
    editCharacterDescription: 'Editar la información del personaje',
    updateCharacter: 'Actualizar personaje',
    cancel: 'Cancelar',
    createSuccess: 'Personaje creado',
    createSuccessDescription: 'El personaje se ha creado con éxito y se ha añadido a la lista de personajes.',
    createError: 'Error al crear personaje',
    createErrorDescription: 'No se pudo crear el personaje. Por favor, inténtalo de nuevo.',
    updateSuccess: 'Personaje actualizado',
    updateSuccessDescription: 'El personaje se ha actualizado con éxito y se ha guardado.',
    updateError: 'Error al actualizar personaje',
    updateErrorDescription: 'No se pudo actualizar el personaje. Por favor, inténtalo de nuevo.',
    confirmDelete: {
      title: '¿Estás seguro de que quieres eliminar este personaje?',
      description: 'Esta acción no se puede deshacer.',
      cancel: 'Cancelar',
      confirm: 'Eliminar'
    },
    deleteSuccess: 'Personaje eliminado',
    deleteSuccessDescription: '{ name } ha sido eliminado con éxito y ya no existe en el proyecto.',
    deleteError: 'Error al eliminar personaje',
    deleteErrorDescription: 'No se pudo eliminar el personaje. Por favor, inténtalo de nuevo.',
    noDescription: 'No hay descripción disponible',
    noAdditionalNotes: 'No hay notas adicionales disponibles',
    noRole: "Indefinido",
    noName: "Personaje sin nombre"
  },
  imageUploader: {
    title: "Imagen del Personaje",
    description: "Sube una imagen para tu personaje",
    click: "Haz clic para subir",
    size: "PNG, JPG o GIF (MÁX. 800x400px)",
    upload: "Subir Imagen",
    change: "Cambiar Imagen",
    defaultAlt: "Imagen del Personaje",
    successTitle: "Imagen Subida",
    successDescription: "La imagen se ha subido con éxito.",
    errorTitle: "Error",
    errorDescription: "No se pudo subir la imagen. Por favor, inténtalo de nuevo.",
  },
  time: {
    created: "Creado {time}",
    updated: "Actualizado {time}",
    justNow: "justo ahora"
  },
  about: {
    version: "Versión {version}",
    showMore: "Ver Registro de Cambios Completo",
    links: "Enlaces Útiles",
    documentation: "Documentación",
    madeBy: "Hecho con ❤️ por ",
    errorFetchingReleaseNotes: "No se pueden obtener las notas de la versión. Por favor, inténtalo de nuevo más tarde."
  }
};