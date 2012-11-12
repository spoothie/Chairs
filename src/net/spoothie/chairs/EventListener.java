package net.spoothie.chairs;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Stairs;
import org.bukkit.util.Vector;

public class EventListener implements Listener {

    public Chairs plugin;

    public EventListener(Chairs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            //event.getPlayer().sendMessage("Block: " + block.getType());
            if (plugin.allowedBlocks.contains(block.getType())) {
                Player player = event.getPlayer();
                Stairs stairs = (Stairs) block.getState().getData();
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
                if (!player.isSneaking() && player.getVehicle() != null) {
                    player.getVehicle().remove();
                    return;
                }

                // Check for distance distance between player and chair.
                if (plugin.distance > 0 && player.getLocation().distance(block.getLocation().add(0.5, 0, 0.5)) > plugin.distance) {
                    return;
                }

                // Check for signs.
                if (plugin.signcheck == true) {
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
                if (plugin.maxchairwidth > 0) {
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
                if (plugin.sneaking == false || (plugin.sneaking == true && event.getPlayer().isSneaking())) {
                    if (player.getVehicle() != null) {
                        player.getVehicle().remove();
                    }

                    Item drop = dropSeat(block);
                    List<Item> drops = checkChair(drop);

                    if (drops != null) {
                        drop.remove();
                        return;
                    }

                    // Rotate the player's view to the descending side of the block.
                    if (plugin.autorotate == true) {
                        Location plocation = player.getLocation();

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
                    }

                    // Changing the drop material is only necessary for the item merge feature of CB++
                    // The client won't update the material, though.
                    drop.setItemStack(new ItemStack(Material.PUMPKIN_STEM));
                    drop.setPassenger(player);

                    // Cancel BlockPlaceEvent Result, if player is rightclicking with a block in his hand.
                    event.setUseInteractedBlock(Result.DENY);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (plugin.allowedBlocks.contains(event.getBlock().getType())) {
            Item drop = dropSeat(event.getBlock());

            for (Entity e : drop.getNearbyEntities(0.2, 0.2, 0.2)) {
                if (e != null && e instanceof Item && e.getPassenger() != null) {
                    e.remove();
                }
            }

            drop.remove();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Entity vehicle = event.getPlayer().getVehicle();

        // Let players stand up when leaving the server.
        if (vehicle != null && vehicle instanceof Item) {
            vehicle.remove();
        }
    }

    private Item dropSeat(Block chair) {
        Location location = chair.getLocation().add(0.5, (plugin.sittingheight - 0.5), 0.5);
        Item drop = location.getWorld().dropItemNaturally(location, new ItemStack(plugin.item));
        drop.setPickupDelay(Integer.MAX_VALUE);
        drop.teleport(location);
        drop.setVelocity(new Vector(0, 0, 0));
        return drop;
    }

    private List<Item> checkChair(Item drop) {
        List<Item> drops = new ArrayList<Item>();

        // Check for already existing chair items.
        for (Entity e : drop.getNearbyEntities(0.2, 0.2, 0.2)) {
            if (e != null && e instanceof Item && e.getPassenger() != null) {
                drops.add(drop);
            }
        }

        if (drops.isEmpty() == false) {
            return drops;
        }

        return null;
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
            if (!plugin.allowedBlocks.contains(relative.getType()) || (block instanceof Stairs && ((Stairs) relative.getState().getData()).getDescendingDirection() != ((Stairs) block.getState().getData()).getDescendingDirection())) {
                if (relative.getType() == Material.SIGN || relative.getType() == Material.WALL_SIGN || relative.getType() == Material.SIGN_POST) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
}