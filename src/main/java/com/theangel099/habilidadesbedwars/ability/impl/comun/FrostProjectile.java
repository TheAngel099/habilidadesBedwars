package com.theangel099.habilidadesbedwars.ability.impl.comun;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import com.theangel099.habilidadesbedwars.ability.Ability;
import com.theangel099.habilidadesbedwars.mbedwars.BedwarsIntegration;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import java.util.Collection;

public class FrostProjectile extends Ability {

    private int slownessDuration;
    private int slownessAmplifier;
    private double projectileSpeed;
    private double maxDistance;
    private double hitRadius;

    public FrostProjectile(HabilidadesBedwarsPlugin plugin) {
        super(plugin, "frost_projectile");
    }

    @Override
    protected void loadMechanics(ConfigurationSection mechanicsSection) {
        if (mechanicsSection != null) {
            this.slownessDuration = mechanicsSection.getInt("slowness-duration", 5) * 20; // En ticks
            this.slownessAmplifier = mechanicsSection.getInt("slowness-amplifier", 1); // Lentitud 2 es amplifier 1
            this.projectileSpeed = mechanicsSection.getDouble("speed", 1.5);
            this.maxDistance = mechanicsSection.getDouble("max-distance", 50.0);
            this.hitRadius = mechanicsSection.getDouble("hit-radius", 1.5);
        } else {
            this.slownessDuration = 5 * 20;
            this.slownessAmplifier = 1;
            this.projectileSpeed = 1.5;
            this.maxDistance = 50.0;
            this.hitRadius = 1.5;
        }
    }

    @Override
    public boolean cast(Player player) {
        Location startLoc = player.getEyeLocation();
        Vector direction = startLoc.getDirection().normalize();
        
        player.getWorld().playSound(startLoc, Sound.ENTITY_SNOWBALL_THROW, 1.0f, 0.5f);

        // Renderizado visual: Display Entity (ItemDisplay) para representar el hielo
        ItemDisplay iceDisplay = player.getWorld().spawn(startLoc, ItemDisplay.class, display -> {
            display.setItemStack(new ItemStack(Material.ICE));
            display.setBillboard(ItemDisplay.Billboard.CENTER);
            
            // Hacerlo un poco más pequeño
            Transformation transform = display.getTransformation();
            transform.getScale().set(0.5f, 0.5f, 0.5f);
            display.setTransformation(transform);
        });

        plugin.getActiveEntityManager().trackEntity(player, iceDisplay);

        // Loop principal del proyectil
        new BukkitRunnable() {
            private final Location currentLoc = startLoc.clone();
            private double distanceTraveled = 0.0;

            @Override
            public void run() {
                // Verificar si se canceló por alguna razón o superó la distancia máxima
                if (!iceDisplay.isValid() || distanceTraveled > maxDistance) {
                    iceDisplay.remove();
                    plugin.getActiveEntityManager().untrackEntity(iceDisplay);
                    this.cancel();
                    return;
                }

                // Mover el proyectil
                currentLoc.add(direction.clone().multiply(projectileSpeed));
                distanceTraveled += projectileSpeed;

                // Actualizar la posición visual suavemente (interpolación de 1 tick)
                iceDisplay.teleport(currentLoc);
                iceDisplay.setTeleportDuration(1); // 1 tick de interpolación para movimiento ultra suave

                // Efectos de partículas de rastro
                currentLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, currentLoc, 5, 0.2, 0.2, 0.2, 0.01);
                
                // Colisión con bloques
                if (currentLoc.getBlock().getType().isSolid()) {
                    shatter(currentLoc);
                    this.cancel();
                    return;
                }

                // Colisión con entidades (Matemáticas vectoriales/Hitbox simple)
                Collection<Entity> nearby = currentLoc.getWorld().getNearbyEntities(currentLoc, hitRadius, hitRadius, hitRadius);
                for (Entity entity : nearby) {
                    if (entity.equals(player) || entity.equals(iceDisplay)) continue;
                    
                    if (entity instanceof LivingEntity && !(entity instanceof org.bukkit.entity.ArmorStand)) {
                        LivingEntity target = (LivingEntity) entity;
                        
                        // Si es jugador, revisar si son enemigos usando BedwarsIntegration
                        if (target instanceof Player) {
                            Player targetPlayer = (Player) target;
                            // Check si está en modo espectador
                            if (targetPlayer.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;
                            
                            if (BedwarsIntegration.isPlayerInActiveArena(player) && 
                                BedwarsIntegration.isPlayerInActiveArena(targetPlayer)) {
                                if (!BedwarsIntegration.areEnemies(player, targetPlayer)) {
                                    continue; // Ignorar compañeros de equipo
                                }
                            }
                        }

                        // ¡Impacto!
                        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, slownessDuration, slownessAmplifier));
                        
                        // Efectos de impacto
                        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.2f);
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1.0f, 1.0f);
                        
                        shatter(currentLoc);
                        this.cancel();
                        return;
                    }
                }
            }

            private void shatter(Location loc) {
                iceDisplay.remove();
                plugin.getActiveEntityManager().untrackEntity(iceDisplay);
                loc.getWorld().spawnParticle(Particle.BLOCK, loc, 30, 0.3, 0.3, 0.3, Material.ICE.createBlockData());
                loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 0.8f, 0.8f);
            }

        }.runTaskTimer(plugin, 1L, 1L); // Correr cada tick

        return true;
    }
}
