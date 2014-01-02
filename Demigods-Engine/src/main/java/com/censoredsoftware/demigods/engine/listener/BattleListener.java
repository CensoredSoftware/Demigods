package com.censoredsoftware.demigods.engine.listener;

import com.censoredsoftware.demigods.engine.data.*;
import com.censoredsoftware.demigods.engine.util.Configs;
import com.censoredsoftware.demigods.engine.util.Zones;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTeleportEvent;

public class BattleListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public static void onDamageBy(EntityDamageByEntityEvent event)
	{
		if(event.isCancelled()) return;
		if(Zones.inNoDemigodsZone(event.getEntity().getLocation())) return;
		Entity damager = event.getDamager();
		if(damager instanceof Projectile) damager = ((Projectile) damager).getShooter();
		if(!Battle.Util.canParticipate(event.getEntity()))
		{
			if(Zones.inNoPvpZone(damager.getLocation()) || Zones.inNoPvpZone(damager.getLocation())) event.setCancelled(true);
			return;
		}

		Participant damageeParticipant = Battle.Util.defineParticipant(event.getEntity());

		if(!Configs.getSettingBoolean("battles.enabled"))
		{
			if(!Battle.Util.canParticipate(damager) && ((LivingEntity) event.getEntity()).getHealth() <= event.getDamage())
			{
				Participant damagerParticipant = Battle.Util.defineParticipant(damager);

				damageeParticipant.getRelatedCharacter().addDeath(damagerParticipant.getRelatedCharacter());
				damagerParticipant.getRelatedCharacter().addKill();
			}
			return;
		}

		// Early battle death (prevents being killed by mobs)
		if(!Battle.Util.canParticipate(damager) && Battle.Util.isInBattle(damageeParticipant) && event.getDamage() >= ((LivingEntity) event.getEntity()).getHealth())
		{
			event.setCancelled(true);
			Battle.Util.battleDeath(damageeParticipant, Battle.Util.getBattle(damageeParticipant));
			return;
		}
		else if(!Battle.Util.canParticipate(damager)) return;

		Participant damagerParticipant = Battle.Util.defineParticipant(damager);

		// Various things that should cancel the event
		if(damageeParticipant.equals(damagerParticipant) || DCharacter.Util.areAllied(damageeParticipant.getRelatedCharacter(), damagerParticipant.getRelatedCharacter()) || !damageeParticipant.getRelatedCharacter().canPvp() || !damagerParticipant.getRelatedCharacter().canPvp())
		{
			event.setCancelled(true);
			return;
		}
		if(damageeParticipant instanceof DCharacter && Data.hasTimed(damageeParticipant.getId().toString(), "just_finished_battle"))
		{
			((Player) damager).sendMessage(ChatColor.YELLOW + "That player is in cooldown from a recent battle.");
			event.setCancelled(true);
			return;
		}
		if(damagerParticipant instanceof DCharacter && Data.hasTimed(damagerParticipant.getId().toString(), "just_finished_battle"))
		{
			((Player) damager).sendMessage(ChatColor.YELLOW + "You are still in cooldown from a recent battle.");
			event.setCancelled(true);
			return;
		}

		// Calculate midpoint location
		Location midpoint = damagerParticipant.getCurrentLocation().toVector().getMidpoint(event.getEntity().getLocation().toVector()).toLocation(damagerParticipant.getCurrentLocation().getWorld());

		if(Battle.Util.isInBattle(damageeParticipant) || Battle.Util.isInBattle(damagerParticipant))
		{
			// Add to existing battle
			Battle battle = Battle.Util.isInBattle(damageeParticipant) ? Battle.Util.getBattle(damageeParticipant) : Battle.Util.getBattle(damagerParticipant);

			// Add participants from this event
			battle.addParticipant(damageeParticipant);
			battle.addParticipant(damagerParticipant);

			// Battle death
			if(event.getDamage() >= ((LivingEntity) event.getEntity()).getHealth())
			{
				event.setCancelled(true);
				Battle.Util.battleDeath(damagerParticipant, damageeParticipant, battle);
			}
			return;
		}

		if(!Battle.Util.existsNear(midpoint) && !Battle.Util.existsInRadius(midpoint))
		{
			// Create new battle
			Battle battle = Battle.Util.create(damagerParticipant, damageeParticipant);

			// Battle death
			if(event.getDamage() >= ((LivingEntity) event.getEntity()).getHealth())
			{
				event.setCancelled(true);
				Battle.Util.battleDeath(damagerParticipant, damageeParticipant, battle);
			}

			battle.sendMessage(ChatColor.YELLOW + "You are now in battle!");
		}
		else
		{
			// Add to existing battle
			Battle battle = Battle.Util.getNear(midpoint) != null ? Battle.Util.getNear(midpoint) : Battle.Util.getInRadius(midpoint);

			// Add participants from this event
			battle.addParticipant(damageeParticipant);
			battle.addParticipant(damagerParticipant);

			// Battle death
			if(event.getDamage() >= ((LivingEntity) event.getEntity()).getHealth())
			{
				event.setCancelled(true);
				Battle.Util.battleDeath(damagerParticipant, damageeParticipant, battle);
			}
		}

		// Pets
		if(damager instanceof LivingEntity) for(DPet pet : damageeParticipant.getRelatedCharacter().getPets())
		{
			LivingEntity entity = pet.getEntity();
			if(entity != null && entity instanceof Monster) ((Monster) entity).setTarget((LivingEntity) damager);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onDamage(EntityDamageEvent event)
	{
		if(!Configs.getSettingBoolean("battles.enabled") || Zones.inNoDemigodsZone(event.getEntity().getLocation())) return;
		if(event instanceof EntityDamageByEntityEvent || !Battle.Util.canParticipate(event.getEntity())) return;

		Participant participant = Battle.Util.defineParticipant(event.getEntity());

		if(participant instanceof DCharacter && Data.hasTimed(participant.getId().toString(), "just_finished_battle"))
		{
			event.setCancelled(true);
			return;
		}

		// Battle death
		if(Battle.Util.isInBattle(participant) && event.getDamage() >= ((LivingEntity) event.getEntity()).getHealth())
		{
			event.setCancelled(true);
			Battle.Util.battleDeath(participant, Battle.Util.getBattle(participant));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBattleTeleport(EntityTeleportEvent event)
	{
		if(!Configs.getSettingBoolean("battles.enabled") || Zones.inNoDemigodsZone(event.getEntity().getLocation())) return;
		if(!Battle.Util.canParticipate(event.getEntity())) return;
		Participant participant = Battle.Util.defineParticipant(event.getEntity());
		if(Battle.Util.isInBattle(participant))
		{
			Battle battle = Battle.Util.getBattle(participant);
			if(!event.getTo().getWorld().equals(battle.getStartLocation().getWorld()) || CLocationManager.distanceFlat(event.getTo(), battle.getStartLocation()) > battle.getRadius()) event.setCancelled(true);
		}
	}
}
