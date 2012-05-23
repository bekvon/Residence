/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.protection;
import org.bukkit.ChatColor;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceFlagChangeEvent;
import com.bekvon.bukkit.residence.event.ResidenceFlagCheckEvent;
import com.bekvon.bukkit.residence.event.ResidenceFlagEvent.FlagType;
import com.bekvon.bukkit.residence.event.ResidenceOwnerChangeEvent;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class ResidencePermissions extends FlagPermissions {
    protected String owner;
    protected String world;
    protected ClaimedResidence residence;

    private ResidencePermissions(ClaimedResidence res)
    {
        residence = res;
    }

    public ResidencePermissions(ClaimedResidence res, String creator, String inworld)
    {
        this(res);
        owner = creator;
        world = inworld;
    }

    public boolean playerHas(String player, String flag, boolean def)
    {
        return this.playerHas(player, world, flag, def);
    }

    @Override
    public boolean playerHas(String player, String world, String flag, boolean def) {
        ResidenceFlagCheckEvent fc = new ResidenceFlagCheckEvent(residence,flag,FlagType.PLAYER,player,def);
        Residence.getServ().getPluginManager().callEvent(fc);
        if(fc.isOverriden())
            return fc.getOverrideValue();
        return super.playerHas(player, world, flag, def);
    }

    @Override
    public boolean groupHas(String group, String flag, boolean def) {
        ResidenceFlagCheckEvent fc = new ResidenceFlagCheckEvent(residence,flag,FlagType.GROUP,group,def);
        Residence.getServ().getPluginManager().callEvent(fc);
        if(fc.isOverriden())
            return fc.getOverrideValue();
        return super.groupHas(group, flag, def);
    }

    @Override
    public boolean has(String flag, boolean def) {
        ResidenceFlagCheckEvent fc = new ResidenceFlagCheckEvent(residence,flag,FlagType.RESIDENCE,null,def);
        Residence.getServ().getPluginManager().callEvent(fc);
        if(fc.isOverriden())
            return fc.getOverrideValue();
        return super.has(flag, def);
    }


    public boolean hasApplicableFlag(String player, String flag)
    {
        return super.inheritanceIsPlayerSet(player,flag) || super.inheritanceIsGroupSet(Residence.getPermissionManager().getGroupNameByPlayer(player,world),flag) || super.inheritanceIsSet(flag);
    }

    public void applyTemplate(Player player, FlagPermissions list, boolean resadmin)
    {
        if(player!=null)
        {
            if(!player.getName().equals(owner) && !resadmin)
            {
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
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
                    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagSetDeny", ChatColor.YELLOW+flag.getKey() + ChatColor.RED));
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
                    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagSetDeny", ChatColor.YELLOW+flag.getKey() + ChatColor.RED));
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
                    player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagSetDeny", ChatColor.YELLOW+flag.getKey() + ChatColor.RED));
                }
            }
        }
        if(player!=null)
            player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("PermissionsApply"));
    }

    public boolean hasResidencePermission(Player player, boolean requireOwner)
    {
        if(Residence.getConfigManager().enabledRentSystem())
        {
            String resname = residence.getName();
            if(Residence.getRentManager().isRented(resname))
            {
                if(requireOwner)
                    return false;
                String renter = Residence.getRentManager().getRentingPlayer(resname);
                if(player.getName().equalsIgnoreCase(renter))
                    return true;
                else
                    return (playerHas(player.getName(), "admin",false));
            }
        }
        if(requireOwner)
            return(owner.equalsIgnoreCase(player.getName()));
        return (playerHas(player.getName(), "admin",false) || owner.equalsIgnoreCase(player.getName()));
    }

    private boolean checkCanSetFlag(Player player, String flag, FlagState state, boolean globalflag, boolean resadmin)
    {
        if(!checkValidFlag(flag,globalflag))
        {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidFlag"));
            return false;
        }
        if(state == FlagState.INVALID)
        {
            player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidFlagState"));
            return false;
        }
        if(!resadmin)
        {
            if(!this.hasResidencePermission(player,false))
            {
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
                return false;
            }
            if(!hasFlagAccess(owner, flag))
            {
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("OwnerNoPermission"));
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

    public boolean setPlayerFlag(Player player, String targetPlayer, String flag, String flagstate, boolean resadmin) {
        FlagState state = FlagPermissions.stringToFlagState(flagstate);
        if (checkCanSetFlag(player, flag, state, false, resadmin)) {
            ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, player, flag, ResidenceFlagChangeEvent.FlagType.PLAYER, state, targetPlayer);
            Residence.getServ().getPluginManager().callEvent(fc);
            if (fc.isCancelled())
                return false;
            if(super.setPlayerFlag(targetPlayer, flag, state))
            {
                player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("FlagSet"));
                return true;
            }
        }
        return false;
    }

    public boolean setGroupFlag(Player player, String group, String flag, String flagstate, boolean resadmin) {
        group = group.toLowerCase();
        FlagState state = FlagPermissions.stringToFlagState(flagstate);
        if (checkCanSetFlag(player, flag, state, false, resadmin)) {
            if (Residence.getPermissionManager().hasGroup(group)) {
                ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, player, flag, ResidenceFlagChangeEvent.FlagType.GROUP, state, group);
                Residence.getServ().getPluginManager().callEvent(fc);
                if (fc.isCancelled())
                    return false;
                if(super.setGroupFlag(group, flag, state))
                {
                    player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("FlagSet"));
                    return true;
                }
            } else {
                player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidGroup"));
                return false;
            }
        }
        return false;
    }

    public boolean setFlag(Player player, String flag, String flagstate, boolean resadmin) {
        FlagState state = FlagPermissions.stringToFlagState(flagstate);
        if (checkCanSetFlag(player, flag, state, true, resadmin)) {
            ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence,player,flag,ResidenceFlagChangeEvent.FlagType.RESIDENCE,state,null);
            Residence.getServ().getPluginManager().callEvent(fc);
            if(fc.isCancelled())
                return false;
            if(super.setFlag(flag, state))
            {
                player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("FlagSet"));
                return true;
            }
        }
        return false;
    }
    
    public boolean removeAllPlayerFlags(Player player, String targetPlayer, boolean resadmin) {
        if (this.hasResidencePermission(player, false)) {
            ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, player, "ALL", ResidenceFlagChangeEvent.FlagType.RESIDENCE, FlagState.NEITHER, null);
            Residence.getServ().getPluginManager().callEvent(fc);
            if (fc.isCancelled()) {
                return false;
            }
            super.removeAllPlayerFlags(targetPlayer);
            player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("FlagSet"));
            return true;
        }
        return false;
    }

    public boolean removeAllGroupFlags(Player player, String group, boolean resadmin) {
        if (this.hasResidencePermission(player, false)) {
            ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, player, "ALL", ResidenceFlagChangeEvent.FlagType.GROUP, FlagState.NEITHER, null);
            Residence.getServ().getPluginManager().callEvent(fc);
            if (fc.isCancelled()) {
                return false;
            }
            super.removeAllGroupFlags(group);
            player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("FlagSet"));
            return true;
        }
        return false;
    }


    @Override
    public boolean setFlag(String flag, FlagState state) {
        ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, null,flag,ResidenceFlagChangeEvent.FlagType.RESIDENCE,state,null);
        Residence.getServ().getPluginManager().callEvent(fc);
        if(fc.isCancelled())
            return false;
        return super.setFlag(flag, state);
    }

    @Override
    public boolean setGroupFlag(String group, String flag, FlagState state) {
        ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, null,flag,ResidenceFlagChangeEvent.FlagType.GROUP,state,group);
        Residence.getServ().getPluginManager().callEvent(fc);
        if(fc.isCancelled())
            return false;
        return super.setGroupFlag(group, flag, state);
    }

    @Override
    public boolean setPlayerFlag(String player, String flag, FlagState state) {
        ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, null,flag,ResidenceFlagChangeEvent.FlagType.PLAYER,state, player);
        Residence.getServ().getPluginManager().callEvent(fc);
        if(fc.isCancelled())
            return false;
        return super.setPlayerFlag(player,flag,state);
    }

    public void applyDefaultFlags(Player player, boolean resadmin)
    {
        if(this.hasResidencePermission(player, true) || resadmin)
        {
            this.applyDefaultFlags();
            player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("FlagsDefault")); 
        }
        else
            player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("NoPermission"));
    }

    public void applyDefaultFlags()
    {
        PermissionManager gm = Residence.getPermissionManager();
        PermissionGroup group = gm.getGroup(owner, world);
        Set<Entry<String, Boolean>> dflags = group.getDefaultResidenceFlags();
        Set<Entry<String, Boolean>> dcflags = group.getDefaultCreatorFlags();
        Set<Entry<String, Map<String, Boolean>>> dgflags = group.getDefaultGroupFlags();
        this.applyGlobalDefaults();
        for (Entry<String, Boolean> next : dflags) {
            if (this.checkValidFlag(next.getKey(), true)) {
                if (next.getValue()) {
                    this.setFlag(next.getKey(), FlagState.TRUE);
                } else {
                    this.setFlag(next.getKey(), FlagState.FALSE);
                }
            }
        }
        for (Entry<String, Boolean> next : dcflags) {
            if (this.checkValidFlag(next.getKey(), false)) {
                if (next.getValue()) {
                    this.setPlayerFlag(owner, next.getKey(), FlagState.TRUE);
                } else {
                    this.setPlayerFlag(owner, next.getKey(), FlagState.FALSE);
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
                    this.setGroupFlag(entry.getKey(), flag.getKey(), FlagState.TRUE);
                }
                else
                {
                    this.setGroupFlag(entry.getKey(), flag.getKey(), FlagState.FALSE);
                }
            }
        }
    }

    public void setOwner(String newOwner, boolean resetFlags)
    {
        ResidenceOwnerChangeEvent ownerchange = new ResidenceOwnerChangeEvent(residence,newOwner);
        Residence.getServ().getPluginManager().callEvent(ownerchange);
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

    public static ResidencePermissions load(ClaimedResidence res, Map<String, Object> root) throws Exception {
        ResidencePermissions newperms = new ResidencePermissions(res);
        newperms.owner = (String) root.get("Owner");
        newperms.world = (String) root.get("World");
        FlagPermissions.load(root, newperms);
        if(newperms.owner==null||newperms.world==null||newperms.playerFlags==null||newperms.groupFlags==null||newperms.cuboidFlags==null)
            throw new Exception("Invalid Residence Permissions...");
        newperms.fixNames();
        return newperms;
    }

    public void fixNames()
    {
        ArrayList<String> fixNames = new ArrayList<String>();
        Iterator<Entry<String, Map<String, Boolean>>> it = playerFlags.entrySet().iterator();
        while(it.hasNext())
        {
            String name = it.next().getKey();
            if(!name.equals(name.toLowerCase()))
            {
                fixNames.add(name);
            }
        }
        for(String name : fixNames)
        {
            Map<String, Boolean> get = playerFlags.get(name);
            playerFlags.remove(name);
            playerFlags.put(name.toLowerCase(), get);
        }
    }

    public void applyGlobalDefaults()
    {
        this.clearFlags();
        FlagPermissions gRD = Residence.getConfigManager().getGlobalResidenceDefaultFlags();
        FlagPermissions gCD = Residence.getConfigManager().getGlobalCreatorDefaultFlags();
        Map<String, FlagPermissions> gGD = Residence.getConfigManager().getGlobalGroupDefaultFlags();
        for(Entry<String, Boolean> entry : gRD.cuboidFlags.entrySet())
        {
            if(entry.getValue())
                this.setFlag(entry.getKey(), FlagState.TRUE);
            else
                this.setFlag(entry.getKey(), FlagState.FALSE);
        }
        for(Entry<String, Boolean> entry : gCD.cuboidFlags.entrySet())
        {
            if(entry.getValue())
                this.setPlayerFlag(owner, entry.getKey(), FlagState.TRUE);
            else
                this.setPlayerFlag(owner, entry.getKey(), FlagState.FALSE);
        }
        for(Entry<String, FlagPermissions> entry : gGD.entrySet())
        {
            for(Entry<String, Boolean> flag : entry.getValue().cuboidFlags.entrySet())
            {
                if(flag.getValue())
                    this.setGroupFlag(entry.getKey(), flag.getKey(), FlagState.TRUE);
                else
                    this.setGroupFlag(entry.getKey(), flag.getKey(), FlagState.FALSE);
            }
        }
    }
}
