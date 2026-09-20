package com.theangel099.habilidadesbedwars.manager;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FallDamageManager implements Listener {

    private final HabilidadesBedwarsPlugin plugin;
    // Map<PlayerUUID, ExpirationTimeMillis>
    private final Map<UUID, Long> immunePlayers = new HashMap<>();

    public FallDamageManager(HabilidadesBedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Otorga inmunidad al daño de caída por el tiempo configurado.
     */
    public void grantImmunity(Player player) {
        int durationSeconds = plugin.getConfigManager().getFallDamageImmunityDuration();
        long expirationTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        immunePlayers.put(player.getUniqueId(), expirationTime);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (immunePlayers.containsKey(player.getUniqueId())) {
            long expirationTime = immunePlayers.get(player.getUniqueId());
            if (System.currentTimeMillis() < expirationTime) {
                // Anular daño de caída
                event.setCancelled(true);
                // Limpiar del mapa una vez se anula un golpe fuerte (opcional, 
                // aquí preferimos dejarlo todo el tiempo o quitarlo para balancear)
                immunePlayers.remove(player.getUniqueId());
            } else {
                // Ya expiró, limpiar map
                immunePlayers.remove(player.getUniqueId());
            }
        }
    }
}

