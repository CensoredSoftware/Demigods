package com.legit2.Demigods.Listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.legit2.Demigods.DConfig;
import com.legit2.Demigods.DDivineBlocks;
import com.legit2.Demigods.Demigods;
import com.legit2.Demigods.DTributeValue;
import com.legit2.Demigods.Utilities.DCharUtil;
import com.legit2.Demigods.Utilities.DDataUtil;
import com.legit2.Demigods.Utilities.DPlayerUtil;
import com.legit2.Demigods.Utilities.DMiscUtil;

public class DDivineBlockListener implements Listener
{
	static Demigods plugin;
	public static double FAVORMULTIPLIER = DConfig.getSettingDouble("global_favor_multiplier");
	public static int RADIUS = 8;
	
	public DDivineBlockListener(Demigods instance)
	{
		plugin = instance;
	}
	
	/* --------------------------------------------
	 *  Handle DivineBlock Interactions
	 * --------------------------------------------
	 */
	@EventHandler (priority = EventPriority.HIGH)
	public void shrineInteract(PlayerInteractEvent event)
	{
		// Exit method if it isn't a block of gold or if the player is mortal
		if(!DCharUtil.isImmortal(event.getPlayer())) return;
		if(event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

		// Define variables
		Location location = event.getClickedBlock().getLocation();
		Player player = event.getPlayer();
		int charID = DPlayerUtil.getCurrentChar(player);
		String charAlliance = DCharUtil.getAlliance(charID);
		String charDeity = DCharUtil.getDeity(charID);
		
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().getItemInHand().getType() == Material.BOOK)
		{						
			try
			{
				// Shrine created!
				ArrayList<Location> locations = new ArrayList<Location>(); locations.add(location);
				DDivineBlocks.createShrine(charID, locations);
				
				location.getWorld().getBlockAt(location).setType(Material.BEDROCK);
				location.getWorld().spawnEntity(location.add(0.5, 0.0, 0.5), EntityType.ENDER_CRYSTAL);
				location.getWorld().strikeLightningEffect(location);

				player.sendMessage(ChatColor.GRAY + "The " + ChatColor.YELLOW + charAlliance + "s" + ChatColor.GRAY + " are pleased...");
				player.sendMessage(ChatColor.GRAY + "A shrine has been created in honor of " + ChatColor.YELLOW + charDeity + ChatColor.GRAY + "!");
			}
			catch(Exception e)
			{
				// Creation of shrine failed...
				e.printStackTrace();
			}
		}
		
		try
		{
			// Check if block is divine
			String shrineDeity = DDivineBlocks.getDeityAtShrine(location);
			if(shrineDeity == null) return;
						
			if(DDivineBlocks.isDivineBlock(location))
			{
				// Check if character has deity
				if(DCharUtil.hasDeity(charID, shrineDeity))
				{
					// Open the tribute inventory
					Inventory ii = DMiscUtil.getPlugin().getServer().createInventory(player, 27, DCharUtil.getName(charID) + "'s Shrine");
					player.openInventory(ii);
					DDataUtil.saveCharData(charID, "temp_tributing", DDivineBlocks.getOwnerOfShrine(event.getClickedBlock().getLocation()));
					event.setCancelled(true);
					return;
				}
				player.sendMessage(ChatColor.YELLOW + "You must be allied to " + shrineDeity + " in order to tribute here.");
			}
		}
		catch(Exception e) {}
	}
	
	/* --------------------------------------------
	 *  Handle Player Tributing
	 * --------------------------------------------
	 */	
	@EventHandler (priority = EventPriority.MONITOR)
	public void playerTribute(InventoryCloseEvent event)
	{
		try
		{
			if(!(event.getPlayer() instanceof Player)) return;
			Player player = (Player)event.getPlayer();
			int charID = DPlayerUtil.getCurrentChar(player);
			
			if(!DCharUtil.isImmortal(player)) return;
			
			// If it isn't a tribute chest then break the method
			if(!event.getInventory().getName().equals("Tributes")) return;
			
			// Get the creator of the shrine
			//int shrineCreator = DDivineBlocks.getOwnerOfShrine((Location) DDataUtil.getCharData(charID, "tributing_temp"));
			DDataUtil.removeCharData(charID, "temp_tributing"); 
			
			//calculate value of chest
			int tirbuteValue = 0;
			int items = 0;
			for(ItemStack ii : event.getInventory().getContents())
			{
				if(ii != null)
				{
					tirbuteValue += DTributeValue.getTributeValue(ii);
					items ++;
				}
			}
			
			tirbuteValue *= FAVORMULTIPLIER;
			
			// Give devotion
			int devotionBefore = DCharUtil.getDevotion(charID);
			DCharUtil.giveDevotion(charID, tirbuteValue);
			DCharUtil.giveDevotion(charID, tirbuteValue / 7);
			
			// Give favor
			int favorBefore = DCharUtil.getMaxFavor(charID);
			//DUtil.setFavorCap(player, DUtil.getFavorCap(username)+value/5); TODO
			
			// Devotion lock TODO
			String charName = DCharUtil.getName(charID);
			if(devotionBefore < DCharUtil.getDevotion(charID)) player.sendMessage(ChatColor.YELLOW + "Your devotion to " + charName + " has increased to " + DCharUtil.getDevotion(charID) + ".");
			if(favorBefore < DCharUtil.getMaxFavor(charID)) player.sendMessage(ChatColor.YELLOW + "Your favor cap has increased to " + DCharUtil.getMaxFavor(charID) + ".");
			if((favorBefore == DCharUtil.getMaxFavor(charID)) && (devotionBefore == DCharUtil.getDevotion(charID)) && (items > 0)) player.sendMessage(ChatColor.YELLOW + "Your tributes were insufficient for " + charName + "'s blessings.");
			
			// Clear the tribute case
			event.getInventory().clear();
		}
		catch(Exception e) {}
	}
	
