package com.theangel099.habilidadesbedwars.manager;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import com.theangel099.habilidadesbedwars.ability.Ability;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final HabilidadesBedwarsPlugin plugin;
    // Map<PlayerUUID, Map<AbilityID, ExpirationTimeMillis>>
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private final Map<UUID, Map<String, ScheduledTask>> cooldownTasks = new HashMap<>();

    public CooldownManager(HabilidadesBedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void setCooldown(Player player, Ability ability) {
        cooldowns.putIfAbsent(player.getUniqueId(), new HashMap<>());
        cooldownTasks.putIfAbsent(player.getUniqueId(), new HashMap<>());
        
        long expirationTime = System.currentTimeMillis() + (ability.getCooldown() * 1000L);
        cooldowns.get(player.getUniqueId()).put(ability.getId(), expirationTime);
        
        // Iniciar tarea dinámica para actualizar el nombre del item
        ScheduledTask task = player.getScheduler().runAtFixedRate(plugin, t -> {
            if (!player.isOnline()) {
                t.cancel();
                return;
            }
            
            long remainingMillis = expirationTime - System.currentTimeMillis();
            long remainingSeconds = (long) Math.ceil(remainingMillis / 1000.0);
            
            if (remainingSeconds <= 0) {
                updateItemName(player, ability, 0);
                // Avisar que la habilidad está lista
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                t.cancel();
                cooldowns.get(player.getUniqueId()).remove(ability.getId());
                cooldownTasks.get(player.getUniqueId()).remove(ability.getId());
                return;
            }
            
            updateItemName(player, ability, remainingSeconds);
        }, null, 1L, 20L); // Ejecutar cada segundo
        
        // Cancelar tarea anterior si existía (por si se reinicia el cooldown)
        if (cooldownTasks.get(player.getUniqueId()).containsKey(ability.getId())) {
            cooldownTasks.get(player.getUniqueId()).get(ability.getId()).cancel();
        }
        cooldownTasks.get(player.getUniqueId()).put(ability.getId(), task);
    }

    private void updateItemName(Player player, Ability ability, long remainingSeconds) {
        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            Ability found = plugin.getAbilityManager().getAbilityByItem(item);
            if (found != null && found.getId().equals(ability.getId())) {
                org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    String baseName = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().serialize(
                        net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(ability.getName())
                    ).replace("§", "&"); // normalizar colores
                    
                    if (remainingSeconds > 0) {
                        Component newName = LegacyComponentSerializer.legacyAmpersand().deserialize(baseName + " &8(&c" + remainingSeconds + "s&8)");
                        meta.displayName(newName);
                    } else {
                        Component newName = LegacyComponentSerializer.legacyAmpersand().deserialize(baseName);
                        meta.displayName(newName);
                    }
                    item.setItemMeta(meta);
                }
            }
        }
    }

    public boolean isOnCooldown(Player player, Ability ability) {
        if (!cooldowns.containsKey(player.getUniqueId())) {
            return false;
        }
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (!playerCooldowns.containsKey(ability.getId())) {
            return false;
        }

        long expirationTime = playerCooldowns.get(ability.getId());
        long currentTime = System.currentTimeMillis();
        if (currentTime < expirationTime) {
            long remainingMillis = expirationTime - currentTime;
            long remainingSeconds = Math.max(1, (long) Math.ceil(remainingMillis / 1000.0));
            sendCooldownMessage(player, ability, remainingSeconds);
            return true;
        }

        // Ya expiró, limpiamos la memoria
        playerCooldowns.remove(ability.getId());
        if (cooldownTasks.containsKey(player.getUniqueId())) {
            ScheduledTask task = cooldownTasks.get(player.getUniqueId()).remove(ability.getId());
            if (task != null) task.cancel();
        }
        return false;
    }

    public void cleanupPlayer(UUID uuid) {
        cooldowns.remove(uuid);
        if (cooldownTasks.containsKey(uuid)) {
            for (ScheduledTask task : cooldownTasks.get(uuid).values()) {
                task.cancel();
            }
            cooldownTasks.remove(uuid);
        }
    }

    public void clearAll() {
        cooldowns.clear();
        for (Map<String, ScheduledTask> map : cooldownTasks.values()) {
            for (ScheduledTask task : map.values()) {
                task.cancel();
            }
        }
        cooldownTasks.clear();
    }

    private void sendCooldownMessage(Player player, Ability ability, long remainingSeconds) {
        String msgStr = plugin.getConfigManager().getMessage("cooldown-active")
                .replace("%ability%", ability.getName())
                .replace("%time%", String.valueOf(remainingSeconds));
        
        // Adventure API (Paper) para Action Bar
        Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(msgStr);
        player.sendActionBar(message);
    }
}

