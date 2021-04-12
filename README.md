### SurvivalFreecam

Bukkit Plugin

- Allows players to enter a freecam-like mode on survival servers to look at things from a different angle.
- When in /freecam mode, players cannot break blocks, interact with blocks, go through blocks, or travel more than 30 blocks from their body (configurable).
- Upon entering freecam mode, an NPC will be left behind in the player's place. Mobs will target the NPC normally, and if damaged you will immediatly be sent back to your body.
- Highly performance optimized: uses [NPCLib](https://github.com/Excel619/NPCLib "GridLib") to manage NPC loading efficiently.
- Prevents any exploits that would be possible if the player entered spectator mode instead (teleportation, seeing through blocks).
- Easy-to-use API that allows other plugins to handle things like custom freecam mode triggers.

Upon entering freecam mode, the plugin sets the player's gamemode to spectator (server side and sends a clientbound packet), and then sends a `PacketPlayOutGameStateChange` to update the player's gamestate to creative. This confuses the client and makes them think they can't go through blocks or interact with the world while the server thinks they are in the normal spectator mode.
Here is a snippet of that code if you want to try it out yourself:
```java
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
}
```

Excel#8392 on discord
