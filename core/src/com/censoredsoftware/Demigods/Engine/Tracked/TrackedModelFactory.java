package com.censoredsoftware.Demigods.Engine.Tracked;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.censoredsoftware.Demigods.Engine.PlayerCharacter.PlayerCharacter;

public class TrackedModelFactory
{
	public static TrackedLocation createTrackedLocation(String world, double X, double Y, double Z, float yaw, float pitch)
	{
		TrackedLocation trackedLocation = new TrackedLocation();
		trackedLocation.setWorld(world);
		trackedLocation.setX(X);
		trackedLocation.setY(Y);
		trackedLocation.setZ(Z);
		trackedLocation.setYaw(yaw);
		trackedLocation.setPitch(pitch);
		TrackedLocation.save(trackedLocation);
		return trackedLocation;
	}

	public static TrackedLocation createTrackedLocation(Location location)
	{
		return createTrackedLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
	}

	public static TrackedPlayer createTrackedPlayer(OfflinePlayer player)
	{
		TrackedPlayer trackedPlayer = new TrackedPlayer();
		trackedPlayer.setPlayer(player.getName());
		trackedPlayer.setLastLoginTime(player.getLastPlayed());
		TrackedPlayer.save(trackedPlayer);
		return trackedPlayer;
	}

	public static TrackedPlayerInventory createTrackedPlayerInventory(PlayerInventory inventory)
	{
		TrackedPlayerInventory trackedInventory = new TrackedPlayerInventory();
		trackedInventory.setSize(inventory.getSize());
		if(inventory.getHelmet() != null) trackedInventory.setHelmet(inventory.getHelmet());
		if(inventory.getChestplate() != null) trackedInventory.setChestplate(inventory.getChestplate());
		if(inventory.getLeggings() != null) trackedInventory.setLeggings(inventory.getLeggings());
		if(inventory.getBoots() != null) trackedInventory.setBoots(inventory.getBoots());
		trackedInventory.setItems(inventory);
		TrackedPlayerInventory.save(trackedInventory);
		return trackedInventory;
	}

	public static TrackedItemStack createTrackedItemStack(ItemStack item)
	{
		TrackedItemStack trackedItem = new TrackedItemStack();
		trackedItem.setTypeId(item.getTypeId());
		trackedItem.setByteId(item.getData().getData());
		trackedItem.setAmount(item.getAmount());
		trackedItem.setDurability(item.getDurability());
		if(item.hasItemMeta())
		{
			if(item.getItemMeta().hasDisplayName()) trackedItem.setName(item.getItemMeta().getDisplayName());
			if(item.getItemMeta().hasLore()) trackedItem.setLore(item.getItemMeta().getLore());
		}
		trackedItem.setEnchantments(item);
		trackedItem.setBookMeta(item);
		TrackedItemStack.save(trackedItem);
		return trackedItem;
	}

	public static TrackedBlock createTrackedBlock(Location location, String type, Material material, byte matByte)
	{
		TrackedLocation trackedLocation = TrackedModelFactory.createTrackedLocation(location);

		TrackedBlock trackedBlock = new TrackedBlock();
		trackedBlock.setLocation(trackedLocation);
		trackedBlock.setType(type);
		trackedBlock.setMaterial(material);
		trackedBlock.setMaterialByte(matByte);
		trackedBlock.setPreviousMaterial(Material.getMaterial(location.getBlock().getTypeId()));
		trackedBlock.setPreviousMaterialByte(location.getBlock().getData());
		location.getBlock().setType(material);
		location.getBlock().setData(matByte);
		TrackedBlock.save(trackedBlock);
		return trackedBlock;
	}

	public static TrackedBlock createTrackedBlock(Location location, String type, Material material)
	{
		return createTrackedBlock(location, type, material, (byte) 0);
	}

	public static TrackedBattle createTrackedBattle(PlayerCharacter attacking, PlayerCharacter defending, final Long startTime)
	{
		TrackedBattle battle = new TrackedBattle();
		Location startedLocation = ((Player) attacking.getPlayer()).getLocation();
		battle.setWhoStarted(attacking);
		battle.setStartLocation(TrackedModelFactory.createTrackedLocation(startedLocation));
		battle.setStartTime(startTime);
		battle.addCharacter(attacking);
		battle.addCharacter(defending);
		battle.setActive(true);
		TrackedBattle.save(battle);
		return battle;
	}
}
