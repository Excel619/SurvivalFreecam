package com.gmail.excel8392.survivalfreecam;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreecamCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("survivalfreecam.toggle")) {
                SurvivalFreecamAPI.toggleFreecam((Player) sender);
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            }
        }
        return true;
    }

}
