package com.theangel099.habilidadesbedwars.manager;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import com.theangel099.habilidadesbedwars.ability.Ability;
import com.theangel099.habilidadesbedwars.ability.impl.comun.KineticDash;
import com.theangel099.habilidadesbedwars.ability.impl.epico.RepulsorBlast;
import com.theangel099.habilidadesbedwars.ability.impl.epico.TacticalBarricade;
import com.theangel099.habilidadesbedwars.ability.impl.legendario.SpectralVision;
import com.theangel099.habilidadesbedwars.ability.impl.legendario.GravitySingularity;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class AbilityManager {

    private final HabilidadesBedwarsPlugin plugin;
    private final Map<String, Ability> abilitiesById = new HashMap<>();
    private final Map<Material, Ability> abilitiesByMaterial = new HashMap<>();

    private final org.bukkit.NamespacedKey abilityKey;

    public AbilityManager(HabilidadesBedwarsPlugin plugin) {
        this.plugin = plugin;
        this.abilityKey = new org.bukkit.NamespacedKey(plugin, "ability_id");
        loadAbilities();
    }

    public final void loadAbilities() {
        abilitiesById.clear();
        abilitiesByMaterial.clear();
        
        try {
            // Comunes
            registerAbility(new KineticDash(plugin));
            registerAbility(new com.theangel099.habilidadesbedwars.ability.impl.comun.FrostProjectile(plugin));

            // Épicas
            registerAbility(new TacticalBarricade(plugin));
            registerAbility(new RepulsorBlast(plugin));

            // Legendarias
            registerAbility(new SpectralVision(plugin));
            registerAbility(new GravitySingularity(plugin));

            plugin.getLogger().info("Habilidades cargadas exitosamente: " + abilitiesById.size());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error crítico al cargar las habilidades.", e);
        }
    }

    private void registerAbility(Ability ability) {
        ability.loadConfig(); // Inicializa variables como itemMaterial y config custom
        if (ability.isEnabled()) {
            abilitiesById.put(ability.getId(), ability);
            abilitiesByMaterial.put(ability.getItemMaterial(), ability);
        }
    }

    /**
     * Identifica una habilidad a partir de un ItemStack revisando su tag PDC seguro.
     */
    public Ability getAbilityByItem(org.bukkit.inventory.ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        
        String id = meta.getPersistentDataContainer().get(this.abilityKey, org.bukkit.persistence.PersistentDataType.STRING);
        if (id != null) {
            return abilitiesById.get(id);
        }
        return null;
    }

    public Ability getAbilityById(String id) {
        return abilitiesById.get(id);
    }

    public Ability getAbilityByMaterial(Material material) {
        return abilitiesByMaterial.get(material);
    }

    public Map<String, Ability> getAbilities() {
        return abilitiesById;
    }
}

