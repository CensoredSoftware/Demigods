package com.legit2.Demigods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.legit2.Demigods.Database.DDatabase;
import com.legit2.Demigods.Libraries.ReflectCommand;
import com.legit2.Demigods.Utilities.*;

public class DCommandExecutor implements CommandExecutor
{
	static Demigods plugin;
	
	public DCommandExecutor(Demigods instance)
	{
		plugin = instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (command.getName().equalsIgnoreCase("dg")) return dg(sender,args);
		else if (command.getName().equalsIgnoreCase("check")) return check(sender);
		else if (command.getName().equalsIgnoreCase("createchar")) return createChar(sender,args);
		else if (command.getName().equalsIgnoreCase("switchchar")) return switchChar(sender,args);
		else if (command.getName().equalsIgnoreCase("removechar")) return removeChar(sender,args);
		else if (command.getName().equalsIgnoreCase("viewmaps")) return viewMaps(sender);
		//else if (command.getName().equalsIgnoreCase("removeplayer")) return removePlayer(sender,args);
		
		// BETA TESTING ONLY
		else if (command.getName().equalsIgnoreCase("test1")) return test1(sender);

		return false;
	}
	
	/*
	 *  Command: "test1"
	 */
	public static boolean test1(CommandSender sender)
	{
		DMiscUtil.serverMsg("Test!");
		
		Player player = (Player) DPlayerUtil.definePlayer(sender.getName());
		DDivineBlocks.createAltar(player.getLocation());
		return true;
	}

	/*
	 *  Command: "dg"
	 */
	@ReflectCommand.Command(name = "dg", sender = ReflectCommand.Sender.PLAYER, permission = "demigods.basic")
	public static boolean dg(CommandSender sender, String[] args)
	{		
		if(args.length > 0)
		{
			dg_extended(sender, args);
			return true;
		}
				
		// Define Player
		Player player = (Player) DPlayerUtil.definePlayer(sender.getName());
		
		// Check Permissions
		if(!DMiscUtil.hasPermissionOrOP(player, "demigods.basic")) return DMiscUtil.noPermission(player);
		
		DMiscUtil.taggedMessage(sender, "Information Directory");
		for(String alliance : DDeityUtil.getLoadedDeityAlliances()) sender.sendMessage(ChatColor.GRAY + "/dg " + alliance.toLowerCase());
		sender.sendMessage(ChatColor.GRAY + "/dg claim");
		sender.sendMessage(ChatColor.GRAY + "/dg shrine");
		sender.sendMessage(ChatColor.GRAY + "/dg tribute");
		sender.sendMessage(ChatColor.GRAY + "/dg player");
		sender.sendMessage(ChatColor.GRAY + "/dg pvp");
		sender.sendMessage(ChatColor.GRAY + "/dg rankings");
		if(DMiscUtil.hasPermissionOrOP(player, "demigods.admin")) sender.sendMessage(ChatColor.RED + "/dg admin");
		sender.sendMessage(ChatColor.WHITE + "Use " + ChatColor.YELLOW + "/check" + ChatColor.WHITE + " to see your player information.");
		return true;
	}

