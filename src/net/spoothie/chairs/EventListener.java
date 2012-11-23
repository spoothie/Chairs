package net.spoothie.chairs;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.Stairs;

public class EventListener implements Listener {

    public Chairs plugin;

    public EventListener(Chairs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove (PlayerMoveEvent event) {
        Player player = event.getPlayer();        
        String pname = player.getName();
        if (plugin.sit.containsKey(player.getName())) {
            Location from = player.getLocation();
            Location to = plugin.sit.get(pname);
            if (from.getWorld() == to.getWorld()) {
                if (from.distance(to) > 1) {                
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
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();      
            Stairs stairs = null;
            if (block.getState().getData() instanceof Stairs) {
                stairs = (Stairs) block.getState().getData();
            }
            if (plugin.allowedBlocks.contains(block.getType())) {
                Player player = event.getPlayer();
                int chairwidth = 1;

                // Check if block beneath chair is solid.
                if (block.getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                    return;
                }
                if (block.getRelative(BlockFace.DOWN).getType() == Material.WATER) {
                    return;
                }
                if (block.getRelative(BlockFace.DOWN).getType() == Material.LAVA) {
                    return;
                }
                if (!net.minecraft.server.Block.byId[block.getTypeId()].material.isSolid()) {
                    return;
                }

                // Permissions Check
                if (plugin.permissions) {
                    if (!player.hasPermission("chairs.sit")) {
                        return;
                    }
                }

                // Check if player is sitting.
                
                if (plugin.sit.containsKey(event.getPlayer().getName())) {
                    plugin.sit.remove(player.getName());
                    event.setCancelled(true);
                    if (plugin.notifyplayer) {
                        player.sendMessage(ChatColor.GRAY + "You are no longer sitting.");
                    }
                    plugin.sendStand(player);   
                    return;                    
                }

                // Check for distance distance between player and chair.
                if (plugin.distance > 0 && player.getLocation().distance(block.getLocation().add(0.5, 0, 0.5)) > plugin.distance) {
                    return;
                }
                                
                if (stairs != null) {                        
                    if (stairs.isInverted() && plugin.upsidedowncheck) {                        
                        return;
                    } 
                }

                // Check for signs.
                if (plugin.signcheck == true && stairs != null) {                    
                    boolean sign1 = false;
                    boolean sign2 = false;

                    if (stairs.getDescendingDirection() == BlockFace.NORTH || stairs.getDescendingDirection() == BlockFace.SOUTH) {
                        sign1 = checkSign(block, BlockFace.EAST);
                        sign2 = checkSign(block, BlockFace.WEST);
                    } else if (stairs.getDescendingDirection() == BlockFace.EAST || stairs.getDescendingDirection() == BlockFace.WEST) {
                        sign1 = checkSign(block, BlockFace.NORTH);
                        sign2 = checkSign(block, BlockFace.SOUTH);
                    }

                    if (!(sign1 == true && sign2 == true)) {
                        return;
                    }
                }

                // Check for maximal chair width.
                if (plugin.maxchairwidth > 0 && stairs != null) {                    
                    if (stairs.getDescendingDirection() == BlockFace.NORTH || stairs.getDescendingDirection() == BlockFace.SOUTH) {
                        chairwidth += getChairWidth(block, BlockFace.EAST);
                        chairwidth += getChairWidth(block, BlockFace.WEST);
                    } else if (stairs.getDescendingDirection() == BlockFace.EAST || stairs.getDescendingDirection() == BlockFace.WEST) {
                        chairwidth += getChairWidth(block, BlockFace.NORTH);
                        chairwidth += getChairWidth(block, BlockFace.SOUTH);
                    }

                    if (chairwidth > plugin.maxchairwidth) {
                        return;
                    }
                }

                // Sit-down process.
                if (!plugin.sneaking || (plugin.sneaking && event.getPlayer().isSneaking())) {
                    if (player.getVehicle() != null) {
                        player.getVehicle().remove();
                    }
                                        
                    plugin.sendSit(player);   
                                        
                    // Rotate the player's view to the descending side of the block.
                    if (plugin.autorotate && stairs != null) {
                        Location plocation = block.getLocation().clone();
                        plocation.add(0.5D, (plugin.sittingheight - 0.5), 0.5D);
                        switch (stairs.getDescendingDirection()) {
                            case NORTH:
                                plocation.setYaw(90);
                                break;
                            case EAST:
                                plocation.setYaw(180);
                                break;
                            case SOUTH:
                                plocation.setYaw(270);
                                break;
                            case WEST:
                                plocation.setYaw(0);
                        }

                        player.teleport(plocation);

                    } else {
                        player.teleport(block.getLocation().add(0.5D, 0.0D, 0.5D));
                    }
                    player.setSneaking(true);
                    if (plugin.notifyplayer) {
                        player.sendMessage(ChatColor.GRAY + "You are now sitting.");
                    }
                    plugin.sit.put(player.getName(), block.getLocation());
                    
                    event.setUseInteractedBlock(Result.DENY);
                }
            }
        }
    }

    private int getChairWidth(Block block, BlockFace face) {
        int width = 0;

        // Go through the blocks next to the clicked block and check if there are any further stairs.
        for (int i = 1; i <= plugin.maxchairwidth; i++) {
            Block relative = block.getRelative(face, i);

            if (plugin.allowedBlocks.contains(relative.getType()) && ((Stairs) relative.getState().getData()).getDescendingDirection() == ((Stairs) block.getState().getData()).getDescendingDirection()) {
                width++;
            } else {
                break;
            }
        }

        return width;
    }

    private boolean checkSign(Block block, BlockFace face) {
        // Go through the blocks next to the clicked block and check if are signs on the end.
        for (int i = 1; true; i++) {
            Block relative = block.getRelative(face, i);
            if (!plugin.allowedBlocks.contains(relative.getType()) || (block.getState().getData() instanceof Stairs && ((Stairs) relative.getState().getData()).getDescendingDirection() != ((Stairs) block.getState().getData()).getDescendingDirection())) {
                if (relative.getType() == Material.SIGN || relative.getType() == Material.WALL_SIGN || relative.getType() == Material.SIGN_POST) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
}