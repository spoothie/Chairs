package net.spoothie.chairs;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
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
		if(event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			
			if(plugin.allowedBlocks.contains(block.getType())) {
				Player player = event.getPlayer();
				Stairs stairs = (Stairs)block.getState().getData();
				int chairwidth = 1; 
				
				// Permissions Check
				if(!player.hasPermission("chairs.sit"))
					return;
				
				// Check if player is sitting.
				if(!player.isSneaking() && player.getVehicle() != null) {
					player.getVehicle().remove();
					return;
				}
				
				//Check for signs.
				if(plugin.signcheck == true) {
					boolean sign1 = false;
					boolean sign2 = false;
					
					if(stairs.getDescendingDirection() == BlockFace.NORTH || stairs.getDescendingDirection() == BlockFace.SOUTH) {
						sign1 = checkSign(block, BlockFace.EAST);
						sign2 = checkSign(block, BlockFace.WEST);
					}
					else if(stairs.getDescendingDirection() == BlockFace.EAST || stairs.getDescendingDirection() == BlockFace.WEST) {
						sign1 = checkSign(block, BlockFace.NORTH);
						sign2 = checkSign(block, BlockFace.SOUTH);
					}
					
					if(!(sign1 == true && sign2 == true))
						return;
				}
				
				// Check for maximal chair width.
				if(plugin.maxchairwidth >= 0) {
					if(stairs.getDescendingDirection() == BlockFace.NORTH || stairs.getDescendingDirection() == BlockFace.SOUTH) {
						chairwidth += getChairWidth(block, BlockFace.EAST);
						chairwidth += getChairWidth(block, BlockFace.WEST);
					}
					else if(stairs.getDescendingDirection() == BlockFace.EAST || stairs.getDescendingDirection() == BlockFace.WEST) {
						chairwidth += getChairWidth(block, BlockFace.NORTH);
						chairwidth += getChairWidth(block, BlockFace.SOUTH);
					}
				
					if(chairwidth > plugin.maxchairwidth )
						return;
				}
				
				// Sit-down process.
				if(plugin.sneaking == false || (plugin.sneaking == true && event.getPlayer().isSneaking())) {
					if(player.getVehicle() != null)
						player.getVehicle().remove();

					Location location = block.getLocation().add(0.5, (plugin.sittingheight - 0.5), 0.5);
					Item drop = player.getWorld().dropItemNaturally(location, new ItemStack(Material.LEVER));
					drop.setPickupDelay(Integer.MAX_VALUE);
					drop.teleport(location);
					drop.setVelocity(new Vector(0, 0, 0));
					
					// Check for players already sitting on the clicked block.
					for(Entity e : drop.getNearbyEntities(0.1, 0.1, 0.1)) {
						if(e != null && e instanceof Item) {
							drop.remove();
							return;
						}
					}
					
					// Rotate the player's view to the descending side of the block.
					if(plugin.autorotate == true) {
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
					
					drop.setItemStack(new ItemStack(Material.LEVER));
					drop.setPassenger(player);		
				}
			}
		}
	}
	
	private int getChairWidth(Block block, BlockFace face) {
		int width = 0;
		
		// Go through the blocks next to the clicked block and check if there are any further stairs.
		for(int i = 1; i <= plugin.maxchairwidth; i++) {
			Block relative = block.getRelative(face, i);
			
			if(plugin.allowedBlocks.contains(relative.getType()) && ((Stairs)relative.getState().getData()).getDescendingDirection() == ((Stairs)block.getState().getData()).getDescendingDirection())
				width++;
			else
				break;
		}
		
		return width;
	}
	
	private boolean checkSign(Block block, BlockFace face) {
		// Go through the blocks next to the clicked block and check if are signs on the end.
		for(int i = 1; true; i++) {
			Block relative = block.getRelative(face, i);
			if(!plugin.allowedBlocks.contains(relative.getType()) || (block instanceof Stairs && ((Stairs)relative.getState().getData()).getDescendingDirection() != ((Stairs)block.getState().getData()).getDescendingDirection())) {
				if(relative.getType() == Material.SIGN || relative.getType() == Material.WALL_SIGN || relative.getType() == Material.SIGN_POST)
					return true;
				else
					return false;
			}
		}
	}
}
