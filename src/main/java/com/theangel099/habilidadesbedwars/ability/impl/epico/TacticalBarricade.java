package com.theangel099.habilidadesbedwars.ability.impl.epico;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import com.theangel099.habilidadesbedwars.ability.Ability;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class TacticalBarricade extends Ability {

    private int durationSeconds;
    private int width;
    private int height;
    private Material displayBlockMaterial;

    public TacticalBarricade(HabilidadesBedwarsPlugin plugin) {
        super(plugin, "tactical_barricade");
    }

    @Override
    protected void loadMechanics(ConfigurationSection mechanicsSection) {
        if (mechanicsSection != null) {
            this.durationSeconds = mechanicsSection.getInt("duration", 5);
            this.width = mechanicsSection.getInt("width", 3);
            this.height = mechanicsSection.getInt("height", 2);
            String matName = mechanicsSection.getString("display-block", "BRICKS");
            if (matName == null) matName = "BRICKS";
            Material mat = Material.getMaterial(matName.toUpperCase());
            this.displayBlockMaterial = (mat != null) ? mat : Material.BRICKS;
        } else {
            this.durationSeconds = 5;
            this.width = 3;
            this.height = 2;
            this.displayBlockMaterial = Material.BRICKS;
        }
    }

    @Override
    public boolean cast(Player player) {
        Location playerLoc = player.getLocation();
        Vector lookDir = playerLoc.getDirection().setY(0);
        if (lookDir.lengthSquared() == 0) {
            lookDir = new Vector(0, 0, 1);
        } else {
            lookDir.normalize();
        }

        // Vector ortogonal hacia la derecha (perpendicular a la mirada del jugador)
        Vector rightDir = new Vector(-lookDir.getZ(), 0, lookDir.getX()).normalize();

        Location spawnLoc = playerLoc.clone().add(lookDir.clone().multiply(2.0));
        spawnLoc.setY(Math.floor(spawnLoc.getY())); // Alinear a la cuadrícula Y

        BlockData blockData = displayBlockMaterial.createBlockData();
        double halfWidthOffset = (width - 1) / 2.0;
        
        java.util.Map<Location, BlockData> originalBlocks = new java.util.HashMap<>();
        
        // Determinar a qué jugadores enviar los paquetes
        List<Player> targetPlayers = new ArrayList<>();
        if (com.theangel099.habilidadesbedwars.mbedwars.BedwarsIntegration.isPlayerInActiveArena(player)) {
            de.marcely.bedwars.api.arena.Arena arena = de.marcely.bedwars.api.BedwarsAPI.getGameAPI().getArenaByPlayer(player);
            if (arena != null) {
                targetPlayers.addAll(arena.getPlayers());
            }
        }
        if (targetPlayers.isEmpty()) {
            // Failsafe: enviar a todos en el mundo cerca
            for (Player p : spawnLoc.getWorld().getPlayers()) {
                if (p.getLocation().distanceSquared(spawnLoc) < 2500) {
                    targetPlayers.add(p);
                }
            }
        }

        List<org.bukkit.entity.Entity> spawnedEntities = new ArrayList<>();

        for (int w = 0; w < width; w++) {
            double lateralOffset = (w - halfWidthOffset);
            Location columnBaseLoc = spawnLoc.clone().add(rightDir.clone().multiply(lateralOffset));
            
            // Alinear al centro del bloque
            columnBaseLoc.setX(columnBaseLoc.getBlockX() + 0.5);
            columnBaseLoc.setZ(columnBaseLoc.getBlockZ() + 0.5);

            for (int h = 0; h < height; h++) {
                Location blockLoc = columnBaseLoc.clone().add(0, h, 0).getBlock().getLocation(); // Obtener loc exacta de bloque
                
                // No sobreescribir bloques que ya son sólidos reales (opcional, para no romper el mapa)
                if (blockLoc.getBlock().getType().isSolid()) continue;
                
                originalBlocks.put(blockLoc, blockLoc.getBlock().getBlockData());
                
                // Enviar paquete falso
                for (Player p : targetPlayers) {
                    p.sendBlockChange(blockLoc, blockData);
                }
            }
            
            // Re-agregar Interaction Entity para intercepción nativa de proyectiles del lado del servidor
            org.bukkit.entity.Interaction interaction = (org.bukkit.entity.Interaction) spawnLoc.getWorld().spawnEntity(columnBaseLoc, EntityType.INTERACTION);
            interaction.setInteractionWidth(1.0f);
            interaction.setInteractionHeight((float) height);
            interaction.setResponsive(true); // Hace que intercepte proyectiles nativamente

            spawnedEntities.add(interaction);
            plugin.getActiveEntityManager().trackEntity(player, interaction);
        }
        
        spawnLoc.getWorld().playSound(spawnLoc, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);

        // Tarea para limpiar los bloques falsos y las entidades
        final org.bukkit.scheduler.BukkitTask[] taskRef = new org.bukkit.scheduler.BukkitTask[1];
        
        taskRef[0] = new BukkitRunnable() {
            @Override
            public void run() {
                // Eliminar Interaction Entities
                for (org.bukkit.entity.Entity entity : spawnedEntities) {
                    plugin.getActiveEntityManager().untrackEntity(entity);
                }
                
                // Restaurar bloques
                for (java.util.Map.Entry<Location, BlockData> entry : originalBlocks.entrySet()) {
                    for (Player p : targetPlayers) {
                        if (p.isOnline()) {
                            p.sendBlockChange(entry.getKey(), entry.getValue());
                        }
                    }
                }
                
                // Efecto visual de rotura y sonido de desaparición
                try {
                    spawnLoc.getWorld().spawnParticle(org.bukkit.Particle.BLOCK, spawnLoc.clone().add(0, 1, 0), 30, 0.5, 0.5, 0.5, blockData);
                } catch (Exception ignored) {}
                spawnLoc.getWorld().playSound(spawnLoc, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.8f);
                
                plugin.getActiveEntityManager().untrackTask(taskRef[0]);
            }
        }.runTaskLater(plugin, durationSeconds * 20L);

        plugin.getActiveEntityManager().trackTask(player, taskRef[0]);

        return true;
    }
}

