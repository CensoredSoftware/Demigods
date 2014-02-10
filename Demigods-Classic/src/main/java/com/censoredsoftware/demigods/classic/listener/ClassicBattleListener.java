package com.censoredsoftware.demigods.classic.listener;

import com.censoredsoftware.demigods.engine.battle.Battle;
import com.censoredsoftware.demigods.engine.battle.Participant;
import com.censoredsoftware.demigods.engine.entity.player.DemigodsCharacter;
import com.censoredsoftware.demigods.engine.util.Zones;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ClassicBattleListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onDamageBy(EntityDamageByEntityEvent event)
	{
		if(event.isCancelled()) return;
		if(Zones.inNoDemigodsZone(event.getEntity().getLocation())) return;
		Entity damager = event.getDamager();
		if(damager instanceof Projectile) damager = ((Projectile) damager).getShooter();
		if(!Battle.Util.canParticipate(event.getEntity())) return;

		Participant damageeParticipant = Battle.Util.defineParticipant(event.getEntity());
		if(!Battle.Util.canParticipate(damager)) return;
		Participant damagerParticipant = Battle.Util.defineParticipant(damager);

		if(damageeParticipant.equals(damagerParticipant) || DemigodsCharacter.Util.areAllied(damageeParticipant.getRelatedCharacter(), damagerParticipant.getRelatedCharacter()))
		{
			event.setCancelled(true);
			return;
		}

		// Battle death
		if(event.getDamage() >= ((LivingEntity) event.getEntity()).getHealth())
		{
			DemigodsCharacter died = damageeParticipant.getRelatedCharacter();
			DemigodsCharacter killed = damagerParticipant.getRelatedCharacter();
			// TODO Death message
		}
	}
}
