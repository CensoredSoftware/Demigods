package com.censoredsoftware.Demigods.Engine.Object;

import java.util.*;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import redis.clients.johm.*;

import com.censoredsoftware.Demigods.Engine.Demigods;
import com.censoredsoftware.Demigods.Engine.Utility.MiscUtility;
import com.censoredsoftware.Demigods.Engine.Utility.ZoneUtility;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;

public abstract class Structure
{
	public abstract String getStructureType();

	public abstract Schematic get(String name);

	public abstract int getRadius();

	public abstract Location getClickableBlock(Location reference);

	public abstract Listener getUniqueListener();

	public abstract List<Save> getAll();

	public abstract Set<Structure.Flag> getFlags();

	public abstract Save createNew(Location reference, boolean generate);

	public enum Flag
	{
		PROTECTED_BLOCKS, NO_GRIEFING, NO_PVP, PRAYER_LOCATION, TRIBUTE_LOCATION;
	}

	public static class BlockData
	{
		private Material material;
		private byte data;
		private int odds;

		/**
		 * Constructor for BlockData with only Material given.
		 * 
		 * @param material Material of the block.
		 */
		public BlockData(Material material)
		{
			this.material = material;
			this.data = 0;
			this.odds = 10;
		}

		/**
		 * Constructor for BlockData with only Material given and odds given.
		 * 
		 * @param material Material of the block.
		 * @param odds The odds of this object being generated.
		 */
		public BlockData(Material material, int odds)
		{
			if(odds == 0 || odds > 10) throw new IllegalArgumentException("Odds must be between 1 and 10.");
			this.material = material;
			this.data = 10;
			this.odds = odds;
		}

		/**
		 * Constructor for BlockData with only Material and byte data given.
		 * 
		 * @param material Material of the block.
		 * @param data Byte data of the block.
		 */
		public BlockData(Material material, byte data)
		{
			this.material = material;
			this.data = data;
			this.odds = 10;
		}

		/**
		 * Constructor for BlockData with Material, byte data, and odds given.
		 * 
		 * @param material Material of the block.
		 * @param data Byte data of the block.
		 * @param odds The odds of this object being generated.
		 */
		public BlockData(Material material, byte data, int odds)
		{
			if(odds == 0 || odds > 10) throw new IllegalArgumentException("Odds must be between 1 and 10.");
			this.material = material;
			this.data = data;
			this.odds = odds;
		}

		/**
		 * Get the Material of this object.
		 * 
		 * @return A Material.
		 */
		public Material getMaterial()
		{
			return this.material;
		}

		/**
		 * Get the byte data of this object.
		 * 
		 * @return Byte data.
		 */
		public byte getData()
		{
			return this.data;
		}

		/**
		 * Get the odds of this object generating.
		 * 
		 * @return Odds (as an integer, out of 5).
		 */
		public int getOdds()
		{
			return this.odds;
		}
	}

	public static class Cuboid
	{
		private int X, Y, Z, XX, YY, ZZ;
		private int eX, eY, eZ, eXX, eYY, eZZ;
		private boolean cuboid;
		private boolean exclude;
		private boolean excludeCuboid;
		private List<Structure.BlockData> blockData;

		/**
		 * Constructor for a Cuboid (non-cuboid).
		 * 
		 * @param X The relative X coordinate of the schematic from the reference location.
		 * @param Y The relative Y coordinate of the schematic from the reference location.
		 * @param Z The relative Z coordinate of the schematic from the reference location.
		 * @param blockData The BlockData objects of this schematic.
		 */
		public Cuboid(int X, int Y, int Z, List<Structure.BlockData> blockData)
		{
			if(blockData.size() == 0 || blockData.size() > 10) throw new IllegalArgumentException("Incorrect block data list size.");
			this.X = this.XX = X;
			this.Y = this.YY = Y;
			this.Z = this.ZZ = Z;
			this.cuboid = false;
			this.exclude = false;
			this.excludeCuboid = false;
			this.blockData = blockData;
		}

