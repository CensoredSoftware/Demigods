package com.censoredsoftware.demigods.ability.passive;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.censoredsoftware.demigods.Demigods;
import com.censoredsoftware.demigods.ability.Ability;
import com.censoredsoftware.demigods.deity.Deity;
import com.google.common.collect.Lists;

public class Swim implements Ability
{
	private final static String name = "Swim", command = null;
	private final static int cost = 0, delay = 0, repeat = 20;
	private final static Devotion.Type type = Devotion.Type.PASSIVE;
	private final static List<String> details = Lists.newArrayList("Crouch while in water to swim very fast.");
	private String deity, permission;

	public Swim(final String deity, String permission)
	{
		this.deity = deity;
		this.permission = permission;
	}

	@Override
	public String getDeity()
	{
		return deity;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getCommand()
	{
		return command;
	}

	@Override
	public String getPermission()
	{
		return permission;
	}

	@Override
	public int getCost()
	{
		return cost;
	}

	@Override
	public int getDelay()
	{
		return delay;
	}

	@Override
	public int getRepeat()
	{
		return repeat;
	}

	@Override
	public List<String> getDetails()
	{
		return details;
	}

	@Override
	public Devotion.Type getType()
	{
		return type;
	}

	@Override
	public Material getWeapon()
	{
		return null;
	}

	@Override
	public boolean hasWeapon()
	{
		return getWeapon() != null;
	}

	@Override
	public Listener getListener()
	{
		return new Listener()
		{
			@EventHandler(priority = EventPriority.HIGHEST)
			private void onPlayerMoveEvent(PlayerMoveEvent event)
			{
				if(Demigods.isDisabledWorld(event.getPlayer().getWorld())) return;

				Player player = event.getPlayer();

				if(!Deity.Util.canUseDeitySilent(player, deity)) return;

				Material playerLocationMaterial = player.getLocation().getBlock().getType();
				if(!playerLocationMaterial.equals(Material.STATIONARY_WATER) && !playerLocationMaterial.equals(Material.WATER))
				{
					Vector victor = player.getLocation().getDirection().normalize().multiply(0.6D);
					player.setVelocity(new Vector(victor.getX(), victor.getY(), victor.getZ()));
					return;
				}

				if(player.isSneaking())
				{
					Vector victor = player.getLocation().getDirection().normalize().multiply(1.3D);
					player.setVelocity(new Vector(victor.getX(), victor.getY(), victor.getZ()));
				}
			}
		};
	}

	@Override
	public BukkitRunnable getRunnable()
	{
		return null;
	}
}
