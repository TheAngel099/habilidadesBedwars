package com.theangel099.habilidadesbedwars.command;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class HabilidadesCommand implements CommandExecutor {

    private final HabilidadesBedwarsPlugin plugin;

    public HabilidadesCommand(HabilidadesBedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission("habilidadesbedwars.admin")) {
            sender.sendMessage(Component.text("No tienes permisos para usar este comando.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Uso: /" + label + " reload", NamedTextColor.RED));
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            try {
                // 1. Recargar config
                plugin.getConfigManager().load();
                // 2. Recargar habilidades (por si activaron/desactivaron alguna en config)
                plugin.getAbilityManager().loadAbilities();
                
                sender.sendMessage(Component.text("¡HabilidadesBedwars recargado con éxito!", NamedTextColor.GREEN));
            } catch (Exception e) {
                com.theangel099.habilidadesbedwars.utils.ErrorLogger.logGeneralError(plugin, "Comando /reload", e);
                sender.sendMessage(Component.text("Hubo un error al recargar. Revisa la consola y errores.log.", NamedTextColor.RED));
            }
            return true;
        }

        sender.sendMessage(Component.text("Comando desconocido.", NamedTextColor.RED));
        return true;
    }
}
