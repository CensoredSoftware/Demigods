package com.legit2.Demigods.Utilities;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.OfflinePlayer;

import com.legit2.Demigods.Database.DDatabase;

public class DDataUtil 
{
	// Define HashMaps
	private static HashMap<String, HashMap<String, Object>> pluginData = new HashMap<String, HashMap<String, Object>>();
	private static HashMap<String, HashMap<String, Object>> playerData = new HashMap<String, HashMap<String, Object>>();
	private static HashMap<Integer, HashMap<String, Object>> charData = new HashMap<Integer, HashMap<String, Object>>();
	private static HashMap<Integer, HashMap<String, Object>> blockData = new HashMap<Integer, HashMap<String, Object>>();

	/* ---------------------------------------------------
	 *  Begin Plugin Data Methods
	 * ---------------------------------------------------
	 * 
	 *  savePluginData() : Saves (String)dataID to pluginData HashMap.
	 */
	public static boolean savePluginData(String dataID, String dataKey, Object dataValue)
	{
		dataKey = dataKey.toLowerCase();
		
		if(pluginData.containsKey(dataID))
		{
			pluginData.get(dataID).put(dataKey, dataValue);
			return true;
		}
		else
		{
			pluginData.put(dataID, new HashMap<String, Object>());
			pluginData.get(dataID).put(dataKey, dataValue);
			return true;
		}
	}
	
	/*
	 *  removePluginData() : Removes (String)dataID from pluginData HashMap.
	 */
	public static boolean removePluginData(String dataID, String dataKey)
	{
		dataKey = dataKey.toLowerCase();
		
		if(pluginData.containsKey(dataID))
		{
			pluginData.get(dataID).remove(dataKey);
			return true;
		}
		else return false;
	}
	
	/*
	 *  hasPluginData() : Returns true/false according to if (String)dataKey exists for (String)dataID.
	 */
	public static boolean hasPluginData(String dataID, String dataKey)
	{
		dataKey = dataKey.toLowerCase();
		
		if(pluginData.containsKey(dataID))
		{
			if(pluginData.get(dataID).get(dataKey) != null) return true;
			else return false;
		}
		else return false;
	}
	
	/*
	 *  getPluginData() : Returns (Object)dataValue for (int)dataID's (String)dataKey.
	 */
	public static Object getPluginData(String dataID, String dataKey)
	{
		dataKey = dataKey.toLowerCase();
		
		if(pluginData.containsKey(dataID))
		{
			if(pluginData.get(dataID) != null) return pluginData.get(dataID).get(dataKey);
			else return null;
		}
		else return null;
	}
	
	/*
	 *  getAllPluginData() : Returns all pluginData.
	 */
	public static HashMap<String, HashMap<String, Object>> getAllPluginData()
	{
		return pluginData;
	}
	
	/* ---------------------------------------------------
	 *  Begin Block Data Methods
	 * ---------------------------------------------------
	 * 
	 *  saveBlockData() : Saves (int)blockID to blockData HashMap.
	 */
	public static boolean saveBlockData(int blockID, String dataKey, Object dataValue)
	{
		dataKey = dataKey.toLowerCase();
		
		if(blockData.containsKey(blockID))
		{
			blockData.get(blockID).put(dataKey, dataValue);
			return true;
		}
		else
		{
			blockData.put(blockID, new HashMap<String, Object>());
			blockData.get(blockID).put(dataKey, dataValue);
			return true;
		}
	}
	
	/*
	 *  removeBlockData() : Removes (int)blockID from pluginData HashMap.
	 */
	public static boolean removeBlockData(int blockID, String dataKey)
	{
		dataKey = dataKey.toLowerCase();
		
		if(blockData.containsKey(blockID))
		{
			blockData.get(blockID).remove(dataKey);
			return true;
		}
		else return false;
	}
	
	/*
	 *  removeAllBlockData() : Removes (int)blockID from pluginData HashMap.
	 */
	public static boolean removeAllBlockData(int blockID)
	{		
		blockData.remove(blockID);
		return true;
	}
	
