# Memoria del Proyecto: Habilidades Modernas para Bedwars

## 1. Resumen y Objetivo
Plugin en Java (Addon para "Marcely's Bedwars" v5.5.8+) que añade un catálogo de habilidades tácticas y modernas (estilo hero-shooter). Utiliza APIs contemporáneas de Minecraft (Display Entities, partículas modernas, vector de físicas, ActionBar) sin requerir mods.

## 2. Entorno y Stack
- **Versión de Minecraft:** 1.21.x (Basado en la indicación 26.1.2 de Spigot/Paper).
- **Java:** 21+
- **API Base:** Paper/Spigot API y `de.marcely.bedwars:MBedwars-API:5.5.8`.

## 3. Arquitectura del Proyecto
- **`com.theangel099.habilidadesbedwars.ability.Ability`**: Interfaz base. Define ID, nombre, cooldown, icono y método `cast(Player)`.
- **`com.theangel099.habilidadesbedwars.manager.CooldownManager`**: Gestión de enfriamientos usando marcas de tiempo por UUID y alertas en ActionBar.
- **`com.theangel099.habilidadesbedwars.manager.FallDamageManager`**: Sistema que anula temporalmente el daño por caída al usar habilidades de movilidad.
- **`com.theangel099.habilidadesbedwars.listener.PlayerInteractListener`**: Escucha clics derechos / Shift+Clic para activar las habilidades.
- **`com.theangel099.habilidadesbedwars.mbedwars.BedwarsIntegration`**: Integración con MBedwars-API, gestión de la tienda y vinculación de ítems.

## 4. Habilidades Planeadas
1. **Kinetic Dash (`KineticDash`)**: Impulso cinético vectorial hacia adelante con partículas `WIND_CHARGE` y efectos de sonido. Anula el daño por caída temporalmente.
2. **Barricada Táctica (`TacticalBarricade`)**: Defensa temporal usando `BlockDisplay` (paquetes falsos visuales) sin afectar los bloques del mapa, evitando interferencias con el sistema de regeneración de Bedwars.

## 5. Decisiones Técnicas y Reglas de Oro
- **Robustez y Optimización:** No iterar todos los jugadores en un runnable pesado; usar eventos. Cooldowns en memoria ligeros (`HashMap<UUID, Long>`).
- **Higiene del Mapa:** Cero modificaciones de bloques reales en la arena. Todo efecto es a través de entidades (Display) o partículas.
- **CI/CD:** Workflow en GitHub Actions configurado para compilar automáticamente en cada push (`.github/workflows/build.yml`).

> **Nota para la IA:** Revisa este documento antes de proponer arquitecturas nuevas, para asegurar que se mantiene la visión original del proyecto.

