package com.theangel099.habilidadesbedwars.utils;

import com.theangel099.habilidadesbedwars.HabilidadesBedwarsPlugin;

public class Profiler {

    private final String name;
    private final long startTime;
    private final HabilidadesBedwarsPlugin plugin;

    public Profiler(HabilidadesBedwarsPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.startTime = System.currentTimeMillis();
    }

    public void stop() {
        long duration = System.currentTimeMillis() - startTime;
        if (duration > 5) { // Log if it takes more than 5ms
            plugin.getLogger().warning("[PROFILER] '" + name + "' tardó " + duration + "ms en ejecutarse.");
        }
    }
}
