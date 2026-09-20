package com.theangel099.habilidadesbedwars.listener;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import com.theangel099.habilidadesbedwars.ability.Ability;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    private final HabilidadesBedwarsPlugin plugin;

    public PlayerInteractListener(HabilidadesBedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        // Buscar si el ítem corresponde a una habilidad registrada
        Ability ability = plugin.getAbilityManager().getAbilityByMaterial(item.getType());
        
        if (ability != null) {
            // Verificar si el juego está activo a través de MBedwars
            if (!com.theangel099.habilidadesbedwars.mbedwars.BedwarsIntegration.isPlayerInActiveArena(player)) {
                // Opcional: Descomentar si SOLO quieres que funcione en partida. 
                // Por ahora lo dejamos activo para permitir pruebas si la API falla.
                // return;
            }
            
            if (plugin.getCooldownManager().isOnCooldown(player, ability)) {
                event.setCancelled(true);
                return;
            }

            // Castear la habilidad con manejo de errores
            try {
                boolean success = ability.cast(player);
                if (success) {
                    // Aplicar cooldown solo si fue exitosa
                    plugin.getCooldownManager().setCooldown(player, ability);
                    // Opcional: reducir cantidad de ítems si son consumibles
                    // item.setAmount(item.getAmount() - 1);
                }
            } catch (Exception e) {
                plugin.getLogger().log(java.util.logging.Level.SEVERE, 
                    "Error crítico al ejecutar la habilidad '" + ability.getId() + "' por el jugador " + player.getName() + ". Revisa el stacktrace para identificar el problema específico:", e);
                player.sendMessage("§cOcurrió un error interno al usar esta habilidad. Contacta a un administrador.");
            }
            
            event.setCancelled(true);
        }
    }
}
