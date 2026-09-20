# Directrices de Desarrollo del Plugin (Skill: Minecraft Moderno)
- Entorno: Java 21+, Paper API y Marcely's Bedwars API (MBedwars).
- Renderizado y Efectos: Usa estrictamente Display Entities (BlockDisplay, ItemDisplay) e Interaction Entities. NUNCA uses ArmorStands invisibles para efectos visuales.
- Interfaz de Texto: Usa Adventure API / Component para mensajes, títulos y ActionBar; evita ChatColor o cadenas legadas con '§'.
- Física y Vectores: Toda habilidad debe basarse en vectores matemáticos normalizados (org.bukkit.util.Vector) y partículas modernas (WIND_CHARGE, TRIAL_SPAWNER_DETECTION, etc.).
- Higiene de Arena: No modifiques bloques físicos del mapa de Bedwars; usa paquetes de bloques falsos o Display Entities.
- Ciclo de Vida: Toda tarea repetitiva (BukkitTask) debe asociarse al jugador y cancelarse inmediatamente si el jugador muere, se desconecta o la arena termina.

