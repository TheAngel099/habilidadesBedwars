package com.theangel099.habilidadesbedwars.listener;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import com.theangel099.habilidadesbedwars.ability.Ability;
import de.marcely.bedwars.api.event.arena.RoundStartEvent;
import de.marcely.bedwars.api.event.player.PlayerIngamePostRespawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collection;

public class MBedwarsListener implements Listener {

    private final HabilidadesBedwarsPlugin plugin;

    public MBedwarsListener(HabilidadesBedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRoundStart(RoundStartEvent event) {
        Collection<Player> players = event.getArena().getPlayers();
        for (Player player : players) {
            player.getScheduler().runDelayed(plugin, task -> {
                if (player.isOnline()) {
                    giveEquippedAbility(player);
                    player.updateInventory();
                }
            }, null, 2L);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerIngamePostRespawnEvent event) {
        Player player = event.getPlayer();
        player.getScheduler().runDelayed(plugin, task -> {
            if (player.isOnline()) {
                giveEquippedAbility(player);
                player.updateInventory();
            }
        }, null, 2L);
    }

    private void giveEquippedAbility(Player player) {
        if (player == null) return;
        
        // Iteramos por todas las habilidades registradas
        for (Ability ability : plugin.getAbilityManager().getAbilities().values()) {
            // Verificamos si el jugador la tiene equipada en el cosmético
            if (plugin.getCosmeticsHook().hasAbilityEquipped(player, ability.getId())) {
                ItemStack abilityItem = ability.createItem(1);
                
                // Dar el item si no lo tiene verificando por PDC (PersistentDataContainer) en lugar del Material
                PlayerInventory inv = player.getInventory();
                boolean hasItem = false;
                for (ItemStack item : inv.getContents()) {
                    if (item == null) continue;
                    Ability found = plugin.getAbilityManager().getAbilityByItem(item);
                    if (found != null && found.getId().equals(ability.getId())) {
                        hasItem = true;
                        break;
                    }
                }
                
                if (!hasItem) {
                    inv.addItem(abilityItem);
                }
                break; // Solo puede tener una habilidad equipada, así que terminamos aquí
            }
        }
    }
}
