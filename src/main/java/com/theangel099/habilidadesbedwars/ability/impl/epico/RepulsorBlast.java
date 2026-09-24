package com.theangel099.habilidadesbedwars.ability.impl.epico;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import com.theangel099.habilidadesbedwars.ability.Ability;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class RepulsorBlast extends Ability {

    private double radius;
    private double knockbackForce;
    private double yBoost;
    private int particleCount;

    public RepulsorBlast(HabilidadesBedwarsPlugin plugin) {
        super(plugin, "repulsor_blast");
    }

    @Override
    protected void loadMechanics(ConfigurationSection mechanicsSection) {
        if (mechanicsSection != null) {
            this.radius = mechanicsSection.getDouble("radius", 5.0);
            this.knockbackForce = mechanicsSection.getDouble("knockback-force", 1.5);
            this.yBoost = mechanicsSection.getDouble("y-boost", 0.8);
            this.particleCount = mechanicsSection.getInt("particle-count", 50);
        } else {
            this.radius = 5.0;
            this.knockbackForce = 1.5;
            this.yBoost = 0.8;
            this.particleCount = 50;
        }
    }

    @Override
    public boolean cast(Player player) {
        Location loc = player.getLocation();
        
        // Reproducir partículas de onda expansiva modernas con geometría circular matemática
        try {
            loc.getWorld().spawnParticle(Particle.SONIC_BOOM, loc.clone().add(0, 1, 0), 1);
            loc.getWorld().spawnParticle(Particle.GUST_EMITTER_LARGE, loc.clone().add(0, 0.8, 0), 1);
            
            // Anillo sónico horizontal con vectores normalizados
            int ringPoints = 32;
            for (int i = 0; i < ringPoints; i++) {
                double angle = (2 * Math.PI * i) / ringPoints;
                double x = Math.cos(angle) * (radius * 0.7);
                double z = Math.sin(angle) * (radius * 0.7);
                Location particleLoc = loc.clone().add(x, 0.5, z);
                loc.getWorld().spawnParticle(Particle.TRIAL_SPAWNER_DETECTION, particleLoc, 1, 0, 0, 0, 0);
            }
            
            loc.getWorld().spawnParticle(Particle.GUST, loc.clone().add(0, 0.5, 0), particleCount, radius / 3, 0.3, radius / 3, 0.08);
        } catch (Exception e) {
            loc.getWorld().spawnParticle(Particle.CLOUD, loc.clone().add(0, 0.5, 0), particleCount, radius / 2, 0.2, radius / 2, 0.2);
        }
        
        loc.getWorld().playSound(loc, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 1.1f);
        loc.getWorld().playSound(loc, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.2f, 0.8f);

        // Obtener entidades cercanas
        List<Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);
        boolean hitSomeone = false;
        
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
                
                // Calcular vector de empuje alejándose del jugador
                Vector knockbackDir = target.getLocation().toVector().subtract(player.getLocation().toVector());
                // Si están en el mismo punto exacto, añadir pequeño offset para evitar vector nulo
                if (knockbackDir.lengthSquared() == 0) {
                    knockbackDir = new Vector(1, 0, 0);
                }
                
                knockbackDir.normalize().multiply(knockbackForce);
                knockbackDir.setY(yBoost); // Darle un empuje vertical para que salgan volando
                
                target.setVelocity(knockbackDir);
                hitSomeone = true;
            }
        }
        
        return true; // Se casteó con éxito, incluso si no golpeó a nadie (se gasta la habilidad)
    }
}

