package com.theangel099.habilidadesbedwars.ability.impl;

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
        Location spawnLoc = player.getLocation().add(player.getLocation().getDirection().setY(0).normalize().multiply(2));
        spawnLoc.setY(Math.floor(spawnLoc.getY())); // Alinear a la cuadrícula Y

        // Alinear la rotación de la barricada según el jugador
        float yaw = player.getLocation().getYaw();
        
        List<BlockDisplay> displays = new ArrayList<>();
        BlockData blockData = displayBlockMaterial.createBlockData();

        // Generar la matriz de bloques usando la API de Display
        // Calculamos offset para centrarla respecto al jugador
        double halfWidth = width / 2.0;

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                // Calcular posición de este "bloque" en relación a spawnLoc (rotando según Yaw del jugador)
                double offsetX = (w - halfWidth) * Math.cos(Math.toRadians(yaw));
                double offsetZ = (w - halfWidth) * Math.sin(Math.toRadians(yaw));

                Location displayLoc = spawnLoc.clone().add(offsetX, h, offsetZ);

                BlockDisplay display = (BlockDisplay) spawnLoc.getWorld().spawnEntity(displayLoc, EntityType.BLOCK_DISPLAY);
                display.setBlock(blockData);
                
                // Aplicar escala estándar
                display.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 1, 0),
                        new Vector3f(1, 1, 1),
                        new AxisAngle4f(0, 0, 1, 0)
                ));
                
                displays.add(display);
            }
        }
        
        spawnLoc.getWorld().playSound(spawnLoc, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.0f);

        // Tarea programada para eliminar las entidades display
        new BukkitRunnable() {
            @Override
            public void run() {
                for (BlockDisplay bd : displays) {
                    if (bd.isValid()) {
                        bd.remove();
                    }
                }
                // Efecto visual de desaparición
                spawnLoc.getWorld().playSound(spawnLoc, Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
            }
        }.runTaskLater(plugin, durationSeconds * 20L);

        return true;
    }
}

