package com.censoredsoftware.demigods.greek.ability.passive;

import java.util.List;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.censoredsoftware.demigods.engine.data.serializable.DCharacter;
import com.censoredsoftware.demigods.engine.util.Zones;
import com.censoredsoftware.demigods.greek.ability.GreekAbility;
import com.google.common.collect.Lists;

public class AlwaysInvisible extends GreekAbility.Passive
{
	private static final String NAME = "Invisible";
	private static final int REPEAT = 20;
	private static final List<String> DETAILS = Lists.newArrayList("They'll never see you coming.");

	public AlwaysInvisible(final String deity)
	{
		super(NAME, deity, REPEAT, DETAILS, null, new Runnable()
		{
			@Override
			public void run()
			{
				for(DCharacter character : DCharacter.Util.getOnlineCharactersWithAbility(NAME))
				{
					if(Zones.inNoDemigodsZone(character.getOfflinePlayer().getPlayer().getLocation())) continue;
					Player player = character.getOfflinePlayer().getPlayer();
					potionEffect(player);
					if(player.isInsideVehicle() && player.getVehicle().getType().equals(EntityType.HORSE)) potionEffect((LivingEntity) player.getVehicle());
				}
			}

			private void potionEffect(LivingEntity entity)
			{
				entity.removePotionEffect(PotionEffectType.INVISIBILITY);
				entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 120, 1));
			}
		});
	}
}
