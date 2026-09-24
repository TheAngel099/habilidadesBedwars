# Memoria del Proyecto: Habilidades Modernas para Bedwars

## 🎯 Tarea Actual (Work in Progress)
- [2026-09-23]: Migración masiva de todo el ecosistema de habilidades a la API de programación concurrente de Paper (Folia-Ready), eliminando BukkitRunnables.

## 1. Resumen y Objetivo
Plugin en Java (Addon para "Marcely's Bedwars" v5.5.8+) que añade un catálogo de habilidades tácticas y modernas (estilo hero-shooter). Utiliza APIs contemporáneas de Minecraft (Display Entities, partículas modernas, vector de físicas, ActionBar) sin requerir mods.

## 2. Entorno y Stack
- **Versión de Minecraft:** 1.21.x (Basado en la indicación 26.1.2 de Paper fork).
- **Java:** 25+ LTS (OpenJDK Temurin / Oracle JDK con Generational ZGC `-XX:+UseZGC -XX:+ZGenerational`).
- **API Base:** Paper 1.21.1-R0.1-SNAPSHOT / Paper 26.1.2 API y `de.marcely.bedwars:MBedwars-API:5.5.8`.
- **Cosméticos:** `BedwarsCosmetics` v1.1.0 (compilado en Java 25 con soporte para `DISPLAY_BLOCK`, `DISPLAY_ITEM` e interpolación suave).
- **Habilidades:** `HabilidadesBedwars` v1.2.0-SNAPSHOT (compilado en Java 25 con partículas modernas y geometría matemática).

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
5. **Frost Projectile (`FrostProjectile`)**: Proyectil matemático de hielo (`ItemDisplay`) con físicas vectoriales (`BukkitRunnable`) que aplica `Lentitud` en un radio preciso sin impactar a aliados o al invocador.

## 5. Decisiones Técnicas y Reglas de Oro
- **Identificación por PDC:** Las habilidades se asocian a un `ItemStack` mediante `PersistentDataContainer` con la clave `NamespacedKey("habilidadesbedwars", "ability_id")`. Se evita asociar habilidades únicamente por `Material`.
- **Adventure API Estricta:** Prohibido el uso de caracteres legados de formato (`§`). Toda comunicación usa `net.kyori.adventure.text.Component` y `NamedTextColor`.
- **Higiene del Mapa y Ciclo de Vida:** Cero modificaciones físicas. Usar `BlockDisplay` e `Interaction` entities gestionadas por `ActiveEntityManager` para garantizar eliminación al morir, salir o al desactivarse el plugin.
- **Entidades Cosméticas (Bailes de Victoria):** Toda entidad invocada (`SPAWN_ENTITY`) debe ser **invulnerable**, no debe soltar ítems/XP al morir (cancelando drops en `EntityDeathEvent`) y no debe poder recoger ítems del suelo para mantener la limpieza del mapa.
- **Robustez y Optimización:** No iterar todos los jugadores en un runnable pesado; usar eventos y `Timestamps` (`System.currentTimeMillis()`) en un `HashMap`.
- **Filtrado de Aliados (Fuego Amigo):** Obligatorio validar siempre `BedwarsIntegration.areEnemies(caster, target)` antes de aplicar cualquier efecto (empujón, buff/debuff, daño) en habilidades de área para no afectar o revelar (trollear) al propio equipo.
- **Geometría Radial (Esférica) y Proyectiles:** Las funciones estándar de Bukkit (`getNearbyEntities`) devuelven un *Bounding Box* cúbico. Siempre se debe filtrar el rango real aplicando la fórmula de la esfera. Los proyectiles se renderizan mediante matemática vectorial en `BukkitRunnable` combinada con `ItemDisplay` y `setTeleportDuration(1)` para interpolación suave sin depender de la física imprecisa de entidades vanilla.
- **Control de Fugas Globales:** Ser consciente de que efectos como `GLOWING` (metadata en Bukkit) son visibles a todos los jugadores del servidor por defecto, sirviendo como *AoE Debuffs* globales. Si se busca privacidad absoluta, se usaría NMS/Packets (fuera del alcance base).
- **Control de Abstractos:** Los métodos sobre-escribibles de config no van dentro del constructor padre. El constructor de la clase `Ability` es `protected`.
- **Estructura de Categorías:** Las implementaciones de habilidades en `ability.impl` están divididas físicamente en subpaquetes por rareza/categoría (ej. `comun`, `epico`, `legendario`) para mantener escalabilidad.
- **Manejo de Errores Centralizado:** Toda habilidad ejecutada en `PlayerInteractListener` está envuelta en un `try-catch`. Los errores son capturados por `ErrorLogger` y volcados en `plugins/HabilidadesBedwars/errores.log`, evitando saturar la consola del servidor con stacktraces crudos.
- **Profiler de Rendimiento:** Uso de `Profiler.java` para medir la latencia de ejecución de las habilidades. Imprime una advertencia si alguna habilidad toma más de 5ms en el hilo principal.
- **Auto-Deploy y Comandos:** Configuración de `maven-antrun-plugin` para copiar directamente el jar a `plugins/update/` permitiendo despliegues limpios al reiniciar. Se incluyó un comando base `/hb reload` administrado en `HabilidadesCommand` para recargar config y managers en caliente.
- **Integración con BedwarsCosmetics (API Cruzada):** Las habilidades ahora se tratan como la categoría `ABILITY` dentro del plugin `BedwarsCosmetics`. El menú, economía (CoinsManager) y guardado (playerdata) lo gestiona *BedwarsCosmetics*. Nuestro plugin (`HabilidadesBedwars`) lee si la habilidad está equipada usando `BedwarsCosmeticsHook` antes de permitir su uso en `PlayerInteractListener`. Solo se puede equipar **1 habilidad a la vez** y **no se puede cambiar in-game**.

