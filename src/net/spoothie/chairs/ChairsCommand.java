/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spoothie.chairs;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class ChairsCommand implements CommandExecutor {

    private final Chairs plugin;
    public ChairsIgnoreList ignoreList;

    public ChairsCommand(Chairs instance, ChairsIgnoreList ignoreList) {
        this.plugin = instance;
        this.ignoreList = ignoreList;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        } 
        if (args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("chairs.reload")) {
                plugin.reloadConfig();
                plugin.loadConfig();
                sender.sendMessage("Chairs configuration file reloaded.");
            } else {
                sender.sendMessage(ChatColor.GRAY + "No permission to do this!");
            }
        }
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args[0].equalsIgnoreCase("on")) {
                if (p.hasPermission("chairs.self")) {
                    ignoreList.removePlayer(p.getName());
                    p.sendMessage(ChatColor.GRAY + "You have enabled chairs for yourself!");
                } else {
                    p.sendMessage(ChatColor.GRAY + "No permission to do this!");
                }
            }
            if (args[0].equalsIgnoreCase("off")) {
                if (p.hasPermission("chairs.self")) {
                    ignoreList.addPlayer(p.getName());
                    p.sendMessage(ChatColor.GRAY + "You have disabled chairs for yourself!");
                } else {
                    p.sendMessage(ChatColor.GRAY + "No permission to do this!");
                }
            }
        }
        return true;
    }
}