	/*
	 *  hasBlockData() : Returns true/false according to if (String)dataKey exists for (int)blockID.
	 */
	public static boolean hasBlockData(int blockID, String dataKey)
	{
		dataKey = dataKey.toLowerCase();
		
		if(blockData.containsKey(blockID))
		{
			if(blockData.get(blockID).get(dataKey) != null) return true;
			else return false;
		}
		else return false;
	}
	
	/*
	 *  getPluginData() : Returns (Object)dataValue for (int)blockID's (String)dataKey.
	 */
	public static Object getBlockData(int blockID, String dataKey)
	{
		dataKey = dataKey.toLowerCase();
		
		if(blockData.containsKey(blockID))
		{
			if(blockData.get(blockID) != null) return blockData.get(blockID).get(dataKey);
			else return null;
		}
		else return null;
	}
	
	/*
	 *  getAllBlockData() : Returns all block data.
	 */
	public static HashMap<Integer, HashMap<String, Object>> getAllBlockData()
	{
		return blockData;
	}
	
	/* ---------------------------------------------------
	 * Begin Player Data Methods
	 * ---------------------------------------------------
	 * 
	 *  savePlayerData() : Saves (String)dataKey to (int)playerID HashMap.
	 */
	public static boolean savePlayerData(OfflinePlayer player, String dataKey, Object dataValue)
	{
		String playerName = player.getName();
		dataKey = dataKey.toLowerCase();
		
		if(playerData.containsKey(playerName))
		{
			playerData.get(playerName).put(dataKey, dataValue);
			return true;
		}
		else return false;
	}
	
	/*
	 *  removePlayerData() : Removes (String)dataKey from (int)playerID's HashMap.
	 */
	public static boolean removePlayerData(OfflinePlayer player, String dataKey)
	{
		String playerName = player.getName();
		dataKey = dataKey.toLowerCase();
		
		if(playerData.containsKey(playerName))
		{
			playerData.get(playerName).remove(dataKey);
			return true;
		}
		else return false;
	}
	
	/*
	 *  hasPlayerData() : Returns true/false according to if (String)dataKey exists for (int)playerID.
	 */
	public static boolean hasPlayerData(OfflinePlayer player, String dataKey)
	{
		String playerName = player.getName();
		dataKey = dataKey.toLowerCase();
		
		if(playerData.containsKey(playerName))
		{
			if(playerData.get(playerName).get(dataKey) != null) return true;
			else return false;
		}
		else return false;
	}
	
	/*
	 *  getPlayerData() : Returns (Object)dataValue for (int)playerID's (String)dataKey.
	 */
	public static Object getPlayerData(OfflinePlayer player, String dataKey)
	{
		String playerName = player.getName();
		dataKey = dataKey.toLowerCase();
		
		if(playerData.containsKey(playerName))
		{
			if(playerData.get(playerName).get(dataKey) != null) return playerData.get(playerName).get(dataKey);
			else return null;
		}
		else return null;
	}
	
	/* ---------------------------------------------------
	 * Begin Character Data Methods
	 * ---------------------------------------------------
	 * 
	 *  charExists() : Returns true/false depening on if the character exists.
	 */
	public static boolean charExists(String charName)
	{
		if(charData.containsKey(charName)) return true;
		else return false;
	}
	
	/*
	 *  charExistsByID() : Returns true/false depening on if the character exists.
	 */
	public static boolean charExistsByID(int charID)
	{		
		if(charData.containsKey(charID)) return true;
		else return false;
	}

	/*
	 *  addChar() : Saves the (int)charID to the charData HashMap.
	 */
	public static boolean addChar(int charID)
	{
		charData.put(charID, new HashMap<String, Object>());
		return true;
	}
	
	/*
	 *  removeChar() : Removes the (int)charID from the charData HashMap.
	 */
	public static boolean removeChar(int charID)
	{
		charData.remove(charID);
		DDatabase.removeChar(charID);
		return true;
	}
	
	/*
	 *  saveCharData() : Saves (String)dataKey to (int)charID HashMap.
	 */
	public static boolean saveCharData(int charID, String dataKey, Object dataValue)
	{
		dataKey = dataKey.toLowerCase();
		
		if(charData.containsKey(charID))
		{
			charData.get(charID).put(dataKey, dataValue);
			return true;
		}
		else return false;
	}
	
