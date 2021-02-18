package com.gmail.excel8392.survivalfreecam;

import com.gmail.excel8392.npclib.NPC;
import com.gmail.excel8392.npclib.NPCLibAPI;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;

public class FreecamPlaceholder {

    private final NPC npc;
    private final int air;
    private final double health;
    private final int hunger;
    private final float saturation;
    private final Collection<PotionEffect> effects;
    private final double absorption;
    private final int fire;
    private final Inventory inventory;
    private final Location location;
    private final Player player;

    public FreecamPlaceholder(Player player) {
        this.air = player.getRemainingAir();
        this.health = player.getHealth();
        player.setHealth(20);
        this.hunger = player.getFoodLevel();
        player.setFoodLevel(20);
        this.saturation = player.getSaturation();
        player.setSaturation(0);
        this.effects = player.getActivePotionEffects();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false, false));
        this.absorption = player.getAbsorptionAmount();
        player.setAbsorptionAmount(0);
        this.fire = player.getFireTicks();
        player.setFireTicks(0);
        this.inventory = Bukkit.createInventory(null, InventoryType.PLAYER);
        this.inventory.setContents(player.getInventory().getContents());
        this.location = new Location(player.getLocation().getWorld(), player.getLocation().getX(), player.getLocation().getBlockY(), player.getLocation().getZ());
        this.player = player;
        this.npc = NPCLibAPI.createNPC(
                this.location,
                this.player.getUniqueId(),
                this.player.getName(),
                true,
                false,
                new Pair<>(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(player.getInventory().getItemInMainHand())),
                new Pair<>(EnumItemSlot.OFFHAND, CraftItemStack.asNMSCopy(player.getInventory().getItemInOffHand())),
                new Pair<>(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(player.getInventory().getArmorContents()[3])),
                new Pair<>(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(player.getInventory().getArmorContents()[2])),
                new Pair<>(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(player.getInventory().getArmorContents()[1])),
                new Pair<>(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(player.getInventory().getArmorContents()[0]))
        );
        player.getInventory().clear();
        player.updateInventory();
    }

    public void transferBack() {
        this.player.setRemainingAir(this.air);
        this.player.setHealth(this.health);
        this.player.setFoodLevel(this.hunger);
        this.player.setSaturation(this.saturation);
        for (PotionEffect effect : this.player.getActivePotionEffects()) {
            this.player.removePotionEffect(effect.getType());
        }
        for (PotionEffect effect : this.effects) {
            this.player.addPotionEffect(effect);
        }
        this.player.setAbsorptionAmount(this.absorption);
        this.player.setFireTicks(this.fire);
        this.player.getInventory().clear();
        this.player.getInventory().setContents(this.inventory.getContents());
        this.player.updateInventory();
        this.player.teleport(this.location);
        NPCLibAPI.deleteNPC(this.npc);
    }

    public NPC getNPC() {
        return this.npc;
    }

    public int getAir() {
        return air;
    }

    public double getHealth() {
        return health;
    }

    public int getHunger() {
        return hunger;
    }

    public float getSaturation() {
        return saturation;
    }

    public Collection<PotionEffect> getEffects() {
        return effects;
    }

    public double getAbsorption() {
        return this.absorption;
    }

    public int getFire() {
        return fire;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public Location getLocation() {
        return location;
    }

    public Player getPlayer() {
        return player;
    }

}
