/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.spoothie.chairs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author cnaude
 */
public class ChairEffects {

    Chairs plugin;
    int taskID;

    public ChairEffects(Chairs plugin) {
        this.plugin = plugin;
        effectsTask();
    }

    public void cancel() {
        plugin.getServer().getScheduler().cancelTask(taskID);
        taskID = 0;
    }
    
    public void restart() {
        this.cancel();
        this.effectsTask();
    }

    private void effectsTask() {
        taskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    String pName = p.getName();
                    if (plugin.sit.containsKey(pName)) {
                        if (p.hasPermission("chairs.sit.health")) {
                            double pHealthPcnt = (double) p.getHealth() / (double) p.getMaxHealth() * 100d;
                            if ((pHealthPcnt < plugin.sitMaxHealth)
                                    && (p.getHealth() < p.getMaxHealth())) {
                                int newHealth = plugin.sitHealthPerInterval + p.getHealth();
                                if (newHealth > p.getMaxHealth()) {
                                    newHealth = p.getMaxHealth();
                                }
                                p.setHealth(newHealth);
                            }
                        }
                    }
                }
            }
        }, plugin.sitEffectInterval, plugin.sitEffectInterval);
    }
}