## 6. Decisiones de Diseño y Arquitectura (DDA)
1. **Registro y Hooks:**
   - `HabilidadesBedwars` se inicia después de `BedwarsCosmetics` (dependencia soft/hard).
   - La comunicación se realiza mediante una clase `BedwarsCosmeticsHook` para verificar el estado de los cosméticos equipados.
2. **Manejo de Ítems e Inventario:**
   - Los ítems físicos de habilidad ahora se entregan automáticamente al jugador dentro de las arenas de MBedwars al iniciar la ronda (`RoundStartEvent`) o al reaparecer (`PlayerIngamePostRespawnEvent`).
   - Los ítems generados contienen soporte para `lore` completamente editable desde `config.yml` (incluyendo reemplazo de variables dinámicas como `%cooldown%`).
   - Los ítems están marcados con un Tag PDC único para distinguirlos de los ítems de Bedwars.
3. **Higiene del Mapa y Colisiones:**
   - Para dar colisión física sin alterar permanentemente el mapa de Bedwars (respetando la regla de higiene), las habilidades que generan muros como `TacticalBarricade` utilizan **paquetes de bloques falsos** (`sendBlockChange`) enviados a todos los jugadores de la arena.
   - La intercepción de proyectiles (flechas, bolas de fuego) se simula del lado del servidor calculando si el proyectil intersecta las coordenadas del bloque falso y destruyéndolo si es así.
4. **Seguridad y Restricciones:** Revisa este documento antes de proponer arquitecturas nuevas o nuevas habilidades, para asegurar que se aplican invariablemente los filtrados radiales y de equipos, y que cualquier clase nueva se maneja a través de los sistemas de Logger y Profiler establecidos.
5. **Entorno Moderno Java 25 LTS, Paper 26.1.2 y Generational ZGC:**
   - Todo el ecosistema de habilidades y cosméticos está compilado y optimizado estrictamente bajo Java 25 LTS y Paper 26.1.2+.
   - **Generational ZGC:** Servidor arrancado con `-XX:+UseZGC -XX:+ZGenerational` en `blog.bat`, garantizando pausas de Garbage Collection inferiores a 1 milisegundo (< 1ms) para eliminar micro-tirones al jugar en host local.
   - **Renderizado Cinemático:** Las Display Entities (`BlockDisplay`, `ItemDisplay`) utilizan `teleportDuration(1)` e `interpolationDuration` para animaciones fluidas a 60/144 FPS en los clientes de los jugadores.
   - **Partículas Modernas:** Soporte pleno para partículas `GUST`, `GUST_EMITTER_LARGE`, `TRIAL_SPAWNER_DETECTION`, `SONIC_BOOM` y `DUST_COLOR_TRANSITION` con soporte de colores HEX.
   - **Vuelo y Red Local:** `server.properties` configurado con `allow-flight=true` (evita expulsiones por falsos positivos de vuelo en dragones o dashes) y `network-compression-threshold=512` (menor sobrecarga de CPU en red local LAN).
   - **Correcciones Post-Migración (Java 25/Paper 1.21+):** Se reemplazaron partículas como `INSTANT_EFFECT` (que requieren clases de datos específicas como `Particle$Spell`) por partículas libres de metadata (`END_ROD`, `SNOWFLAKE`) para prevenir `IllegalArgumentException`. Además, se aseguró la correcta compilación y empaquetado de clases internas anónimas (`$1.class`) mediante ejecuciones limpias de Maven (`mvn clean package`) para evitar `NoClassDefFoundError`.

## 7. Estructura del config.yml
Toda nueva habilidad debe registrarse en el archivo `config.yml` respetando esta estructura base para que sea procesada correctamente:
```yaml
habilidades:
  nombre_de_la_habilidad:
    nombre: "<green>Nombre Visual"
    cooldown: 15
    material: FEATHER
    # Agregar variables custom aquí según requiera la habilidad
```

## 8. Historial de Cambios Arquitectónicos
- [2026-09-23] - [Core/Memory]: Separación del ciclo de limpieza de Cooldowns y FallDamage en `ActiveEntityManager` para prevenir exploit de recarga de habilidades tras morir (ahora se asocian estrictamente a `PlayerQuitEvent`).
- [2026-09-23] - [Integration]: Modificación en `MBedwarsListener` para iterar el inventario y validar habilidades mediante su PDC, solucionando conflictos con compras de ítems vanilla de la tienda de Bedwars.
- [2026-09-23] - [Geometría]: Corrección en `FrostProjectile` para aplicar fórmula radial esférica estricta (`distanceSquared`) reemplazando la colisión por BoundingBox cúbico de Bukkit.
- [2026-09-23] - [Scheduler/Folia]: Refactorización completa del sistema de tareas. Se eliminaron todas las referencias a `BukkitRunnable` y `BukkitTask` en favor de `ScheduledTask`, `player.getScheduler()` y `Bukkit.getRegionScheduler()`, logrando que el plugin sea 100% compatible con Folia.