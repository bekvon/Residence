package com.bekvon.bukkit.residence.listeners;

import java.util.ArrayList;
import java.util.List;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagCombo;
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
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public class ResidenceBlockListener implements Listener {

    private List<String> MessageInformed = new ArrayList<String>();
    private List<String> ResCreated = new ArrayList<String>();

    private Residence plugin;

    public ResidenceBlockListener(Residence residence) {
	this.plugin = residence;
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAnvilInventoryClick(InventoryClickEvent e) {
	// Disabling listener if flag disabled globally
	if (!Flags.anvilbreak.isGlobalyEnabled())
	    return;
	Inventory inv = e.getInventory();

	try {
	    if (inv == null || inv.getType() != InventoryType.ANVIL || e.getInventory().getLocation() == null)
		return;
	} catch (Exception | NoSuchMethodError ex) {
	    return;
	}
	Block b = e.getInventory().getLocation().getBlock();
	if (b == null || b.getType() != Material.ANVIL)
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(e.getInventory().getLocation());
	if (res == null)
	    return;
	// Fix anvil only when item is picked up
	if (e.getRawSlot() != 2)
	    return;
	if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
	    return;
	if (!res.getPermissions().has(Flags.anvilbreak, FlagCombo.OnlyFalse))
	    return;

	b.setData((byte) 1);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlantGrow(BlockGrowEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.grow.isGlobalyEnabled())
	    return;
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.grow, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVineGrow(BlockSpreadEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.grow.isGlobalyEnabled())
	    return;
	if (event.getSource().getType() != Material.VINE)
	    return;
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.grow, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onleaveDecay(LeavesDecayEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.decay.isGlobalyEnabled())
	    return;
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.decay, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTreeGrowt(StructureGrowEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.grow.isGlobalyEnabled())
	    return;
	if (plugin.isDisabledWorldListener(event.getWorld()))
	    return;
	FlagPermissions perms = plugin.getPermsByLoc(event.getLocation());
	if (!perms.has(Flags.grow, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTreeGrow(StructureGrowEvent event) {

	if (plugin.isDisabledWorldListener(event.getWorld()))
	    return;

	ClaimedResidence startRes = plugin.getResidenceManager().getByLoc(event.getLocation());
	List<BlockState> blocks = event.getBlocks();
	int i = 0;
	for (BlockState one : blocks) {
	    ClaimedResidence targetRes = plugin.getResidenceManager().getByLoc(one.getLocation());
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
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	Player player = event.getPlayer();
	if (plugin.isResAdminOn(player)) {
	    return;
	}

	Block block = event.getBlock();
	Material mat = block.getType();
	String world = block.getWorld().getName();

	ResidencePlayer resPlayer = plugin.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = resPlayer.getGroup();
	if (plugin.getItemManager().isIgnored(mat, group, world)) {
	    return;
	}
	ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());
	if (plugin.getConfigManager().enabledRentSystem() && res != null) {
	    String resname = res.getName();
	    if (plugin.getConfigManager().preventRentModify() && plugin.getRentManager().isRented(resname)) {
		plugin.msg(player, lm.Rent_ModifyDeny);
		event.setCancelled(true);
		return;
	    }
	}
	FlagPermissions perms = plugin.getPermsByLocForPlayer(block.getLocation(), player);
	if (res != null && res.getItemIgnoreList().isListed(mat))
	    return;

	boolean hasdestroy = perms.playerHas(player, Flags.destroy, perms.playerHas(player, Flags.build, true));

	if (!hasdestroy && !player.hasPermission("residence.bypass.destroy")) {
	    plugin.msg(player, lm.Flag_Deny, Flags.destroy);
	    event.setCancelled(true);
	} else if (mat == Material.CHEST && !perms.playerHas(player, Flags.container, true)) {
	    plugin.msg(player, lm.Flag_Deny, Flags.container);
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.snowtrail.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (!(event instanceof EntityBlockFormEvent))
	    return;

	if (((EntityBlockFormEvent) event).getEntity() instanceof Snowman) {
	    FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	    if (!perms.has(Flags.snowtrail, true)) {
		event.setCancelled(true);
	    }
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onIceForm(BlockFormEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.iceform.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	Material ice = Material.getMaterial("FROSTED_ICE");

	if (event.getNewState().getType() != Material.SNOW && event.getNewState().getType() != Material.ICE && ice != null && ice != event.getNewState().getType())
	    return;

	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.iceform, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onIceMelt(BlockFadeEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.icemelt.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	if (event.getNewState().getType() != Material.STATIONARY_WATER && event.getBlock().getState().getType() != Material.SNOW && event.getBlock().getState()
	    .getType() != Material.SNOW_BLOCK)
	    return;

	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.icemelt, true)) {
	    event.setCancelled(true);
	}
    }

    public static final String SourceResidenceName = "SourceResidenceName";

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.fallinprotection.isGlobalyEnabled())
	    return;
	if (event.getEntityType() != EntityType.FALLING_BLOCK)
	    return;
	Entity ent = event.getEntity();
	if (!ent.hasMetadata(SourceResidenceName)) {
	    ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());
	    String resName = res == null ? "NULL" : res.getName();
	    ent.setMetadata(SourceResidenceName, new FixedMetadataValue(plugin, resName));
	} else {
	    String saved = ent.getMetadata(SourceResidenceName).get(0).asString();
	    ClaimedResidence res = plugin.getResidenceManager().getByLoc(ent.getLocation());

	    if (res != null && res.getPermissions().has(Flags.fallinprotection, FlagCombo.OnlyFalse))
		return;

	    String resName = res == null ? "NULL" : res.getName();
	    if (!saved.equalsIgnoreCase(resName)) {
		event.setCancelled(true);
		ent.remove();
	    }
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFall(EntityChangeBlockEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.fallinprotection.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (!plugin.getConfigManager().isBlockFall())
	    return;

	if ((event.getEntityType() != EntityType.FALLING_BLOCK))
	    return;

	if (event.getTo().hasGravity())
	    return;

	Block block = event.getBlock();

	if (block == null)
	    return;

	if (!plugin.getConfigManager().getBlockFallWorlds().contains(block.getLocation().getWorld().getName()))
	    return;

	if (block.getY() <= plugin.getConfigManager().getBlockFallLevel())
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());
	Location loc = new Location(block.getLocation().getWorld(), block.getX(), block.getY(), block.getZ());
	for (int i = loc.getBlockY() - 1; i >= plugin.getConfigManager().getBlockFallLevel() - 1; i--) {
	    loc.setY(i);
	    if (loc.getBlock().getType() != Material.AIR) {
		ClaimedResidence targetRes = plugin.getResidenceManager().getByLoc(loc);
		if (targetRes == null)
		    continue;
		if (res != null && !res.getName().equals(targetRes.getName())) {
		    if (targetRes.getPermissions().has(Flags.fallinprotection, FlagCombo.OnlyFalse))
			continue;
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
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (!plugin.getConfigManager().ShowNoobMessage())
	    return;

	Player player = event.getPlayer();
	if (plugin.isResAdminOn(player))
	    return;
	Block block = event.getBlock();
	if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
	    return;

	if (!player.hasPermission("residence.newguyresidence"))
	    return;

	ArrayList<String> list = plugin.getPlayerManager().getResidenceList(player.getName());
	if (list.size() != 0)
	    return;

	if (MessageInformed.contains(player.getName()))
	    return;

	plugin.msg(player, lm.General_NewPlayerInfo);

	MessageInformed.add(player.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestPlaceCreateRes(BlockPlaceEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (!plugin.getConfigManager().isNewPlayerUse())
	    return;

	Player player = event.getPlayer();
	if (plugin.isResAdminOn(player))
	    return;
	Block block = event.getBlock();
	if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
	    return;

	ArrayList<String> list = plugin.getPlayerManager().getResidenceList(player.getName());
	if (list.size() != 0)
	    return;

	if (ResCreated.contains(player.getName()))
	    return;

	Location loc = block.getLocation();

	plugin.getSelectionManager().placeLoc1(player, new Location(loc.getWorld(), loc.getBlockX() - plugin.getConfigManager().getNewPlayerRangeX(), loc
	    .getBlockY() - plugin.getConfigManager().getNewPlayerRangeY(), loc.getBlockZ() - plugin.getConfigManager().getNewPlayerRangeZ()), true);
	plugin.getSelectionManager().placeLoc2(player, new Location(loc.getWorld(), loc.getBlockX() + plugin.getConfigManager().getNewPlayerRangeX(), loc
	    .getBlockY() + plugin.getConfigManager().getNewPlayerRangeY(), loc.getBlockZ() + plugin.getConfigManager().getNewPlayerRangeZ()), true);

	boolean created = plugin.getResidenceManager().addResidence(player, player.getName(), plugin.getSelectionManager().getPlayerLoc1(player.getName()),
	    plugin.getSelectionManager().getPlayerLoc2(player.getName()), plugin.getConfigManager().isNewPlayerFree());
	if (created)
	    ResCreated.add(player.getName());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	Player player = event.getPlayer();
	if (plugin.isResAdminOn(player)) {
	    return;
	}
	Block block = event.getBlock();
	Material mat = block.getType();
	String world = block.getWorld().getName();

	ResidencePlayer resPlayer = plugin.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = resPlayer.getGroup();
	if (plugin.getItemManager().isIgnored(mat, group, world)) {
	    return;
	}
	ClaimedResidence res = plugin.getResidenceManager().getByLoc(block.getLocation());
	if (plugin.getConfigManager().enabledRentSystem() && res != null) {
	    String resname = res.getName();
	    if (plugin.getConfigManager().preventRentModify() && plugin.getRentManager().isRented(resname)) {
		plugin.msg(player, lm.Rent_ModifyDeny);
		event.setCancelled(true);
		return;
	    }
	}
	if (res != null && !res.getItemBlacklist().isAllowed(mat)) {
	    plugin.msg(player, lm.General_ItemBlacklisted);
	    event.setCancelled(true);
	    return;
	}
	FlagPermissions perms = plugin.getPermsByLocForPlayer(block.getLocation(), player);
	boolean hasplace = perms.playerHas(player, Flags.place, perms.playerHas(player, Flags.build, true));
	if (!hasplace && !player.hasPermission("residence.bypass.build")) {
	    event.setCancelled(true);
	    plugin.msg(player, lm.Flag_Deny, Flags.place.getName());
	    return;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.spread.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	Location loc = event.getBlock().getLocation();
	FlagPermissions perms = plugin.getPermsByLoc(loc);
	if (!perms.has(Flags.spread, true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	// Disabling listener if flag disabled globally
	if (!Flags.piston.isGlobalyEnabled())
	    return;

	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.piston, true)) {
	    event.setCancelled(true);
	    return;
	}

	// Disabling listener if flag disabled globally
	if (!Flags.pistonprotection.isGlobalyEnabled())
	    return;

	List<Block> blocks = plugin.getNms().getPistonRetractBlocks(event);

	if (!event.isSticky())
	    return;

	ClaimedResidence pistonRes = plugin.getResidenceManager().getByLoc(event.getBlock().getLocation());

	for (Block block : blocks) {
	    Location locFrom = block.getLocation();
	    ClaimedResidence blockFrom = plugin.getResidenceManager().getByLoc(locFrom);
	    if (blockFrom == null)
		continue;
	    if (blockFrom == pistonRes)
		continue;
	    if (pistonRes != null && blockFrom.isOwner(pistonRes.getOwner()))
		continue;
	    if (!blockFrom.getPermissions().has(Flags.pistonprotection, FlagCombo.OnlyTrue))
		continue;
	    event.setCancelled(true);
	    break;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	// Disabling listener if flag disabled globally
	if (!Flags.piston.isGlobalyEnabled())
	    return;
	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.piston, true)) {
	    event.setCancelled(true);
	    return;
	}

	// Disabling listener if flag disabled globally
	if (!Flags.pistonprotection.isGlobalyEnabled())
	    return;
	ClaimedResidence pistonRes = plugin.getResidenceManager().getByLoc(event.getBlock().getLocation());

	BlockFace dir = event.getDirection();
	for (Block block : event.getBlocks()) {
	    Location locFrom = block.getLocation();
	    Location locTo = new Location(block.getWorld(), block.getX() + dir.getModX(), block.getY() + dir.getModY(), block.getZ() + dir.getModZ());
	    ClaimedResidence blockFrom = plugin.getResidenceManager().getByLoc(locFrom);
	    ClaimedResidence blockTo = plugin.getResidenceManager().getByLoc(locTo);

	    if (pistonRes == null && blockTo != null && blockTo.getPermissions().has(Flags.pistonprotection, FlagCombo.OnlyTrue)) {
		event.setCancelled(true);
		return;
	    } else if (blockTo != null && blockFrom == null && blockTo.getPermissions().has(Flags.pistonprotection, FlagCombo.OnlyTrue)) {
		event.setCancelled(true);
		return;
	    } else if (blockTo != null && blockFrom != null && (pistonRes != null && !blockTo.isOwner(pistonRes.getOwner()) || !blockTo.isOwner(blockFrom.getOwner()))
		&& blockTo.getPermissions().has(Flags.pistonprotection, FlagCombo.OnlyTrue)) {
		event.setCancelled(true);
		return;
	    }

	}

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	ClaimedResidence fromRes = plugin.getResidenceManager().getByLoc(event.getBlock().getLocation());
	ClaimedResidence toRes = plugin.getResidenceManager().getByLoc(event.getToBlock().getLocation());

	FlagPermissions perms = plugin.getPermsByLoc(event.getToBlock().getLocation());
	boolean hasflow = perms.has(Flags.flow, FlagCombo.TrueOrNone);
	Material mat = event.getBlock().getType();

	if (perms.has(Flags.flowinprotection, FlagCombo.TrueOrNone))
	    if (fromRes == null && toRes != null || fromRes != null && toRes != null && !fromRes.equals(toRes) && !fromRes.isOwner(toRes.getOwner())) {
		event.setCancelled(true);
		return;
	    }

	if (perms.has(Flags.flow, FlagCombo.OnlyFalse)) {
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
	// Disabling listener if flag disabled globally
	if (!Flags.dryup.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	Material mat = event.getBlock().getType();
	if (mat != Material.SOIL)
	    return;

	FlagPermissions perms = plugin.getPermsByLoc(event.getNewState().getLocation());
	if (!perms.has(Flags.dryup, true)) {
	    event.getBlock().setData((byte) 7);
	    event.setCancelled(true);
	    return;
	}
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLandDryPhysics(BlockPhysicsEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.dryup.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;

	Material mat = event.getBlock().getType();
	if (mat != Material.SOIL)
	    return;

	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.dryup, true)) {
	    event.getBlock().setData((byte) 7);
	    event.setCancelled(true);
	    return;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	if (event.isCancelled())
	    return;

	Location location = new Location(event.getBlock().getWorld(), event.getVelocity().getBlockX(), event.getVelocity().getBlockY(), event.getVelocity().getBlockZ());

	ClaimedResidence targetres = plugin.getResidenceManager().getByLoc(location);

	if (targetres == null && location.getBlockY() >= plugin.getConfigManager().getPlaceLevel() && plugin.getConfigManager().getNoPlaceWorlds().contains(location
	    .getWorld().getName())) {
	    ItemStack mat = event.getItem();
	    if (plugin.getConfigManager().isNoLavaPlace())
		if (mat.getType() == Material.LAVA_BUCKET) {
		    event.setCancelled(true);
		    return;
		}

	    if (plugin.getConfigManager().isNoWaterPlace())
		if (mat.getType() == Material.WATER_BUCKET) {
		    event.setCancelled(true);
		    return;
		}
	}

	ClaimedResidence sourceres = plugin.getResidenceManager().getByLoc(event.getBlock().getLocation());

	if ((sourceres == null && targetres != null || sourceres != null && targetres == null || sourceres != null && targetres != null && !sourceres.getName().equals(
	    targetres.getName())) && (event.getItem().getType() == Material.LAVA_BUCKET || event.getItem().getType() == Material.WATER_BUCKET)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLavaWaterFlow(BlockFromToEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	Material mat = event.getBlock().getType();

	Location location = event.getToBlock().getLocation();
	if (!plugin.getConfigManager().getNoFlowWorlds().contains(location.getWorld().getName()))
	    return;

	if (location.getBlockY() < plugin.getConfigManager().getFlowLevel())
	    return;

	ClaimedResidence res = plugin.getResidenceManager().getByLoc(location);

	if (res != null)
	    return;

	if (plugin.getConfigManager().isNoLava())
	    if (mat == Material.LAVA || mat == Material.STATIONARY_LAVA) {
		event.setCancelled(true);
		return;
	    }

	if (plugin.getConfigManager().isNoWater())
	    if (mat == Material.WATER || mat == Material.STATIONARY_WATER) {
		event.setCancelled(true);
		return;
	    }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
	// Disabling listener if flag disabled globally
	if (!Flags.firespread.isGlobalyEnabled())
	    return;
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has(Flags.firespread, true))
	    event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
	// disabling event on world
	if (plugin.isDisabledWorldListener(event.getBlock().getWorld()))
	    return;
	IgniteCause cause = event.getCause();
	if (cause == IgniteCause.SPREAD) {
	    // Disabling listener if flag disabled globally
	    if (!Flags.firespread.isGlobalyEnabled())
		return;
	    FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	    if (!perms.has(Flags.firespread, true))
		event.setCancelled(true);
	} else if (cause == IgniteCause.FLINT_AND_STEEL) {
	    // Disabling listener if flag disabled globally
	    if (!Flags.ignite.isGlobalyEnabled())
		return;
	    Player player = event.getPlayer();
	    FlagPermissions perms = plugin.getPermsByLocForPlayer(event.getBlock().getLocation(), player);
	    if (player != null && !perms.playerHas(player, Flags.ignite, true) && !plugin.isResAdminOn(player)) {
		event.setCancelled(true);
		plugin.msg(player, lm.Flag_Deny, Flags.ignite.getName());
	    }
	} else {
	    // Disabling listener if flag disabled globally
	    if (!Flags.ignite.isGlobalyEnabled())
		return;
	    FlagPermissions perms = plugin.getPermsByLoc(event.getBlock().getLocation());
	    if (!perms.has(Flags.ignite, true)) {
		event.setCancelled(true);
	    }
	}
    }
}
