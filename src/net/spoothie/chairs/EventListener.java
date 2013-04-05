package net.spoothie.chairs;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.material.Stairs;
import org.bukkit.material.Step;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class EventListener implements Listener {

    public Chairs plugin;
    public ChairsIgnoreList ignoreList;

    public EventListener(Chairs plugin, ChairsIgnoreList ignoreList) {
        this.plugin = plugin;
        this.ignoreList = ignoreList;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String pname = player.getName();
        if (plugin.sit.containsKey(player.getName())) {
            Location from = player.getLocation();
            Location to = plugin.sit.get(pname);
            if (from.getWorld() == to.getWorld()) {
                if (from.distance(to) > 1.5) {
                    plugin.sendStand(player);
                } else {
                    plugin.sendSit(player);
                }
            } else {
                plugin.sendStand(player);
            }
        }
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) { 
        delayedSitTask();
    }
    
    private void delayedSitTask() {
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {                
                plugin.sendSit();
            }
        }, 20 );  
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.sit.containsKey(player.getName())) {
            plugin.sendStand(player);
            Location loc = player.getLocation().clone();
            loc.setY(loc.getY() + 1);
            player.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
    }

    @EventHandler
    public void onBlockDestroy(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!plugin.sit.isEmpty()) {
            ArrayList<String> standList = new ArrayList<String>();
            for (String s : plugin.sit.keySet()) {
                if (plugin.sit.get(s).equals(block.getLocation())) {
                    standList.add(s);
                }
            }
            for (String s : standList) {
                Player player = Bukkit.getPlayer(s);
                plugin.sendStand(player);
            }
            standList.clear();
        }
    }
    
    public boolean isValidChair(Block block) {
        for (ChairBlock cb : plugin.allowedBlocks) {
            if (cb.getMat().equals(block.getType())) {
                return true;                
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer().getItemInHand().getType().isBlock() 
                && (event.getPlayer().getItemInHand().getTypeId() != 0)
                && plugin.ignoreIfBlockInHand) {
            return;
        }
        if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            
            Block block = event.getClickedBlock();
            Stairs stairs = null;
            Step step = null;
            double sh = plugin.sittingHeight;
            boolean blockOkay = false;
                        
            Player player = event.getPlayer();
            if (ignoreList.isIgnored(player.getName())) {
                return;
            }
            // Permissions Check
            if (plugin.permissions) {
                if (!player.hasPermission("chairs.sit")) {
                    return;
                }
            }
            if (plugin.perItemPerms) {
                if (plugin.pm.getPermission("chairs.sit." + block.getTypeId()) == null) {
                    plugin.pm.addPermission(new Permission("chairs.sit." + block.getTypeId(),
                            "Allow players to sit on a '" + block.getType().name() + "'",
                            PermissionDefault.FALSE));
                }
                if (plugin.pm.getPermission("chairs.sit." + block.getType().toString()) == null) {
                    plugin.pm.addPermission(new Permission("chairs.sit." + block.getType().toString(),
                            "Allow players to sit on a '" + block.getType().name() + "'",
                            PermissionDefault.FALSE));
                }
            }
                        
            for (ChairBlock cb : plugin.allowedBlocks) {
                if (cb.getMat().equals(block.getType())) {
                    blockOkay = true;
                    sh = cb.getSitHeight();                    
                }
            }
            if (blockOkay
                    || (player.hasPermission("chairs.sit." + block.getTypeId()) && plugin.perItemPerms)
                    || (player.hasPermission("chairs.sit." + block.getType().toString()) && plugin.perItemPerms)) {
                
                if (block.getState().getData() instanceof Stairs) {
                    stairs = (Stairs) block.getState().getData();
                } else if (block.getState().getData() instanceof Step) {
                    step = (Step) block.getState().getData();
                } else {
                    sh += plugin.sittingHeightAdj;
                }

                int chairwidth = 1;

                // Check if block beneath chair is solid.
                if (block.getRelative(BlockFace.DOWN).isLiquid()) {
                    return;
                }                
                if (block.getRelative(BlockFace.DOWN).isEmpty()) {
                    return;
                }                
                if (!block.getRelative(BlockFace.DOWN).getType().isSolid()) {                        
                    return;
                }

                // Check if player is sitting.
                if (plugin.sit.containsKey(event.getPlayer().getName())) {
                    plugin.sit.remove(player.getName());
                    event.setCancelled(true);
                    if (plugin.notifyplayer && !plugin.msgStanding.isEmpty()) {
                        player.sendMessage(plugin.msgStanding);
                    }
                    plugin.sendStand(player);
                    return;
                }

                // Check for distance distance between player and chair.
                if (plugin.distance > 0 && player.getLocation().distance(block.getLocation().add(0.5, 0, 0.5)) > plugin.distance) {
                    return;
                }

                if (stairs != null) {
                    if (stairs.isInverted() && plugin.invertedStairCheck) {
                        return;
                    }
                }
                if (step != null) {
                    if (step.isInverted() && plugin.invertedStepCheck) {
                        return;
                    }
                }

                // Check for signs.
                if (plugin.signCheck == true && stairs != null) {
                    boolean sign1 = false;
                    boolean sign2 = false;

                    if (stairs.getDescendingDirection() == BlockFace.NORTH || stairs.getDescendingDirection() == BlockFace.SOUTH) {
                        sign1 = checkSign(block, BlockFace.EAST) || checkFrame(block, BlockFace.EAST, player);
                        sign2 = checkSign(block, BlockFace.WEST) || checkFrame(block, BlockFace.WEST, player);
                    } else if (stairs.getDescendingDirection() == BlockFace.EAST || stairs.getDescendingDirection() == BlockFace.WEST) {
                        sign1 = checkSign(block, BlockFace.NORTH) || checkFrame(block, BlockFace.NORTH, player);
                        sign2 = checkSign(block, BlockFace.SOUTH) || checkFrame(block, BlockFace.SOUTH, player);
                    }

                    if (!(sign1 == true && sign2 == true)) {
                        return;
                    }
                }

                // Check for maximal chair width.
                if (plugin.maxChairWidth > 0 && stairs != null) {
                    if (stairs.getDescendingDirection() == BlockFace.NORTH || stairs.getDescendingDirection() == BlockFace.SOUTH) {
                        chairwidth += getChairWidth(block, BlockFace.EAST);
                        chairwidth += getChairWidth(block, BlockFace.WEST);
                    } else if (stairs.getDescendingDirection() == BlockFace.EAST || stairs.getDescendingDirection() == BlockFace.WEST) {
                        chairwidth += getChairWidth(block, BlockFace.NORTH);
                        chairwidth += getChairWidth(block, BlockFace.SOUTH);
                    }                                        

                    if (chairwidth > plugin.maxChairWidth) {
                        return;
                    }
                }

                // Sit-down process.
                if (!plugin.sneaking || (plugin.sneaking && event.getPlayer().isSneaking())) {
                    if (plugin.seatOccupiedCheck) {
                        if (!plugin.sit.isEmpty()) {
                            for (String s : plugin.sit.keySet()) {
                                if (plugin.sit.get(s).equals(block.getLocation())) {
                                    if (!plugin.msgOccupied.isEmpty()) {
                                        player.sendMessage(plugin.msgOccupied.replaceAll("%PLAYER%", s));
                                    }
                                    return;
                                }
                            }
                        }
                    }

                    if (player.getVehicle() != null) {
                        player.getVehicle().remove();
                    }

                    // Rotate the player's view to the descending side of the block.
                    if (plugin.autoRotate && stairs != null) {
                        Location plocation = block.getLocation().clone();
                        plocation.add(0.5D, (sh - 0.5D), 0.5D);
                        switch (stairs.getDescendingDirection()) {
                            case NORTH:
                                plocation.setYaw(180);
                                break;
                            case EAST:
                                plocation.setYaw(-90);
                                break;
                            case SOUTH:
                                plocation.setYaw(0);
                                break;
                            case WEST:
                                plocation.setYaw(90);
                        }
                        player.teleport(plocation);
                    } else {
                        Location plocation = block.getLocation().clone();
                        plocation.setYaw(player.getLocation().getYaw());
                        player.teleport(plocation.add(0.5D, (sh - 0.5D), 0.5D));
                    }
                    player.setSneaking(true);
                    if (plugin.notifyplayer && !plugin.msgSitting.isEmpty()) {
                        player.sendMessage(plugin.msgSitting);
                    }
                    plugin.sit.put(player.getName(), block.getLocation());
                    event.setUseInteractedBlock(Result.DENY);
                    
                    delayedSitTask();
                }
            }
        }
    }

    private int getChairWidth(Block block, BlockFace face) {
        int width = 0;

        // Go through the blocks next to the clicked block and check if there are any further stairs.
        for (int i = 1; i <= plugin.maxChairWidth; i++) {
            Block relative = block.getRelative(face, i);
            if (relative.getState().getData() instanceof Stairs) {                
                if (isValidChair(relative) && ((Stairs) relative.getState().getData()).getDescendingDirection() == ((Stairs) block.getState().getData()).getDescendingDirection()) {
                    width++;
                } else {
                    break;
                }
            }
        }

        return width;
    }

    private boolean checkSign(Block block, BlockFace face) {
        // Go through the blocks next to the clicked block and check if are signs on the end.
        for (int i = 1; true; i++) {
            Block relative = block.getRelative(face, i);            
            if (!isValidChair(relative) || (block.getState().getData() instanceof Stairs && ((Stairs) relative.getState().getData()).getDescendingDirection() != ((Stairs) block.getState().getData()).getDescendingDirection())) {
                if (plugin.validSigns.contains(relative.getType())) {                
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
    
    private boolean checkFrame(Block block, BlockFace face, Player player) {
        // Go through the blocks next to the clicked block and check if are signs on the end.
        
        for (int i = 1; true; i++) {
            Block relative = block.getRelative(face, i);          
            int x = relative.getLocation().getBlockX();
            int y = relative.getLocation().getBlockY();
            int z = relative.getLocation().getBlockZ();                                
            if (!isValidChair(relative) || (block.getState().getData() instanceof Stairs && ((Stairs) relative.getState().getData()).getDescendingDirection() != ((Stairs) block.getState().getData()).getDescendingDirection())) {
                if (relative.getType().equals(Material.AIR)) {                                        
                    for (Entity e : player.getNearbyEntities(plugin.distance, plugin.distance, plugin.distance)) {
                        if (e instanceof ItemFrame && plugin.validSigns.contains(Material.ITEM_FRAME)) {
                            int x2 = e.getLocation().getBlockX();
                            int y2 = e.getLocation().getBlockY();
                            int z2 = e.getLocation().getBlockZ();
                            if (x == x2 && y == y2 && z == z2) {                                
                                return true;
                            }
                        }
                    }                    
                } else {
                    return false;
                }
            }
        }
    }
}