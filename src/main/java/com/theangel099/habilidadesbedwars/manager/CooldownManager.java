package com.theangel099.habilidadesbedwars.manager;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import com.theangel099.habilidadesbedwars.ability.Ability;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final HabilidadesBedwarsPlugin plugin;
    // Map<PlayerUUID, Map<AbilityID, ExpirationTimeMillis>>
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public CooldownManager(HabilidadesBedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void setCooldown(Player player, Ability ability) {
        cooldowns.putIfAbsent(player.getUniqueId(), new HashMap<>());
        long expirationTime = System.currentTimeMillis() + (ability.getCooldown() * 1000L);
        cooldowns.get(player.getUniqueId()).put(ability.getId(), expirationTime);
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
        return false;
    }

    public void cleanupPlayer(UUID uuid) {
        cooldowns.remove(uuid);
    }

    public void clearAll() {
        cooldowns.clear();
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

