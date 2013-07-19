package com.censoredsoftware.Demigods.Engine.Object.Ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.censoredsoftware.Demigods.Engine.Demigods;
import com.censoredsoftware.Demigods.Engine.Event.Ability.AbilityEvent;
import com.censoredsoftware.Demigods.Engine.Event.Ability.AbilityTargetEvent;
import com.censoredsoftware.Demigods.Engine.Object.Deity.Deity;
import com.censoredsoftware.Demigods.Engine.Object.Mob.TameableWrapper;
import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerCharacter;
import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerWrapper;
import com.censoredsoftware.Demigods.Engine.Utility.ZoneUtility;

public abstract class Ability
{
	private AbilityInfo info;
	private Listener listener;
	private Runnable runnable;

	public Ability(AbilityInfo info, Listener listener, Runnable runnable)
	{
		this.info = info;
		this.listener = listener;
		this.runnable = runnable;
	}

	public AbilityInfo getInfo()
	{
		return info;
	}

	public Listener getListener()
	{
		return listener;
	}

	public Runnable getRunnable()
	{
		return runnable;
	}

	private static final int TARGETOFFSET = 5;

	private static boolean doAbilityPreProcess(Player player, int cost)
	{
		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();

		if(!ZoneUtility.canTarget(player))
		{
			player.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
			return false;
		}
		else if(character.getMeta().getFavor() < cost)
		{
			player.sendMessage(ChatColor.YELLOW + "You do not have enough favor.");
			return false;
		}
		else return true;
	}

	/**
	 * Returns true if the ability for <code>player</code>, called <code>name</code>,
	 * with a cost of <code>cost</code>, that is Type <code>type</code>, has
	 * passed all pre-process tests.
	 * 
	 * @param player the player doing the ability
	 * @param name the name of the ability
	 * @param cost the cost (in favor) of the ability
	 * @param info the AbilityInfo object
	 * @return true/false depending on if all pre-process tests have passed
	 */
	public static boolean doAbilityPreProcess(Player player, String name, int cost, AbilityInfo info)
	{
		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();

		return doAbilityPreProcess(player, cost) && callAbilityEvent(name, character, cost, info);
	}

