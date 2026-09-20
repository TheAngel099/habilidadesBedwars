package com.theangel099.habilidadesbedwars.ability.impl;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import com.theangel099.habilidadesbedwars.ability.Ability;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class SpectralVision extends Ability {

    private double radius;
    private int durationSeconds;

    public SpectralVision(HabilidadesBedwarsPlugin plugin) {
        super(plugin, "spectral_vision");
    }

    @Override
    protected void loadMechanics(ConfigurationSection mechanicsSection) {
        if (mechanicsSection != null) {
            this.radius = mechanicsSection.getDouble("radius", 30.0);
            this.durationSeconds = mechanicsSection.getInt("duration-seconds", 10);
        } else {
            this.radius = 30.0;
            this.durationSeconds = 10;
        }
    }

    @Override
    public boolean cast(Player player) {
        Location loc = player.getLocation();
        
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
        loc.getWorld().spawnParticle(Particle.PORTAL, loc.add(0, 1, 0), 100, 1.0, 1.0, 1.0, 0.5);

        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);
        int enemiesRevealed = 0;
        
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player target) {
                // Validación de radio esférico exacto
                if (player.getLocation().distanceSquared(target.getLocation()) > radius * radius) {
                    continue;
                }
                
                // Validación de enemigos
                if (!com.theangel099.habilidadesbedwars.mbedwars.BedwarsIntegration.areEnemies(player, target)) {
                    continue;
                }
                
                // Aplicar efecto Glowing (brillo global, advertencia: visible para todos los clientes que rendericen la entidad)
                target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, durationSeconds * 20, 1, false, false, true));
                enemiesRevealed++;
            }
        }
        
        // Notificar al jugador cuántos encontró (usando la API de Adventure de forma sencilla)
        if (enemiesRevealed > 0) {
            player.sendMessage("§a[Spectral Vision] §fHas revelado a §e" + enemiesRevealed + " §fenemigos cercanos por " + durationSeconds + "s.");
        } else {
            player.sendMessage("§c[Spectral Vision] §fNo hay enemigos cercanos en un radio de " + (int)radius + " bloques.");
        }
        
        return true;
    }
}

