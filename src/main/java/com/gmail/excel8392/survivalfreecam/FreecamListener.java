package com.gmail.excel8392.survivalfreecam;

import com.gmail.excel8392.npclib.NPCInteractEvent;
import com.gmail.excel8392.npclib.NPCInteractType;
import com.gmail.excel8392.npclib.NPCLib;
import com.gmail.excel8392.npclib.NPCLibAPI;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class FreecamListener implements Listener {

    private static final Map<Player, ParticleInfo> particles = new HashMap<>();

    public FreecamListener() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(SurvivalFreecam.getInstance(), () -> {
            for (Player player : SurvivalFreecam.getInFreecam().keySet()) {
                if (Math.pow(Math.abs(player.getLocation().getX() - SurvivalFreecam.getInFreecam().get(player).getLocation().getX()), 2)
                        + Math.pow(Math.abs(player.getLocation().getZ() - SurvivalFreecam.getInFreecam().get(player).getLocation().getZ()), 2)
                        > SurvivalFreecam.MAX_DISTANCE_SQUARED_FROM_NPC) {
                    Bukkit.getScheduler().runTask(SurvivalFreecam.getInstance(), () -> {
                        player.teleport(SurvivalFreecam.getInFreecam().get(player).getLocation());
                        player.sendMessage(ChatColor.RED + "You went too far from your body!");
                    });
                }
            }
        }, 0L, SurvivalFreecam.DISTANCE_CHECK_INTERVAL);
        Bukkit.getScheduler().runTaskTimer(NPCLib.getInstance(), () -> {
            for (Player player : SurvivalFreecam.getInFreecam().keySet()) {
                for (Entity entity : player.getLocation().getWorld().getNearbyEntities(SurvivalFreecam.getInFreecam().get(player).getLocation(), 30, 30, 30, (entity) -> entity instanceof Monster && (((Mob) entity).getTarget() != player))) {
                        ((Mob) entity).setTarget(SurvivalFreecam.getInFreecam().get(player).getNPC().getEntityPlayer().getBukkitEntity());
                }
            }
        }, 0L, SurvivalFreecam.MOB_CHECK_INTERVAL);
    }

    @EventHandler
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        if (!SurvivalFreecam.getInFreecam().containsKey(event.getPlayer())) return;
        if (event.isCancelled()) return;
        if (event.getPlayer().getGameMode() != GameMode.SPECTATOR) return;
        if (!event.getCause().equals(PlayerTeleportEvent.TeleportCause.SPECTATE)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
        if (event.getEntity() instanceof Monster) {
            Monster monster = (Monster) event.getEntity();
            for (Player player : SurvivalFreecam.getInFreecam().keySet()) {
                if (monster.getLocation().distanceSquared(player.getLocation()) <= 306.25) {
                    monster.setTarget(SurvivalFreecam.getInFreecam().get(player).getNPC().getEntityPlayer().getBukkitEntity());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (SurvivalFreecam.getInFreecam().containsKey(event.getPlayer())) {
            event.getPlayer().setGameMode(GameMode.SURVIVAL);
            SurvivalFreecam.getInFreecam().get(event.getPlayer()).transferBack();
            SurvivalFreecam.getInFreecam().remove(event.getPlayer());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            EntityPlayer entityPlayer = ((CraftPlayer) event.getEntity()).getHandle();
            if (NPCLibAPI.isNPC(entityPlayer)) {
                for (Player player : SurvivalFreecam.getInFreecam().keySet()) {
                    if (SurvivalFreecam.getInFreecam().get(player).getNPC() == NPCLibAPI.getNPCFromEntity(entityPlayer)) {
                        player.setGameMode(GameMode.SURVIVAL);
                        SurvivalFreecam.getInFreecam().get(player).transferBack();
                        SurvivalFreecam.getInFreecam().remove(player);
                        player.damage(event.getDamage());
                        player.sendMessage(ChatColor.RED + "Your body was damaged so you were put back into it!");
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onNPCClick(NPCInteractEvent event) {
        if (event.getInteractType() == NPCInteractType.RIGHT_CLICK) {
            if (event.getPlayer().getGameMode() != GameMode.SPECTATOR) {
                for (Player freecam : SurvivalFreecam.getInFreecam().keySet()) {
                    if (SurvivalFreecam.getInFreecam().get(freecam).getNPC() == event.getNPC()) {
                        if (event.getPlayer() != freecam) {
                            if (!particles.containsKey(freecam)) {
                                BukkitTask task = Bukkit.getScheduler().runTaskTimer(SurvivalFreecam.getInstance(), () -> {
                                    if (freecam == null) {
                                        particles.get(null).getTask().cancel();
                                        particles.remove(null);
                                        return;
                                    }
                                    if (!SurvivalFreecam.getInFreecam().containsKey(freecam)) {
                                        particles.get(freecam).getTask().cancel();
                                        particles.remove(freecam);
                                        return;
                                    }
                                    if (particles.get(freecam).getTime() <= 0) {
                                        particles.get(freecam).getTask().cancel();
                                        particles.remove(freecam);
                                        return;
                                    }
                                    freecam.getLocation().getWorld().spawnParticle(
                                            Particle.REDSTONE,
                                            freecam.getLocation().getX(),
                                            freecam.getLocation().getY() + 0.5,
                                            freecam.getLocation().getZ(),
                                            10,
                                            new Particle.DustOptions(Color.WHITE, 2));
                                    particles.get(freecam).setTime(particles.get(freecam).getTime() - 1);
                                }, 1L, 3L);
                                particles.put(freecam, new ParticleInfo(66, task));
                                event.getPlayer().sendMessage(ChatColor.GREEN + freecam.getName() + " is now spawning particles for 10 seconds!");
                            }
                        }
                    }
                }
            }
        }
    }

    private static class ParticleInfo {

        private int time;
        private final BukkitTask task;

        public ParticleInfo(int time, BukkitTask task) {
            this.time = time;
            this.task = task;
        }

        public int getTime() {
            return this.time;
        }

        public BukkitTask getTask() {
            return this.task;
        }

        public void setTime(int time) {
            this.time = time;
        }

    }

}
