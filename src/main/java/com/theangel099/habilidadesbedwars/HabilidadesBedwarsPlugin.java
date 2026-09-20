package com.theangel099.habilidadesbedwars;

import com.theangel099.habilidadesbedwars.manager.ConfigManager;
import com.theangel099.habilidadesbedwars.manager.CooldownManager;
import com.theangel099.habilidadesbedwars.manager.FallDamageManager;
import com.theangel099.habilidadesbedwars.manager.AbilityManager;
import com.theangel099.habilidadesbedwars.listener.PlayerInteractListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class HabilidadesBedwarsPlugin extends JavaPlugin {

    private static HabilidadesBedwarsPlugin instance;
    private ConfigManager configManager;
    private CooldownManager cooldownManager;
    private FallDamageManager fallDamageManager;
    private AbilityManager abilityManager;

    @Override
    public void onEnable() {
        instance = this;
        
        try {
            getLogger().info("Iniciando HabilidadesBedwars...");
            
            // 1. Cargar Configuración
            saveDefaultConfig();
            this.configManager = new ConfigManager(this);
            this.configManager.load();
            
            // 2. Inicializar Managers
            this.cooldownManager = new CooldownManager(this);
            this.fallDamageManager = new FallDamageManager(this);
            this.abilityManager = new AbilityManager(this);
            
            // 3. Registrar Listeners
            getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
            getServer().getPluginManager().registerEvents(this.fallDamageManager, this);
            
            // 4. Integración con MBedwars (Tienda/Addon API)
            // Aquí irá el registro en el API de MBedwars más adelante
            
            getLogger().info("HabilidadesBedwars habilitado correctamente.");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Ocurrió un error grave al iniciar el plugin. Revisa el stacktrace:", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("HabilidadesBedwars deshabilitado.");
        // Limpiar recursos si es necesario
    }

    public static HabilidadesBedwarsPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public FallDamageManager getFallDamageManager() {
        return fallDamageManager;
    }

    public AbilityManager getAbilityManager() {
        return abilityManager;
    }
}
