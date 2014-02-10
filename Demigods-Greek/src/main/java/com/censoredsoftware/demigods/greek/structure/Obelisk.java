package com.censoredsoftware.demigods.greek.structure;

import com.censoredsoftware.censoredlib.schematic.BlockData;
import com.censoredsoftware.censoredlib.schematic.Schematic;
import com.censoredsoftware.censoredlib.schematic.Selection;
import com.censoredsoftware.censoredlib.util.Colors;
import com.censoredsoftware.demigods.engine.conversation.Administration;
import com.censoredsoftware.demigods.engine.data.Data;
import com.censoredsoftware.demigods.engine.data.serializable.StructureSave;
import com.censoredsoftware.demigods.engine.entity.player.DemigodsCharacter;
import com.censoredsoftware.demigods.engine.entity.player.DemigodsPlayer;
import com.censoredsoftware.demigods.engine.mythos.Deity;
import com.censoredsoftware.demigods.engine.mythos.StructureType;
import com.censoredsoftware.demigods.engine.util.Configs;
import com.censoredsoftware.demigods.engine.util.Messages;
import com.censoredsoftware.demigods.engine.util.Zones;
import com.censoredsoftware.demigods.greek.language.English;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Obelisk extends GreekStructureType
{
	private static final String name = "Obelisk";
	private static final Function<Location, GreekStructureType.Design> getDesign = new Function<Location, StructureType.Design>()
	{
		@Override
		public StructureType.Design apply(Location reference)
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
	private static final Function<GreekStructureType.Design, StructureSave> createNew = new Function<GreekStructureType.Design, StructureSave>()
	{
		@Override
		public StructureSave apply(GreekStructureType.Design design)
		{
			StructureSave save = new StructureSave();
			save.setSanctifiers(new HashMap<String, Long>());
			save.setCorruptors(new HashMap<String, Long>());
			return save;
		}
	};
	private static final StructureType.InteractFunction<Boolean> sanctify = new StructureType.InteractFunction<Boolean>()
	{
		@Override
		public Boolean apply(StructureSave data, DemigodsCharacter character)
		{
			if(!DemigodsCharacter.Util.areAllied(character, Data.CHARACTER.get(data.getOwner())) || !data.getSanctifiers().contains(character.getId())) return false;
			Location location = data.getReferenceLocation();
			location.getWorld().playSound(location, Sound.CAT_PURR, 0.3f, 0.7F);
			MaterialData colorData = Colors.getMaterial(character.getDeity().getColor());
			location.getWorld().playEffect(location.clone().add(0, 1, 0), Effect.STEP_SOUND, colorData.getItemTypeId(), colorData.getData());
			return true;
		}
	};
	private static final StructureType.InteractFunction<Boolean> corrupt = new StructureType.InteractFunction<Boolean>()
	{
		@Override
		public Boolean apply(StructureSave data, DemigodsCharacter character)
		{
			if(DemigodsCharacter.Util.areAllied(character, Data.CHARACTER.get(data.getOwner()))) return false;
			if(Data.CHARACTER.containsKey(data.getOwner()))
			{
				DemigodsPlayer demigodsPlayer = DemigodsPlayer.Util.getPlayer(Data.CHARACTER.get(data.getOwner()).getOfflinePlayer());
				long lastLogoutTime = demigodsPlayer.getLastLogoutTime();
				Calendar calendarHalfHour = Calendar.getInstance();
				calendarHalfHour.add(Calendar.MINUTE, -30);
				long thirtyMinutesAgo = calendarHalfHour.getTime().getTime();
				Calendar calendarWeek = Calendar.getInstance();
				calendarWeek.add(Calendar.WEEK_OF_YEAR, -3);
				long threeWeeksAgo = calendarWeek.getTime().getTime();
				if(!demigodsPlayer.getOfflinePlayer().isOnline() && lastLogoutTime != -1 && (lastLogoutTime > System.currentTimeMillis() - thirtyMinutesAgo || lastLogoutTime < threeWeeksAgo))
				{
					character.getOfflinePlayer().getPlayer().sendMessage(ChatColor.YELLOW + "This obelisk currently immune to damage.");
					return false;
				}
			}
			Location location = data.getReferenceLocation();
			location.getWorld().playSound(location, Sound.WITHER_HURT, 0.4F, 1.5F);
			for(Location found : data.getLocations())
				location.getWorld().playEffect(found, Effect.STEP_SOUND, Material.REDSTONE_BLOCK.getId());

			character.getOfflinePlayer().getPlayer().sendMessage("Corruption: " + (data.getSanctity() - data.getCorruption()));

			return true;
		}
	};
	private static final StructureType.InteractFunction<Boolean> birth = new StructureType.InteractFunction<Boolean>()
	{
		@Override
		public Boolean apply(StructureSave data, DemigodsCharacter character)
		{
			Location location = data.getReferenceLocation();
			location.getWorld().strikeLightningEffect(location);
			location.getWorld().strikeLightningEffect(character.getLocation());
			return true;
		}
	};
	private static final StructureType.InteractFunction<Boolean> kill = new StructureType.InteractFunction<Boolean>()
	{
		@Override
		public Boolean apply(StructureSave data, DemigodsCharacter character)
		{
			Location location = data.getReferenceLocation();
			location.getWorld().playSound(location, Sound.WITHER_DEATH, 1F, 1.2F);
			location.getWorld().createExplosion(location, 2F, false);
			character.addKill();
			return true;
		}
	};
	private static final Set<StructureType.Flag> flags = new HashSet<StructureType.Flag>()
	{
		{
			add(StructureType.Flag.DESTRUCT_ON_BREAK);
			add(StructureType.Flag.NO_GRIEFING);
		}
	};
	private static final Listener listener = new Listener()
	{
		@EventHandler(priority = EventPriority.HIGH)
		public void createAndRemove(PlayerInteractEvent event)
		{
			if(event.getClickedBlock() == null) return;

			if(Zones.inNoDemigodsZone(event.getPlayer().getLocation())) return;

			// Define variables
			Block clickedBlock = event.getClickedBlock();
			Location location = clickedBlock.getLocation();
			Player player = event.getPlayer();

			if(DemigodsPlayer.Util.isImmortal(player))
			{
				DemigodsCharacter character = DemigodsPlayer.Util.getPlayer(player).getCurrent();

				if(event.getAction() == Action.RIGHT_CLICK_BLOCK && !character.getDeity().getFlags().contains(Deity.Flag.NO_OBELISK) && character.getDeity().getClaimItems().keySet().contains(event.getPlayer().getItemInHand().getType()) && Util.validBlockConfiguration(event.getClickedBlock()))
				{
					if(StructureType.Util.noOverlapStructureNearby(location))
					{
						player.sendMessage(ChatColor.YELLOW + "This location is too close to a no-pvp zone, please try again.");
						return;
					}

					try
					{
						// Obelisk created!
						Administration.Util.sendDebug(ChatColor.RED + "Obelisk created by " + character.getName() + " at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ());
						StructureSave save = inst().createNew(true, null, location);
						save.setOwner(character.getId());
						inst().birth(save, character);

						player.sendMessage(ChatColor.GRAY + English.NOTIFICATION_OBELISK_CREATED.getLine());
						event.setCancelled(true);
					}
					catch(Exception errored)
					{
						// Creation of shrine failed...
						Messages.warning(errored.getMessage());
					}
				}
			}

			if(Administration.Util.useWand(player) && StructureType.Util.partOfStructureWithType(location, "Obelisk"))
			{
				event.setCancelled(true);

				StructureSave save = StructureType.Util.getStructureRegional(location);
				DemigodsCharacter owner = DemigodsCharacter.Util.load(save.getOwner());

				if(Data.TIMED.boolContainsKey(player.getName() + "destroy_obelisk"))
				{
					// Remove the Obelisk
					save.remove();
					Data.TIMED.removeBool(player.getName() + "destroy_obelisk");

					Administration.Util.sendDebug(ChatColor.RED + "Obelisk owned by (" + owner.getName() + ") at: " + ChatColor.GRAY + "(" + location.getWorld().getName() + ") " + location.getX() + ", " + location.getY() + ", " + location.getZ() + " removed.");

					player.sendMessage(ChatColor.GREEN + English.ADMIN_WAND_REMOVE_OBELISK_COMPLETE.getLine());
				}
				else
				{
					Data.TIMED.setBool(player.getName() + "destroy_obelisk", true, 5, TimeUnit.SECONDS);
					player.sendMessage(ChatColor.RED + English.ADMIN_WAND_REMOVE_OBELISK.getLine());
				}
			}
		}
	};
	private static final int radius = Configs.getSettingInt("zones.obelisk_radius");
	private static final Predicate<Player> allow = new Predicate<Player>()
	{
		@Override
		public boolean apply(Player player)
		{
			return true;
		}
	};
	private static final float sanctity = 850F, sanctityRegen = 1F;

	private static final Schematic general = new Schematic("general", "HmmmQuestionMark", 3)
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
	private static final Schematic desert = new Schematic("desert", "HmmmQuestionMark", 3)
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
	private static final Schematic nether = new Schematic("nether", "HmmmQuestionMark", 3)
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

	private static final int generationPoints = 1;

	private Obelisk()
	{
		super(name, ObeliskDesign.values(), getDesign, createNew, sanctify, corrupt, birth, kill, flags, listener, radius, allow, sanctity, sanctityRegen, generationPoints);
	}

	public static enum ObeliskDesign implements GreekStructureType.Design
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
			return getSchematic(null).getLocations(reference);
		}

		@Override
		public Schematic getSchematic(StructureSave unused)
		{
			return schematic;
		}
	}

	public static class Util
	{
		public static boolean validBlockConfiguration(Block block)
		{
			if(!block.getType().equals(Material.REDSTONE_BLOCK)) return false;
			if(!block.getRelative(1, 0, 0).getType().equals(Material.STONE)) return false;
			if(!block.getRelative(-1, 0, 0).getType().equals(Material.STONE)) return false;
			if(!block.getRelative(0, 0, 1).getType().equals(Material.STONE)) return false;
			if(!block.getRelative(0, 0, -1).getType().equals(Material.STONE)) return false;
			return !block.getRelative(1, 0, 1).getType().isSolid() && !block.getRelative(1, 0, -1).getType().isSolid() && !block.getRelative(-1, 0, 1).getType().isSolid() && !block.getRelative(-1, 0, -1).getType().isSolid();
		}
	}

	private static final StructureType INST = new Obelisk();

	public static StructureType inst()
	{
		return INST;
	}
}
