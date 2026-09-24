package com.theangel099.habilidadesbedwars.manager;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gestiona el ciclo de vida de tareas programadas y entidades temporales
 * (BlockDisplay, Interaction) generadas por habilidades activas.
 * Garantiza la higiene del mapa eliminando entidades si el jugador muere, se desconecta
 * o si el servidor/plugin se deshabilita.
 */
public class ActiveEntityManager implements Listener {

    private final HabilidadesBedwarsPlugin plugin;

    // Registros asociados por jugador
    private final Map<UUID, List<Entity>> playerEntities = new ConcurrentHashMap<>();
    private final Map<UUID, List<BukkitTask>> playerTasks = new ConcurrentHashMap<>();

    // Registro global de entidades activas para limpieza absoluta
    private final List<Entity> allActiveEntities = Collections.synchronizedList(new ArrayList<>());
    private final List<BukkitTask> allActiveTasks = Collections.synchronizedList(new ArrayList<>());

    public ActiveEntityManager(HabilidadesBedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Registra una entidad creada por una habilidad de un jugador.
     */
    public void trackEntity(Player player, Entity entity) {
        if (entity == null) return;
        playerEntities.computeIfAbsent(player.getUniqueId(), k -> Collections.synchronizedList(new ArrayList<>())).add(entity);
        allActiveEntities.add(entity);
    }

    /**
     * Registra una tarea Bukkit asociada a una habilidad de un jugador.
     */
    public void trackTask(Player player, BukkitTask task) {
        if (task == null) return;
        playerTasks.computeIfAbsent(player.getUniqueId(), k -> Collections.synchronizedList(new ArrayList<>())).add(task);
        allActiveTasks.add(task);
    }

    /**
     * Remueve el rastreo de una entidad una vez que ha concluido su ciclo natural.
     */
    public void untrackEntity(Entity entity) {
        if (entity == null) return;
        allActiveEntities.remove(entity);
        playerEntities.values().forEach(list -> list.remove(entity));
        if (entity.isValid()) {
            entity.remove();
        }
    }

    /**
     * Remueve el rastreo de una tarea una vez ejecutada.
     */
    public void untrackTask(BukkitTask task) {
        if (task == null) return;
        allActiveTasks.remove(task);
        playerTasks.values().forEach(list -> list.remove(task));
    }

    /**
     * Cancela tareas y elimina entidades de un jugador específico.
     */
    public void cleanupPlayer(UUID playerId) {
        List<BukkitTask> tasks = playerTasks.remove(playerId);
        if (tasks != null) {
            for (BukkitTask task : tasks) {
                try {
                    task.cancel();
                } catch (Exception ignored) {}
                allActiveTasks.remove(task);
            }
        }

        List<Entity> entities = playerEntities.remove(playerId);
        if (entities != null) {
            for (Entity entity : entities) {
                if (entity != null && entity.isValid()) {
                    try {
                        entity.remove();
                    } catch (Exception ignored) {}
                }
                allActiveEntities.remove(entity);
            }
        }
    }

    /**
     * Limpieza total requerida en onDisable() o reinicio de arena.
     */
    public void cleanupAll() {
        for (BukkitTask task : allActiveTasks) {
            try {
                task.cancel();
            } catch (Exception ignored) {}
        }
        allActiveTasks.clear();
        playerTasks.clear();

        for (Entity entity : allActiveEntities) {
            if (entity != null && entity.isValid()) {
                try {
                    entity.remove();
                } catch (Exception ignored) {}
            }
        }
        allActiveEntities.clear();
        playerEntities.clear();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        cleanupPlayer(uuid);
        plugin.getCooldownManager().cleanupPlayer(uuid);
        plugin.getFallDamageManager().cleanupPlayer(uuid);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        cleanupPlayer(event.getEntity().getUniqueId());
    }
}