	/* --------------------------------------------
	 *  Handle Miscellaneous Divine Block Events
	 * --------------------------------------------
	 */	
	@EventHandler(priority = EventPriority.HIGH)
	public void divineBlockAlerts(PlayerMoveEvent event)
	{
		if(event.getFrom().distance(event.getTo()) < 0.1) return;

		// Define variables
		for(Location divineBlock : DDivineBlocks.getAllShrines())
		{
			OfflinePlayer charOwner = DCharUtil.getOwner(DDivineBlocks.getOwnerOfShrine(divineBlock));

			// Check for world errors
			if(!divineBlock.getWorld().equals(event.getPlayer().getWorld())) return;
			if(event.getFrom().getWorld() != divineBlock.getWorld()) return;
			
			/*
			 * Entering
			 */
			if(event.getFrom().distance(divineBlock) > RADIUS)
			{
				if(divineBlock.distance(event.getTo()) <= RADIUS)
				{
					event.getPlayer().sendMessage(ChatColor.GRAY + "You have entered " + charOwner.getName() + "'s shrine to " + ChatColor.YELLOW + DDivineBlocks.getDeityAtShrine(divineBlock) + ChatColor.GRAY + ".");
					return;
				}
			}
			
			/*
			 * Leaving
			 */
			else if(event.getFrom().distance(divineBlock) <= RADIUS)
			{
				if(divineBlock.distance(event.getTo()) > RADIUS)
				{
					event.getPlayer().sendMessage(ChatColor.GRAY + "You have left a holy area.");
					return;
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static void stopDestroyEnderCrystal(EntityDamageEvent event)
	{
		try
		{
			for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
			{
				if(event.getEntity().getLocation().subtract(0.5, 0, 0.5).equals(divineBlock))
				{
					 event.setDamage(0);
					 return;
				}
			}
		}
		catch(Exception e) {}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public static void stopDestroyDivineBlock(BlockBreakEvent event)
	{
		try
		{
			for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
			{
				if(event.getBlock().getLocation().equals(divineBlock))
				{
					event.getPlayer().sendMessage(ChatColor.YELLOW + "Divine blocks cannot be broken by hand.");
					event.setCancelled(true);
					return;
				}
			}
		}
		catch(Exception e) {}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void stopDivineBlockDamage(BlockDamageEvent event)
	{
		try
		{
			for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
			{
				if(event.getBlock().getLocation().equals(divineBlock))
				{
					event.setCancelled(true);
				}
			}
		}
		catch(Exception e) {}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void stopDivineBlockIgnite(BlockIgniteEvent event)
	{
		try
		{
			for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
			{
				if(event.getBlock().getLocation().equals(divineBlock))
				{
					event.setCancelled(true);
				}
			}
		}
		catch(Exception e) {}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void stopDivineBlockBurn(BlockBurnEvent event)
	{
		try
		{
			for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
			{
				if(event.getBlock().getLocation().equals(divineBlock))
				{
					event.setCancelled(true);
				}
			}
		}
		catch(Exception e) {}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void stopDivineBlockPistonExtend(BlockPistonExtendEvent event)
	{		
		List<Block> blocks = event.getBlocks();
		
		CHECKBLOCKS:
		for(Block block : blocks)
		{
			try
			{
				for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
				{
					if(block.getLocation().equals(divineBlock))
					{
						event.setCancelled(true);
						break CHECKBLOCKS;
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void stopDivineBlockPistonRetract(BlockPistonRetractEvent event)
	{
		// Define variables
		final Block block = event.getBlock().getRelative(event.getDirection(), 2);
		
		try
		{
			for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
			{
				if(block.getLocation().equals((divineBlock)) && event.isSticky())
				{
					event.setCancelled(true);
				}
			}
		}
		catch(Exception e) {}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void divineBlockExplode(final EntityExplodeEvent event)
	{
		try
		{
			// Remove divineBlock blocks from explosions
			Iterator<Block> i = event.blockList().iterator();
			while(i.hasNext())
			{
				Block block = i.next();
				if(!DMiscUtil.canLocationPVP(block.getLocation())) i.remove();
				for(Location divineBlock : DDivineBlocks.getAllDivineBlocks())
				{
					if(block.getLocation().equals(divineBlock)) i.remove();
				}
			}
		} 
		catch (Exception er) {}
	}
}
