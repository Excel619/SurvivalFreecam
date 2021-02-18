package com.gmail.excel8392.survivalfreecam;

import com.gmail.excel8392.npclib.NPCLibAPI;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class SurvivalFreecam extends JavaPlugin implements CommandExecutor, Listener {

    public static final int MAX_DISTANCE_SQUARED_FROM_NPC = (int) Math.pow(30, 2);
    public static final long DISTANCE_CHECK_INTERVAL = 20L; // Ticks
    public static final long MOB_CHECK_INTERVAL = 20 * 5; // Ticks

    private static SurvivalFreecam instance;

    private static final Map<Player, FreecamPlaceholder> inFreecam = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        NPCLibAPI.autoCachePlayerSkins(true);
        Bukkit.getPluginCommand("freecam").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(new FreecamListener(), this);
    }

    @Override
    public void onDisable() {
        for (Player player : inFreecam.keySet()) {
            player.setGameMode(GameMode.SURVIVAL);
            inFreecam.get(player).transferBack();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("survivalfreecam.toggle")) {
                toggleFreecam((Player) sender);
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            }
        }
        return true;
    }

    /**
     * Toggle's a player into or out of freecam mode
     * @param player - Player to enter/exit freecam
     * @return success
     */
    public static boolean toggleFreecam(Player player) {
        if (!inFreecam.containsKey(player)) {
            if (player.getLocation().add(0, -1, 0).getBlock().getType() == org.bukkit.Material.AIR) {
                player.sendMessage(ChatColor.RED + "You cannot enter freecam while in the air!");
                return false;
            }
            player.setGameMode(GameMode.SPECTATOR);
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE, entityPlayer);
            try {
                List<Object> list = new ArrayList<>();
                Class clazz = Class.forName("net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo$PlayerInfoData");
                Constructor playerInfoDataConstructor = clazz.getDeclaredConstructor(PacketPlayOutPlayerInfo.class, GameProfile.class, int.class, EnumGamemode.class, IChatBaseComponent.class);
                list.add(playerInfoDataConstructor.newInstance(packet, entityPlayer.getProfile(), 1, EnumGamemode.CREATIVE, entityPlayer.listName));
                Field field = packet.getClass().getDeclaredField("b");
                field.setAccessible(true);
                field.set(packet, list);
                entityPlayer.playerConnection.sendPacket(packet);
                entityPlayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(new PacketPlayOutGameStateChange.a(3), 3f));
            } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
            FreecamPlaceholder freecamPlaceholder = new FreecamPlaceholder(player);
            for (Entity entity : player.getLocation().getWorld().getNearbyEntities(player.getLocation(), 30, 30, 30, (entity) -> {
                if (!(entity instanceof Mob)) return false;
                if (((Mob) entity).getTarget() != player) {
                    if (entity instanceof Monster) {
                        return true;
                    }
                }
                return true;
            })) {
                ((Mob) entity).setTarget(freecamPlaceholder.getNPC().getEntityPlayer().getBukkitEntity());
            }
            inFreecam.put(player, freecamPlaceholder);
            player.sendMessage(ChatColor.GREEN + "You have entered freecam mode! If your body gets hit you will be sent back!");
        } else {
            player.setGameMode(GameMode.SURVIVAL);
            inFreecam.get(player).transferBack();
            inFreecam.remove(player);
        }
        return true;
    }

    public static Map<Player, FreecamPlaceholder> getInFreecam() {
        return inFreecam;
    }

    public static SurvivalFreecam getInstance() {
        return instance;
    }

}
