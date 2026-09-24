package com.theangel099.habilidadesbedwars.ability.impl.legendario;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import com.theangel099.habilidadesbedwars.ability.Ability;
import com.theangel099.habilidadesbedwars.mbedwars.BedwarsIntegration;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import java.util.Collection;

public class GravitySingularity extends Ability {

    private double projectileSpeed;
    private double pullRadius;
    private double pullStrength;
    private int durationTicks;

    public GravitySingularity(HabilidadesBedwarsPlugin plugin) {
        super(plugin, "gravity_singularity");
    }

    @Override
    protected void loadMechanics(ConfigurationSection section) {
        if (section != null) {
            this.projectileSpeed = section.getDouble("projectile-speed", 1.5);
            this.pullRadius = section.getDouble("pull-radius", 6.0);
            this.pullStrength = section.getDouble("pull-strength", 0.08);
            this.durationTicks = section.getInt("duration-ticks", 80); // 4 seconds
        } else {
            this.projectileSpeed = 1.5;
            this.pullRadius = 6.0;
            this.pullStrength = 0.08;
            this.durationTicks = 80;
        }
    }

    @Override
    public boolean cast(Player player) {
        Location startLoc = player.getEyeLocation();
        Vector velocity = startLoc.getDirection().normalize().multiply(projectileSpeed);
        
        player.getWorld().playSound(startLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);

        ItemDisplay projectile = player.getWorld().spawn(startLoc, ItemDisplay.class, display -> {
            display.setItemStack(new ItemStack(Material.ENDER_PEARL));
            display.setBillboard(ItemDisplay.Billboard.CENTER);
            display.setTeleportDuration(1); // Para 60 FPS interpolation
            
            Transformation transform = display.getTransformation();
            transform.getScale().set(0.7f, 0.7f, 0.7f);
            display.setTransformation(transform);
        });

        plugin.getActiveEntityManager().trackEntity(player, projectile);

        ScheduledTask flightTask = projectile.getScheduler().runAtFixedRate(plugin, new java.util.function.Consumer<ScheduledTask>() {
            private final Location currentLoc = startLoc.clone();
            private int flightTicks = 0;

            @Override
            public void accept(ScheduledTask t) {
                if (!projectile.isValid() || flightTicks > 60) { // Timeout de vuelo
                    projectile.remove();
                    plugin.getActiveEntityManager().untrackEntity(projectile);
                    t.cancel();
                    return;
                }

                currentLoc.add(velocity);
                flightTicks++;

                projectile.teleport(currentLoc);
                projectile.setTeleportDuration(1);
                
                // Rastro espacial
                currentLoc.getWorld().spawnParticle(Particle.PORTAL, currentLoc, 5, 0.1, 0.1, 0.1, 0.05);

                if (currentLoc.getBlock().getType().isSolid()) {
                    t.cancel();
                    createSingularity(player, currentLoc);
                    projectile.remove();
                    plugin.getActiveEntityManager().untrackEntity(projectile);
                }
            }
        }, null, 1L, 1L);

        plugin.getActiveEntityManager().trackTask(player, flightTask);
        return true;
    }

    private void createSingularity(Player caster, Location center) {
        center.getWorld().playSound(center, Sound.BLOCK_BEACON_ACTIVATE, 1.5f, 0.5f);
        double pullRadiusSq = pullRadius * pullRadius;

        // Entidad base inofensiva para renderizar el agujero y anclar la tarea
        ItemDisplay singularityCore = center.getWorld().spawn(center, ItemDisplay.class, display -> {
            display.setItemStack(new ItemStack(Material.BLACK_CONCRETE));
            display.setBillboard(ItemDisplay.Billboard.CENTER);
            
            Transformation transform = display.getTransformation();
            transform.getScale().set(0.5f, 0.5f, 0.5f);
            display.setTransformation(transform);
        });
        plugin.getActiveEntityManager().trackEntity(caster, singularityCore);

        ScheduledTask singularityTask = singularityCore.getScheduler().runAtFixedRate(plugin, new java.util.function.Consumer<ScheduledTask>() {
            private int ticks = 0;
            private double angle = 0;

            @Override
            public void accept(ScheduledTask t) {
                if (ticks >= durationTicks || !singularityCore.isValid()) {
                    singularityCore.getWorld().playSound(center, Sound.BLOCK_BEACON_DEACTIVATE, 1.5f, 0.5f);
                    
                    // Efecto de explosión final al colapsar
                    center.getWorld().spawnParticle(Particle.SONIC_BOOM, center, 1);
                    
                    singularityCore.remove();
                    plugin.getActiveEntityManager().untrackEntity(singularityCore);
                    t.cancel();
                    return;
                }

                // Matemáticas vectoriales para animación espiral de agujero negro
                angle += 0.4;
                double currentRadius = pullRadius - ((ticks % 20) * (pullRadius / 20.0));
                double x = Math.cos(angle) * currentRadius;
                double z = Math.sin(angle) * currentRadius;
                
                center.getWorld().spawnParticle(Particle.SQUID_INK, center.clone().add(x, 0, z), 2, 0, 0, 0, 0);
                center.getWorld().spawnParticle(Particle.PORTAL, center, 10, pullRadius/2, pullRadius/2, pullRadius/2, 0.5);

                // Lógica de succión (optimizado, cada 2 ticks)
                if (ticks % 2 == 0) {
                    Collection<Entity> nearby = center.getWorld().getNearbyEntities(center, pullRadius, pullRadius, pullRadius);
                    for (Entity entity : nearby) {
                        if (entity.equals(caster) || entity.equals(singularityCore)) continue;
                        
                        Location entLoc = entity.getLocation();
                        if (entLoc.distanceSquared(center) > pullRadiusSq) continue;

                        if (entity instanceof LivingEntity target && !(target instanceof org.bukkit.entity.ArmorStand)) {
                            // Fuego amigo
                            if (target instanceof Player targetPlayer) {
                                if (targetPlayer.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;
                                if (BedwarsIntegration.isPlayerInActiveArena(caster) && BedwarsIntegration.isPlayerInActiveArena(targetPlayer)) {
                                    if (!BedwarsIntegration.areEnemies(caster, targetPlayer)) {
                                        continue;
                                    }
                                }
                            }
                            
                            // Matemáticas de atracción
                            Vector toCenter = new Vector(
                                center.getX() - entLoc.getX(),
                                center.getY() - entLoc.getY(),
                                center.getZ() - entLoc.getZ()
                            );
                            
                            if (toCenter.lengthSquared() > 0) {
                                toCenter.normalize().multiply(pullStrength);
                                target.setVelocity(target.getVelocity().add(toCenter));
                            }
                        }
                    }
                }
                
                // Rotación del núcleo negro para darle vida usando JOML indirecto a través de Transformation (Yaw)
                Transformation transform = singularityCore.getTransformation();
                transform.getRightRotation().rotateY(0.1f);
                singularityCore.setTransformation(transform);
                singularityCore.setInterpolationDuration(1);
                
                ticks++;
            }
        }, null, 1L, 1L);
        plugin.getActiveEntityManager().trackTask(caster, singularityTask);
    }
}
