package com.censoredsoftware.Demigods.Engine.Object.Deity;

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public class DeityInfo
{
	private String name, alliance, permission;
	private ChatColor color;
	private Set<Material> claimItems;
	private List<String> lore;
	private Deity.Type type;

	public DeityInfo(String name, String alliance, String permission, ChatColor color, Set<Material> claimItems, List<String> lore, Deity.Type type)
	{
		this.name = name;
		this.color = color;
		this.alliance = alliance;
		this.permission = permission;
		this.claimItems = claimItems;
		this.lore = lore;
		this.type = type;
	}

	public String getName()
	{
		return name;
	}

	public String getAlliance()
	{
		return alliance;
	}

	public String getPermission()
	{
		return permission;
	}

	public ChatColor getColor()
	{
		return color;
	}

	public Set<Material> getClaimItems()
	{
		return claimItems;
	}

	public List<String> getLore()
	{
		return lore;
	}

	public Deity.Type getType()
	{
		return type;
	}
}
