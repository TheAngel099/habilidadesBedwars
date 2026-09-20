package com.theangel099.habilidadesbedwars.manager;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import com.theangel099.habilidadesbedwars.ability.Ability;
import com.theangel099.habilidadesbedwars.ability.impl.KineticDash;
import com.theangel099.habilidadesbedwars.ability.impl.TacticalBarricade;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class AbilityManager {

    private final HabilidadesBedwarsPlugin plugin;
    private final Map<String, Ability> abilitiesById = new HashMap<>();
    private final Map<Material, Ability> abilitiesByMaterial = new HashMap<>();

    public AbilityManager(HabilidadesBedwarsPlugin plugin) {
        this.plugin = plugin;
        loadAbilities();
    }

    public final void loadAbilities() {
        abilitiesById.clear();
        abilitiesByMaterial.clear();
        
        try {
            registerAbility(new KineticDash(plugin));
            registerAbility(new TacticalBarricade(plugin));
            plugin.getLogger().info("Habilidades cargadas exitosamente: " + abilitiesById.size());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error crítico al cargar las habilidades.", e);
        }
    }

    private void registerAbility(Ability ability) {
        ability.loadConfig(); // Inicializa variables como itemMaterial y config custom
        if (plugin.getConfigManager().isAbilityEnabled(ability.getId())) {
            abilitiesById.put(ability.getId(), ability);
            abilitiesByMaterial.put(ability.getItemMaterial(), ability);
        }
    }

    public Ability getAbilityById(String id) {
        return abilitiesById.get(id);
    }

    public Ability getAbilityByMaterial(Material material) {
        return abilitiesByMaterial.get(material);
    }
}

