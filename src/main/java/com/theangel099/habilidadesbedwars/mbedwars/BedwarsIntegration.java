package com.theangel099.habilidadesbedwars.mbedwars;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class BedwarsIntegration {

    /**
     * Verifica si el jugador está actualmente jugando en una arena activa.
     * Usa reflexión para evitar errores de compilación ya que Addons-API no incluye el core.
     * @param player El jugador a revisar.
     * @return true si está en una arena y está corriendo.
     */
    public static boolean isPlayerInActiveArena(Player player) {
        try {
            Class<?> apiClass = Class.forName("de.marcely.bedwars.api.BedwarsAPI");
            Method getArenaMethod = apiClass.getMethod("getArenaByPlayer", Player.class);
            Object arena = getArenaMethod.invoke(null, player);
            if (arena == null) {
                return false;
            }
            
            Method getStatusMethod = arena.getClass().getMethod("getStatus");
            Object status = getStatusMethod.invoke(arena);
            
            if (status != null && status.toString().equals("RUNNING")) {
                return true;
            }
            return false;
        } catch (Exception e) {
            // Fallback: Si no se encuentra la API (ej. en un entorno de pruebas sin Bedwars)
            // se puede cambiar a 'true' para probar las habilidades libremente.
            return false; 
        }
    }
}
