/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.utils.Debug;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.bekvon.bukkit.residence.Residence;
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

/**
 *
 * @author Administrator
 */
public class ResidenceBlockListener implements Listener {

    private List<String> MessageInformed = new ArrayList<String>();
    private List<String> ResCreated = new ArrayList<String>();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTreeGrow(StructureGrowEvent event) {
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

	Debug.D("destroy event");
	
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
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
		event.setCancelled(true);
		return;
	    }
	}
	FlagPermissions perms = Residence.getPermsByLocForPlayer(block.getLocation(), player);
	String pname = player.getName();
	if (res != null && res.getItemIgnoreList().isListed(mat))
	    return;

	boolean hasdestroy = perms.playerHas(pname, player.getWorld().getName(), "destroy", perms.playerHas(pname, player.getWorld().getName(), "build", true));
	boolean hasContainer = perms.playerHas(pname, player.getWorld().getName(), "container", true);
	if (!hasdestroy) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "destroy"));
	    event.setCancelled(true);
	} else if (!hasContainer && mat == Material.CHEST) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "container"));
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {

	if (!(event instanceof EntityBlockFormEvent))
	    return;

	if (((EntityBlockFormEvent) event).getEntity() instanceof Snowman) {
	    FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	    if (!perms.has("snowtrail", true)) {
		event.setCancelled(true);
	    }
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFall(EntityChangeBlockEvent event) {

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

	player.sendMessage(Residence.getLM().getMessage("Language.NewPlayerInfo"));

	MessageInformed.add(player.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChestPlaceCreateRes(BlockPlaceEvent event) {

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
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
		event.setCancelled(true);
		return;
	    }
	}
	String pname = player.getName();
	if (res != null && !res.getItemBlacklist().isAllowed(mat)) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ItemBlacklisted"));
	    event.setCancelled(true);
	    return;
	}
	FlagPermissions perms = Residence.getPermsByLocForPlayer(block.getLocation(), player);
	boolean hasplace = perms.playerHas(pname, player.getWorld().getName(), "place", perms.playerHas(pname, player.getWorld().getName(), "build", true));
	if (!hasplace) {
	    event.setCancelled(true);
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "place"));
	    return;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
	Location loc = event.getBlock().getLocation();
	FlagPermissions perms = Residence.getPermsByLoc(loc);
	if (!perms.has("spread", true)) {
	    event.setCancelled(true);
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {

	FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has("piston", true)) {
	    event.setCancelled(true);
	    return;
	}

	List<Block> blocks = Residence.getNms().getPistonRetractBlocks(event);

	if (event.isSticky()) {
	    for (Block oneBlock : blocks) {
		FlagPermissions blockperms = Residence.getPermsByLoc(oneBlock.getLocation());
		if (!blockperms.has("piston", true)) {
		    event.setCancelled(true);
		    return;
		}
	    }

	    for (Block block : blocks) {
		ClaimedResidence blockRes = Residence.getResidenceManager().getByLoc(block.getLocation());
		ClaimedResidence pistonRes = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
		if (blockRes == null && pistonRes != null || blockRes != null && pistonRes == null || blockRes != null && pistonRes != null && !blockRes.getName()
		    .equalsIgnoreCase(pistonRes.getName())) {
		    event.setCancelled(true);
		    return;
		}
	    }
	}

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
	FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has("piston", true)) {
	    event.setCancelled(true);
	}
	for (Block block : event.getBlocks()) {
	    FlagPermissions blockpermsfrom = Residence.getPermsByLoc(block.getLocation());
	    if (!blockpermsfrom.has("piston", true)) {
		event.setCancelled(true);
		return;
	    }
	}

	BlockFace dir = event.getDirection();
	for (Block block : event.getBlocks()) {
	    Location loc = new Location(block.getWorld(), block.getX() + dir.getModX(), block.getY() + dir.getModY(), block.getZ() + dir.getModZ());
	    ClaimedResidence blockRes = Residence.getResidenceManager().getByLoc(loc);
	    ClaimedResidence pistonRes = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
	    if (blockRes == null && pistonRes != null || blockRes != null && pistonRes == null || blockRes != null && pistonRes != null && !blockRes.getName()
		.equalsIgnoreCase(pistonRes.getName())) {
		event.setCancelled(true);
		return;
	    }
	}

    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {

	FlagPermissions perms = Residence.getPermsByLoc(event.getToBlock().getLocation());
	boolean hasflow = perms.has("flow", true);
	Material mat = event.getBlock().getType();
	if (!hasflow) {
	    event.setCancelled(true);
	    return;
	}
	if (mat == Material.LAVA || mat == Material.STATIONARY_LAVA) {
	    if (!perms.has("lavaflow", hasflow)) {
		event.setCancelled(true);
	    }
	    return;
	}
	if (mat == Material.WATER || mat == Material.STATIONARY_WATER) {
	    if (!perms.has("waterflow", hasflow)) {
		event.setCancelled(true);
	    }
	    return;
	}
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {

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
	FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	if (!perms.has("firespread", true))
	    event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
	IgniteCause cause = event.getCause();
	if (cause == IgniteCause.SPREAD) {
	    FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	    if (!perms.has("firespread", true))
		event.setCancelled(true);
	} else if (cause == IgniteCause.FLINT_AND_STEEL) {
	    Player player = event.getPlayer();
	    FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlock().getLocation(), player);
	    if (player != null && !perms.playerHas(player.getName(), player.getWorld().getName(), "ignite", true) && !Residence.isResAdminOn(player)) {
		event.setCancelled(true);
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagDeny", "ignite"));
	    }
	} else {
	    FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
	    if (!perms.has("ignite", true)) {
		event.setCancelled(true);
	    }
	}
    }
}
