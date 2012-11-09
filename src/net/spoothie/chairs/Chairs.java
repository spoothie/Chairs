package net.spoothie.chairs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Chairs extends JavaPlugin {

    public List<Material> allowedBlocks = new ArrayList<Material>();
    public Material item;
    public boolean sneaking, autorotate, signcheck;
    public double sittingheight, distance;
    public int maxchairwidth;
    private File pluginFolder;
    private File configFile;

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
    }

    @Override
    public void onDisable() {
    }

    private void createConfig() {
        if (!pluginFolder.exists()) {
            try {
                pluginFolder.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadConfig() {
        item = Material.getMaterial(getConfig().getString("item"));
        autorotate = getConfig().getBoolean("auto-rotate");
        sneaking = getConfig().getBoolean("sneaking");
        signcheck = getConfig().getBoolean("sign-check");
        sittingheight = getConfig().getDouble("sitting-height");
        distance = getConfig().getDouble("distance");
        maxchairwidth = getConfig().getInt("max-chair-width");

        for (String type : getConfig().getStringList("allowed-blocks")) {
            allowedBlocks.add(Material.getMaterial(type));
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
}
