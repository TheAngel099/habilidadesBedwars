package com.theangel099.habilidadesbedwars.ability.impl;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import com.theangel099.habilidadesbedwars.ability.Ability;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KineticDash extends Ability {

    private double velocityMultiplier;
    private double yBoost;
    private int particleCount;
    private float soundPitch;

    public KineticDash(HabilidadesBedwarsPlugin plugin) {
        super(plugin, "kinetic_dash");
    }

    @Override
    protected void loadMechanics(ConfigurationSection mechanicsSection) {
        if (mechanicsSection != null) {
            this.velocityMultiplier = mechanicsSection.getDouble("velocity-multiplier", 1.5);
            this.yBoost = mechanicsSection.getDouble("y-boost", 0.5);
            this.particleCount = mechanicsSection.getInt("particle-count", 20);
            this.soundPitch = (float) mechanicsSection.getDouble("sound-pitch", 1.0);
        } else {
            this.velocityMultiplier = 1.5;
            this.yBoost = 0.5;
            this.particleCount = 20;
            this.soundPitch = 1.0f;
        }
    }

    @Override
    public boolean cast(Player player) {
        Location loc = player.getLocation();
        
        // Calcular vector direccional
        Vector direction = loc.getDirection();
        direction.setY(0).normalize().multiply(velocityMultiplier);
        direction.setY(yBoost); // Pequeño salto hacia arriba
        
        player.setVelocity(direction);
        
        // Otorgar inmunidad al daño de caída
        plugin.getFallDamageManager().grantImmunity(player);
        
        // Efectos visuales y de sonido
        try {
            loc.getWorld().spawnParticle(Particle.CLOUD, loc.add(0, 1, 0), particleCount, 0.5, 0.5, 0.5, 0.1);
        } catch (Exception e) {
            // Fallback general
        }
        
        loc.getWorld().playSound(loc, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0f, soundPitch);
        
        return true;
    }
}

