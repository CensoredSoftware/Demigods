package com.censoredsoftware.demigods.engine;

import com.censoredsoftware.censoredlib.CensoredLibPlugin;
import com.censoredsoftware.censoredlib.helper.CensoredJavaPlugin;
import com.censoredsoftware.demigods.engine.data.DataManager;
import com.censoredsoftware.demigods.engine.data.ThreadManager;
import com.censoredsoftware.demigods.engine.player.DCharacter;
import com.censoredsoftware.demigods.engine.player.DPlayer;
import com.censoredsoftware.demigods.engine.util.Messages;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Class for all plugins of demigods.
 */
public class DemigodsPlugin extends CensoredJavaPlugin {
    private static final String CENSORED_LIBRARY_VERSION = "1.0";
    static boolean READY = false;

    /**
     * The Bukkit enable method.
     */
    @Override
    public void onEnable() {
        if (!checkForCensoredLib()) return;

        // handleDependentPlugins();

        // Load the game engine.
        Demigods.load();

        // Print success!
        Messages.info("Successfully enabled.");
    }

    /**
     * The Bukkit disable method.
     */
    @Override
    public void onDisable() {

        if (READY) {
            // Save all the data.
            DataManager.save();

            // Handle online characters
            for (DCharacter character : DCharacter.Util.getOnlineCharacters()) {
                // Toggle prayer off and clear the session
                DPlayer.Util.togglePrayingSilent(character.getOfflinePlayer().getPlayer(), false, false);
                DPlayer.Util.clearPrayerSession(character.getOfflinePlayer().getPlayer());
            }
        }

        // Cancel all threads, event calls, and unregister permissions.
        try {
            ThreadManager.stopThreads();
            HandlerList.unregisterAll(this);
            Demigods.unloadPermissions();
        } catch (Throwable ignored) {
        }

        Messages.info("Successfully disabled.");
    }

    private boolean checkForCensoredLib() {
        // Check for CensoredLib
        Plugin check = Bukkit.getPluginManager().getPlugin("CensoredLib");
        if (check instanceof CensoredLibPlugin && check.getDescription().getVersion().startsWith(CENSORED_LIBRARY_VERSION))
            return true;
        // TODO Auto-download/update.
        getLogger().severe("Demigods cannot load without CensoredLib installed.");
        return false;
    }

    private void handleDependentPlugins() {
        // Unload all incorrectly installed plugins
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            // Not soft-depend
            List<String> depends = plugin.getDescription().getDepend();
            if (depends != null && !depends.isEmpty() && depends.contains("Demigods")) {
                getLogger().warning(plugin.getName() + " v" + plugin.getDescription().getVersion() + " was installed in the wrong directory.");
                getLogger().warning("Please put all plugins that depend on Demigods in the correct folder.");
                getLogger().warning("Like this: " + getDataFolder().getPath() + "/plugins/" + plugin.getName() + ".jar");
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }

        // Load Demigods plugins
        File pluginsFolder = new File(getDataFolder() + "/plugins/");
        if (!pluginsFolder.exists()) pluginsFolder.mkdirs();

        Set<File> files = Sets.filter(Sets.newHashSet(pluginsFolder.listFiles()), new Predicate<File>() {
            @Override
            public boolean apply(File file) {
                try {
                    return new JarFile(file).getJarEntry("plugin.yml") != null;
                } catch (Throwable ignored) {
                }
                return false;
            }
        });
        File[] plugins = new File[files.size()];
        for (File file : plugins) {
            try {
                Bukkit.getServer().getPluginManager().loadPlugin(file);
            } catch (Throwable errored) {
                errored.printStackTrace();
            }
        }

        getPluginLoader().disablePlugin(this);
    }
}
