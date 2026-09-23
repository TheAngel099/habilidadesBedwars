package com.theangel099.habilidadesbedwars.hook;

import com.alexraton.bedwarscosmetics.Main;
import com.alexraton.bedwarscosmetics.cosmetics.Cosmetic;
import com.alexraton.bedwarscosmetics.cosmetics.CosmeticType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.Bukkit;

public class BedwarsCosmeticsHook {

    private final boolean enabled;

    public BedwarsCosmeticsHook() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("BedwarsCosmetics");
        this.enabled = plugin != null && plugin.isEnabled();
        if (this.enabled) {
            Bukkit.getLogger().info("[HabilidadesBedwars] Hook con BedwarsCosmetics establecido correctamente.");
        } else {
            Bukkit.getLogger().warning("[HabilidadesBedwars] BedwarsCosmetics no encontrado. Las habilidades no se podrán restringir por equipamiento.");
        }
    }

    /**
     * Verifica si el jugador tiene la habilidad equipada en BedwarsCosmetics.
     * Si el plugin de cosméticos no está, asume true para no romper la funcionalidad.
     */
    public boolean hasAbilityEquipped(Player player, String abilityId) {
        if (!enabled) return true;
        
        try {
            Cosmetic equipped = Main.getInstance().getCosmeticsManager().getEquipped(player.getUniqueId(), CosmeticType.ABILITY);
            if (equipped != null) {
                return equipped.getId().equalsIgnoreCase(abilityId);
            }
        } catch (Exception e) {
            // Failsafe in case of API change or CosmeticType.ABILITY not found
            return true;
        }
        return false;
    }
}
