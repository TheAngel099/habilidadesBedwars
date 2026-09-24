package com.theangel099.habilidadesbedwars.manager;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;

public class ConfigManager {

    private final HabilidadesBedwarsPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(HabilidadesBedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        try {
            plugin.reloadConfig();
            this.config = plugin.getConfig();
            plugin.getLogger().info("Configuración cargada correctamente.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Fallo al leer config.yml. Asegúrate de que el formato YAML sea válido.", e);
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }
    
    public int getFallDamageImmunityDuration() {
        return config.getInt("settings.fall-damage-immunity-duration", 3);
    }
    
    public String getMessage(String path) {
        return config.getString("settings.messages." + path, "&cMessage not found: " + path);
    }
    
}

