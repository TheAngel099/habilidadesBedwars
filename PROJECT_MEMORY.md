# Memoria del Proyecto: Habilidades Modernas para Bedwars

## 1. Resumen y Objetivo
Plugin en Java (Addon para "Marcely's Bedwars" v5.5.8+) que añade un catálogo de habilidades tácticas y modernas (estilo hero-shooter). Utiliza APIs contemporáneas de Minecraft (Display Entities, partículas modernas, vector de físicas, ActionBar) sin requerir mods.

## 2. Entorno y Stack
- **Versión de Minecraft:** 1.21.x (Basado en la indicación 26.1.2 de Spigot/Paper).
- **Java:** 21+
- **API Base:** Paper/Spigot API y `de.marcely.bedwars:MBedwars-API:5.5.8`.

## 3. Arquitectura del Proyecto
- **`com.theangel099.habilidadesbedwars.ability.Ability`**: Interfaz base. Define ID, nombre, cooldown, icono, PDC (`PersistentDataContainer`) y método `cast(Player)`.
- **`com.theangel099.habilidadesbedwars.manager.ActiveEntityManager`**: Gestión de ciclo de vida de entidades temporales (`BlockDisplay`, `Interaction`) y `BukkitTask`s (limpieza en muerte, desconexión y `onDisable`).
- **`com.theangel099.habilidadesbedwars.manager.CooldownManager`**: Gestión de enfriamientos usando marcas de tiempo por UUID, redondeo ceiling de segundos y alertas en ActionBar.
- **`com.theangel099.habilidadesbedwars.manager.FallDamageManager`**: Sistema que anula temporalmente el daño por caída al usar habilidades de movilidad.
- **`com.theangel099.habilidadesbedwars.listener.PlayerInteractListener`**: Escucha clics derechos identificando habilidades de forma segura mediante PDC en el ItemStack para no interferir con ítems o bloques vanilla.
- **`com.theangel099.habilidadesbedwars.mbedwars.BedwarsIntegration`**: Integración con MBedwars-API, gestión de la tienda y vinculación de ítems.

## 4. Habilidades Implementadas
1. **Kinetic Dash (`KineticDash`)**: Impulso cinético vectorial hacia adelante con partículas modernas (`GUST`, `WIND_CHARGE`). Anula daño por caída.
2. **Barricada Táctica (`TacticalBarricade`)**: Defensa temporal con `BlockDisplay` e `Interaction` entities (para colisión e impacto de proyectiles) sin modificar bloques físicos del mapa.
3. **Repulsor Blast (`RepulsorBlast`)**: Onda expansiva matemática (`SONIC_BOOM`, `TRIAL_SPAWNER_DETECTION`) que expulsa enemigos cercanos basándose en cálculo vectorial esférico exacto.
4. **Spectral Vision (`SpectralVision`)**: Aplica efecto `GLOWING` a los enemigos en un área controlada matemáticamente como "AoE Debuff" temporal y notifica al jugador usando Adventure API pura.

## 5. Decisiones Técnicas y Reglas de Oro
- **Identificación por PDC:** Las habilidades se asocian a un `ItemStack` mediante `PersistentDataContainer` con la clave `NamespacedKey("habilidadesbedwars", "ability_id")`. Se evita asociar habilidades únicamente por `Material`.
- **Adventure API Estricta:** Prohibido el uso de caracteres legados de formato (`§`). Toda comunicación usa `net.kyori.adventure.text.Component` y `NamedTextColor`.
- **Higiene del Mapa y Ciclo de Vida:** Cero modificaciones físicas. Usar `BlockDisplay` e `Interaction` entities gestionadas por `ActiveEntityManager` para garantizar eliminación al morir, salir o al desactivarse el plugin.
- **Robustez y Optimización:** No iterar todos los jugadores en un runnable pesado; usar eventos y `Timestamps` (`System.currentTimeMillis()`) en un `HashMap`.
- **Filtrado de Aliados (Fuego Amigo):** Obligatorio validar siempre `BedwarsIntegration.areEnemies(caster, target)` antes de aplicar cualquier efecto (empujón, buff/debuff, daño) en habilidades de área para no afectar o revelar (trollear) al propio equipo.
- **Geometría Radial (Esférica):** Las funciones estándar de Bukkit (`getNearbyEntities`) devuelven un *Bounding Box* cúbico. Siempre se debe filtrar el rango real aplicando la fórmula de la esfera: `player.getLocation().distanceSquared(target.getLocation()) <= radius * radius`.
- **Control de Fugas Globales:** Ser consciente de que efectos como `GLOWING` (metadata en Bukkit) son visibles a todos los jugadores del servidor por defecto, sirviendo como *AoE Debuffs* globales. Si se busca privacidad absoluta, se usaría NMS/Packets (fuera del alcance base).
- **Control de Abstractos:** Los métodos sobre-escribibles de config no van dentro del constructor padre. El constructor de la clase `Ability` es `protected`.
- **CI/CD:** Workflow en GitHub Actions compilando siempre sobre Java 25.

> **Nota para la IA:** Revisa este documento antes de proponer arquitecturas nuevas o nuevas habilidades, para asegurar que se aplican invariablemente los filtrados radiales y de equipos.