		/**
		 * Constructor for a Cuboid (cuboid).
		 * 
		 * @param X The relative X coordinate of the schematic from the reference location.
		 * @param Y The relative Y coordinate of the schematic from the reference location.
		 * @param Z The relative Z coordinate of the schematic from the reference location.
		 * @param XX The second relative X coordinate of the schematic from the reference location, creating a cuboid.
		 * @param YY The second relative Y coordinate of the schematic from the reference location, creating a cuboid.
		 * @param ZZ The second relative Z coordinate of the schematic from the reference location, creating a cuboid.
		 * @param blockData The BlockData objects of this schematic.
		 */
		public Cuboid(int X, int Y, int Z, int XX, int YY, int ZZ, List<Structure.BlockData> blockData)
		{
			if(blockData.size() == 0 || blockData.size() > 10) throw new IllegalArgumentException("Incorrect block data list size.");
			this.X = X;
			this.Y = Y;
			this.Z = Z;
			this.XX = XX;
			this.YY = YY;
			this.ZZ = ZZ;
			this.cuboid = true;
			this.exclude = false;
			this.excludeCuboid = false;
			this.blockData = blockData;
		}

		/**
		 * Excluding for a Cuboid (non-cuboid).
		 * 
		 * @param X The relative X coordinate of the schematic from the reference location.
		 * @param Y The relative Y coordinate of the schematic from the reference location.
		 * @param Z The relative Z coordinate of the schematic from the reference location.
		 * @return This schematic.
		 */
		public Cuboid exclude(int X, int Y, int Z)
		{
			this.eX = this.eXX = X;
			this.eY = this.eYY = Y;
			this.eZ = this.eZZ = Z;
			this.exclude = true;
			return this;
		}

		/**
		 * Excluding for a Cuboid (cuboid).
		 * 
		 * @param X The relative X coordinate of the schematic from the reference location.
		 * @param Y The relative Y coordinate of the schematic from the reference location.
		 * @param Z The relative Z coordinate of the schematic from the reference location.
		 * @param XX The second relative X coordinate of the schematic from the reference location, creating a cuboid.
		 * @param YY The second relative Y coordinate of the schematic from the reference location, creating a cuboid.
		 * @param ZZ The second relative Z coordinate of the schematic from the reference location, creating a cuboid.
		 * @return This schematic.
		 */
		public Cuboid exclude(int X, int Y, int Z, int XX, int YY, int ZZ)
		{
			this.eX = X;
			this.eY = Y;
			this.eZ = Z;
			this.eXX = XX;
			this.eYY = YY;
			this.eZZ = ZZ;
			this.exclude = true;
			this.excludeCuboid = true;
			return this;
		}

		/**
		 * Get the material of the object (a random material is chosen based on the configured odds).
		 * 
		 * TODO This method needs work, I'm not sure this is the more efficient way to do what we want.
		 * 
		 * @return A material.
		 */
		public Structure.BlockData getStructureBlockData()
		{
			if(blockData.size() == 1) return blockData.get(0);
			return new ArrayList<BlockData>(10)
			{
				{
					for(Structure.BlockData block : blockData)
						for(int i = 0; i < block.getOdds(); i++)
							add(block);
				}
			}.get(MiscUtility.generateIntRange(0, 9));
		}

		/**
		 * Get the block locations in this object.
		 * 
		 * @param reference The reference location.
		 * @return A set of locations.
		 */
		public Set<Location> getBlockLocations(final Location reference)
		{
			return new HashSet<Location>()
			{
				{
					if(cuboid) addAll(rangeLoop(reference, X, XX, Y, YY, Z, ZZ));
					else add(getLocation(reference, X, Y, Z));
					if(exclude)
					{
						if(excludeCuboid) removeAll(rangeLoop(reference, eX, eXX, eY, eYY, eZ, eZZ));
						else remove(getLocation(reference, eX, eY, eZ));
					}
				}
			};
		}

		/**
		 * Generate this schematic.
		 * 
		 * @param reference The reference Location.
		 */
		public void generate(Location reference)
		{
			for(Location location : getBlockLocations(reference))
			{
				Structure.BlockData data = getStructureBlockData();
				location.getBlock().setTypeIdAndData(data.getMaterial().getId(), data.getData(), false);
			}
		}

		/**
		 * Get a relative location, based on the <code>X</code>, <code>Y</code>, <code>Z</code> coordinates relative to the object's central location.
		 * 
		 * @param X Relative X coordinate.
		 * @param Y Relative Y coordinate.
		 * @param Z Relative Z coordinate.
		 * @return New relative location.
		 */
		public Location getLocation(Location reference, int X, int Y, int Z)
		{
			return reference.clone().add(X, Y, Z);
		}

