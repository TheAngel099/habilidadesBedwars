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

    /**
     * Verifica si dos jugadores están en la misma arena y pertenecen a equipos distintos.
     */
    public static boolean areEnemies(Player p1, Player p2) {
        if (p1.equals(p2)) return false;
        
        try {
            Class<?> apiClass = Class.forName("de.marcely.bedwars.api.BedwarsAPI");
            Method getArenaMethod = apiClass.getMethod("getArenaByPlayer", Player.class);
            
            Object arena1 = getArenaMethod.invoke(null, p1);
            Object arena2 = getArenaMethod.invoke(null, p2);
            
            // Si no están en la misma arena, no son enemigos activos en la misma partida
            if (arena1 == null || arena2 == null || !arena1.equals(arena2)) {
                return false;
            }
            
            // Intentar obtener los equipos
            Method getTeamMethod;
            try {
                getTeamMethod = arena1.getClass().getMethod("getPlayerTeam", Player.class);
            } catch (NoSuchMethodException e) {
                getTeamMethod = arena1.getClass().getMethod("getTeam", Player.class);
            }
            
            Object team1 = getTeamMethod.invoke(arena1, p1);
            Object team2 = getTeamMethod.invoke(arena2, p2);
            
            // Si ambos tienen equipo y son diferentes, son enemigos
            if (team1 != null && team2 != null) {
                return !team1.equals(team2);
            }
            
            return true;
        } catch (Exception e) {
            // Fallback en entorno sin Bedwars: cualquiera distinto es enemigo
            return true;
        }
    }
}
