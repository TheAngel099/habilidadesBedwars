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
- **Estructura de Categorías:** Las implementaciones de habilidades en `ability.impl` están divididas físicamente en subpaquetes por rareza/categoría (ej. `comun`, `epico`, `legendario`) para mantener escalabilidad.
- **Manejo de Errores Centralizado:** Toda habilidad ejecutada en `PlayerInteractListener` está envuelta en un `try-catch`. Los errores son capturados por `ErrorLogger` y volcados en `plugins/HabilidadesBedwars/errores.log`, evitando saturar la consola del servidor con stacktraces crudos.
- **Profiler de Rendimiento:** Uso de `Profiler.java` para medir la latencia de ejecución de las habilidades. Imprime una advertencia si alguna habilidad toma más de 5ms en el hilo principal.
- **Auto-Deploy y Comandos:** Configuración de `maven-antrun-plugin` para copiar directamente el jar a `plugins/update/` permitiendo despliegues limpios al reiniciar. Se incluyó un comando base `/hb reload` administrado en `HabilidadesCommand` para recargar config y managers en caliente.
- **Integración con BedwarsCosmetics (API Cruzada):** Las habilidades ahora se tratan como la categoría `ABILITY` dentro del plugin `BedwarsCosmetics`. El menú, economía (CoinsManager) y guardado (playerdata) lo gestiona *BedwarsCosmetics*. Nuestro plugin (`HabilidadesBedwars`) lee si la habilidad está equipada usando `BedwarsCosmeticsHook` antes de permitir su uso en `PlayerInteractListener`. Solo se puede equipar **1 habilidad a la vez** y **no se puede cambiar in-game**.

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
- **Estructura de Categorías:** Las implementaciones de habilidades en `ability.impl` están divididas físicamente en subpaquetes por rareza/categoría (ej. `comun`, `epico`, `legendario`) para mantener escalabilidad.
- **Manejo de Errores Centralizado:** Toda habilidad ejecutada en `PlayerInteractListener` está envuelta en un `try-catch`. Los errores son capturados por `ErrorLogger` y volcados en `plugins/HabilidadesBedwars/errores.log`, evitando saturar la consola del servidor con stacktraces crudos.
- **Profiler de Rendimiento:** Uso de `Profiler.java` para medir la latencia de ejecución de las habilidades. Imprime una advertencia si alguna habilidad toma más de 5ms en el hilo principal.
- **Auto-Deploy y Comandos:** Configuración de `maven-antrun-plugin` para copiar directamente el jar a `plugins/update/` permitiendo despliegues limpios al reiniciar. Se incluyó un comando base `/hb reload` administrado en `HabilidadesCommand` para recargar config y managers en caliente.
- **Integración con BedwarsCosmetics (API Cruzada):** Las habilidades ahora se tratan como la categoría `ABILITY` dentro del plugin `BedwarsCosmetics`. El menú, economía (CoinsManager) y guardado (playerdata) lo gestiona *BedwarsCosmetics*. Nuestro plugin (`HabilidadesBedwars`) lee si la habilidad está equipada usando `BedwarsCosmeticsHook` antes de permitir su uso en `PlayerInteractListener`. Solo se puede equipar **1 habilidad a la vez** y **no se puede cambiar in-game**.

## Decisiones de Diseño y Arquitectura (DDA)

1.  **Registro y Hooks:**
    *   `HabilidadesBedwars` se inicia después de `BedwarsCosmetics` (dependencia soft/hard).
    *   La comunicación se realiza mediante una clase `BedwarsCosmeticsHook` para verificar el estado de los cosméticos equipados.

2.  **Manejo de Ítems e Inventario:**
    *   Los ítems físicos de habilidad ahora se entregan automáticamente al jugador dentro de las arenas de MBedwars al iniciar la ronda (`RoundStartEvent`) o al reaparecer (`PlayerIngamePostRespawnEvent`).
    *   Los ítems generados contienen soporte para `lore` completamente editable desde `config.yml` (incluyendo reemplazo de variables dinámicas como `%cooldown%`).
    *   Los ítems están marcados con un Tag PDC único para distinguirlos de los ítems de Bedwars.

3.  **Higiene del Mapa y Colisiones:**
    *   Para dar colisión física sin alterar permanentemente el mapa de Bedwars (respetando la regla de higiene), las habilidades que generan muros como `TacticalBarricade` utilizan **paquetes de bloques falsos** (`sendBlockChange`) enviados a todos los jugadores de la arena.
    *   La intercepción de proyectiles (flechas, bolas de fuego) se simula del lado del servidor calculando si el proyectil intersecta las coordenadas del bloque falso y destruyéndolo si es así.

4.  **Seguridad y Restricciones:** Revisa este documento antes de proponer arquitecturas nuevas o nuevas habilidades, para asegurar que se aplican invariablemente los filtrados radiales y de equipos, y que cualquier clase nueva se maneja a través de los sistemas de Logger y Profiler establecidos.