	/*
	 *  Command: "dg_extended"
	 */
	@SuppressWarnings("unchecked")
	public static boolean dg_extended(CommandSender sender, String[] args)
	{
		// Define Player
		Player player = (Player) DPlayerUtil.definePlayer(sender.getName());
		
		// Define args
		String category = args[0];
		String option1 = null;
		if(args.length == 2) option1 = args[1];
		
		// Check Permissions
		if(!DMiscUtil.hasPermissionOrOP(player, "demigods.basic")) return DMiscUtil.noPermission(player);
		
		for(String alliance : DDeityUtil.getLoadedDeityAlliances())
		{
			if(category.equalsIgnoreCase(alliance))
			{
				if(args.length < 2)
				{
					DMiscUtil.taggedMessage(sender, alliance + " Directory");
					for(String deity : DDeityUtil.getAllDeitiesInAlliance(alliance)) sender.sendMessage(ChatColor.GRAY + "/dg " + alliance.toLowerCase() + " " + deity.toLowerCase());	
				}
				else
				{
					for(String deity : DDeityUtil.getAllDeitiesInAlliance(alliance))
					{
						if(args[1].equalsIgnoreCase(deity))
						{
							try
							{
								for(String toPrint : (ArrayList<String>) DDeityUtil.invokeDeityMethodWithPlayer(DDeityUtil.getDeityClass(deity), "getInfo", player)) sender.sendMessage(toPrint);
								return true;
							}
							catch (Exception e)
							{
								sender.sendMessage(ChatColor.RED + "Error code: 3001.  Please report this immediatly.");
								e.printStackTrace(); //DEBUG
								return true;
							}
						}
					}
					sender.sendMessage(ChatColor.DARK_RED + "No such deity, please try again.");
					return false;
				}
			}
		}
		
	
		if(category.equalsIgnoreCase("save"))
		{
			if(DMiscUtil.hasPermissionOrOP(player, "demigods.admin"))
			{
				DMiscUtil.serverMsg(ChatColor.RED + "Manually forcing Demigods save...");
				if(DDatabase.saveAllData())
				{
					DMiscUtil.serverMsg(ChatColor.GREEN + "Save complete!");
				}
				else
				{
					DMiscUtil.serverMsg(ChatColor.RED + "There was a problem with saving...");
					DMiscUtil.serverMsg(ChatColor.RED + "An admin should check the log immediately.");
				}
			}
			else DMiscUtil.noPermission(player);
		}
		else if(category.equalsIgnoreCase("claim"))
		{
			DMiscUtil.taggedMessage(sender, "Claiming");
			sender.sendMessage(ChatColor.GRAY + " This is some info about Claiming.");
		}
		else if(category.equalsIgnoreCase("shrine"))
		{
			DMiscUtil.taggedMessage(sender, "Shrines");
			sender.sendMessage(ChatColor.GRAY + " This is some info about Shrines.");
		}
		else if(category.equalsIgnoreCase("tribute"))
		{
			DMiscUtil.taggedMessage(sender, "Tributes");
			sender.sendMessage(ChatColor.GRAY + " This is some info about Tributes.");
		}
		else if(category.equalsIgnoreCase("player"))
		{
			DMiscUtil.taggedMessage(sender, "Players");
			sender.sendMessage(ChatColor.GRAY + " This is some info about Players.");
		}
		else if(category.equalsIgnoreCase("pvp"))
		{
			DMiscUtil.taggedMessage(sender, "PVP");
			sender.sendMessage(ChatColor.GRAY + " This is some info about PVP.");
		}
		else if(category.equalsIgnoreCase("stats"))
		{
			DMiscUtil.taggedMessage(sender, "Stats");
			sender.sendMessage(ChatColor.GRAY + " Read some global stats made from Demigods.");
		}
		else if(category.equalsIgnoreCase("rankings"))
		{
			DMiscUtil.taggedMessage(sender, "Rankings");
			sender.sendMessage(ChatColor.GRAY + " This is some ranking info about Demigods.");
		}
		else if(category.equalsIgnoreCase("admin"))
		{
			if(option1 != null)
			{
				if(!DDataUtil.hasPlayerData(player, "temp_admin_wand") || DDataUtil.getPlayerData(player, "temp_admin_wand").equals(false))
				{
					DDataUtil.savePlayerData(player, "temp_admin_wand", true);
					player.sendMessage(ChatColor.RED + "Your admin wand has been enabled for " + Material.getMaterial(DConfigUtil.getSettingInt("admin_wand_tool")));
				}
				else if(DDataUtil.hasPlayerData(player, "temp_admin_wand") && DDataUtil.getPlayerData(player, "temp_admin_wand").equals(true))
				{
					DDataUtil.savePlayerData(player, "temp_admin_wand", false);
					player.sendMessage(ChatColor.RED + "You have disabled your admin wand.");
				}
				return true;
			}
			
			DMiscUtil.taggedMessage(sender, ChatColor.RED + "Admin Commands");
			sender.sendMessage(ChatColor.GRAY + " /dg admin wand");
			sender.sendMessage(ChatColor.GRAY + " /setalliance <player> <alliance>");
			sender.sendMessage(ChatColor.GRAY + " /givedeity <player> <deity>");
			sender.sendMessage(ChatColor.GRAY + " /setdevotion <player> <deity> <amount>");
			sender.sendMessage(ChatColor.GRAY + " /setfavor <player> <amount>");
			sender.sendMessage(ChatColor.GRAY + " /setascensions <player> <amount>");
		}
		
		return true;
	}
	
