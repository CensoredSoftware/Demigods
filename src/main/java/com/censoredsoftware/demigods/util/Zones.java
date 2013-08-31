package com.censoredsoftware.demigods.util;

import com.censoredsoftware.demigods.Demigods;
import com.censoredsoftware.demigods.player.DPlayer;
import com.censoredsoftware.demigods.structure.Structure;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Zones
{
	/**
	 * Returns true if <code>location</code> is within a no-PVP zone.
	 * 
	 * @param location the location to check.
	 * @return true/false depending on if it's a no-PVP zone or not.
	 */
	public static boolean zoneNoPVP(Location location)
	{
		if(Configs.getSettingBoolean("zones.allow_skills_anywhere")) return false;
		if(Demigods.WORLD_GUARD != null) return Structure.Util.isInRadiusWithFlag(location, Structure.Flag.NO_PVP) || Iterators.any(Demigods.WORLD_GUARD.getRegionManager(location.getWorld()).getApplicableRegions(location).iterator(), new Predicate<ProtectedRegion>()
		{
			@Override
			public boolean apply(ProtectedRegion region)
			{
				return region.getId().toLowerCase().contains("nopvp");
			}
		});
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
	public static boolean zoneNoBuild(Player player, Location location) // TODO BROKEN
	{
		if(Demigods.WORLD_GUARD != null && !Demigods.WORLD_GUARD.canBuild(player, location)) return true;
		Structure save = Structure.Util.getInRadiusWithFlag(location, Structure.Flag.NO_GRIEFING);
		if(save != null && save.getOwner() != null) return !save.getOwner().equals(DPlayer.Util.getPlayer(player).getCurrent().getId());
		return false;
	}
}
