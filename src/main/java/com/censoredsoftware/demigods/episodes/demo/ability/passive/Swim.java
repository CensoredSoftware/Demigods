package com.censoredsoftware.demigods.episodes.demo.ability.passive;

import com.censoredsoftware.demigods.engine.element.Ability;
import com.censoredsoftware.demigods.engine.element.Deity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Swim extends Ability
{
	private final static String name = "Swim", command = null;
	private final static int cost = 0, delay = 0, repeat = 0;
	private static Info info;
	private final static List<String> details = new ArrayList<String>(1)
	{
		{
			add("Crouch while in water to swim very fast.");
		}
	};
	private final static Devotion.Type type = Devotion.Type.PASSIVE;

	public Swim(final String deity, String permission)
	{
		super(info = new Info(deity, name, command, permission, cost, delay, repeat, details, type), new Listener()
		{
			@EventHandler(priority = EventPriority.HIGH)
			public void onPlayerMove(PlayerMoveEvent event)
			{
				Player player = event.getPlayer();
				if(!Deity.Util.canUseDeitySilent(player, deity)) return;

				// PHELPS SWIMMING
				if(player.getLocation().getBlock().getType().equals(Material.STATIONARY_WATER) || player.getLocation().getBlock().getType().equals(Material.WATER))
				{
					Vector direction = player.getLocation().getDirection().normalize().multiply(1.3D);
					Vector victor = new Vector(direction.getX(), direction.getY(), direction.getZ());
					if(player.isSneaking()) player.setVelocity(victor);
				}
			}
		}, null);
	}
}
