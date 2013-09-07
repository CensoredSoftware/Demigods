package com.censoredsoftware.demigods.structure.deity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.censoredsoftware.demigods.Demigods;
import com.censoredsoftware.demigods.data.DataManager;
import com.censoredsoftware.demigods.deity.Deity;
import com.censoredsoftware.demigods.language.Translation;
import com.censoredsoftware.demigods.player.Character;
import com.censoredsoftware.demigods.player.PlayerSave;
import com.censoredsoftware.demigods.structure.BlockData;
import com.censoredsoftware.demigods.structure.Schematic;
import com.censoredsoftware.demigods.structure.Selection;
import com.censoredsoftware.demigods.structure.Structure;
import com.censoredsoftware.demigods.util.Admins;
import com.censoredsoftware.demigods.util.Configs;
import com.google.common.base.Function;

public class Obelisk
{
	public static final String name = "Obelisk";
	public static final Function<Location, Structure.Type.Design> getDesign = new Function<Location, Structure.Type.Design>()
	{
		@Override
		public Structure.Type.Design apply(Location reference)
		{
			switch(reference.getBlock().getBiome())
			{
				case OCEAN:
				case BEACH:
				case DESERT:
				case DESERT_HILLS:
					return ObeliskDesign.DESERT;
				case HELL:
					return ObeliskDesign.NETHER;
				default:
					return ObeliskDesign.GENERAL;
			}
		}
	};
	public static final Function<Structure.Type.Design, Structure> createNew = new Function<Structure.Type.Design, Structure>()
	{
		@Override
		public Structure apply(Structure.Type.Design design)
		{
			Structure save = new Structure();
			save.setMembers(new ArrayList<String>());
			return save;
		}
	};
	public static final Set<Structure.Flag> flags = new HashSet<Structure.Flag>()
	{
		{
			add(Structure.Flag.PROTECTED_BLOCKS);
			add(Structure.Flag.NO_GRIEFING);
		}
	};
	public static final Listener listener = new Listener()
	{
		@EventHandler(priority = EventPriority.HIGH)
		public void createAndRemove(PlayerInteractEvent event)
		{
			if(event.getClickedBlock() == null) return;

			if(Demigods.MiscUtil.isDisabledWorld(event.getPlayer().getWorld())) return;

			// Define variables
			Block clickedBlock = event.getClickedBlock();
			Location location = clickedBlock.getLocation();
			Player player = event.getPlayer();

			if(PlayerSave.Util.isImmortal(player))
			{
				Character character = PlayerSave.Util.getPlayer(player).getCurrent();

				if(event.getAction() == Action.RIGHT_CLICK_BLOCK && !character.getDeity().getFlags().contains(Deity.Flag.NO_OBELISK) && character.getDeity().getClaimItems().keySet().contains(event.getPlayer().getItemInHand().getType()) && Util.validBlockConfiguration(event.getClickedBlock()))
				{
					if(Structure.Util.noOverlapStructureNearby(location))
					{
						player.sendMessage(ChatColor.YELLOW + "This location is too close to a no-pvp zone, please try again.");
						return;
					}

					try
					{
						// Obelisk created!
						Admins.sendDebug(ChatColor.RED + "Obelisk created by " + character.getName() + " at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ());
						Structure save = Structure.Type.OBELISK.createNew(location, true);
						save.setOwner(character.getId());
						location.getWorld().strikeLightningEffect(location);

						player.sendMessage(ChatColor.GRAY + Demigods.LANGUAGE.getText(Translation.Text.NOTIFICATION_OBELISK_CREATED));
						event.setCancelled(true);
					}
					catch(Exception e)
					{
						// Creation of shrine failed...
						e.printStackTrace();
					}
				}
			}

			if(Admins.useWand(player) && Structure.Util.partOfStructureWithType(location, "Obelisk"))
			{
				event.setCancelled(true);

				Structure save = Structure.Util.getStructureRegional(location);
				Character owner = com.censoredsoftware.demigods.player.Character.Util.load(save.getOwner());

				if(DataManager.hasTimed(player.getName(), "destroy_obelisk"))
				{
					// Remove the Obelisk
					save.remove();
					DataManager.removeTimed(player.getName(), "destroy_obelisk");

					Admins.sendDebug(ChatColor.RED + "Obelisk owned by (" + owner.getName() + ") at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ() + " removed.");

					player.sendMessage(ChatColor.GREEN + Demigods.LANGUAGE.getText(Translation.Text.ADMIN_WAND_REMOVE_OBELISK_COMPLETE));
				}
				else
				{
					DataManager.saveTimed(player.getName(), "destroy_obelisk", true, 5);
					player.sendMessage(ChatColor.RED + Demigods.LANGUAGE.getText(Translation.Text.ADMIN_WAND_REMOVE_OBELISK));
				}
			}
		}
	};
	public static final int radius = Configs.getSettingInt("zones.obelisk_radius");

	private final static Schematic general = new Schematic("general", "HmmmQuestionMark", 3)
	{
		{
			// Everything else.
			add(new Selection(0, 0, -1, 0, 2, -1, BlockData.Preset.STONE_BRICK));
			add(new Selection(0, 0, 1, 0, 2, 1, BlockData.Preset.STONE_BRICK));
			add(new Selection(1, 0, 0, 1, 2, 0, BlockData.Preset.STONE_BRICK));
			add(new Selection(-1, 0, 0, -1, 2, 0, BlockData.Preset.STONE_BRICK));
			add(new Selection(0, 4, -1, 0, 5, -1, BlockData.Preset.STONE_BRICK));
			add(new Selection(0, 4, 1, 0, 5, 1, BlockData.Preset.STONE_BRICK));
			add(new Selection(1, 4, 0, 1, 5, 0, BlockData.Preset.STONE_BRICK));
			add(new Selection(-1, 4, 0, -1, 5, 0, BlockData.Preset.STONE_BRICK));
			add(new Selection(0, 0, 0, 0, 4, 0, Material.REDSTONE_BLOCK));
			add(new Selection(0, 3, -1, Material.REDSTONE_LAMP_ON));
			add(new Selection(0, 3, 1, Material.REDSTONE_LAMP_ON));
			add(new Selection(1, 3, 0, Material.REDSTONE_LAMP_ON));
			add(new Selection(-1, 3, 0, Material.REDSTONE_LAMP_ON));
			add(new Selection(0, 5, 0, Material.REDSTONE_LAMP_ON));
			add(new Selection(1, 5, -1, BlockData.Preset.VINE_1));
			add(new Selection(-1, 5, -1, BlockData.Preset.VINE_1));
			add(new Selection(1, 5, 1, BlockData.Preset.VINE_4));
			add(new Selection(-1, 5, 1, BlockData.Preset.VINE_4));
		}
	};
	private final static Schematic desert = new Schematic("desert", "HmmmQuestionMark", 3)
	{
		{
			// Everything else.
			add(new Selection(0, 0, -1, 0, 2, -1, Material.SANDSTONE));
			add(new Selection(0, 0, 1, 0, 2, 1, Material.SANDSTONE));
			add(new Selection(1, 0, 0, 1, 2, 0, Material.SANDSTONE));
			add(new Selection(-1, 0, 0, -1, 2, 0, Material.SANDSTONE));
			add(new Selection(0, 4, -1, 0, 5, -1, Material.SANDSTONE));
			add(new Selection(0, 4, 1, 0, 5, 1, Material.SANDSTONE));
			add(new Selection(1, 4, 0, 1, 5, 0, Material.SANDSTONE));
			add(new Selection(-1, 4, 0, -1, 5, 0, Material.SANDSTONE));
			add(new Selection(0, 0, 0, 0, 4, 0, Material.REDSTONE_BLOCK));
			add(new Selection(0, 3, -1, Material.REDSTONE_LAMP_ON));
			add(new Selection(0, 3, 1, Material.REDSTONE_LAMP_ON));
			add(new Selection(1, 3, 0, Material.REDSTONE_LAMP_ON));
			add(new Selection(-1, 3, 0, Material.REDSTONE_LAMP_ON));
			add(new Selection(0, 5, 0, Material.REDSTONE_LAMP_ON));
		}
	};
	private final static Schematic nether = new Schematic("nether", "HmmmQuestionMark", 3)
	{
		{
			// Everything else.
			add(new Selection(0, 0, -1, 0, 2, -1, Material.NETHER_BRICK));
			add(new Selection(0, 0, 1, 0, 2, 1, Material.NETHER_BRICK));
			add(new Selection(1, 0, 0, 1, 2, 0, Material.NETHER_BRICK));
			add(new Selection(-1, 0, 0, -1, 2, 0, Material.NETHER_BRICK));
			add(new Selection(0, 4, -1, 0, 5, -1, Material.NETHER_BRICK));
			add(new Selection(0, 4, 1, 0, 5, 1, Material.NETHER_BRICK));
			add(new Selection(1, 4, 0, 1, 5, 0, Material.NETHER_BRICK));
			add(new Selection(-1, 4, 0, -1, 5, 0, Material.NETHER_BRICK));
			add(new Selection(0, 0, 0, 0, 4, 0, Material.REDSTONE_BLOCK));
			add(new Selection(0, 3, -1, Material.REDSTONE_LAMP_ON));
			add(new Selection(0, 3, 1, Material.REDSTONE_LAMP_ON));
			add(new Selection(1, 3, 0, Material.REDSTONE_LAMP_ON));
			add(new Selection(-1, 3, 0, Material.REDSTONE_LAMP_ON));
			add(new Selection(0, 5, 0, Material.REDSTONE_LAMP_ON));
		}
	};

	public static enum ObeliskDesign implements Structure.Type.Design
	{
		GENERAL("general", general), DESERT("desert", desert), NETHER("nether", nether);

		private final String name;
		private final Schematic schematic;

		private ObeliskDesign(String name, Schematic schematic)
		{
			this.name = name;
			this.schematic = schematic;
		}

		@Override
		public String getName()
		{
			return name;
		}

		@Override
		public Set<Location> getClickableBlocks(Location reference)
		{
			return getSchematic().getLocations(reference);
		}

		@Override
		public Schematic getSchematic()
		{
			return schematic;
		}
	}

	public static class Util
	{
		public static boolean validBlockConfiguration(Block block)
		{
			if(!block.getType().equals(Material.EMERALD_BLOCK)) return false;
			if(!block.getRelative(1, 0, 0).getType().equals(Material.COBBLESTONE)) return false;
			if(!block.getRelative(-1, 0, 0).getType().equals(Material.COBBLESTONE)) return false;
			if(!block.getRelative(0, 0, 1).getType().equals(Material.COBBLESTONE)) return false;
			if(!block.getRelative(0, 0, -1).getType().equals(Material.COBBLESTONE)) return false;
			if(block.getRelative(1, 0, 1).getType().isSolid()) return false;
			return !block.getRelative(1, 0, -1).getType().isSolid() && !block.getRelative(-1, 0, 1).getType().isSolid() && !block.getRelative(-1, 0, -1).getType().isSolid();
		}
	}
}
