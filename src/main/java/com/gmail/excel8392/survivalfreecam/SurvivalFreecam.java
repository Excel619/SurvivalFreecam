package com.gmail.excel8392.survivalfreecam;

import com.gmail.excel8392.npclib.NPCLibAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class SurvivalFreecam extends JavaPlugin {

    public static int MAX_DISTANCE_SQUARED_FROM_NPC = (int) Math.pow(30, 2);
    public static final long DISTANCE_CHECK_INTERVAL = 20L; // Ticks
    public static final long MOB_CHECK_INTERVAL = 20 * 5; // Ticks

    private static SurvivalFreecam instance;

    private static final Map<Player, FreecamPlaceholder> inFreecam = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        if (this.getConfig().contains("max-distance-from-npc")) {
            MAX_DISTANCE_SQUARED_FROM_NPC = (int) Math.pow(this.getConfig().getInt("max-distance-from-npc"), 2);
        }
        NPCLibAPI.autoCachePlayerSkins(true);
        Bukkit.getPluginCommand("freecam").setExecutor(new FreecamCommandExecutor());
        Bukkit.getPluginManager().registerEvents(new FreecamListener(), this);
    }

    @Override
    public void onDisable() {
        for (Player player : inFreecam.keySet()) {
            player.setGameMode(GameMode.SURVIVAL);
            inFreecam.get(player).transferBack();
        }
    }

    public static Map<Player, FreecamPlaceholder> getInFreecam() {
        return inFreecam;
    }

    public static SurvivalFreecam getInstance() {
        return instance;
    }

}
