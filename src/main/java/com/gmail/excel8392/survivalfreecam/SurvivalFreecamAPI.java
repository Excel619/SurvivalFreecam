package com.gmail.excel8392.survivalfreecam;

import com.gmail.excel8392.npclib.NPC;
import com.gmail.excel8392.npclib.NPCLibAPI;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SurvivalFreecamAPI {

    /**
     * Gets the JavaPlugin subclass of this plugin
     * @return SurvivalFreecam
     */
    public SurvivalFreecam getSurvivalFreecam() {
        return SurvivalFreecam.getInstance();
    }

    /**
     * Toggle's a player into or out of freecam mode
     * @param player - Player to enter/exit freecam
     * @return success
     */
    public static boolean toggleFreecam(Player player) {
        if (!SurvivalFreecam.getInFreecam().containsKey(player)) {
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
            } catch (NoSuchFieldException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException exception) {
                exception.printStackTrace();
                return false;
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
            SurvivalFreecam.getInFreecam().put(player, freecamPlaceholder);
            player.sendMessage(ChatColor.GREEN + "You have entered freecam mode! If your body gets hit you will be sent back!");
        } else {
            player.setGameMode(GameMode.SURVIVAL);
            SurvivalFreecam.getInFreecam().get(player).transferBack();
            SurvivalFreecam.getInFreecam().remove(player);
        }
        return true;
    }

    /**
     * Gets a map of players in freecam mode, and their placeholders
     * @return Map of freecam players and their freecam placeholders
     */
    public Map<Player, FreecamPlaceholder> getInFreecam() {
        return SurvivalFreecam.getInFreecam();
    }

    /**
     * Checks if a player is in freecam mode
     * @param player - player to check
     * @return boolean, if they are in freecam or not
     */
    public boolean isInFreecam(Player player) {
        return SurvivalFreecam.getInFreecam().containsKey(player);
    }

    /**
     * Checks if an EntityPlayer in the world is a placeholder for a freecam player
     * @param entityPlayer - EntityPlayer to check
     * @return boolean, if they are a placeholder
     */
    public boolean isPlaceholder(EntityPlayer entityPlayer) {
        return getPlaceholder(entityPlayer) != null;
    }

    /**
     * Checks if an NPC in the world is a placeholder for a freecam player
     * @param npc - NPC to check
     * @return boolean, if they are a placeholder
     */
    public boolean isPlaceholder(NPC npc) {
        return getPlaceholder(npc) != null;
    }

    /**
     * Gets a FreecamPlaceholder from it's EntityPlayer. Null if the EntityPlayer is not a FreecamPlaceholder.
     * @param entityPlayer - EntityPlayer to check
     * @return FreecamPlaceholder
     */
    public FreecamPlaceholder getPlaceholder(EntityPlayer entityPlayer) {
        if (!NPCLibAPI.isNPC(entityPlayer)) return null;
        NPC npc = NPCLibAPI.getNPCFromEntity(entityPlayer);
        for (Player player : SurvivalFreecam.getInFreecam().keySet()) {
            if (SurvivalFreecam.getInFreecam().get(player).getNPC() == npc) {
                return SurvivalFreecam.getInFreecam().get(player);
            }
        }
        return null;
    }

    /**
     * Gets a FreecamPlaceholder from it's NPC. Null if the NPC is not a FreecamPlaceholder.
     * @param npc - NPC to check
     * @return FreecamPlaceholder
     */
    public FreecamPlaceholder getPlaceholder(NPC npc) {
        for (Player player : SurvivalFreecam.getInFreecam().keySet()) {
            if (SurvivalFreecam.getInFreecam().get(player).getNPC() == npc) {
                return SurvivalFreecam.getInFreecam().get(player);
            }
        }
        return null;
    }

    /**
     * Gets a Player's FreecamPlaceholder. Null if the player is not in freecam mode.
     * @param player - Player to check
     * @return FreecamPlaceholder
     */
    public FreecamPlaceholder getPlaceholder(Player player) {
        if (!SurvivalFreecam.getInFreecam().containsKey(player)) return null;
        return SurvivalFreecam.getInFreecam().get(player);
    }

}
