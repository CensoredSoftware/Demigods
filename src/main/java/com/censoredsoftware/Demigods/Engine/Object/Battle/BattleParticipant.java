package com.censoredsoftware.Demigods.Engine.Object.Battle;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import com.censoredsoftware.Demigods.Engine.Object.Deity.Deity;
import com.censoredsoftware.Demigods.Engine.Object.Player.PlayerCharacter;

public interface BattleParticipant
{
	public Deity getDeity();

	public Long getId();

	public Location getCurrentLocation();

	public PlayerCharacter getRelatedCharacter();

	public LivingEntity getEntity();
}