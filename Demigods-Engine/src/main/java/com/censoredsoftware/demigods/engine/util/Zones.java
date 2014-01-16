package com.censoredsoftware.demigods.engine.util;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.censoredsoftware.censoredlib.util.WorldGuards;
import com.censoredsoftware.demigods.base.listener.ZoneListener;
import com.censoredsoftware.demigods.engine.DemigodsPlugin;
import com.censoredsoftware.demigods.engine.data.serializable.DPlayer;
import com.censoredsoftware.demigods.engine.data.serializable.StructureSave;
import com.censoredsoftware.demigods.engine.mythos.Structure;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class Zones
{
	private static final Set<String> ENABLED_WORLDS = Sets.newHashSet();

	public static int init()
	{
		// Load disabled worlds
		Set<String> enabledWorlds = Sets.newHashSet();
		int erroredWorlds = 0;
		for(String world : Configs.getSettingList("restrictions.enabled_worlds"))
		{
			enabledWorlds.add(world);
			erroredWorlds += Bukkit.getServer().getWorld(world) == null ? 1 : 0;
		}
		ENABLED_WORLDS.addAll(enabledWorlds);

		// Zone listener (load here for consistency)
		Bukkit.getPluginManager().registerEvents(new ZoneListener(), DemigodsPlugin.plugin());

		// Init WorldGuard stuff
		WorldGuards.createFlag("STATE", "demigods", true, "ALL");
		WorldGuards.registerCreatedFlag("demigods");
		WorldGuards.setWhenToOverridePVP(DemigodsPlugin.plugin(), new Predicate<EntityDamageByEntityEvent>()
		{
			@Override
			public boolean apply(EntityDamageByEntityEvent event)
			{
				return !Zones.inNoDemigodsZone(event.getEntity().getLocation());
			}
		});

		return erroredWorlds;
	}

	/**
	 * Returns true if <code>location</code> is within a no-PVP zone.
	 * 
	 * @param location the location to check.
	 * @return true/false depending on if it's a no-PVP zone or not.
	 */
	public static boolean inNoPvpZone(Location location)
	{
		if(Configs.getSettingBoolean("zones.allow_skills_anywhere")) return false;
		if(WorldGuards.worldGuardEnabled()) return Structure.Util.isInRadiusWithFlag(location, Structure.Flag.NO_PVP) || !WorldGuards.canPVP(location);
		return Structure.Util.isInRadiusWithFlag(location, Structure.Flag.NO_PVP);
	}

	/**
	 * Returns true if <code>location</code> is within a no-build zone
	 * for <code>player</code>.
	 * 
	 * @param player the player to check.
	 * @param location the location to check.
	 * @return true/false depending on the position of the <code>player</code>.
	 */
	public static boolean inNoBuildZone(Player player, Location location)
	{
		if(WorldGuards.worldGuardEnabled() && !WorldGuards.canBuild(player, location)) return true;
		StructureSave save = Iterables.getFirst(Structure.Util.getInRadiusWithFlag(location, Structure.Flag.NO_GRIEFING), null);
		return DPlayer.Util.getPlayer(player).getCurrent() != null && save != null && !DPlayer.Util.getPlayer(player).getCurrent().getId().equals(save.getOwner());
	}

	public static boolean inNoDemigodsZone(Location location)
	{
		return isNoDemigodsWorld(location.getWorld()); // || WorldGuards.worldGuardEnabled() && WorldGuards.checkForCreatedFlagValue("demigods", "deny", location);
	}

	public static boolean isNoDemigodsWorld(World world)
	{
		return !ENABLED_WORLDS.contains(world.getName());
	}

	public static void enableWorld(String worldName)
	{
		ENABLED_WORLDS.add(worldName);
	}

	public static void disableWorld(String worldName)
	{
		ENABLED_WORLDS.remove(worldName);
	}
}
