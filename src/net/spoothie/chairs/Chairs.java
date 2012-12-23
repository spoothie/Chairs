package net.spoothie.chairs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.server.v1_4_6.Packet40EntityMetadata;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Chairs extends JavaPlugin {

    public List<Material> allowedBlocks = new ArrayList<Material>();    
    public boolean sneaking, autoRotate, signCheck, permissions, notifyplayer;
    public boolean invertedStairCheck, seatOccupiedCheck, invertedStepCheck, perItemPerms;
    public double sittingHeight, distance;
    public int maxChairWidth;
    private File pluginFolder;
    private File configFile;
    public byte metadata;
    public HashMap<String, Location> sit = new HashMap<String, Location>();
    public static final String PLUGIN_NAME = "Chairs";
    public static final String LOG_HEADER = "[" + PLUGIN_NAME + "]";
    static final Logger log = Logger.getLogger("Minecraft");
    public PluginManager pm;

    @Override
    public void onEnable() {

        pluginFolder = getDataFolder();
        configFile = new File(pluginFolder, "config.yml");
        createConfig();
        this.getConfig().options().copyDefaults(true);
        saveConfig();
        loadConfig();
        EventListener eventListener = new EventListener(this);
        getServer().getPluginManager().registerEvents(eventListener, this);
        pm = this.getServer().getPluginManager();
    }

    @Override
    public void onDisable() {
    }

    private void createConfig() {
        if (!pluginFolder.exists()) {
            try {
                pluginFolder.mkdir();
            } catch (Exception e) {
                logInfo("ERROR: " + e.getMessage());                
            }
        }

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (Exception e) {
                logInfo("ERROR: " + e.getMessage());
            }
        }
    }

    private void loadConfig() {       
        autoRotate = getConfig().getBoolean("auto-rotate");
        sneaking = getConfig().getBoolean("sneaking");
        signCheck = getConfig().getBoolean("sign-check");
        sittingHeight = getConfig().getDouble("sitting-height");
        distance = getConfig().getDouble("distance");
        maxChairWidth = getConfig().getInt("max-chair-width");
        permissions = getConfig().getBoolean("permissions");
        notifyplayer = getConfig().getBoolean("notify-player");
        invertedStairCheck = getConfig().getBoolean("upside-down-check");
        seatOccupiedCheck = getConfig().getBoolean("seat-occupied-check");
        invertedStepCheck = getConfig().getBoolean("upper-step-check");
        perItemPerms = getConfig().getBoolean("per-item-perms");

        for (String type : getConfig().getStringList("allowed-blocks")) {
            try {
                if (type.matches("\\d+")) {
                    allowedBlocks.add(Material.getMaterial(Integer.parseInt(type)));
                } else {
                    allowedBlocks.add(Material.getMaterial(type));
                }
            }
            catch (Exception e) {
                logInfo("ERROR: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("chairs")) {
            if (sender instanceof Player && !((Player) sender).hasPermission("chairs.reload")) {
                return true;
            }

            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                loadConfig();
                sender.sendMessage("Chairs configuration file reloaded.");
            } else {
                sender.sendMessage("Use '/chairs reload' to reload the configuration file.");
            }
        }

        return true;
    }
    
    // Send sit packet to all online players
    public void sendSit(Player p) {
        Packet40EntityMetadata packet = new Packet40EntityMetadata(p.getPlayer().getEntityId(), new ChairWatcher((byte) 4), false);
        for (Player play : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) play).getHandle().playerConnection.sendPacket(packet);
            //((CraftPlayer) play).getHandle().netServerHandler.sendPacket(packet);
        }
    }
    
    public void sendSit() {
        for (String s : sit.keySet()) {
            Player p = Bukkit.getPlayer(s);
            if (p != null) {
                sendSit(p);
            }
        }
    }
    
    // Send stand packet to all online players
    public void sendStand(Player p) {
        if (sit.containsKey(p.getName())) {
            if (notifyplayer) {                
                p.sendMessage(ChatColor.GRAY + "You are no longer sitting.");
            }
            sit.remove(p.getName());
        }
        Packet40EntityMetadata packet = new Packet40EntityMetadata(p.getPlayer().getEntityId(), new ChairWatcher((byte) 0), false);
        for (Player play : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) play).getHandle().playerConnection.sendPacket(packet);
            //((CraftPlayer) play).getHandle().netServerHandler.sendPacket(packet);
        }
    }
    
    public void logInfo(String _message) {
        log.log(Level.INFO, String.format("%s %s", LOG_HEADER, _message));
    }

    public void logError(String _message) {
        log.log(Level.SEVERE, String.format("%s %s", LOG_HEADER, _message));
    }
    
}
