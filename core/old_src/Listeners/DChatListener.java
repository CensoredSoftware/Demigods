/*
	Copyright (c) 2013 The Demigods Team
	
	Demigods License v1
	
	This plugin is provided "as is" and without any warranty.  Any express or
	implied warranties, including, but not limited to, the implied warranties
	of merchantability and fitness for a particular purpose are disclaimed.
	In no event shall the authors be liable to any party for any direct,
	indirect, incidental, special, exemplary, or consequential damages arising
	in any way out of the use or misuse of this plugin.
	
	Definitions
	
	 1. This Plugin is defined as all of the files within any archive
	    file or any group of files released in conjunction by the Demigods Team,
	    the Demigods Team, or a derived or modified work based on such files.
	
	 2. A Modification, or a Mod, is defined as this Plugin or a derivative of
	    it with one or more Modification applied to it, or as any program that
	    depends on this Plugin.
	
	 3. Distribution is defined as allowing one or more other people to in
	    any way download or receive a copy of this Plugin, a Modified
	    Plugin, or a derivative of this Plugin.
	
	 4. The Software is defined as an installed copy of this Plugin, a
	    Modified Plugin, or a derivative of this Plugin.
	
	 5. The Demigods Team is defined as Alex Bennett and Alexander Chauncey
	    of http://www.censoredsoftware.com/.
	
	Agreement
	
	 1. Permission is hereby granted to use, copy, modify and/or
	    distribute this Plugin, provided that:
	
	    a. All copyright notices within source files and as generated by
	       the Software as output are retained, unchanged.
	
	    b. Any Distribution of this Plugin, whether as a Modified Plugin
	       or not, includes this license and is released under the terms
	       of this Agreement. This clause is not dependant upon any
	       measure of changes made to this Plugin.
	
	    c. This Plugin, Modified Plugins, and derivative works may not
	       be sold or released under any paid license without explicit 
	       permission from the Demigods Team. Copying fees for the 
	       transport of this Plugin, support fees for installation or
	       other services, and hosting fees for hosting the Software may,
	       however, be imposed.
	
	    d. Any Distribution of this Plugin, whether as a Modified
	       Plugin or not, requires express written consent from the
	       Demigods Team.
	
	 2. You may make Modifications to this Plugin or a derivative of it,
	    and distribute your Modifications in a form that is separate from
	    the Plugin. The following restrictions apply to this type of
	    Modification:
	
	    a. A Modification must not alter or remove any copyright notices
	       in the Software or Plugin, generated or otherwise.
	
	    b. When a Modification to the Plugin is released, a
	       non-exclusive royalty-free right is granted to the Demigods Team
	       to distribute the Modification in future versions of the
	       Plugin provided such versions remain available under the
	       terms of this Agreement in addition to any other license(s) of
	       the initial developer.
	
	    c. Any Distribution of a Modified Plugin or derivative requires
	       express written consent from the Demigods Team.
	
	 3. Permission is hereby also granted to distribute programs which
	    depend on this Plugin, provided that you do not distribute any
	    Modified Plugin without express written consent.
	
	 4. The Demigods Team reserves the right to change the terms of this
	    Agreement at any time, although those changes are not retroactive
	    to past releases, unless redefining the Demigods Team. Failure to
	    receive notification of a change does not make those changes invalid.
	    A current copy of this Agreement can be found included with the Plugin.
	
	 5. This Agreement will terminate automatically if you fail to comply
	    with the limitations described herein. Upon termination, you must
	    destroy all copies of this Plugin, the Software, and any
	    derivatives within 48 hours.
 */

package com.censoredsoftware.Demigods.Listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.censoredsoftware.Demigods.Demigods;
import com.censoredsoftware.Demigods.Objects.Character.PlayerCharacter;

public class DChatListener implements Listener
{
	public static final Demigods API = Demigods.INSTANCE;

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChatCommand(AsyncPlayerChatEvent event)
	{
		// Define variables
		Player player = event.getPlayer();
		Set<Player> viewing = event.getRecipients();
		String message = event.getMessage();

		if(message.equals("pl")) pl(player, event);

		// No chat toggle
		if(API.data.hasPlayerData(player, "temp_no_chat")) event.setCancelled(true);
		for(Player victim : API.player.getOnlinePlayers())
		{
			if(API.data.hasPlayerData(victim, "temp_no_chat")) viewing.remove(victim);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChatMessage(AsyncPlayerChatEvent event)
	{
		// Define variables
		Player player = event.getPlayer();
		String message = event.getMessage();

		// Handle chat for character switching
		if(API.data.hasPlayerData(player, "temp_chat_number"))
		{
			// Define variables
			PlayerCharacter prevChar = API.character.getChar(API.object.toInteger(API.data.getPlayerData(player, "previous_char")));

			API.data.savePlayerData(player, "temp_chat_number", API.object.toInteger(API.data.getPlayerData(player, "temp_chat_number")) + 1);

			if(API.object.toInteger(API.data.getPlayerData(player, "temp_chat_number")) <= 2)
			{
				event.setMessage(ChatColor.GRAY + "(Previously " + API.deity.getDeityColor(prevChar.getDeity()) + prevChar.getName() + ChatColor.GRAY + ") " + ChatColor.WHITE + message);
			}
			else API.data.removePlayerData(player, "temp_chat_number");
		}
	}

	private void pl(Player player, AsyncPlayerChatEvent event)
	{
		HashMap<String, ArrayList<String>> alliances = new HashMap<String, ArrayList<String>>();

		for(Player onlinePlayer : API.getServer().getOnlinePlayers())
		{
			String alliance = API.player.getCurrentAlliance(player);

			if(!alliances.containsKey(alliance.toUpperCase())) alliances.put(alliance.toUpperCase(), new ArrayList<String>());
			alliances.get(alliance.toUpperCase()).add(onlinePlayer.getName());
		}

		for(String alliance : alliances.keySet())
		{
			String names = "";
			for(String name : alliances.get(alliance))
			{
				names += " " + name;
			}
			player.sendMessage(ChatColor.YELLOW + alliance + ": " + ChatColor.WHITE + names);
		}

		event.getRecipients().clear();
		event.setCancelled(true);
	}
}