	/**
	 * Returns true if the ability for <code>player</code>, called <code>name</code>,
	 * with a cost of <code>cost</code>, that is Type <code>type</code>, that
	 * is doTargeting the LivingEntity <code>target</code>, has passed all pre-process tests.
	 * 
	 * @param player the Player doing the ability
	 * @param target the LivingEntity being targeted
	 * @param name the name of the ability
	 * @param cost the cost (in favor) of the ability
	 * @param info the AbilityInfo object
	 * @return true/false depending on if all pre-process tests have passed
	 */
	public static boolean doAbilityPreProcess(Player player, LivingEntity target, String name, int cost, AbilityInfo info)
	{
		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();

		if(doAbilityPreProcess(player, cost) && callAbilityEvent(name, character, cost, info))
		{
			if(!(target instanceof LivingEntity))
			{
				player.sendMessage(ChatColor.YELLOW + "No target found.");
				return false;
			}
			else if(!ZoneUtility.canTarget(target))
			{
				player.sendMessage(ChatColor.YELLOW + "Target is in a no-PVP zone.");
				return false;
			}
			else if(target instanceof Player)
			{
				PlayerCharacter attacked = PlayerWrapper.getPlayer(((Player) target)).getCurrent();
				if(attacked != null && PlayerCharacter.areAllied(character, attacked)) return false;
			}
			else if(target instanceof Tameable)
			{
				TameableWrapper attacked = TameableWrapper.getTameable(target);
				if(attacked != null && PlayerCharacter.areAllied(character, attacked.getOwner())) return false;
			}
			Bukkit.getServer().getPluginManager().callEvent(new AbilityTargetEvent(character, target, info));
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the callAbilityEvent <code>callAbilityEvent</code> is caused by a left click.
	 * 
	 * @param event the interact callAbilityEvent
	 * @return true/false depending on if the callAbilityEvent is caused by a left click or not
	 */
	public static boolean isLeftClick(PlayerInteractEvent event)
	{
		Action action = event.getAction();
		return action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
	}

	/**
	 * Returns the LivingEntity that <code>player</code> is doTargeting.
	 * 
	 * @param player the interact callAbilityEvent
	 * @return the targeted LivingEntity
	 */
	public static LivingEntity autoTarget(Player player)
	{
		int targetRangeCap = Demigods.config.getSettingInt("caps.target_range");

		/*
		 * BlockIterator iterator = new BlockIterator(player.getWorld(), player.getLocation().toVector(), player.getEyeLocation().getDirection(), 0, checkArea);
		 * 
		 * while(iterator.hasNext())
		 * {
		 * Block block = iterator.next();
		 * for(Entity entity : player.getNearbyEntities(checkArea, checkArea, checkArea))
		 * {
		 * if(entity instanceof Tameable && ((Tameable) entity).isTamed())
		 * {
		 * TameableWrapper wrapper = TameableWrapper.getTameable((LivingEntity) entity);
		 * if(wrapper != null && PlayerCharacter.areAllied(PlayerWrapper.getPlayer(player).getCurrent(), wrapper.getOwner())) continue;
		 * }
		 * else if(entity instanceof LivingEntity)
		 * {
		 * int acc = 2;
		 * for(int x = -acc; x < acc; x++)
		 * {
		 * for(int z = -acc; z < acc; z++)
		 * {
		 * for(int y = -acc; y < acc; y++)
		 * {
		 * if(entity.getLocation().getBlock().getRelative(x, y, z).equals(block)) return (LivingEntity) entity;
		 * }
		 * }
		 * }
		 * }
		 * }
		 * }
		 */

		Location targetLoc = player.getTargetBlock(null, targetRangeCap).getLocation();

		ENTITIES: for(Entity entity : player.getNearbyEntities(targetRangeCap, targetRangeCap, targetRangeCap))
		{
			if(entity.getLocation().distance(targetLoc) < 3 && entity instanceof LivingEntity)
			{
				if(entity instanceof Tameable && ((Tameable) entity).isTamed())
				{
					TameableWrapper wrapper = TameableWrapper.getTameable((LivingEntity) entity);
					if(wrapper != null && !PlayerCharacter.areAllied(PlayerWrapper.getPlayer(player).getCurrent(), wrapper.getOwner())) continue ENTITIES;
				}

				return (LivingEntity) entity;
			}
		}

		return null;
	}

	public static Location directTarget(Player player)
	{
		return player.getTargetBlock(null, Demigods.config.getSettingInt("caps.target_range")).getLocation();
	}

	/**
	 * Returns true if the <code>player</code> ability hits <code>target</code>.
	 * 
	 * @param player the player using the ability
	 * @param target the targeted LivingEntity
	 * @return true/false depending on if the ability hits or misses
	 */
	public static boolean doTargeting(Player player, Location target, boolean notify)
	{
		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
		Location toHit = adjustedAimLocation(character, target);
		if(isHit(target, toHit)) return true;
		if(notify) player.sendMessage(ChatColor.RED + "Missed..."); // TODO Better message.
		return false;
	}

	/**
	 * Returns true if the ability callAbilityEvent for <code>character</code>, called <code>name</code>,
	 * with a cost of <code>cost</code>, that is Type <code>type</code>, has passed
	 * all pre-process tests.
	 * 
	 * @param character the character triggering the ability callAbilityEvent
	 * @param name the name of the ability
	 * @param cost the cost (in favor) of the ability
	 * @param info the AbilityInfo object
	 * @return true/false if the callAbilityEvent isn't cancelled or not
	 */
	public static boolean callAbilityEvent(String name, PlayerCharacter character, int cost, AbilityInfo info)
	{
		AbilityEvent event = new AbilityEvent(name, character, cost, info);
		Bukkit.getServer().getPluginManager().callEvent(event);
		return !event.isCancelled();
	}

	/**
	 * Returns the location that <code>character</code> is actually aiming
	 * at when doTargeting <code>target</code>.
	 * 
	 * @param character the character triggering the ability callAbilityEvent
	 * @param target the location the character is doTargeting at
	 * @return the aimed at location
	 */
	public static Location adjustedAimLocation(PlayerCharacter character, Location target)
	{
		int ascensions = character.getMeta().getAscensions();
		if(ascensions < 3) ascensions = 3;

		int offset = (int) (TARGETOFFSET + character.getOfflinePlayer().getPlayer().getLocation().distance(target));
		int adjustedOffset = offset / ascensions;
		if(adjustedOffset < 1) adjustedOffset = 1;
		Random random = new Random();
		World world = target.getWorld();

		int randomInt = random.nextInt(adjustedOffset);

		int sampleSpace = random.nextInt(3);

		double X = target.getX();
		double Z = target.getZ();
		double Y = target.getY();

		if(sampleSpace == 0)
		{
			X += randomInt;
			Z += randomInt;
		}
		else if(sampleSpace == 1)
		{
			X -= randomInt;
			Z -= randomInt;
		}
		else if(sampleSpace == 2)
		{
			X -= randomInt;
			Z += randomInt;
		}
		else if(sampleSpace == 3)
		{
			X += randomInt;
			Z -= randomInt;
		}

		return new Location(world, X, Y, Z);
	}

	/**
	 * Returns true if <code>target</code> is hit at <code>hit</code>.
	 * 
	 * @param target the LivingEntity being targeted
	 * @param hit the location actually hit
	 * @return true/false if <code>target</code> is hit
	 */
	public static boolean isHit(Location target, Location hit)
	{
		Location shouldHit = target;
		return hit.distance(shouldHit) <= 2;
	}

	public static List<Ability> getLoadedAbilities()
	{
		return new ArrayList<Ability>()
		{
			{
				for(Deity deity : Demigods.getLoadedDeities())
				{
					addAll(deity.getAbilities());
				}
			}
		};
	}

	public static boolean invokeAbilityCommand(Player player, String command, boolean bind)
	{
		PlayerCharacter character = PlayerWrapper.getPlayer(player).getCurrent();
		for(Ability ability : getLoadedAbilities())
		{
			if(ability.getInfo().getType() == Devotion.Type.PASSIVE) continue;
			if(ability.getInfo().getCommand() != null && ability.getInfo().getCommand().equalsIgnoreCase(command))
			{
				if(!player.hasPermission(ability.getInfo().getPermission())) return true;

				if(!Deity.canUseDeity(player, ability.getInfo().getDeity())) return true;

				if(bind)
				{
					// Bind item
					character.getMeta().setBound(ability.getInfo().getName(), player.getItemInHand().getType());
				}
				else
				{
					if(character.getMeta().isEnabledAbility(ability.getInfo().getName()))
					{
						character.getMeta().toggleAbility(ability.getInfo().getName(), false);
						player.sendMessage(ChatColor.YELLOW + ability.getInfo().getName() + " is no longer active.");
					}
					else
					{
						character.getMeta().toggleAbility(ability.getInfo().getName(), true);
						player.sendMessage(ChatColor.YELLOW + ability.getInfo().getName() + " is now active.");
					}
				}
				return true;
			}
		}
		return false;
	}

	public static void dealDamage(LivingEntity source, LivingEntity target, double amount, EntityDamageEvent.DamageCause cause)
	{
		if(source instanceof Player)
		{
			PlayerWrapper owner = PlayerWrapper.getPlayer(((Player) source));
			if(owner != null)
			{
				if(target instanceof Player)
				{
					PlayerCharacter targetChar = PlayerWrapper.getPlayer(((Player) target)).getCurrent();
					if(targetChar != null && PlayerCharacter.areAllied(owner.getCurrent(), targetChar)) return;
				}
				else if(target instanceof Tameable && ((Tameable) target).isTamed())
				{
					TameableWrapper wrapper = TameableWrapper.getTameable(target);
					if(wrapper != null && PlayerCharacter.areAllied(owner.getCurrent(), wrapper.getOwner())) return;
				}
			}
		}
		EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(source, target, cause, amount);
		target.setLastDamageCause(event);
		if(amount >= 1 && !event.isCancelled()) target.damage(amount);
	}
}
