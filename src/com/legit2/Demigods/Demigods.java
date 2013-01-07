package com.legit2.Demigods;

import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.legit2.Demigods.Libraries.ReflectCommand;
import com.legit2.Demigods.Listeners.DEntityListener;
import com.legit2.Demigods.Listeners.DPlayerListener;
import com.legit2.Demigods.Listeners.DDivineBlockListener;
import com.massivecraft.factions.P;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class Demigods extends JavaPlugin
{
	// Soft dependencies
	protected static WorldGuardPlugin WORLDGUARD = null;
	protected static P FACTIONS = null;
	public ReflectCommand commandRegistrator;
	
	// Did dependencies load correctly?
	boolean okayToLoad = true;
	
	@Override
	public void onEnable()
	{
		// Initialize Configuration
		new DUtil(this);
		
		loadDependencies();
		
		if(okayToLoad)
		{
			DDatabase.initializeDatabase();
			DConfig.initializeConfig();
			DScheduler.startThreads();
			loadListeners();
			loadCommands();
			loadDeities();
			loadMetrics();
			checkUpdate();
			
			
			//////// Test Code Loader
			// loadTestCode();
			//////// End Test Code Loader
			
			DUtil.info("Enabled!");
		}
		else
		{
			DUtil.severe("Demigods cannot enable correctly because at least one required dependency was not found.");
			getPluginLoader().disablePlugin(getServer().getPluginManager().getPlugin("Demigods"));
		}		
	}

	@Override
	public void onDisable()
	{
		if(okayToLoad)
		{
			// Uninitialize Plugin
			DDatabase.uninitializeDatabase();
			DScheduler.stopThreads();
						
			DUtil.info("Disabled!");
		}		
	}
	
	/*
	 *  loadTestCode() : Loads the code upon plugin enable.
	 */
	@SuppressWarnings("unused")
	private void loadTestCode()
	{
		ArrayList<ItemStack> allSouls = DSouls.returnAllSouls();
		int numberOfSouls = 0;
		
		for(ItemStack soul : allSouls)
		{
			DUtil.severe("Soul: " + soul.getType().name());
			numberOfSouls++;
		}
		DUtil.severe("Total Souls: " + numberOfSouls);
	}
	
	/*
	 *  loadCommands() : Loads all plugin commands and sets their executors.
	 */
	private void loadCommands()
	{
		// Define Main CommandExecutor
		commandRegistrator = new ReflectCommand(this);
		commandRegistrator.register(DCommandExecutor.class);
	}
	
	/*
	 *  loadListeners() : Loads all plugin listeners.
	 */
	private void loadListeners()
	{		
		/* Player Listener */
		getServer().getPluginManager().registerEvents(new DPlayerListener(this), this);
		getServer().getPluginManager().registerEvents(new DDivineBlockListener(this), this);
		getServer().getPluginManager().registerEvents(new DEntityListener(this), this);
}
	
	/*
	 *  loadDeities() : Loads the deities.
	 */
	@SuppressWarnings("rawtypes")
	public void loadDeities()
	{
		DUtil.info("Loading deities...");
		ArrayList<String> deityList = new ArrayList<String>();
		
		// Find all deities
		CodeSource demigodsSrc = Demigods.class.getProtectionDomain().getCodeSource();
		if(demigodsSrc != null)
		{
			try
			{
				URL demigodsJar = demigodsSrc.getLocation();
				ZipInputStream demigodsZip = new ZipInputStream(demigodsJar.openStream());
				
				ZipEntry demigodsFile = null;
				
				// Define variables
				int deityCount = 0;
				long startTimer = System.currentTimeMillis();
				
				while((demigodsFile = demigodsZip.getNextEntry()) != null)
				{
					String deityName = demigodsFile.getName().replace("/", ".").replace(".class", "");
					if(deityName.contains("_deity"))
					{
						deityCount++;
						deityList.add(deityName);
					}
				}
				
				for(String deity : deityList)
				{
					// No Paramaters
					Class noparams[] = {};
					
					Object obj = Class.forName(deity, true, this.getClass().getClassLoader()).newInstance();
					 
					// Load Deity commands
					commandRegistrator.register(Class.forName(deity, true, this.getClass().getClassLoader()));
					 
					// Load everything else for the Deity (Listener, etc.)
					Method loadDeity = Class.forName(deity, true, this.getClass().getClassLoader()).getMethod("loadDeity", noparams);
					String deityMessage = (String)loadDeity.invoke(obj, (Object[])null);
					 
					// Display the success message
					DUtil.info(deityMessage);

				}
				// Stop the timer
				long stopTimer = System.currentTimeMillis();
				double totalTime = (double) (stopTimer - startTimer);

				DUtil.info(deityCount + " deities loaded in " + totalTime/1000 + " seconds.");
			}
			catch(Exception e)
			{
				DUtil.severe("There was a problem while loading deities!");
				e.printStackTrace();
			}
		}
	}
	
	/*
	 *  loadMetrics() : Loads the metrics.
	 */
	private void loadMetrics()
	{
		new DMetrics(this);
		DMetrics.allianceStatsPastWeek();
		DMetrics.allianceStatsAllTime();
	}
	
	/*
	 *  loadDependencies() : Loads all dependencies.
	 */
	public void loadDependencies()
	{
		// Check for the SQLibrary plugin (needed)
		Plugin pg = getServer().getPluginManager().getPlugin("SQLibrary");
		if (pg == null)
		{
			DUtil.severe("SQLibrary plugin (required) not found!");
			okayToLoad = false;
		}
		
		// Check for the WorldGuard plugin (optional)
		pg = getServer().getPluginManager().getPlugin("WorldGuard");
		if ((pg != null) && (pg instanceof WorldGuardPlugin))
		{
			WORLDGUARD = (WorldGuardPlugin)pg;
			if (!DConfig.getSettingBoolean("allow_skills_everywhere")) DUtil.info("WorldGuard detected. Skills are disabled in no-PvP zones.");
		}

		// Check for the Factions plugin (optional)
		pg = getServer().getPluginManager().getPlugin("Factions");
		if (pg != null)
		{
			FACTIONS = ((P)pg);
			if(!DConfig.getSettingBoolean("allow_skills_everywhere")) DUtil.info("Factions detected. Skills are disabled in peaceful zones.");
		}

		// Check to see if a player has the SimpleNotice client mod installed
		getServer().getMessenger().registerOutgoingPluginChannel(this, "SimpleNotice");
	}
	
	private void checkUpdate()
	{
		// Check for updates, and then update if need be		
		new DUpdate(this);
		Boolean shouldUpdate = DUpdate.shouldUpdate();
		if(shouldUpdate && DConfig.getSettingBoolean("auto_update"))
		{
			DUpdate.demigodsUpdate();
		}
	}
}