		/**
		 * Get a cuboid selection as a HashSet.
		 * 
		 * @param reference The reference location.
		 * @param X The relative X coordinate.
		 * @param XX The second relative X coordinate.
		 * @param Y The relative Y coordinate.
		 * @param YY The second relative Y coordinate.
		 * @param Z The relative Z coordinate.
		 * @param ZZ The second relative Z coordinate.
		 * @return The HashSet collection of a cuboid selection.
		 */
		public Set<Location> rangeLoop(final Location reference, final int X, final int XX, final int Y, final int YY, final int Z, final int ZZ)
		{
			return new HashSet<Location>()
			{
				{
					for(int x : Ranges.closed(X < XX ? X : XX, X < XX ? XX : X).asSet(DiscreteDomains.integers()))
						for(int y : Ranges.closed(Y < YY ? Y : YY, Y < YY ? YY : Y).asSet(DiscreteDomains.integers()))
							for(int z : Ranges.closed(Z < ZZ ? Z : ZZ, Z < ZZ ? ZZ : Z).asSet(DiscreteDomains.integers()))
								add(getLocation(reference, x, y, z));
				}
			};
		}
	}

	@Model
	public static class Save
	{
		@Id
		private Long id;
		@Indexed
		@Attribute
		private String type;
		@Indexed
		@Attribute
		private String design;
		@Indexed
		@Attribute
		private Boolean active;
		@Reference
		private DLocation reference;
		@Indexed
		@Reference
		private DPlayer.Character owner;
		@Indexed
		@CollectionSet(of = String.class)
		private Set<String> flags;
		@Indexed
		@Attribute
		private int regionX;
		@Indexed
		@Attribute
		private int regionZ;

		public void init()
		{
			this.flags = Sets.newHashSet();
		}

		public void setType(String type)
		{
			this.type = type;
		}

		public void setDesign(String name)
		{
			this.design = name;
		}

		public void setReferenceLocation(Location reference)
		{
			this.reference = DLocation.Util.create(reference);
			setRegion(reference.getBlockX(), reference.getBlockY());
		}

		public void setOwner(DPlayer.Character character)
		{
			this.owner = character;
		}

		public void setActive(Boolean bool)
		{
			this.active = bool;
		}

		public void setRegion(int X, int Z)
		{
			this.regionX = X;
			this.regionZ = Z;
		}

		public void setRegion(Region region)
		{
			this.regionX = region.getX();
			this.regionZ = region.getZ();
			save();
		}

		public void addFlags(Set<Structure.Flag> flags)
		{
			for(Structure.Flag flag : flags)
			{
				this.flags.add(flag.name());
			}
		}

		public void addFlag(Structure.Flag flag)
		{
			this.flags.add(flag.name());
			save();
		}

		public Location getReferenceLocation()
		{
			return this.reference.toLocation();
		}

		public Location getClickableBlock()
		{
			return getStructure().getClickableBlock(this.reference.toLocation());
		}

		public Set<Location> getLocations()
		{
			return getStructure().get(this.design).getLocations(this.reference.toLocation());
		}

		public Structure getStructure()
		{
			for(Structure structure : Demigods.getLoadedStructures())
			{
				if(structure.getStructureType().equalsIgnoreCase(this.type)) return structure;
			}
			return null;
		}

		public Boolean hasOwner()
		{
			return this.owner != null;
		}

		public DPlayer.Character getOwner()
		{
			return this.owner;
		}

		public Boolean getActive()
		{
			return this.active;
		}

		public Region getRegion()
		{
			return Region.Util.getRegion(this.regionX, this.regionZ);
		}

		public Boolean hasFlag(Structure.Flag flag)
		{
			return this.flags != null && this.flags.contains(flag.name());
		}

		public Set<Structure.Flag> getFlags()
		{
			return new HashSet<Structure.Flag>()
			{
				{
					for(String flag : getRawFlags())
					{
						add(Structure.Flag.valueOf(flag));
					}
				}
			};
		}

		public Set<String> getRawFlags()
		{
			return this.flags;
		}

		public long getId()
		{
			return this.id;
		}

		public void generate()
		{
			getStructure().get(this.design).generate(this.reference.toLocation());
		}

		public void save()
		{
			JOhm.save(this);
		}

		public void remove()
		{
			for(Location location : getLocations())
			{
				location.getBlock().setTypeId(Material.AIR.getId());
			}
			JOhm.delete(DLocation.class, reference.getId());
			JOhm.delete(Save.class, this.id);
		}
	}

