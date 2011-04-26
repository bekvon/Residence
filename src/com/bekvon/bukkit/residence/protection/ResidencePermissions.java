/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class ResidencePermissions extends PermissionList {
    protected String owner;
    protected String world;

    private ResidencePermissions()
    {
        
    }

    public ResidencePermissions(String creator, String inworld)
    {
        owner = creator;
        world = inworld.toLowerCase();
    }

    public boolean playerHas(String player, String flag, boolean def)
    {
        String group = Residence.getPermissionManager().getGroupNameByPlayer(player, world);
        if(playerFlags.containsKey(player))
        {
            Map<String, Boolean> pmap = playerFlags.get(player);
            if(pmap.containsKey(flag))
                return pmap.get(flag);
        }
        if(groupFlags.containsKey(group))
        {
            Map<String, Boolean> gmap = groupFlags.get(group);
            if(gmap.containsKey(flag))
                return gmap.get(flag);
        }
        return has(flag,def);
    }

    public void applyTemplate(Player player, PermissionList list)
    {

        boolean resadmin;
        if(player!=null)
        {
            resadmin = Residence.getPermissionManager().isResidenceAdmin(player);
            if(!player.getName().equals(owner) && !resadmin)
            {
                player.sendMessage("§cOnly the residence owner can apply permission lists.");
                return;
            }
        }
        else
        {
            resadmin = true;
        }
        PermissionGroup group = Residence.getPermissionManager().getGroup(owner,world);
        for(Entry<String, Boolean> flag : list.cuboidFlags.entrySet())
        {
            if(group.hasFlagAccess(flag.getKey()) || resadmin)
            {
                this.cuboidFlags.put(flag.getKey(), flag.getValue());
            }
            else
            {
                player.sendMessage("§cError, no permission to apply flag (" + flag.getKey() + ") to this residence.");
            }
        }
        for(Entry<String, Map<String, Boolean>> plists : list.playerFlags.entrySet())
        {
            for(Entry<String, Boolean> flag : plists.getValue().entrySet())
            {
                if(group.hasFlagAccess(flag.getKey()) || resadmin)
                {
                    if(!this.playerFlags.containsKey(plists.getKey()))
                        this.playerFlags.put(plists.getKey(), Collections.synchronizedMap(new HashMap<String,Boolean>()));
                    this.playerFlags.get(plists.getKey()).put(flag.getKey(), flag.getValue());
                }
                else
                {
                    player.sendMessage("§cError, no permission to apply flag (" + flag.getKey() + ") to this residence.");
                }
            }
        }
        for(Entry<String, Map<String, Boolean>> glists : list.groupFlags.entrySet())
        {
            for(Entry<String, Boolean> flag : glists.getValue().entrySet())
            {
                if(group.hasFlagAccess(flag.getKey()) || resadmin)
                {
                    if(!this.groupFlags.containsKey(glists.getKey()))
                        this.groupFlags.put(glists.getKey(), Collections.synchronizedMap(new HashMap<String,Boolean>()));
                    this.groupFlags.get(glists.getKey()).put(flag.getKey(), flag.getValue());
                }
                else
                {
                    player.sendMessage("§cError, no permission to apply flag (" + flag.getKey() + ") to this residence.");
                }
            }
        }
        if(player!=null)
            player.sendMessage("§cPermissions list applied to residence...");
    }

    public boolean hasResidencePermission(Player player, boolean requireOwner)
    {
        if(this.hasResidenceAdmin(player))
            return true;
        if(requireOwner)
            return(owner.equalsIgnoreCase(player.getName()));
        return (playerHas(player.getName(), "admin",false) || owner.equalsIgnoreCase(player.getName()));
    }

    private boolean hasResidenceAdmin(Player player)
    {
        return Residence.getPermissionManager().hasAuthority(player, "residence.admin", player.isOp());
    }

    private boolean checkCanSetFlag(Player player, String flag, FlagState state, boolean globalflag)
    {
        if(!checkValidFlag(flag,globalflag))
        {
            player.sendMessage("§cInvalid flag.");
            return false;
        }
        if(state == FlagState.INVALID)
        {
            player.sendMessage("§cInvalid flag state, must be true(t), false(f), or remove(r).");
            return false;
        }
        if(!Residence.getPermissionManager().isResidenceAdmin(player))
        {
            if(!this.hasResidencePermission(player,false))
            {
                player.sendMessage("§cYou dont have permission to do this.");
                return false;
            }
            if(!hasFlagAccess(owner, flag))
            {
                player.sendMessage("§cThe residence owner does not have access to this flag.");
                return false;
            }
        }
        return true;
    }

    private boolean hasFlagAccess(String player, String flag)
    {
        PermissionGroup group = Residence.getPermissionManager().getGroup(player,world);
        return group.hasFlagAccess(flag);
    }

    public void setPlayerFlag(Player player, String targetPlayer, String flag, String flagstate) {
        FlagState state = PermissionList.stringToFlagState(flagstate);
        if (checkCanSetFlag(player, flag, state, false)) {
            this.setPlayer(targetPlayer, flag, state);
            player.sendMessage("§aFlag Set.");
        }
    }

    public void setGroupFlag(Player player, String group, String flag, String flagstate) {
        group = group.toLowerCase();
        FlagState state = PermissionList.stringToFlagState(flagstate);
        if (checkCanSetFlag(player, flag, state, false)) {
            if (Residence.getPermissionManager().hasGroup(group)) {
                this.setGroup(group, flag, state);
                player.sendMessage("§aFlag Set.");
            } else {
                player.sendMessage("§cGroup does not exist.");
            }
        }
    }

    public void setFlag(Player player, String flag, String flagstate) {
        FlagState state = PermissionList.stringToFlagState(flagstate);
        if (checkCanSetFlag(player, flag, state, true)) {
            this.set(flag, state);
            player.sendMessage("§aFlag Set.");
        }
    }

    public void applyDefaultFlags(Player player)
    {
        if(this.hasResidencePermission(player, true))
        {
            this.applyDefaultFlags();
            player.sendMessage("Reset flags to default...");
        }
        else
            player.sendMessage("§cYou dont have permisssion.");
    }

    public void applyDefaultFlags()
    {
        PermissionManager gm = Residence.getPermissionManager();
        PermissionGroup group = gm.getGroup(owner, world);
        Set<Entry<String, Boolean>> dflags = group.getDefaultResidenceFlags();
        Set<Entry<String, Boolean>> dcflags = group.getDefaultCreatorFlags();
        Set<Entry<String, Map<String, Boolean>>> dgflags = group.getDefaultGroupFlags();
        this.clearFlags();
        for (Entry<String, Boolean> next : dflags) {
            if (this.checkValidFlag(next.getKey(), true)) {
                if (next.getValue()) {
                    this.set(next.getKey(), FlagState.TRUE);
                } else {
                    this.set(next.getKey(), FlagState.FALSE);
                }
            }
        }
        for (Entry<String, Boolean> next : dcflags) {
            if (this.checkValidFlag(next.getKey(), false)) {
                if (next.getValue()) {
                    this.setPlayer(owner, next.getKey(), FlagState.TRUE);
                } else {
                    this.setPlayer(owner, next.getKey(), FlagState.FALSE);
                }
            }
        }
        for (Entry<String, Map<String, Boolean>> entry : dgflags)
        {
            Map<String, Boolean> value = entry.getValue();
            for(Entry<String, Boolean> flag : value.entrySet())
            {
                if(flag.getValue())
                {
                    this.setGroup(entry.getKey(), flag.getKey(), FlagState.TRUE);
                }
                else
                {
                    this.setGroup(entry.getKey(), flag.getKey(), FlagState.FALSE);
                }
            }
        }
    }

    public void setOwner(String newOwner, boolean resetFlags)
    {
        owner = newOwner;
        if(resetFlags)
            this.applyDefaultFlags();
    }

    public String getOwner()
    {
        return owner;
    }

    public String getWorld()
    {
        return world;
    }

    @Override
    public Map<String, Object> save() {
        Map<String, Object> root = super.save();
        root.put("Owner", owner);
        root.put("World", world);
        return root;
    }

    public static ResidencePermissions load(Map<String, Object> root) throws Exception {
        ResidencePermissions newperms = new ResidencePermissions();
        newperms.owner = (String) root.get("Owner");
        newperms.world = (String) root.get("World");
        newperms.playerFlags = (Map) root.get("PlayerFlags");
        newperms.groupFlags = (Map) root.get("GroupFlags");
        newperms.cuboidFlags = (Map) root.get("AreaFlags");
        if(newperms.owner==null||newperms.world==null||newperms.playerFlags==null||newperms.groupFlags==null||newperms.cuboidFlags==null)
            throw new Exception("Invalid Residence Permissions...");
        return newperms;
    }
}
