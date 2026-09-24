# Directrices de Desarrollo del Plugin (Skill: Minecraft Moderno)

## 🛠️ 1. ENTORNO Y STACK
- **Entorno Base:** Java 25+ LTS, Paper 26.1.2+ en adelante.
- **Dependencias Externas:** Marcely's Bedwars API (MBedwars 5.5.8+).
- **Anti-Alucinaciones:** Si desconoces la firma exacta de un método de MBedwars, **NO LO INVENTES**. Solicita al usuario que proporcione la interfaz o documentación de esa clase antes de escribir el código.

## ⚙️ 2. ESTÁNDARES DE CÓDIGO Y ARQUITECTURA
- **Seguridad de Hilos (CRÍTICO):** Toda interacción con la API de Bukkit (generar entidades, modificar bloques, abrir inventarios) DEBE hacerse en el Hilo Principal (Main Thread). Usa tareas asíncronas ÚNICAMENTE para I/O, bases de datos o cálculos matemáticos puros, y regresa al hilo principal para aplicar los resultados.
- **Gestión de Memoria (ZGC):** El servidor usa Generational ZGC (pausas < 1ms). Evita instanciar objetos efímeros masivamente (como `new Vector()` dentro de bucles de partículas por tick); reutiliza objetos u opta por Object Pooling en tareas de muy alta frecuencia.
- **Higiene de Arena:** NO modifiques bloques físicos reales del mapa de Bedwars. Utiliza exclusivamente paquetes (Fake Blocks) o Display Entities para evitar corromper los mundos al reiniciar las arenas.
- **Ciclo de Vida Estricto:** Toda tarea repetitiva (`BukkitTask`) debe estar vinculada a la sesión del jugador y **cancelarse inmediatamente** si el jugador muere, se desconecta o la partida termina. Cero tolerancia a fugas de memoria (memory leaks).

## 🎨 3. RENDERIZADO Y FRONTEND
- **Entidades Visuales:** Usa ESTRICTAMENTE Display Entities (`BlockDisplay`, `ItemDisplay`) e Interaction Entities con interpolación fluida (`teleportDuration` / `interpolationDuration`). **NUNCA** uses ArmorStands invisibles para cosméticos o hologramas.
- **Efectos Modernos:** Toda habilidad debe basarse en vectores matemáticos normalizados (`org.bukkit.util.Vector`) y partículas modernas (ej. `WIND_CHARGE`, `TRIAL_SPAWNER_DETECTION`, `DUST_COLOR_TRANSITION`).
- **Interfaz de Texto:** Usa EXCLUSIVAMENTE Adventure API (`Component`) para mensajes, títulos y ActionBars con soporte RGB/HEX. Está PROHIBIDO usar `ChatColor` o cadenas legadas con el símbolo `§`.

## 🔄 4. FLUJO DE TRABAJO DEL AGENTE
- **Contexto Obligatorio (Lectura):** Al iniciar CUALQUIER tarea, DEBES leer el archivo `PROJECT_MEMORY.md` antes de escribir código para asegurar que no contradices la arquitectura actual.
- **Documentación Obligatoria (Escritura):** Antes de dar por terminada la tarea, DEBES actualizar `PROJECT_MEMORY.md` añadiendo al final el formato: `[Fecha] - [Módulo]: Breve descripción del cambio arquitectónico`.
- **Versionado Semántico:** Es OBLIGATORIO incrementar la versión en el archivo `pom.xml` antes de compilar cada vez que apliques un cambio (ej. `1.0.1` a `1.0.2` para parches; `1.1.0` para nuevas habilidades).