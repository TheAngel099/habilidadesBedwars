package com.theangel099.habilidadesbedwars.utils;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class ErrorLogger {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void logAbilityError(HabilidadesBedwarsPlugin plugin, String abilityId, Exception exception) {
        // Log to console first
        plugin.getLogger().log(Level.SEVERE, "[ERROR CRÍTICO] La habilidad '" + abilityId + "' falló. Revisa errores.log para más detalles.");

        // Save to file
        File errorFile = new File(plugin.getDataFolder(), "errores.log");
        try {
            if (!errorFile.exists()) {
                errorFile.getParentFile().mkdirs();
                errorFile.createNewFile();
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(errorFile, true))) {
                writer.println("=========================================");
                writer.println("FECHA: " + DATE_FORMAT.format(new Date()));
                writer.println("VERSIÓN PLUGIN: " + plugin.getDescription().getVersion());
                writer.println("HABILIDAD: " + abilityId);
                writer.println("ERROR: " + exception.getMessage());
                writer.println("STACKTRACE:");
                exception.printStackTrace(writer);
                writer.println("=========================================\n");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo escribir en errores.log", e);
        }
    }

    public static void logGeneralError(HabilidadesBedwarsPlugin plugin, String context, Exception exception) {
        plugin.getLogger().log(Level.SEVERE, "[ERROR CRÍTICO] Ocurrió un error en: " + context + ". Revisa errores.log para más detalles.");

        File errorFile = new File(plugin.getDataFolder(), "errores.log");
        try {
            if (!errorFile.exists()) {
                errorFile.getParentFile().mkdirs();
                errorFile.createNewFile();
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(errorFile, true))) {
                writer.println("=========================================");
                writer.println("FECHA: " + DATE_FORMAT.format(new Date()));
                writer.println("VERSIÓN PLUGIN: " + plugin.getDescription().getVersion());
                writer.println("CONTEXTO: " + context);
                writer.println("ERROR: " + exception.getMessage());
                writer.println("STACKTRACE:");
                exception.printStackTrace(writer);
                writer.println("=========================================\n");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "No se pudo escribir en errores.log", e);
        }
    }
}
