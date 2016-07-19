package com.bekvon.bukkit.residence.listeners;

import java.util.ArrayList;
import java.util.List;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class ResidenceBlockListener implements Listener {

    private List<String> MessageInformed = new ArrayList<String>();
    private List<String> ResCreated = new ArrayList<String>();

    private Residence plugin;

    public ResidenceBlockListener(Residence residence) {
	this.plugin = residence;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTreeGrow(StructureGrowEvent event) {

	if (Residence.isDisabledWorldListener(event.getWorld()))
	    return;

	ClaimedResidence startRes = Residence.getResidenceManager().getByLoc(event.getLocation());
	List<BlockState> blocks = event.getBlocks();
	int i = 0;
	for (BlockState one : blocks) {
	    ClaimedResidence targetRes = Residence.getResidenceManager().getByLoc(one.getLocation());
	    if (startRes == null && targetRes != null ||
		targetRes != null && startRes != null && !startRes.getName().equals(targetRes.getName()) && !startRes.isOwner(targetRes.getOwner())) {
		BlockState matas = blocks.get(i);
		matas.setType(Material.AIR);
		blocks.set(i, matas);
	    }
	    i++;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	Player player = event.getPlayer();
	if (Residence.isResAdminOn(player)) {
	    return;
	}

	Block block = event.getBlock();
	Material mat = block.getType();
	String world = block.getWorld().getName();
	String group = Residence.getPermissionManager().getGroupNameByPlayer(player);
	if (Residence.getItemManager().isIgnored(mat, group, world)) {
	    return;
	}
	ClaimedResidence res = Residence.getResidenceManager().getByLoc(block.getLocation());
	if (Residence.getConfigManager().enabledRentSystem() && res != null) {
	    String resname = res.getName();
	    if (Residence.getConfigManager().preventRentModify() && Residence.getRentManager().isRented(resname)) {
		Residence.msg(player, lm.Rent_ModifyDeny);
		event.setCancelled(true);
		return;
	    }
	}
	FlagPermissions perms = Residence.getPermsByLocForPlayer(block.getLocation(), player);
	String pname = player.getName();
	if (res != null && res.getItemIgnoreList().isListed(mat))
	    return;

	boolean hasdestroy = perms.playerHas(pname, player.getWorld().getName(), Flags.destroy, perms.playerHas(pname, player.getWorld().getName(), Flags.build, true));
	boolean hasContainer = perms.playerHas(pname, player.getWorld().getName(), Flags.container, true);
	if (!hasdestroy && !player.hasPermission("residence.bypass.destroy")) {
	    Residence.msg(player, lm.Flag_Deny, Flags.destroy);
	    event.setCancelled(true);
	} else if (!hasContainer && mat == Material.CHEST) {
	    Residence.msg(player, lm.Flag_Deny, Flags.container);
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (!(event instanceof EntityBlockFormEvent))
	    return;

	if (((EntityBlockFormEvent) event).getEntity() instanceof Snowman) {
	    FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	    if (!perms.has(Flags.snowtrail, true)) {
		event.setCancelled(true);
	    }
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onIceForm(BlockFormEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	Material ice = Material.getMaterial("FROSTED_ICE");

	if (event.getNewState().getType() != Material.SNOW && event.getNewState().getType() != Material.ICE && ice != null && ice != event.getNewState().getType())
	    return;

	FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.iceform, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onIceMelt(BlockFadeEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	if (event.getNewState().getType() != Material.STATIONARY_WATER && event.getBlock().getState().getType() != Material.SNOW && event.getBlock().getState()
	    .getType() != Material.SNOW_BLOCK)
	    return;

	FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.icemelt, true)) {
	    event.setCancelled(true);
	}
    }

    public static final String SourceResidenceName = "SourceResidenceName";

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
	if (event.getEntityType() != EntityType.FALLING_BLOCK)
	    return;
	Entity ent = event.getEntity();
	if (!ent.hasMetadata(SourceResidenceName)) {
	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(ent.getLocation());
	    String resName = res == null ? "NULL" : res.getName();
	    ent.setMetadata(SourceResidenceName, new FixedMetadataValue(plugin, resName));
	} else {
	    String saved = ent.getMetadata(SourceResidenceName).get(0).asString();
	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(ent.getLocation());
	    String resName = res == null ? "NULL" : res.getName();
	    if (!saved.equalsIgnoreCase(resName)) {
		event.setCancelled(true);
		ent.remove();
	    }
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFall(EntityChangeBlockEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (!Residence.getConfigManager().isBlockFall())
	    return;

	if ((event.getEntityType() != EntityType.FALLING_BLOCK))
	    return;

	if (event.getTo().hasGravity())
	    return;

	Block block = event.getBlock();

	if (block == null)
	    return;

	if (!Residence.getConfigManager().getBlockFallWorlds().contains(block.getLocation().getWorld().getName()))
	    return;

	if (block.getY() <= Residence.getConfigManager().getBlockFallLevel())
	    return;

	ClaimedResidence res = Residence.getResidenceManager().getByLoc(block.getLocation());
	Location loc = new Location(block.getLocation().getWorld(), block.getX(), block.getY(), block.getZ());
	for (int i = loc.getBlockY() - 1; i >= Residence.getConfigManager().getBlockFallLevel() - 1; i--) {
	    loc.setY(i);
	    if (loc.getBlock().getType() != Material.AIR) {
		ClaimedResidence targetRes = Residence.getResidenceManager().getByLoc(loc);
		if (res == null && targetRes != null || res != null && targetRes == null || res != null && targetRes != null && !res.getName()
		    .equals(targetRes.getName())) {
		    event.setCancelled(true);
		    block.setType(Material.AIR);
		}
		return;
	    }
	}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestPlace(BlockPlaceEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (!Residence.getConfigManager().ShowNoobMessage())
	    return;

	Player player = event.getPlayer();
	if (Residence.isResAdminOn(player))
	    return;
	Block block = event.getBlock();
	if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
	    return;

	ArrayList<String> list = Residence.getPlayerManager().getResidenceList(player.getName());
	if (list.size() != 0)
	    return;

	if (MessageInformed.contains(player.getName()))
	    return;

	Residence.msg(player, lm.General_NewPlayerInfo);

	MessageInformed.add(player.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestPlaceCreateRes(BlockPlaceEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (!Residence.getConfigManager().isNewPlayerUse())
	    return;

	Player player = event.getPlayer();
	if (Residence.isResAdminOn(player))
	    return;
	Block block = event.getBlock();
	if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
	    return;

	ArrayList<String> list = Residence.getPlayerManager().getResidenceList(player.getName());
	if (list.size() != 0)
	    return;

	if (ResCreated.contains(player.getName()))
	    return;

	Location loc = block.getLocation();

	Residence.getSelectionManager().placeLoc1(player, new Location(loc.getWorld(), loc.getBlockX() - Residence.getConfigManager().getNewPlayerRangeX(), loc
	    .getBlockY() - Residence.getConfigManager().getNewPlayerRangeY(), loc.getBlockZ() - Residence.getConfigManager().getNewPlayerRangeZ()), true);
	Residence.getSelectionManager().placeLoc2(player, new Location(loc.getWorld(), loc.getBlockX() + Residence.getConfigManager().getNewPlayerRangeX(), loc
	    .getBlockY() + Residence.getConfigManager().getNewPlayerRangeY(), loc.getBlockZ() + Residence.getConfigManager().getNewPlayerRangeZ()), true);

	boolean created = Residence.getResidenceManager().addResidence(player, player.getName(), Residence.getSelectionManager().getPlayerLoc1(player.getName()),
	    Residence.getSelectionManager().getPlayerLoc2(player.getName()), Residence.getConfigManager().isNewPlayerFree());
	if (created)
	    ResCreated.add(player.getName());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	Player player = event.getPlayer();
	if (Residence.isResAdminOn(player)) {
	    return;
	}
	Block block = event.getBlock();
	Material mat = block.getType();
	String world = block.getWorld().getName();
	String group = Residence.getPermissionManager().getGroupNameByPlayer(player);
	if (Residence.getItemManager().isIgnored(mat, group, world)) {
	    return;
	}
	ClaimedResidence res = Residence.getResidenceManager().getByLoc(block.getLocation());
	if (Residence.getConfigManager().enabledRentSystem() && res != null) {
	    String resname = res.getName();
	    if (Residence.getConfigManager().preventRentModify() && Residence.getRentManager().isRented(resname)) {
		Residence.msg(player, lm.Rent_ModifyDeny);
		event.setCancelled(true);
		return;
	    }
	}
	String pname = player.getName();
	if (res != null && !res.getItemBlacklist().isAllowed(mat)) {
	    Residence.msg(player, lm.General_ItemBlacklisted);
	    event.setCancelled(true);
	    return;
	}
	FlagPermissions perms = Residence.getPermsByLocForPlayer(block.getLocation(), player);
	boolean hasplace = perms.playerHas(pname, world, Flags.place, perms.playerHas(pname, world, Flags.build, true));
	if (!hasplace && !player.hasPermission("residence.bypass.build")) {
	    event.setCancelled(true);
	    Residence.msg(player, lm.Flag_Deny, Flags.place.getName());
	    return;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	Location loc = event.getBlock().getLocation();
	FlagPermissions perms = Residence.getPermsByLoc(loc);
	if (!perms.has(Flags.spread, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.piston, true)) {
	    event.setCancelled(true);
	    return;
	}

	List<Block> blocks = Residence.getNms().getPistonRetractBlocks(event);

	if (!event.isSticky())
	    return;

	ClaimedResidence pistonRes = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());

	BlockFace dir = event.getDirection();
	for (Block block : blocks) {
	    Location locFrom = block.getLocation();
	    Location locTo = new Location(block.getWorld(), block.getX() + dir.getModX(), block.getY() + dir.getModY(), block.getZ() + dir.getModZ());
	    ClaimedResidence blockFrom = Residence.getResidenceManager().getByLoc(locFrom);
	    ClaimedResidence blockTo = Residence.getResidenceManager().getByLoc(locTo);
	    if (pistonRes == null && blockTo != null && blockTo.getPermissions().has(Flags.pistonprotection, true)) {
		event.setCancelled(true);
		return;
	    } else if (blockTo != null && blockFrom != null && !blockTo.isOwner(blockFrom.getOwner()) && blockFrom.getPermissions().has(
		Flags.pistonprotection, true)) {
		event.setCancelled(true);
		return;
	    }
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.piston, true)) {
	    event.setCancelled(true);
	    return;
	}

	ClaimedResidence pistonRes = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());

	BlockFace dir = event.getDirection();
	for (Block block : event.getBlocks()) {
	    Location locFrom = block.getLocation();
	    Location locTo = new Location(block.getWorld(), block.getX() + dir.getModX(), block.getY() + dir.getModY(), block.getZ() + dir.getModZ());
	    ClaimedResidence blockFrom = Residence.getResidenceManager().getByLoc(locFrom);
	    ClaimedResidence blockTo = Residence.getResidenceManager().getByLoc(locTo);

	    if (pistonRes == null && blockTo != null && blockTo.getPermissions().has(Flags.pistonprotection, true)) {
		event.setCancelled(true);
		return;
	    } else if (blockTo != null && blockFrom == null && blockTo.getPermissions().has(Flags.pistonprotection, true)) {
		event.setCancelled(true);
		return;
	    } else if (blockTo != null && blockFrom != null && (pistonRes != null && !blockTo.isOwner(pistonRes.getOwner()) || !blockTo.isOwner(blockFrom.getOwner()))
		&& blockTo.getPermissions().has(Flags.pistonprotection, true)) {
		event.setCancelled(true);
		return;
	    }

	}

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	FlagPermissions perms = Residence.getPermsByLoc(event.getToBlock().getLocation());
	boolean hasflow = perms.has(Flags.flow, true);
	Material mat = event.getBlock().getType();
	if (!hasflow) {
	    event.setCancelled(true);
	    return;
	}
	if (mat == Material.LAVA || mat == Material.STATIONARY_LAVA) {
	    if (!perms.has(Flags.lavaflow, hasflow)) {
		event.setCancelled(true);
	    }
	    return;
	}
	if (mat == Material.WATER || mat == Material.STATIONARY_WATER) {
	    if (!perms.has(Flags.waterflow, hasflow)) {
		event.setCancelled(true);
	    }
	    return;
	}
    }

    @SuppressWarnings({ "deprecation" })
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLandDryFade(BlockFadeEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	Material mat = event.getBlock().getType();
	if (mat != Material.SOIL)
	    return;

	FlagPermissions perms = Residence.getPermsByLoc(event.getNewState().getLocation());
	if (!perms.has(Flags.dryup, true)) {
	    event.getBlock().setData((byte) 7);
	    event.setCancelled(true);
	    return;
	}
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLandDryPhysics(BlockPhysicsEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	Material mat = event.getBlock().getType();
	if (mat != Material.SOIL)
	    return;

	FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.dryup, true)) {
	    event.getBlock().setData((byte) 7);
	    event.setCancelled(true);
	    return;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (event.isCancelled())
	    return;

	Location location = new Location(event.getBlock().getWorld(), event.getVelocity().getBlockX(), event.getVelocity().getBlockY(), event.getVelocity().getBlockZ());

	ClaimedResidence targetres = Residence.getResidenceManager().getByLoc(location);

	if (targetres == null && location.getBlockY() >= Residence.getConfigManager().getPlaceLevel() && Residence.getConfigManager().getNoPlaceWorlds().contains(location
	    .getWorld().getName())) {
	    ItemStack mat = event.getItem();
	    if (Residence.getConfigManager().isNoLavaPlace())
		if (mat.getType() == Material.LAVA_BUCKET) {
		    event.setCancelled(true);
		    return;
		}

	    if (Residence.getConfigManager().isNoWaterPlace())
		if (mat.getType() == Material.WATER_BUCKET) {
		    event.setCancelled(true);
		    return;
		}
	}

	ClaimedResidence sourceres = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());

	if ((sourceres == null && targetres != null || sourceres != null && targetres == null || sourceres != null && targetres != null && !sourceres.getName().equals(
	    targetres.getName())) && (event.getItem().getType() == Material.LAVA_BUCKET || event.getItem().getType() == Material.WATER_BUCKET)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLavaWaterFlow(BlockFromToEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	Material mat = event.getBlock().getType();

	Location location = event.getToBlock().getLocation();
	if (!Residence.getConfigManager().getNoFlowWorlds().contains(location.getWorld().getName()))
	    return;

	if (location.getBlockY() < Residence.getConfigManager().getFlowLevel())
	    return;

	ClaimedResidence res = Residence.getResidenceManager().getByLoc(location);

	if (res != null)
	    return;

	if (Residence.getConfigManager().isNoLava())
	    if (mat == Material.LAVA || mat == Material.STATIONARY_LAVA) {
		event.setCancelled(true);
		return;
	    }

	if (Residence.getConfigManager().isNoWater())
	    if (mat == Material.WATER || mat == Material.STATIONARY_WATER) {
		event.setCancelled(true);
		return;
	    }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.firespread, true))
	    event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
	// disabling event on world
	if (Residence.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	IgniteCause cause = event.getCause();
	if (cause == IgniteCause.SPREAD) {
	    FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	    if (!perms.has(Flags.firespread, true))
		event.setCancelled(true);
	} else if (cause == IgniteCause.FLINT_AND_STEEL) {
	    Player player = event.getPlayer();
	    FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlock().getLocation(), player);
	    if (player != null && !perms.playerHas(player.getName(), player.getWorld().getName(), Flags.ignite, true) && !Residence.isResAdminOn(player)) {
		event.setCancelled(true);
		Residence.msg(player, lm.Flag_Deny, Flags.ignite.getName());
	    }
	} else {
	    FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	    if (!perms.has(Flags.ignite, true)) {
		event.setCancelled(true);
	    }
	}
    }
}
