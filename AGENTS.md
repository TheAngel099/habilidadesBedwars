# Directrices de Desarrollo del Plugin (Skill: Minecraft Moderno)
- Entorno: Java 21+, Paper API y Marcely's Bedwars API (MBedwars).
- Renderizado y Efectos: Usa estrictamente Display Entities (BlockDisplay, ItemDisplay) e Interaction Entities. NUNCA uses ArmorStands invisibles para efectos visuales.
- Interfaz de Texto: Usa Adventure API / Component para mensajes, títulos y ActionBar; evita ChatColor o cadenas legadas con '§'.
- Física y Vectores: Toda habilidad debe basarse en vectores matemáticos normalizados (org.bukkit.util.Vector) y partículas modernas (WIND_CHARGE, TRIAL_SPAWNER_DETECTION, etc.).
- Higiene de Arena: No modifiques bloques físicos del mapa de Bedwars; usa paquetes de bloques falsos o Display Entities.
- Ciclo de Vida: Toda tarea repetitiva (BukkitTask) debe asociarse al jugador y cancelarse inmediatamente si el jugador muere, se desconecta o la arena termina.
- Documentación Obligatoria (Escritura): Es ESTRICTAMENTE OBLIGATORIO que cada vez que se implemente una nueva habilidad, se haga un cambio arquitectónico o una mejora significativa, el agente debe actualizar el archivo `PROJECT_MEMORY.md` para reflejar dicho cambio antes de dar por terminada la tarea.
- Contexto Obligatorio (Lectura): Al iniciar cualquier tarea compleja, refactorización o creación de habilidad, el agente DEBE leer `PROJECT_MEMORY.md` antes de escribir código para evitar contradecir la arquitectura establecida.
- Versionado Semántico: Cada vez que se realicen cambios, mejoras o correcciones en el código, es OBLIGATORIO incrementar la versión del proyecto en el archivo `pom.xml` (ej. de `1.0.1` a `1.0.2` para parches menores, o `1.1.0` para nuevas características) antes de compilar.
