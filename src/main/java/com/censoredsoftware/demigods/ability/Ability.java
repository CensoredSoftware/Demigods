package com.censoredsoftware.demigods.ability;

import com.censoredsoftware.demigods.Demigods;
import com.censoredsoftware.demigods.Element;
import com.censoredsoftware.demigods.battle.Battle;
import com.censoredsoftware.demigods.language.Translation;
import com.censoredsoftware.demigods.player.DCharacter;
import com.censoredsoftware.demigods.player.DPlayer;
import com.censoredsoftware.demigods.player.Pet;
import com.censoredsoftware.demigods.player.Skill;
import com.censoredsoftware.demigods.util.Strings;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public interface Ability
{
	public static final int TARGET_OFFSET = 5;

	public String getDeity();

	public String getName();

	public String getCommand();

	public String getPermission();

	public int getCost();

	public int getDelay();

	public int getRepeat();

	public List<String> getDetails();

	public Skill.Type getType();

	public Material getWeapon();

	public boolean hasWeapon();

	public Listener getListener();

	public BukkitRunnable getRunnable();

	public static class Util
	{
		private static boolean doAbilityPreProcess(Player player, int cost)
		{
			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();

			if(!Battle.Util.canTarget(character))
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
		 * @param cost the cost (in favor) of the ability
		 * @return true/false depending on if all pre-process tests have passed
		 */
		public static boolean doAbilityPreProcess(Player player, int cost, Skill.Type type)
		{
			// DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
			return doAbilityPreProcess(player, cost); // TODO callAbilityEvent(name, character, cost, info);
		}

		/**
		 * Returns true if the ability for <code>player</code>, called <code>name</code>,
		 * with a cost of <code>cost</code>, that is Type <code>type</code>, that
		 * is doTargeting the LivingEntity <code>target</code>, has passed all pre-process tests.
		 * 
		 * @param player the Player doing the ability
		 * @param target the LivingEntity being targeted
		 * @param cost the cost (in favor) of the ability
		 * @return true/false depending on if all pre-process tests have passed
		 */
		public static boolean doAbilityPreProcess(Player player, LivingEntity target, int cost, Skill.Type type)
		{
			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();

			if(doAbilityPreProcess(player, cost)) // TODO callAbilityEvent(name, character, cost, info))
			{
				if(target == null)
				{
					player.sendMessage(ChatColor.YELLOW + "No target found.");
					return false;
				}
				else if(Battle.Util.canParticipate(target) && !Battle.Util.canTarget(Battle.Util.defineParticipant(target)))
				{
					player.sendMessage(ChatColor.YELLOW + "Target is in a no-PVP zone.");
					return false;
				}
				else if(target instanceof Player)
				{
					DCharacter attacked = DPlayer.Util.getPlayer(((Player) target)).getCurrent();
					if(attacked != null && DCharacter.Util.areAllied(character, attacked)) return false;
				}
				else if(target instanceof Tameable)
				{
					Pet attacked = Pet.Util.getTameable(target);
					if(attacked != null && DCharacter.Util.areAllied(character, attacked.getOwner())) return false;
				}
				// TODO Bukkit.getServer().getPluginManager().callEvent(new AbilityTargetEvent(character, target, info));
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
			// Define variables
			int range = Demigods.config.getSettingInt("caps.target_range") > 140 ? 140 : Demigods.config.getSettingInt("caps.target_range");
			final int correction = 3;
			Location target = player.getTargetBlock(null, range).getLocation();
			BlockIterator iterator = new BlockIterator(player, range);
			List<Entity> targets = Lists.newArrayList();
			final DCharacter looking = DPlayer.Util.getPlayer(player).getCurrent();

			// Iterate through the blocks and find the target
			while(iterator.hasNext())
			{
				final Block block = iterator.next();

				targets.addAll(Collections2.filter(player.getNearbyEntities(range, range, range), new Predicate<Entity>()
				{
					@Override
					public boolean apply(Entity entity)
					{
						if(entity instanceof LivingEntity && entity.getLocation().distance(block.getLocation()) <= correction)
						{
							if(entity instanceof Tameable && ((Tameable) entity).isTamed() && Pet.Util.getTameable((LivingEntity) entity) != null)
							{
								Pet wrapper = Pet.Util.getTameable((LivingEntity) entity);
								if(DCharacter.Util.areAllied(looking, wrapper.getOwner())) return false;
							}
							else if(entity instanceof Player && DPlayer.Util.getPlayer(((Player) entity)).getCurrent() != null)
							{
								DCharacter character = DPlayer.Util.getPlayer(((Player) entity)).getCurrent();
								if(DCharacter.Util.areAllied(looking, character)) return false;
							}
							return true;
						}
						return false;
					}
				}));
			}

			// Attempt to return the closest entity to the cursor
			for(Entity entity : targets)
				if(entity.getLocation().distance(target) <= correction) return (LivingEntity) entity;

			// If it failed to do that then just return the first entity
			try
			{
				return (LivingEntity) targets.get(0);
			}
			catch(Exception ignored)
			{}

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
			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
			Location toHit = adjustedAimLocation(character, target);
			if(isHit(target, toHit)) return true;
			if(notify) player.sendMessage(ChatColor.RED + "Missed..."); // TODO Better message.
			return false;
		}

		/**
		 * Returns the location that <code>character</code> is actually aiming
		 * at when doTargeting <code>target</code>.
		 * 
		 * @param character the character triggering the ability callAbilityEvent
		 * @param target the location the character is doTargeting at
		 * @return the aimed at location
		 */
		public static Location adjustedAimLocation(DCharacter character, Location target)
		{
			// TODO: This needs major work.

			int accuracy = character.getDeity().getAccuracy();
			if(accuracy < 3) accuracy = 3;

			int offset = (int) (TARGET_OFFSET + character.getOfflinePlayer().getPlayer().getLocation().distance(target));
			int adjustedOffset = offset / accuracy;
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
			return hit.distance(target) <= 2;
		}

		public static List<Ability> getLoadedAbilities()
		{
			List<Ability> list = new ArrayList<Ability>();
			for(Element.ListedDeity deity : Element.Deity.values())
				list.addAll(deity.getDeity().getAbilities());
			return list;
		}

		public static boolean invokeAbilityCommand(Player player, String command)
		{
			DCharacter character = DPlayer.Util.getPlayer(player).getCurrent();
			for(final Ability ability : character.getDeity().getAbilities())
			{
				if(ability.getType().equals(Skill.Type.PASSIVE)) continue;

				if(ability.getCommand() != null && ability.getCommand().equalsIgnoreCase(command))
				{
					// Ensure that the deity can be used, permission allows it, etc
					if(!com.censoredsoftware.demigods.deity.Deity.Util.canUseDeity(character, ability.getDeity())) return true;
					if(!player.hasPermission(ability.getPermission())) return true;

					// Handle enabling the command
					String abilityName = ability.getName();
					ItemStack itemInHand = player.getItemInHand();

					if(!character.getMeta().isBound(ability))
					{
						if(itemInHand == null || itemInHand.getType().equals(Material.AIR))
						{
							// Slot must not be empty
							player.sendMessage(ChatColor.RED + Demigods.language.getText(Translation.Text.ERROR_EMPTY_SLOT));
							return true;
						}
						else if(character.getMeta().isBound(itemInHand.getType()))
						{
							// Material already bound
							player.sendMessage(ChatColor.RED + Demigods.language.getText(Translation.Text.ERROR_MATERIAL_BOUND));
							return true;
						}
						else if(ability.hasWeapon() && !itemInHand.getType().equals(ability.getWeapon()))
						{
							// Weapon required
							player.sendMessage(ChatColor.RED + Demigods.language.getText(Translation.Text.ERROR_BIND_WEAPON_REQUIRED).replace("{weapon}", Strings.beautify(ability.getWeapon().name()).toLowerCase()).replace("{ability}", abilityName));
							return true;
						}

						// Save the bind
						character.getMeta().setBind(ability, itemInHand.getType());

						// Let them know
						player.sendMessage(ChatColor.GREEN + Demigods.language.getText(Translation.Text.SUCCESS_ABILITY_BOUND).replace("{ability}", StringUtils.capitalize(abilityName)).replace("{material}", (Strings.beginsWithVowel(itemInHand.getType().name()) ? "an " : "a ") + Strings.beautify(itemInHand.getType().name()).toLowerCase()));

						return true;
					}
					else
					{
						// Remove the bind
						character.getMeta().removeBind(ability);

						// Let them know
						player.sendMessage(ChatColor.GREEN + Demigods.language.getText(Translation.Text.SUCCESS_ABILITY_UNBOUND).replace("{ability}", StringUtils.capitalize(abilityName)));

						return true;
					}
				}
			}
			return false;
		}

		public static void dealDamage(LivingEntity source, LivingEntity target, double amount, EntityDamageEvent.DamageCause cause)
		{
			if(source instanceof Player)
			{
				DPlayer owner = DPlayer.Util.getPlayer(((Player) source));
				if(owner != null)
				{
					if(target instanceof Player)
					{
						DCharacter targetChar = DPlayer.Util.getPlayer(((Player) target)).getCurrent();
						if(targetChar != null && DCharacter.Util.areAllied(owner.getCurrent(), targetChar)) return;
					}
					else if(target instanceof Tameable && ((Tameable) target).isTamed())
					{
						Pet wrapper = Pet.Util.getTameable(target);
						if(wrapper != null && DCharacter.Util.areAllied(owner.getCurrent(), wrapper.getOwner())) return;
					}
				}
			}

			/*
			 * This code below MAY cause issues, it should be watched whenever something is changed.
			 */
			EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(source, target, cause, amount);
			Bukkit.getPluginManager().callEvent(event);
			if(amount >= 1 && !event.isCancelled())
			{
				target.setLastDamageCause(event);
				target.damage(amount);
			}
		}
	}
}
