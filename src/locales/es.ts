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
    deleteCharacter: 'Eliminar Personaje',
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
  },
  help: {
    title: "Ayuda y Soporte",
    searchPlaceholder: "Buscar temas de ayuda...",
    usage: {
      title: "Cómo usar Lore Designer",
      intro: "Aquí están los pasos básicos para comenzar con Lore Designer:",
      steps: {
        1: "Crea un nuevo proyecto o abre uno existente",
        2: "Usa el creador de personajes para diseñar tus protagonistas",
        3: "Construye tu mundo usando las herramientas de creación de mundos",
        4: "Crea la estructura de tu historia en el esquema de trama",
        5: "Escribe tus escenas en el modo de escritura sin distracciones"
      },
      moreInfo: "Para instrucciones más detalladas, consulta nuestra {0}.",
      userGuide: "guía de usuario completa"
    },
    reportBug: {
      title: "Reportar un Error",
      intro: "Si encuentras un error, por favor sigue estos pasos para reportarlo:",
      steps: {
        1: "Describe el problema en detalle",
        2: "Anota los pasos para reproducir el error",
        3: "Incluye la información del sistema y la versión de la aplicación (proporcionada abajo)",
        4: "Adjunta capturas de pantalla o mensajes de error relevantes",
        5: "Envía el reporte a través de nuestra página de problemas en GitHub"
      },
      sysInfo: {
        title: "Información del Sistema",
        description: "Por favor, incluye esta información al reportar un error",
        appVersion: "Versión de la Aplicación",
        logDirectory: "Directorio de Registros"
      },
      buttons: {
        copy: "Copiar Info",
        openLog: "Abrir Directorio de Registros",
        report: "Reportar en GitHub"
      }
    },
    faq: {
      title: "Preguntas Frecuentes",
      questions: {
        updateFrequency: {
          q: "¿Con qué frecuencia se actualiza Lore Designer?",
          a: "Lore Designer es desarrollado por una sola persona, por lo que las actualizaciones se lanzan cuando esta persona tiene tiempo disponible. Nos esforzamos por proporcionar actualizaciones con la mayor frecuencia posible, pero el calendario puede variar."
        },
        offlineUse: {
          q: "¿Puedo usar Lore Designer sin conexión?",
          a: "Sí, el 99% de las funciones de Lore Designer están diseñadas para funcionar localmente, utilizando una base de datos SQLite local. Hasta esta versión, la única función que usa internet es la que revisa los changelogs de cada release."
        },
        contributions: {
          q: "¿Se aceptan contribuciones?",
          a: "¡Sí, se aceptan contribuciones! Si estás interesado en contribuir a Lore Designer, por favor revisa nuestro repositorio de GitHub para más información sobre cómo involucrarte."
        },
        dataSecurity: {
          q: "¿Cómo maneja Lore Designer la privacidad y seguridad de mis datos?",
          a: "No manejamos ninguno de tus datos. Todo se ejecuta dentro de tu computadora y no tenemos forma de saber nada sobre lo que haces."
        },
        projectTypes: {
          q: "¿Para qué tipos de proyectos es útil Lore Designer?",
          a: "Lore Designer es útil para juegos RPG, novelas visuales o historias lineales. También es excelente para la planificación de juegos."
        },
        platforms: {
          q: "¿En qué plataformas está disponible Lore Designer?",
          a: "De momento, como no poseemos una Mac, Lore Designer solo está disponible en Linux y Windows. Existe un archivo para ejecutarlo en Mac con cada release, pero no ha sido probado en absoluto."
        },
        gameEngineCompatibility: {
          q: "¿Lore Designer es compatible con algún motor de juegos específico?",
          a: "¡Cualquier motor que tenga soporte a JSON es compatible con Lore Designer!"
        },
        characterLimit: {
          q: "¿Hay un límite en el número de personajes que puedo crear?",
          a: "¡No! Puedes volverte loco con ello."
        }
      }
    },
    tips: {
      title: "Consejos y Trucos",
      list: [
        "Presiona Ctrl + P para abrir la paleta de comandos y viajar más rápido a través de la aplicación",
        "¿No te gusta el tema de la aplicación? Puedes cambiar el modo claro y oscuro, inclusive la fuente global de la app en configuración",
        "Utiliza el panel Kanban para manejar las tareas de tu proyecto"
      ]
    },
    moreHelp: {
      title: "¿Necesitas Más Ayuda?",
      description: "Si no encontraste la respuesta que buscas, no dudes en contactar a nuestro equipo de soporte:",
      contact: {
        email: "Correo electrónico: cuervolu＠protonmail.com",
        twitter: "Twitter: ＠cuervolu29",
      },
    },
    toasts: {
      copySuccess: {
        title: "Éxito",
        description: "Información del sistema copiada al portapapeles"
      },
      copyError: {
        title: "Error",
        description: "No se pudo copiar al portapapeles"
      },
      openLogError: {
        title: "Error",
        description: "No se pudo abrir el directorio de registros"
      },
      loadInfoError: {
        title: "Error",
        description: "No se pudo cargar la información del sistema"
      }
    }
  },
  globalCommand: {
    searchPlaceholder: "Escribe un comando o busca...",
    noResults: "No se encontraron resultados.",
    characters: "Personajes",
    navigation: "Navegación",
    language: "Cambiar Idioma",
    actions: "Acciones",
    external: "Enlaces Externos",
    english: "Inglés",
    spanish: "Español",
    relaunch: "Reiniciar App",
    github: "Repositorio GitHub",
    documentation: "Documentación",
    fonts: "Fuentes",
    loadMore: "Cargar Más",
    exit: "Salir de la Aplicación",
  },
  updateNotification: {
    title: "Actualización Disponible",
    description: "Una nueva versión ({version}) de Lore Designer está disponible.",
    whatsNew: "Qué hay de nuevo:",
    later: "Más tarde",
    updating: "Actualizando...",
    updateNow: "Actualizar ahora"
  },
  projects: {
    title: "Proyectos",
    loading: "Cargando proyectos...",
    noProjects: "No se encontraron proyectos. ¡Crea tu primer proyecto!",
    noName: "Proyecto sin nombre",
    noDescription: "Sin descripción disponible",
    created: "Creado",
    lastUpdated: "Última actualización",
    never: "Nunca",
    createProject: "Crear Nuevo Proyecto",
    editProject: "Editar Proyecto",
    deleteProject: "Eliminar Proyecto",
    confirmDelete: {
      title: "¿Estás seguro de que quieres eliminar este proyecto?",
      description: "Esta acción no se puede deshacer.",
      confirm: "Eliminar",
      cancel: "Cancelar"
    },
    createSuccess: "Proyecto creado con éxito",
    updateSuccess: "Proyecto actualizado con éxito",
    deleteSuccess: "Proyecto eliminado con éxito",
    createError: "Error al crear el proyecto",
    updateError: "Error al actualizar el proyecto",
    deleteError: "Error al eliminar el proyecto",
    dashboard: {
      download: "Descargar",
      tasksOverview: "Resumen de Tareas",
      tasksCompleted: "{completed} de {total} tareas completadas",
      activeTasks: "tareas activas",
      budget: "Presupuesto",
      budgetRemaining: "${remaining} restantes",
      teamSize: "Tamaño del Equipo",
      activeMembers: "miembros activos",
      nextDeadline: "Próxima Fecha Límite",
      daysRemaining: "{days} días restantes",
      progressOverview: "Resumen de Progreso",
      progressChartPlaceholder: "El gráfico de progreso se mostrará aquí",
      recentTasks: "Tareas Recientes",
      recentTasksDescription: "Últimas actualizaciones de las tareas del proyecto",
      recentTasksPlaceholder: "Las tareas recientes se listarán aquí"
    },
    tabs: {
      overview: "Visión General",
      kanban: "Tablero Kanban",
      gdd: "Editor GDD",
      timeline: "Línea de Tiempo",
      expenses: "Gastos"
    },
    content: {
      kanban: "El contenido del tablero Kanban se mostrará aquí.",
      gdd: "El editor GDD se integrará aquí.",
      timeline: "La línea de tiempo del proyecto se mostrará aquí.",
      expenses: "La información de seguimiento de gastos se mostrará aquí."
    }
  },
  createProjectDialog: {
    title: "Crear un Nuevo Proyecto",
    description: "Ingresa el nombre para tu nuevo proyecto.",
    name: {
      label: "Nombre del Proyecto",
      placeholder: "Ingresa el nombre del proyecto",
      required_error: "El nombre del proyecto es obligatorio",
    },
    submit: "Crear Proyecto",
  },
  submitting: "Creando...",
  branchDialogue: {
    showMessage: {
      label: 'Mostrar Mensaje',
      placeholder: 'Ingresa el diálogo...'
    },
    execute: 'Ejecutar',
    wait: 'Esperar',
    conditionBranch: 'Rama Condicional',
    randomBranch: 'Rama Aleatoria',
    chanceBranch: 'Rama de Probabilidad',
    repeat: 'Repetir',
    setLocalVariable: 'Establecer Variable Local',
    comment: 'Comentario',
    selectCharacter: 'Selecciona un personaje',
    removeElementTitle: 'Eliminar Elemento',
    removeElementDescription: '¿Estás seguro de que quieres eliminar este {elementType}?',
    cancel: 'Cancelar',
    remove: 'Eliminar',
    load: 'Cargar',
    save: 'Guardar',
    export: 'Exportar',
    variables: 'Variables',
    localVariables: 'Variables Locales',
    addVariable: 'Añadir Variable',
    variableName: 'Nombre de la variable',
    variableType: {
      string: 'TEXTO',
      number: 'NÚMERO',
      boolean: 'BOOLEANO'
    }
  },
};