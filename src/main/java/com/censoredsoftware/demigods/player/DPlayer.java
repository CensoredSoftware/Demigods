package com.censoredsoftware.demigods.player;

import com.censoredsoftware.demigods.Demigods;
import com.censoredsoftware.demigods.battle.Battle;
import com.censoredsoftware.demigods.conversation.Prayer;
import com.censoredsoftware.demigods.data.DataManager;
import com.censoredsoftware.demigods.helper.ColoredStringBuilder;
import com.censoredsoftware.demigods.language.Translation;
import com.censoredsoftware.demigods.location.Region;
import com.censoredsoftware.demigods.structure.Structure;
import com.censoredsoftware.demigods.util.Configs;
import com.censoredsoftware.demigods.util.Messages;
import com.censoredsoftware.demigods.util.Zones;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DPlayer implements ConfigurationSerializable
{
	private String player;
	private boolean canPvp;
	private long lastLoginTime, lastLogoutTime;
	private String currentDeityName;
	private UUID current;
	private UUID previous;
	private static ChatRecorder chatRecording;

	public DPlayer()
	{}

	public DPlayer(String player, ConfigurationSection conf)
	{
		this.player = player;
		canPvp = conf.getBoolean("canPvp");
		if(conf.isLong("lastLoginTime")) lastLoginTime = conf.getLong("lastLoginTime");
		else lastLoginTime = -1;
		if(conf.isLong("lastLogoutTime")) lastLogoutTime = conf.getLong("lastLogoutTime");
		else lastLogoutTime = -1;
		if(conf.getString("currentDeityName") != null) currentDeityName = conf.getString("currentDeityName");
		if(conf.getString("current") != null) current = UUID.fromString(conf.getString("current"));
		if(conf.getString("previous") != null) previous = UUID.fromString(conf.getString("previous"));
	}

	@Override
	public Map<String, Object> serialize()
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("canPvp", canPvp);
		map.put("lastLoginTime", lastLoginTime);
		map.put("lastLogoutTime", lastLogoutTime);
		if(currentDeityName != null) map.put("currentDeityName", currentDeityName);
		if(current != null) map.put("current", current.toString());
		if(previous != null) map.put("previous", previous.toString());
		return map;
	}

	void setPlayer(String player)
	{
		this.player = player;
	}

	public void resetCurrent()
	{
		this.current = null;
		this.currentDeityName = null;

		if(getOfflinePlayer().isOnline())
		{
			getOfflinePlayer().getPlayer().setDisplayName(getOfflinePlayer().getName());
			getOfflinePlayer().getPlayer().setPlayerListName(getOfflinePlayer().getName());
			getOfflinePlayer().getPlayer().setMaxHealth(20.0);
		}
	}

	public void setCanPvp(boolean pvp)
	{
		this.canPvp = pvp;
		Util.save(this);
	}

	public void updateCanPvp()
	{
		if(!getOfflinePlayer().isOnline()) return;

		// Define variables
		final Player player = getOfflinePlayer().getPlayer();
		final boolean inNoPvpZone = Zones.inNoPvpZone(player.getLocation());

		if(getCurrent() != null && Battle.Util.isInBattle(getCurrent())) return;

		if(!canPvp() && !inNoPvpZone)
		{
			setCanPvp(true);
			player.sendMessage(ChatColor.GRAY + Demigods.LANGUAGE.getText(Translation.Text.UNSAFE_FROM_PVP));
		}
		else if(!inNoPvpZone)
		{
			setCanPvp(true);
			DataManager.removeTimed(player.getName(), "pvp_cooldown");
		}
		else if(canPvp() && !DataManager.hasTimed(player.getName(), "pvp_cooldown"))
		{
			int delay = Configs.getSettingInt("zones.pvp_area_delay_time");
			DataManager.saveTimed(player.getName(), "pvp_cooldown", true, delay);

			Bukkit.getScheduler().scheduleSyncDelayedTask(Demigods.PLUGIN, new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if(Structure.Util.isInRadiusWithFlag(player.getLocation(), Structure.Flag.NO_PVP))
					{
						if(getCurrent() != null && Battle.Util.isInBattle(getCurrent())) return;
						setCanPvp(false);
						player.sendMessage(ChatColor.GRAY + Demigods.LANGUAGE.getText(Translation.Text.SAFE_FROM_PVP));
					}
				}
			}, (delay * 20));
		}
	}

	public OfflinePlayer getOfflinePlayer()
	{
		return Bukkit.getOfflinePlayer(this.player);
	}

	public void setLastLoginTime(Long time)
	{
		this.lastLoginTime = time;
		Util.save(this);
	}

	public Long getLastLoginTime()
	{
		if(lastLoginTime != -1) return this.lastLoginTime;
		return null;
	}

	public void setLastLogoutTime(Long time)
	{
		this.lastLogoutTime = time;
		Util.save(this);
	}

	public Long getLastLogoutTime()
	{
		if(lastLogoutTime != -1) return this.lastLogoutTime;
		return null;
	}

	public void switchCharacter(final DCharacter newChar)
	{
		final Player player = getOfflinePlayer().getPlayer();

		if(!newChar.getPlayer().equals(this.player))
		{
			player.sendMessage(ChatColor.RED + "You can't do that.");
			return;
		}

		// Update the current character
		final DCharacter currChar = getCurrent();

		if(currChar != null)
		{
			// Set to inactive and update previous
			currChar.setActive(false);
			this.previous = currChar.getId();

			// Set the values
			// TODO: Confirm that this covers all of the bases.
			currChar.setHealth(player.getHealth() >= currChar.getMaxHealth() ? currChar.getMaxHealth() : player.getHealth());
			currChar.setHunger(player.getFoodLevel());
			currChar.setLevel(player.getLevel());
			currChar.setExperience(player.getExp());
			currChar.setLocation(player.getLocation());
			Bukkit.getScheduler().scheduleSyncDelayedTask(Demigods.PLUGIN, new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if(player.getBedSpawnLocation() != null) currChar.setBedSpawn(player.getBedSpawnLocation());
				}
			}, 1);
			currChar.setPotionEffects(player.getActivePotionEffects());
			currChar.saveInventory();

			// Disown pets
			Pet.Util.disownPets(currChar.getName());

			// Save it
			DCharacter.Util.save(currChar);
		}

		// Set new character to active
		newChar.setActive(true);
		this.current = newChar.getId();

		// Set new deity
		currentDeityName = newChar.getDeity().getName();

		// Update their inventory
		if(getCharacters().size() == 1) newChar.saveInventory();
		newChar.getInventory().setToPlayer(player);

		// Update health, experience, and name
		// TODO: Confirm that this covers all of the bases too.
		player.setDisplayName(newChar.getDeity().getColor() + newChar.getName());
		try
		{
			player.setPlayerListName(newChar.getDeity().getColor() + newChar.getName());
		}
		catch(Exception e)
		{
			Messages.warning("Character name too long.");
		}
		player.setMaxHealth(newChar.getMaxHealth());
		player.setHealth(newChar.getHealth() >= newChar.getMaxHealth() ? newChar.getMaxHealth() : newChar.getHealth());
		player.setFoodLevel(newChar.getHunger());
		player.setExp(newChar.getExperience());
		player.setLevel(newChar.getLevel());
		for(PotionEffect potion : player.getActivePotionEffects())
			player.removePotionEffect(potion.getType());
		if(newChar.getPotionEffects() != null) player.addPotionEffects(newChar.getPotionEffects());
		Bukkit.getScheduler().scheduleSyncDelayedTask(Demigods.PLUGIN, new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if(newChar.getBedSpawn() != null) player.setBedSpawnLocation(newChar.getBedSpawn());
			}
		}, 1);

		// Re-own pets
		Pet.Util.reownPets(player, newChar);

		// Teleport them
		try
		{
			player.teleport(newChar.getLocation());
		}
		catch(Exception e)
		{
			Messages.severe("There was a problem while teleporting a player to their character.");
		}

		// Toggle praying
		Util.togglePraying(player, false);

		// Save instances
		Util.save(this);
		DCharacter.Util.save(newChar);
	}

	public boolean canPvp()
	{
		return this.canPvp;
	}

	public String getPlayerName()
	{
		return player;
	}

	public boolean hasCurrent()
	{
		return getCurrent() != null;
	}

	public Region getRegion()
	{
		if(getOfflinePlayer().isOnline()) return Region.Util.getRegion(getOfflinePlayer().getPlayer().getLocation());
		return Region.Util.getRegion(getCurrent().getLocation());
	}

	public DCharacter getCurrent()
	{
		if(this.current == null) return null;
		DCharacter character = DCharacter.Util.load(this.current);
		if(character != null && character.isUsable()) return character;
		return null;
	}

	public DCharacter getPrevious()
	{
		if(this.previous == null) return null;
		return DCharacter.Util.load(this.previous);
	}

	public String getCurrentDeityName()
	{
		return currentDeityName;
	}

	public Set<DCharacter> getCharacters()
	{
		return Sets.newHashSet(Collections2.filter(DCharacter.Util.loadAll(), new Predicate<DCharacter>()
		{
			@Override
			public boolean apply(DCharacter character)
			{
				return character != null && character.getPlayer().equals(player) && character.isUsable();
			}
		}));
	}

	public boolean canUseCurrent()
	{
		if(getCurrent() == null || !getCurrent().isUsable())
		{
			getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "Your current character was unable to load!");
			getOfflinePlayer().getPlayer().sendMessage(ChatColor.RED + "Please contact the server administrator immediately.");
			return false;
		}
		else return getOfflinePlayer().isOnline();
	}

	public static class Util
	{
		public static DPlayer create(OfflinePlayer player)
		{
			DPlayer trackedPlayer = new DPlayer();
			trackedPlayer.setPlayer(player.getName());
			trackedPlayer.setLastLoginTime(player.getLastPlayed());
			trackedPlayer.setCanPvp(true);
			Util.save(trackedPlayer);
			return trackedPlayer;
		}

		public static void save(DPlayer player)
		{
			DataManager.players.put(player.getPlayerName(), player);
		}

		public static DPlayer getPlayer(OfflinePlayer player)
		{
			DPlayer found = getPlayer(player.getName());
			if(found == null) return create(player);
			return found;
		}

		public static DPlayer getPlayer(String player)
		{
			if(DataManager.players.containsKey(player)) return DataManager.players.get(player);
			return null;
		}

		/**
		 * Returns true if the <code>player</code> is currently immortal.
		 * 
		 * @param player the player to check.
		 * @return boolean
		 */
		public static boolean isImmortal(OfflinePlayer player)
		{
			DCharacter character = getPlayer(player).getCurrent();
			return character != null;
		}

		/**
		 * Returns true if <code>player</code> has a character with the name <code>charName</code>.
		 * 
		 * @param player the player to check.
		 * @param charName the charName to check with.
		 * @return boolean
		 */
		public static boolean hasCharName(OfflinePlayer player, String charName)
		{
			for(DCharacter character : getPlayer(player).getCharacters())
				if(character.getName().equalsIgnoreCase(charName)) return true;
			return false;
		}

		/**
		 * Returns true if the <code>player</code> is currently praying.
		 * 
		 * @param player the player to check.
		 * @return boolean
		 */
		public static boolean isPraying(Player player)
		{
			try
			{
				return DataManager.hasKeyTemp(player.getName(), "prayer_conversation");
			}
			catch(Exception ignored)
			{}
			return false;
		}

		/**
		 * Removes all temp data related to prayer for the <code>player</code>.
		 * 
		 * @param player the player to clean.
		 */
		public static void clearPrayerSession(OfflinePlayer player)
		{
			DataManager.removeTemp(player.getName(), "prayer_conversation");
			DataManager.removeTemp(player.getName(), "prayer_context");
			DataManager.removeTemp(player.getName(), "prayer_location");
			DataManager.removeTimed(player.getName(), "currently_creating");
			DataManager.removeTimed(player.getName(), "currently_forsaking");
		}

		/**
		 * Returns the context for the <code>player</code>'s prayer converstion.
		 * 
		 * @param player the player whose context to return.
		 * @return ConversationContext
		 */
		public static ConversationContext getPrayerContext(Player player)
		{
			if(!isPraying(player)) return null;
			return (ConversationContext) DataManager.getValueTemp(player.getName(), "prayer_context");
		}

		/**
		 * Changes prayer status for <code>player</code> to <code>option</code> and tells them.
		 * 
		 * @param player the player the manipulate.
		 * @param option the boolean to set to.
		 */
		public static void togglePraying(Player player, boolean option)
		{
			if(option)
			{
				// Toggle on
				togglePrayingSilent(player, true, false);

				// Record chat
				startRecording(player);
			}
			else
			{
				// Toggle off
				togglePrayingSilent(player, false, false);

				// Message them
				Messages.clearChat(player);
				for(String message : Demigods.LANGUAGE.getTextBlock(Translation.Text.PRAYER_ENDED))
					player.sendMessage(message);

				// Handle recorded chat
				stopRecording(player, true);
			}
		}

		/**
		 * Changes prayer status for <code>player</code> to <code>option</code> silently.
		 * 
		 * @param player the player the manipulate.
		 * @param option the boolean to set to.
		 * @param recordChat whether or not the chat should be recorded.
		 */
		public static void togglePrayingSilent(Player player, boolean option, boolean recordChat)
		{
			if(option)
			{
				// Create the conversation and save it
				Conversation prayer = Prayer.startPrayer(player);
				DataManager.saveTemp(player.getName(), "prayer_conversation", prayer);
				DataManager.saveTemp(player.getName(), "prayer_location", player.getLocation());
				player.setSneaking(true);

				// Record chat if enabled
				if(recordChat) startRecording(player);
			}
			else
			{
				// Save context and abandon the conversation
				if(DataManager.hasKeyTemp(player.getName(), "prayer_conversation"))
				{
					Conversation prayer = (Conversation) DataManager.getValueTemp(player.getName(), "prayer_conversation");
					DataManager.saveTemp(player.getName(), "prayer_context", prayer.getContext());
					prayer.abandon();
				}

				// Remove the data
				DataManager.removeTemp(player.getName(), "prayer_conversation");
				DataManager.removeTemp(player.getName(), "prayer_location");
				player.setSneaking(false);

				// Handle recorded chat
				stopRecording(player, recordChat);
			}
		}

		/**
		 * Starts recording recording the <code>player</code>'s chat.
		 * 
		 * @param player the player to stop recording for.
		 */
		public static void startRecording(Player player)
		{
			chatRecording = ChatRecorder.Util.startRecording(player);
		}

		/**
		 * Stops recording and sends all messages that have been recorded thus far to the player.
		 * 
		 * @param player the player to stop recording for.
		 * @param display if true, the chat will be sent to the player
		 */
		public static List<String> stopRecording(Player player, boolean display)
		{
			// Handle recorded chat
			if(chatRecording != null && chatRecording.isRecording())
			{
				// Send held back chat
				List<String> messages = chatRecording.stop();
				if(messages.size() > 0 && display)
				{
					player.sendMessage(" ");
					player.sendMessage(new ColoredStringBuilder().italic().gray(Demigods.LANGUAGE.getText(Translation.Text.HELD_BACK_CHAT).replace("{size}", "" + messages.size())).build());
					for(String message : messages)
						player.sendMessage(message);
				}

				return messages;
			}
			return null;
		}
	}
}
