package com.theangel099.habilidadesbedwars.ability;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public abstract class Ability {

    protected final HabilidadesBedwarsPlugin plugin;
    protected final String id;
    
    protected String name;
    protected AbilityCategory category;
    protected Material itemMaterial;
    protected int cooldown; // en segundos

    public Ability(HabilidadesBedwarsPlugin plugin, String id) {
        this.plugin = plugin;
        this.id = id;
        loadConfig();
    }

    /**
     * Carga o recarga la configuración específica de esta habilidad.
     */
    public void loadConfig() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("abilities." + id);
        if (section != null) {
            this.name = section.getString("name", id);
            try {
                this.category = AbilityCategory.valueOf(section.getString("category", "COMUN").toUpperCase());
            } catch (IllegalArgumentException e) {
                this.category = AbilityCategory.COMUN;
            }
            String matName = section.getString("item", "STONE");
            Material mat = Material.matchMaterial(matName);
            this.itemMaterial = (mat != null) ? mat : Material.STONE;
            this.cooldown = section.getInt("cooldown", 10);
            
            // Permitir que las subclases carguen mecánicas personalizadas
            loadMechanics(section.getConfigurationSection("mechanics"));
        }
    }

    /**
     * Permite a las clases hijas cargar configuraciones específicas (ej. daño, velocidad).
     */
    protected abstract void loadMechanics(ConfigurationSection mechanicsSection);

    /**
     * Método principal que ejecuta la habilidad.
     * @param player El jugador que casteó la habilidad.
     * @return true si se casteó con éxito, false si falló (para no aplicar cooldown).
     */
    public abstract boolean cast(Player player);

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public AbilityCategory getCategory() { return category; }
    public Material getItemMaterial() { return itemMaterial; }
    public int getCooldown() { return cooldown; }
}