	public static class Schematic extends HashSet<Cuboid>
	{
		private String name, designer;

		public Schematic(String name, String designer)
		{
			this.name = name;
			this.designer = designer;
		}

		public Set<Location> getLocations(Location reference)
		{
			Set<Location> locations = Sets.newHashSet();
			for(Cuboid cuboid : this)
				locations.addAll(cuboid.getBlockLocations(reference));
			return locations;
		}

		public void generate(Location reference)
		{
			for(Cuboid cuboid : this)
				cuboid.generate(reference);
		}

		@Override
		public String toString()
		{
			return this.name;
		}
	}

	public static class Util
	{
		public static Save getStructureSave(Location location)
		{
			for(Save save : filterForRegion(location, loadAll()))
			{
				if(save.getLocations().contains(location)) return save;
			}
			return null;
		}

		public static Set<Save> getStructuresInRegion(Region region)
		{
			return Sets.intersection(Sets.newHashSet(findAll("regionX", region.getX())), Sets.newHashSet(findAll("regionX", region.getZ())));
		}

		public static boolean partOfStructureWithType(Location location, String type)
		{
			for(Save save : filterForRegion(location, findAll("type", type)))
			{
				if(save.getLocations().contains(location)) return true;
			}
			return false;
		}

		public static boolean partOfStructureWithFlag(Location location, Flag flag)
		{
			for(Save save : _findAll("flags", flag.name()))
			{
				Demigods.message.broadcast(save.getId() + " - " + save.getFlags());
			}
			return false;
		}

		public static boolean isReferenceBlockWithFlag(Location location, Flag flag)
		{
			for(Save save : filterForRegion(location, _findAll("flags", flag.name())))
			{
				if(save.getLocations().contains(location)) return true;
			}
			return false;
		}

		public static boolean isClickableBlockWithFlag(Location location, Flag flag)
		{
			for(Save save : _findAll("flags", flag.name()))
			{
				if(save.getClickableBlock().equals(location)) return true;
			}
			return false;
		}

		public static boolean isInRadiusWithFlag(Location location, Flag flag)
		{
			return getInRadiusWithFlag(location, flag) != null;
		}

		public static Save getInRadiusWithFlag(Location location, Flag flag)
		{
			for(Save save : filterForRegion(location, _findAll("flags", flag.name())))
			{
				if(save.getReferenceLocation().distance(location) <= save.getStructure().getRadius()) return save;
			}
			return null;
		}

		public static boolean isTrespassingInNoGriefingZone(Player player)
		{
			Location location = player.getLocation();
			if(ZoneUtility.zoneNoBuild(player, player.getLocation())) return true;
			if(isInRadiusWithFlag(location, Flag.NO_GRIEFING))
			{
				Save save = getInRadiusWithFlag(location, Flag.NO_GRIEFING);
				if(save.getOwner() != null && save.getOwner().getId().equals(DPlayer.Util.getPlayer(player).getCurrent().getId())) return false;
				return true;
			}
			return false;
		}

		/**
		 * @deprecated Only to get it working again until we figure out why JOhm isn't finding the flags.
		 */
		public static Set<Save> _findAll(String ignored, final String flagName)
		{
			if(flagName.equals("NO_GRIEFING") || flagName.equals("TRIBUTE_LOCATION")) return Sets.newHashSet();
			return Sets.newHashSet(findAll("type", "Altar"));
		}

		public static void regenerateStructures()
		{
			for(Save save : loadAll())
			{
				save.generate();
			}
		}

		public static Set<Save> filterForRegion(Location location, Collection<Save> structures)
		{
			return Sets.intersection(getStructuresInRegion(Region.Util.getRegion(location)), Sets.newHashSet(structures));
		}

		public static Set<Structure> getStructuresWithFlag(final Structure.Flag flag)
		{
			return new HashSet<Structure>()
			{
				{
					for(Structure structure : Demigods.getLoadedStructures())
					{
						if(structure.getFlags().contains(flag)) add(structure);
					}
				}
			};
		}

		public static Save load(Long id)
		{
			return JOhm.get(Save.class, id);
		}

		public static Set<Save> loadAll()
		{
			return JOhm.getAll(Save.class);
		}

		public static List<Save> findAll(String label, Object value)
		{
			return JOhm.find(Save.class, label, value);
		}
	}

	public interface Design
	{
		public String getName();
	}

}