	/*
	 *  Command: "check"
	 */
	public static boolean check(CommandSender sender)
	{
		// Define Player and Username
		Player player = (Player) DPlayerUtil.definePlayer(sender.getName());
		//String username = player.getName();
		
		if(!DCharUtil.isImmortal(player))
		{
			player.sendMessage(ChatColor.RED + "You cannot use that command, mortal.");
			return true;
		}		
			
		// Send the user their info via chat
		DMiscUtil.customTaggedMessage(sender, "Demigods Player Check", null);
		
		/*
		sender.sendMessage(ChatColor.RESET + "Name: " + ChatColor.AQUA + username + ChatColor.RESET + " of the " + ChatColor.ITALIC + DObjUtil.capitalize(alliance) + "s");
		sender.sendMessage("Favor: " + ChatColor.GREEN + favor);
		sender.sendMessage("Ascensions: " + ChatColor.GREEN + ascensions);
		sender.sendMessage(" ");
		
		sender.sendMessage("Deities: ");
		
			// List each deity separately
			for(Object deity : deity_list)
			{
				sender.sendMessage("  " + deity);
			}
			
		sender.sendMessage(" ");
		sender.sendMessage("Kills: " + ChatColor.GREEN + kills + ChatColor.WHITE + " / Deaths: " + ChatColor.RED + deaths);
		*/
		
		return true;
	}
	
	/*
	 *  Command: "viewMaps"
	 */
	public static boolean viewMaps(CommandSender sender)
	{
		sender.sendMessage("-- Players ------------------");
		sender.sendMessage(" ");

		for(Entry<String, HashMap<String, Object>> player : DDataUtil.getAllPlayers().entrySet())
		{

			String playerName = player.getKey();
			HashMap<String, Object> playerData = player.getValue();
			
			sender.sendMessage(playerName + ": ");

			for(Entry<String, Object> playerDataEntry : playerData.entrySet())
			{
				sender.sendMessage("  - " + playerDataEntry.getKey() + ": " + playerDataEntry.getValue());
			}
		}
		
		sender.sendMessage(" ");
		sender.sendMessage("-- Characters ---------------");
		sender.sendMessage(" ");

		for(Entry<Integer, HashMap<String, Object>> character : DDataUtil.getAllPlayerChars((Player) sender).entrySet())
		{
			int charID = character.getKey();
			HashMap<String, Object> charData = character.getValue();
			
			sender.sendMessage(charID + ": ");

			for(Entry<String, Object> charDataEntry : charData.entrySet())
			{
				sender.sendMessage("  - " + charDataEntry.getKey() + ": " + charDataEntry.getValue());
			}
		}
		return true;
	}

	/*
	 *  Command: "createChar"
	 */
	public static boolean createChar(CommandSender sender, String[] args)
	{
		if(args.length != 2) return false;
		
		// Define args
		Player player = (Player) DPlayerUtil.definePlayer(sender.getName());
		String charName = args[0];
		String charDeity = args[1];
		
		if(charName.length() >= 15)
		{
			sender.sendMessage(ChatColor.YELLOW + "Too long of a name, please try again.");
			return false;
		}
		
		if(DCharUtil.createChar(player, charName, charDeity)) sender.sendMessage(ChatColor.YELLOW + "Character " + charName + "(" + charDeity + ") created!");
		else player.sendMessage(ChatColor.RED + "You already have a character with that name.");

		return true;
	}
	
	/*
	 *  Command: "switchChar"
	 */
	public static boolean switchChar(CommandSender sender, String[] args)
	{
		if(args.length != 1) return false;
		
		// Define args
		Player player = (Player) DPlayerUtil.definePlayer(sender.getName());
		String charName = args[0];
		
		if(DPlayerUtil.hasCharName(player, charName))
		{
			int charID = DCharUtil.getID(charName);
			DDataUtil.savePlayerData(player, "current_char", charID);
			sender.sendMessage(ChatColor.YELLOW + "Your current character has been changed!");
		}
		else sender.sendMessage(ChatColor.RED + "There was an error while changing your current character.");
		
		return true;
	}
	
	/*
	 *  Command: "removeChar"
	 */
	public static boolean removeChar(CommandSender sender, String[] args)
	{
		if(args.length != 1) return false;
		
		// Define args
		Player player = (Player) DPlayerUtil.definePlayer(sender.getName());
		String charName = args[0];
		
		if(DPlayerUtil.hasCharName(player, charName))
		{
			int charID = DCharUtil.getID(charName);
			DDataUtil.removeChar(charID);
			
			sender.sendMessage(ChatColor.RED + "Character removed!");
		}
		else sender.sendMessage(ChatColor.RED + "There was an error while removing your character.");
		
		
		return true;
	}
}