	/*
	 *  removeCharData() : Removes (String)dataKey from (int)charID's HashMap.
	 */
	public static boolean removeCharData(int charID, String dataKey)
	{
		dataKey = dataKey.toLowerCase();
		
		if(charData.containsKey(charID))
		{
			charData.get(charID).remove(dataKey);
			return true;
		}
		else return false;
	}
	
	/*
	 *  hashCharData() : Returns true/false according to if (String)dataKey exists for (int)charID.
	 */
	public static boolean hasCharData(int charID, String dataKey)
	{
		dataKey = dataKey.toLowerCase();
		
		if(charData.containsKey(charID))
		{
			if(charData.get(charID).get(dataKey) != null) return true;
			else return false;
		}
		else return false;
	}
	
	/*
	 *  getCharData() : Returns (Object)dataValue for (int)charID's (String)dataKey.
	 */
	public static Object getCharData(int charID, String dataKey)
	{
		dataKey = dataKey.toLowerCase();
		
		if(charData.containsKey(charID))
		{
			if(charData.get(charID).get(dataKey) != null) return charData.get(charID).get(dataKey);
			else return null;
		}
		else return null;
	}
	
	/* ---------------------------------------------------
	 * Begin Miscellaneous Data Methods
	 * ---------------------------------------------------
	 *
	 *  addPlayer() : Saves new (String)username to HashMap playerData.
	 */
	public static boolean addPlayer(OfflinePlayer player, int playerID)
	{
		String playerName = player.getName();

		// Returns false if the player already has the playerData.
		if(newPlayer(player))
		{
			// Creates new player HashMap save.
			playerData.put(playerName, new HashMap<String, Object>());
			return true;
		}
		else return false;
	}
	
	/*
	 *  newPlayer() : Checks to see if (String)username already has HashMap playerData.
	 */
	public static boolean newPlayer(OfflinePlayer player)
	{
		String playerName = player.getName();

		if(playerData.containsKey(playerName)) return false;
		else return true;
	}
	
	/*
	 *  removePlayer() : Removes the (OfflinePlayer)player from the playerData HashMap.
	 */
	public static boolean removePlayer(OfflinePlayer player)
	{
		String playerName = player.getName();;
		playerData.remove(playerName);
		charData.remove(playerName);
		DDatabase.removePlayer(player);
		return true;
	}
	
	/*
	 *  getAllPlayers() : Returns all players in the playerData HashMap.
	 */
	public static HashMap<String, HashMap<String, Object>> getAllPlayers()
	{
		return playerData;
	}
	
	/*
	 *  getAllPlayerData() : Returns all playerData for (Player)player.
	 */
	public static HashMap<String, Object> getAllPlayerData(OfflinePlayer player)
	{
		String playerName = player.getName();;
		return playerData.get(playerName);
	}
	
	/*
	 *  getAllChars() : Returns all players in the playerData HashMap.
	 */
	public static HashMap<Integer, HashMap<String, Object>> getAllChars()
	{
		return charData;
	}
	
	/*
	 *  getAllCharData() : Returns all charData for (int)charID.
	 */
	public static HashMap<String, Object> getAllCharData(int charID)
	{
		return charData.get(charID);
	}
	
	/*
	 *  getAllPlayerChars() : Returns all charData for (int)charID.
	 */
	public static HashMap<Integer, HashMap<String, Object>> getAllPlayerChars(OfflinePlayer player)
	{
		HashMap<Integer, HashMap<String, Object>> temp = new HashMap<Integer, HashMap<String, Object>>();
		int playerID = DPlayerUtil.getPlayerID(player);
		
		for(Entry<Integer, HashMap<String, Object>> characters : getAllChars().entrySet())
		{
			int charID = characters.getKey();
			
			if(characters.getValue().get("char_owner") != null && characters.getValue().get("char_owner").equals(playerID))
			{
				temp.put(charID, characters.getValue());
			}
		}
		return temp;
	}
	
}
