package com.theangel099.habilidadesbedwars.ability;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public abstract class Ability {

    protected final HabilidadesBedwarsPlugin plugin;
    protected final String id;
    
    protected boolean enabled;
    protected String name;
    protected AbilityCategory category;
    protected Material itemMaterial;
    protected int cooldown; // en segundos
    protected int customModelData;
    protected java.util.List<String> lore;
    protected final org.bukkit.NamespacedKey abilityKey;

    protected Ability(HabilidadesBedwarsPlugin plugin, String id) {
        this.plugin = plugin;
        this.id = id;
        this.abilityKey = new org.bukkit.NamespacedKey(plugin, "ability_id");
    }

    /**
     * Carga o recarga la configuración específica de esta habilidad desde su propio archivo.
     */
    public void loadConfig() {
        java.io.File dir = new java.io.File(plugin.getDataFolder(), "habilidades");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        java.io.File file = new java.io.File(dir, id + ".yml");
        org.bukkit.configuration.file.YamlConfiguration section = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(file);
        
        if (!file.exists()) {
            section.set("enabled", true);
            section.set("name", "<bold><gradient:#00bfff:#8a2be2>" + id + "</gradient></bold>");
            section.set("category", "COMUN");
            section.set("item", "FEATHER");
            section.set("cooldown", 15);
            section.set("custom-model-data", 0);
            section.set("lore", java.util.Arrays.asList("<gray>Habilidad base.", "<yellow>Cooldown: %cooldown%s"));
            try {
                section.save(file);
            } catch (java.io.IOException e) {
                plugin.getLogger().warning("No se pudo crear el archivo para " + id);
            }
        }

        this.enabled = section.getBoolean("enabled", true);
        this.name = section.getString("name", id);
        try {
            this.category = AbilityCategory.valueOf(section.getString("category", "COMUN").toUpperCase());
        } catch (IllegalArgumentException e) {
            this.category = AbilityCategory.COMUN;
        }
        String matName = section.getString("item", "STONE");
        if (matName == null) matName = "STONE";
        Material mat = Material.getMaterial(matName.toUpperCase());
        this.itemMaterial = (mat != null) ? mat : Material.STONE;
        this.cooldown = section.getInt("cooldown", 10);
        this.customModelData = section.getInt("custom-model-data", 0);
        this.lore = section.getStringList("lore");
        
        // Permitir que las subclases carguen mecánicas personalizadas
        loadMechanics(section.getConfigurationSection("mechanics"));
        
        // Autoguardado para asegurar que si se agregaron mechanics por código, se reflejen (opcional)
        // pero preferible no reescribir si ya existe para preservar comentarios.
    }

    /**
     * Crea un ItemStack representativo de esta habilidad con PDC único.
     */
    public org.bukkit.inventory.ItemStack createItem(int amount) {
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(this.itemMaterial, Math.max(1, amount));
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // Adventure API para nombre
            net.kyori.adventure.text.Component displayName;
            if (this.name.contains("&") || this.name.contains("§")) {
                displayName = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(this.name);
            } else {
                displayName = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(this.name);
            }
            meta.displayName(displayName);
            
            if (this.lore != null && !this.lore.isEmpty()) {
                java.util.List<net.kyori.adventure.text.Component> loreComponents = new java.util.ArrayList<>();
                for (String line : this.lore) {
                    String parsedLine = line.replace("%cooldown%", String.valueOf(this.cooldown));
                    if (parsedLine.contains("&") || parsedLine.contains("§")) {
                        loreComponents.add(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(parsedLine));
                    } else {
                        loreComponents.add(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(parsedLine));
                    }
                }
                meta.lore(loreComponents);
            }
            
            // Marcar con PDC único para evitar colisiones con ítems regulares
            meta.getPersistentDataContainer().set(this.abilityKey, org.bukkit.persistence.PersistentDataType.STRING, this.id);
            
            if (this.customModelData > 0) {
                meta.setCustomModelData(this.customModelData);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Comprueba si un ItemStack dado contiene el tag PDC correspondiente a esta habilidad.
     */
    public boolean isAbilityItem(org.bukkit.inventory.ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        String val = meta.getPersistentDataContainer().get(this.abilityKey, org.bukkit.persistence.PersistentDataType.STRING);
        return this.id.equalsIgnoreCase(val);
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
    public boolean isEnabled() { return enabled; }
    public String getId() { return id; }
    public String getName() { return name; }
    public AbilityCategory getCategory() { return category; }
    public Material getItemMaterial() { return itemMaterial; }
    public int getCooldown() { return cooldown; }
    public int getCustomModelData() { return customModelData; }
    public org.bukkit.NamespacedKey getAbilityKey() { return abilityKey; }
}

