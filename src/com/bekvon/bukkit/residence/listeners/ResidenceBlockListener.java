/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.listeners;
import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.inori.utils.ILog;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;

/**
 *
 * @author Administrator
 */
public class ResidenceBlockListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.isCancelled())
            return;
        Player player = event.getPlayer();
        ILog.sendToPlayer(player, "onBlockBreak Fired");
        if(Residence.isResAdminOn(player);)
            return;
        Material mat = event.getBlock().getType();
        String world = event.getBlock().getWorld().getName();
        String group = Residence.getPermissionManager().getGroupNameByPlayer(player);
        if(Residence.getItemManager().isIgnored(mat, group, world))
        {
            return;
        }
        ClaimedResidence res;
        if(Residence.getConfigManager().enabledRentSystem())
        {
            String resname = Residence.getResidenceManager().getNameByLoc(event.getBlock().getLocation());
            if(Residence.getConfigManager().preventRentModify() && Residence.getRentManager().isRented(resname))
            {
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("RentedModifyDeny"));
                event.setCancelled(true);
                return;
            }
            res = Residence.getResidenceManager().getByName(resname);
        }
        else
        {
            res = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
        }
        String pname = player.getName();
        if (res != null) {
            if (res.getItemIgnoreList().isListed(mat)) {
                return;
            }
            ResidencePermissions perms = res.getPermissions();
            boolean hasbuild = perms.playerHas(pname, "build", true);
            boolean hasdestroy = perms.playerHas(pname, "destroy", hasbuild);
            if ((!hasbuild && !hasdestroy) || !hasdestroy) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
            }
        } else {
            FlagPermissions perms = Residence.getWorldFlags().getPerms(player);
            boolean hasbuild = perms.has("build", true);
            boolean hasdestroy = perms.has("destroy", hasbuild);
            if((!hasbuild && !hasdestroy) || !hasdestroy)
            {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.isCancelled())
            return;
        Player player = event.getPlayer();
        ILog.sendToPlayer(player, "onBlockPlace Fired");
        if(Residence.isResAdminOn(player);)
            return;
        Material mat = event.getBlock().getType();
        String world = event.getBlock().getWorld().getName();
        String group = Residence.getPermissionManager().getGroupNameByPlayer(player);
        if(Residence.getItemManager().isIgnored(mat, group, world))
            return;
        ClaimedResidence res;
        if(Residence.getConfigManager().enabledRentSystem())
        {
            String resname = Residence.getResidenceManager().getNameByLoc(event.getBlock().getLocation());
            if(Residence.getConfigManager().preventRentModify() && Residence.getRentManager().isRented(resname))
            {
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("RentedModifyDeny"));
                event.setCancelled(true);
                return;
            }
            res = Residence.getResidenceManager().getByName(resname);
        }
        else
        {
            res = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
        }
        String pname = player.getName();
        if (res != null) {
            if (!res.getItemBlacklist().isAllowed(mat)) {
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("ItemBlacklisted"));
                event.setCancelled(true);
                return;
            }
            ResidencePermissions perms = res.getPermissions();
            boolean hasbuild = perms.playerHas(pname, "build", true);
            boolean hasplace = perms.playerHas(pname, "place", hasbuild);
            if ((!hasbuild && !hasplace) || !hasplace) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
            }
        } else {
            FlagPermissions perms = Residence.getWorldFlags().getPerms(player);
            boolean hasbuild = perms.has("build", true);
            boolean hasplace = perms.has("place", hasbuild);
            if ((!hasbuild && !hasplace) || !hasplace) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
            }
        }
        //super.onBlockPlace(event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockSpread(BlockSpreadEvent event) {
        Location loc = event.getBlock().getLocation();
        FlagPermissions perms = Residence.getPermsByLoc(loc);
        if(!perms.has("spread", true))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
        if(res!=null)
            if(!res.getPermissions().has("piston", true))
                event.setCancelled(true);
        if(event.isSticky())
        {
            Location location = event.getBlock().getLocation().add((event.getDirection().getModX()*2), (event.getDirection().getModY()*2), (event.getDirection().getModZ()*2));
            ClaimedResidence checkRes = Residence.getResidenceManager().getByLoc(location);
            if(checkRes!=null)
            {
                if(!checkRes.getPermissions().has("piston", true))
                    event.setCancelled(true);
            }
            else
            {
                FlagPermissions worldPerms = Residence.getWorldFlags().getPerms(event.getBlock().getWorld().getName());
                if(!worldPerms.has("piston", true))
                {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
        if(res!=null)
            if(!res.getPermissions().has("piston", true))
                event.setCancelled(true);
        for (Block block : event.getBlocks()) {
            ClaimedResidence checkRes = Residence.getResidenceManager().getByLoc(block.getLocation().add(event.getDirection().getModX(),event.getDirection().getModY(),event.getDirection().getModZ()));
            if(checkRes!=null)
            {
                if(!checkRes.getPermissions().has("piston", true))
                {
                    event.setCancelled(true);
                    return;
                }
            }
            else
            {
                FlagPermissions worldPerms = Residence.getWorldFlags().getPerms(event.getBlock().getWorld().getName());
                if(!worldPerms.has("piston", true))
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.isCancelled()) {
            return;
        }
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getToBlock().getLocation());
        if (res != null) {
            boolean hasflow = res.getPermissions().has("flow", true);
            Material mat = event.getBlock().getType();
            if (mat == Material.LAVA || mat == Material.STATIONARY_LAVA) {
                if (!res.getPermissions().has("lavaflow", hasflow)) {
                    event.setCancelled(true);
                }
            } else if (mat == Material.WATER || mat == Material.STATIONARY_WATER) {
                if (!res.getPermissions().has("waterflow", hasflow)) {
                    event.setCancelled(true);
                }
            } else if(!hasflow) {
                event.setCancelled(true);
            }
        }
        else
        {
            FlagPermissions perms = Residence.getWorldFlags().getPerms(event.getBlock().getWorld().getName());
            boolean hasflow = perms.has("flow", true);
            Material mat = event.getBlock().getType();
            if (mat == Material.LAVA || mat == Material.STATIONARY_LAVA) {
                if (!perms.has("lavaflow", hasflow)) {
                    event.setCancelled(true);
                }
            } else if (mat == Material.WATER || mat == Material.STATIONARY_WATER) {
                if (!perms.has("waterflow", hasflow)) {
                    event.setCancelled(true);
                }
            } else if(!hasflow) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBurn(BlockBurnEvent event) {
        if(event.isCancelled())
            return;
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
        if(res!=null)
        {
            if(!res.getPermissions().has("firespread", true))
            {
                event.setCancelled(true);
            }
        }
        else
        {
            if (!Residence.getWorldFlags().getPerms(event.getBlock().getWorld().getName()).has("firespread", true)) {
                event.setCancelled(true);
            }
        }
        //super.onBlockBurn(event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if(event.isCancelled())
            return;
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
        IgniteCause cause = event.getCause();
        if (res != null) {
            if(cause == IgniteCause.SPREAD)
            {
                if(!res.getPermissions().has("firespread", true))
                {
                    event.setCancelled(true);
                }
            }
            else if(cause == IgniteCause.FLINT_AND_STEEL) {
                Player player = event.getPlayer();
                if (!res.getPermissions().playerHas(player.getName(), "ignite", true) && !Residence.isResAdminOn(player)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
                }
            }
            else
            {
                if(!res.getPermissions().has("ignite", true))
                {
                    event.setCancelled(true);
                }
            }
        } else {
            if(cause == IgniteCause.SPREAD)
            {
                if (!Residence.getWorldFlags().getPerms(event.getBlock().getWorld().getName()).has("firespread", true)) {
                    event.setCancelled(true);
                }
            }
            else
            {
                if(!Residence.getWorldFlags().getPerms(event.getBlock().getWorld().getName()).has("ignite", true))
                {
                    event.setCancelled(true);
                }
            }
        }
        //super.onBlockIgnite(event);
    }
/*
    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if(event.isCancelled())
            return;
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
        if (res != null) {
            if (!res.getPermissions().has("physics", true)) {
                event.getBlock().breakNaturally();
                event.setCancelled(true);
            }
        }
    }*/
}
