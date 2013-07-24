package com.censoredsoftware.Demigods.Engine.Runnable;

import org.bukkit.Effect;
import org.bukkit.scheduler.BukkitRunnable;

import com.censoredsoftware.Demigods.Engine.Object.Battle.Battle;
import com.censoredsoftware.Demigods.Engine.Utility.SpigotUtility;

public class SpigotBattleRunnable extends BukkitRunnable
{
	@Override
	public void run()
	{
		for(Battle battle : Battle.getAllActive())
		{
			SpigotUtility.drawCircle(battle.getStartLocation(), Effect.MOBSPAWNER_FLAMES, battle.getRange(), 60);
		}
	}
}
