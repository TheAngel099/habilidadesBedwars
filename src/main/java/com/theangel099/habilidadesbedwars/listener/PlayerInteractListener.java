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

        // Evitar que el evento se dispare dos veces (mano principal y secundaria)
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        // Buscar si el ítem corresponde a una habilidad registrada mediante PDC seguro
        Ability ability = plugin.getAbilityManager().getAbilityByItem(item);
        
        // Si no es un ítem de habilidad con PDC, ignoramos el evento para no interferir con bloques/ítems vanilla
        if (ability == null) {
            return;
        }

        // Cancelar la interacción vanilla (ej. colocar bloques o usar ítems vanilla)
        event.setCancelled(true);

        // Verificar si el juego está activo a través de MBedwars
        if (!com.theangel099.habilidadesbedwars.mbedwars.BedwarsIntegration.isPlayerInActiveArena(player)) {
            // Opcional: Descomentar si SOLO quieres que funcione en partida. 
            // Por ahora lo dejamos activo para permitir pruebas si la API falla.
            // return;
        }
        
        if (plugin.getCooldownManager().isOnCooldown(player, ability)) {
            return;
        }

        // Verificar si la habilidad está equipada en BedwarsCosmetics
        if (!plugin.getCosmeticsHook().hasAbilityEquipped(player, ability.getId())) {
            player.sendMessage(net.kyori.adventure.text.Component.text("¡No tienes esta habilidad equipada! Ve a la tienda de cosméticos para equiparla.", net.kyori.adventure.text.format.NamedTextColor.RED));
            return;
        }

        // Castear la habilidad con manejo de errores y profiler
        com.theangel099.habilidadesbedwars.utils.Profiler profiler = new com.theangel099.habilidadesbedwars.utils.Profiler(plugin, "Ability." + ability.getId());
        try {
            boolean success = ability.cast(player);
            if (success) {
                // Aplicar cooldown solo si fue exitosa
                plugin.getCooldownManager().setCooldown(player, ability);
                // Opcional: reducir cantidad de ítems si son consumibles
                // item.setAmount(item.getAmount() - 1);
            }
        } catch (Exception e) {
            com.theangel099.habilidadesbedwars.utils.ErrorLogger.logAbilityError(plugin, ability.getId(), e);
            player.sendMessage(net.kyori.adventure.text.Component.text("Ocurrió un error interno al usar esta habilidad. Contacta a un administrador.", net.kyori.adventure.text.format.NamedTextColor.RED));
        } finally {
            profiler.stop();
        }
    }
}
