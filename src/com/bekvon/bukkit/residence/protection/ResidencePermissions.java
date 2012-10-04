/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.protection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceFlagChangeEvent;
import com.bekvon.bukkit.residence.event.ResidenceFlagCheckEvent;
import com.bekvon.bukkit.residence.event.ResidenceFlagEvent.FlagType;
import com.bekvon.bukkit.residence.event.ResidenceOwnerChangeEvent;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager;

/**
 *
 * @author Administrator
 */
public class ResidencePermissions extends FlagPermissions {
	protected static HashMap<String, ArrayList<String>> validFlagGroups = new HashMap<String, ArrayList<String>>();

	public static void addFlagToFlagGroup(String group, String flag) {
		if (!FlagPermissions.validFlags.contains(group) && !FlagPermissions.validAreaFlags.contains(group) && !FlagPermissions.validPlayerFlags.contains(group)) {
			if (!ResidencePermissions.validFlagGroups.containsKey(group)) {
				ResidencePermissions.validFlagGroups.put(group, new ArrayList<String>());
			}
			ArrayList<String> flags = ResidencePermissions.validFlagGroups.get(group);
			flags.add(flag);
		}
	}

	public static void removeFlagFromFlagGroup(String group, String flag) {
		if (ResidencePermissions.validFlagGroups.containsKey(group)) {
			ArrayList<String> flags = ResidencePermissions.validFlagGroups.get(group);
			flags.remove(flag);
			if (flags.isEmpty()) {
				ResidencePermissions.validFlagGroups.remove(group);
			}
		}
	}

	public static boolean flagGroupExists(String group) {
		return validFlagGroups.containsKey(group);
	}
	protected String owner;
	protected String world;
	protected ClaimedResidence residence;

	private ResidencePermissions(ClaimedResidence res) {
		residence = res;
		sql = Residence.getSQLManager();
	}

	public ResidencePermissions(ClaimedResidence res, String creator, String inworld) {
		this(res);
		owner = creator;
		world = inworld;
		sql = Residence.getSQLManager();
	}

	@Override
	public boolean playerHas(String player, String flag, boolean def) {
		return this.playerHas(player, world, flag, def);
	}

	public boolean playerHas(String player, String world, String flag, boolean def) {
		ResidenceFlagCheckEvent fc = new ResidenceFlagCheckEvent(residence,flag,FlagType.PLAYER,player,def);
		Residence.getServ().getPluginManager().callEvent(fc);
		if(fc.isOverriden()) {
			return fc.getOverrideValue();
		}
		boolean value = sql.getResPlayerFlag(player, flag, residence.getId());
		if(value != (Boolean)null){
			return value;
		}
		return def;
	}

	@Override
	public boolean groupHas(String group, String flag, boolean def) {
		ResidenceFlagCheckEvent fc = new ResidenceFlagCheckEvent(residence,flag,FlagType.GROUP,group,def);
		Residence.getServ().getPluginManager().callEvent(fc);
		if(fc.isOverriden()) {
			return fc.getOverrideValue();
		}
		return super.groupHas(group, flag, def);
	}

	@Override
	public boolean has(String flag, boolean def) {
		ResidenceFlagCheckEvent fc = new ResidenceFlagCheckEvent(residence,flag,FlagType.RESIDENCE,null,def);
		Residence.getServ().getPluginManager().callEvent(fc);
		if(fc.isOverriden()) {
			return fc.getOverrideValue();
		}
		boolean value = sql.getResidenceFlag(flag, residence.getId());
		if(value != (Boolean)null){
			return value;
		}
		if(parent!=null) {
			return parent.has(flag, def);
		}
		return def;
	}

	public void applyTemplate(Player player, FlagPermissions list, boolean resadmin) {
		if(player!=null) {
			if(!player.getName().equals(owner) && !resadmin) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
				return;
			}
		} else {
			resadmin = true;
		}
		PermissionGroup group = Residence.getPermissionManager().getGroup(owner,world);
		for(Entry<String, Boolean> flag : list.getAreaFlags().entrySet()) {
			if(group.hasFlagAccess(flag.getKey()) || resadmin) {
				this.setFlag(flag.getKey(), FlagState.valueOf(flag.getValue().toString()));
			} else {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagSetDeny", ChatColor.YELLOW+flag.getKey() + ChatColor.RED));
			}
		}
		for(Entry<String, Map<String, Boolean>> plists : list.getPlayerFlags().entrySet()) {
			for(Entry<String, Boolean> flag : plists.getValue().entrySet()) {
				if(group.hasFlagAccess(flag.getKey()) || resadmin) {
					setPlayerFlag(plists.getKey(), flag.getKey(), stringToFlagState(flag.getValue().toString()));
				} else {
					player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagSetDeny", ChatColor.YELLOW+flag.getKey() + ChatColor.RED));
				}
			}
		}
		for(Entry<String, Map<String, Boolean>> glists : list.getGroupFlags().entrySet()) {
			for(Entry<String, Boolean> flag : glists.getValue().entrySet()) {
				if(group.hasFlagAccess(flag.getKey()) || resadmin) {
					setGroupFlag(glists.getKey(), flag.getKey(), stringToFlagState(flag.getValue().toString()));
				} else {
					player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("FlagSetDeny", ChatColor.YELLOW+flag.getKey() + ChatColor.RED));
				}
			}
		}
		if(player!=null) {
			player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("PermissionsApply"));
		}
	}

	public boolean hasResidencePermission(Player player, boolean requireOwner) {
		if(Residence.getConfigManager().enabledRentSystem()) {
			String resname = residence.getName();
			if(Residence.getRentManager().isRented(resname)) {
				if(requireOwner) {
					return false;
				}
				String renter = Residence.getRentManager().getRentingPlayer(resname);
				if(player.getName().equalsIgnoreCase(renter)) {
					return true;
				} else {
					return playerHas(player.getName(), "admin",false);
				}
			}
		}
		if(requireOwner) {
			return owner.equalsIgnoreCase(player.getName());
		}
		return playerHas(player.getName(), "admin",false) || owner.equalsIgnoreCase(player.getName());
	}

	private boolean checkCanSetFlag(Player player, String flag, FlagState state, boolean globalflag, boolean resadmin) {
		if(!checkValidFlag(flag,globalflag)) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidFlag"));
			return false;
		}
		if(state == FlagState.INVALID) {
			player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("InvalidFlagState"));
			return false;
		}
		if(!resadmin) {
			if(!this.hasResidencePermission(player,false)) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("NoPermission"));
				return false;
			}
			if(!hasFlagAccess(owner, flag)) {
				player.sendMessage(ChatColor.RED+Residence.getLanguage().getPhrase("OwnerNoPermission"));
				return false;
			}
		}
		return true;
	}

	private boolean hasFlagAccess(String player, String flag) {
		PermissionGroup group = Residence.getPermissionManager().getGroup(player,world);
		return group.hasFlagAccess(flag);
	}

	public boolean setPlayerFlag(Player player, String targetPlayer, String flag, String flagstate, boolean resadmin) {
		if(validFlagGroups.containsKey(flag)) {
			return this.setFlagGroupOnPlayer(player, targetPlayer, flag, flagstate, resadmin);
		}
		FlagState state = FlagPermissions.stringToFlagState(flagstate);
		if (checkCanSetFlag(player, flag, state, false, resadmin)) {
			ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, player, flag, ResidenceFlagChangeEvent.FlagType.PLAYER, state, targetPlayer);
			Residence.getServ().getPluginManager().callEvent(fc);
			if (fc.isCancelled()) {
				return false;
			}
			if(setPlayerFlag(targetPlayer, flag, state)) {
				player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("FlagSet"));
				return true;
			}
		}
		return false;
	}
	public boolean setPlayerFlag(String player, ClaimedResidence res, String flag, FlagState state) {
		player = player.toLowerCase();
		if (state == FlagState.FALSE) {
			sql.setPlayerResFlag(player, res.getName(), flag, false);
		} else if (state == FlagState.TRUE) {
			sql.setPlayerResFlag(player, res.getName(), flag, true);
		} else if (state == FlagState.NEITHER) {
			sql.setPlayerResFlag(player, res.getName(), flag, (Boolean)null);
		}
		if(sql.getPlayerFlagsByResidence(player, res.getId()) == null) {
			sql.alterResPlayer(player, res.getName(), false);
		}
		return true;
	}
	public boolean setGroupFlag(Player player, String group, String flag, String flagstate, boolean resadmin) {
		group = group.toLowerCase();
		if(validFlagGroups.containsKey(flag)) {
			return this.setFlagGroupOnGroup(player, flag, group, flagstate, resadmin);
		}
		FlagState state = FlagPermissions.stringToFlagState(flagstate);
		if (checkCanSetFlag(player, flag, state, false, resadmin)) {
			if (Residence.getPermissionManager().hasGroup(group)) {
				ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, player, flag, ResidenceFlagChangeEvent.FlagType.GROUP, state, group);
				Residence.getServ().getPluginManager().callEvent(fc);
				if (fc.isCancelled()) {
					return false;
				}
				if(setGroupFlag(group, flag, state)) {
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

	public boolean setGroupFlag(String group, ClaimedResidence res, String flag, FlagState state) {
		group = group.toLowerCase();
		if (state == FlagState.FALSE) {
			sql.setGroupResFlag(group, res.getName(), flag, false);
		} else if (state == FlagState.TRUE) {
			sql.setGroupResFlag(group, res.getName(), flag, true);
		} else if (state == FlagState.NEITHER) {
			sql.setGroupResFlag(group, res.getName(), flag, (Boolean)null);
		}
		if(sql.getGroupFlagsByResidence(group, res.getId()) == null) {
			sql.alterResGroup(group, res.getName(), false);
		}
		return true;
	}

	public boolean setFlag(Player player, String flag, String flagstate, boolean resadmin) {
		if(validFlagGroups.containsKey(flag)) {
			return this.setFlagGroup(player, flag, flagstate, resadmin);
		}
		FlagState state = FlagPermissions.stringToFlagState(flagstate);
		if (checkCanSetFlag(player, flag, state, true, resadmin)) {
			ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence,player,flag,ResidenceFlagChangeEvent.FlagType.RESIDENCE,state,null);
			Residence.getServ().getPluginManager().callEvent(fc);
			if(fc.isCancelled()) {
				return false;
			}
			if(setFlag(flag, state)) {
				player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("FlagSet"));
				return true;
			}
		}
		return false;
	}

	public boolean setFlag(String flag, ClaimedResidence res, FlagState state) {
		if (state == FlagState.FALSE) {
			sql.setRegionResFlag(res.getName(), flag, false);
		} else if (state == FlagState.TRUE) {
			sql.setRegionResFlag(res.getName(), flag, true);
		} else if (state == FlagState.NEITHER) {
			sql.setRegionResFlag(res.getName(), flag, (Boolean)null);
		}
		return true;
	}

	public boolean removeAllPlayerFlags(Player player, String targetPlayer, boolean resadmin) {
		if (this.hasResidencePermission(player, false)) {
			ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, player, "ALL", ResidenceFlagChangeEvent.FlagType.RESIDENCE, FlagState.NEITHER, null);
			Residence.getServ().getPluginManager().callEvent(fc);
			if (fc.isCancelled()) {
				return false;
			}
			sql.alterResPlayer(targetPlayer, residence.getName(), false);
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
			sql.alterResGroup(group, residence.getName(), false);
			player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("FlagSet"));
			return true;
		}
		return false;
	}

	public void removeAllPlayerFlags(String player) {
		sql.alterResPlayer(player, residence.getName(), false);
	}

	@Override
	public void removeAllGroupFlags(String group) {
		sql.alterResGroup(group, residence.getName(), false);
	}

	@Override
	public boolean setFlag(String flag, FlagState state) {
		ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, null,flag,ResidenceFlagChangeEvent.FlagType.RESIDENCE,state,null);
		Residence.getServ().getPluginManager().callEvent(fc);
		if(fc.isCancelled()) {
			return false;
		}
		return setFlag(flag, residence, state);
	}

	@Override
	public boolean setGroupFlag(String group, String flag, FlagState state) {
		ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, null,flag,ResidenceFlagChangeEvent.FlagType.GROUP,state,group);
		Residence.getServ().getPluginManager().callEvent(fc);
		if(fc.isCancelled()) {
			return false;
		}
		return setGroupFlag(group, residence, flag, state);
	}

	@Override
	public boolean setPlayerFlag(String player, String flag, FlagState state) {
		ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, null,flag,ResidenceFlagChangeEvent.FlagType.PLAYER,state, player);
		Residence.getServ().getPluginManager().callEvent(fc);
		if(fc.isCancelled()) {
			return false;
		}
		return setPlayerFlag(player, residence, flag,state);
	}

	public void applyDefaultFlags(Player player, boolean resadmin) {
		if(this.hasResidencePermission(player, true) || resadmin) {
			this.applyDefaultFlags();
			player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("FlagsDefault"));
		} else {
			player.sendMessage(ChatColor.GREEN+Residence.getLanguage().getPhrase("NoPermission"));
		}
	}

	public void applyDefaultFlags() {
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
		for (Entry<String, Map<String, Boolean>> entry : dgflags) {
			Map<String, Boolean> value = entry.getValue();
			for(Entry<String, Boolean> flag : value.entrySet()) {
				if(flag.getValue()) {
					this.setGroupFlag(entry.getKey(), flag.getKey(), FlagState.TRUE);
				} else {
					this.setGroupFlag(entry.getKey(), flag.getKey(), FlagState.FALSE);
				}
			}
		}
	}

	public void setOwner(String newOwner, boolean resetFlags) {
		ResidenceOwnerChangeEvent ownerchange = new ResidenceOwnerChangeEvent(residence,newOwner);
		Residence.getServ().getPluginManager().callEvent(ownerchange);
		owner = newOwner;
		if(resetFlags) {
			this.applyDefaultFlags();
		}
	}

	public String getOwner() {
		return owner;
	}

	public void applyGlobalDefaults() {
		this.clearFlags();
		for(Entry<String, Boolean> entry : Residence.getConfigManager().getGlobalResidenceDefaultFlags().entrySet()) {
			if(entry.getValue()) {
				this.setFlag(entry.getKey(), FlagState.TRUE);
			} else {
				this.setFlag(entry.getKey(), FlagState.FALSE);
			}
		}
		for(Entry<String, Boolean> entry : Residence.getConfigManager().getGlobalCreatorDefaultFlags().entrySet()) {
			if(entry.getValue()) {
				this.setPlayerFlag(owner, entry.getKey(), FlagState.TRUE);
			} else {
				this.setPlayerFlag(owner, entry.getKey(), FlagState.FALSE);
			}
		}
		for(Entry<String, Map<String, Boolean>> entry : Residence.getConfigManager().getGlobalGroupDefaultFlags().entrySet()) {
			for(Entry<String, Boolean> flag : entry.getValue().entrySet()) {
				if(flag.getValue()) {
					this.setGroupFlag(entry.getKey(), flag.getKey(), FlagState.TRUE);
				} else {
					this.setGroupFlag(entry.getKey(), flag.getKey(), FlagState.FALSE);
				}
			}
		}
	}

	@Override
	public void clearFlags() {
		sql.clearFlags(residence);
	}

	public void clearPlayersFlags(String user) {
		sql.clearPlayerFlags(residence, user);
	}
	public boolean setFlagGroup(Player player, String flaggroup, String state, boolean resadmin) {
		if (ResidencePermissions.validFlagGroups.containsKey(flaggroup)) {
			ArrayList<String> flags = ResidencePermissions.validFlagGroups.get(flaggroup);
			boolean changed = false;
			for (String flag : flags) {
				if (this.setFlag(player, flag, state, resadmin)) {
					changed = true;
				}
			}
			return changed;
		}
		return false;
	}

	public boolean setFlagGroupOnGroup(Player player, String flaggroup, String group, String state, boolean resadmin) {
		if (ResidencePermissions.validFlagGroups.containsKey(flaggroup)) {
			ArrayList<String> flags = ResidencePermissions.validFlagGroups.get(flaggroup);
			boolean changed = false;
			for (String flag : flags) {
				if (this.setGroupFlag(player, group, flag, state, resadmin)) {
					changed = true;
				}
			}
			return changed;
		}
		return false;
	}

	public boolean setFlagGroupOnPlayer(Player player, String target, String flaggroup, String state, boolean resadmin) {
		if (ResidencePermissions.validFlagGroups.containsKey(flaggroup)) {
			ArrayList<String> flags = ResidencePermissions.validFlagGroups.get(flaggroup);
			boolean changed = false;
			for (String flag : flags) {
				if (this.setPlayerFlag(player, target, flag, state, resadmin)) {
					changed = true;
				}
			}
			return changed;
		}
		return false;
	}
	@Override
	public Map<String, Boolean> getAreaFlags() {
		return sql.getAreaFlagsByRes(residence.getId());
	}
	@Override
	public Map<String, Map<String, Boolean>> getPlayerFlags() {
		return sql.getAllPlayerFlagsByRes(residence.getId());
	}
	@Override
	public Map<String, Map<String, Boolean>> getGroupFlags() {
		return sql.getAllGroupFlagsByRes(residence.getId());
	}
	public void printFlags(Player player) {
		player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Flags")+":"+ChatColor.BLUE+" " + listFlags());
		player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Your.Flags")+":"+ChatColor.GREEN+" " + listPlayerFlags(player.getName()));
		player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Group.Flags")+":"+ChatColor.RED+" " + listGroupFlags());
		player.sendMessage(ChatColor.YELLOW+Residence.getLanguage().getPhrase("Others.Flags")+":"+ChatColor.RED+" " + listOtherPlayersFlags(player.getName()));
	}

	public void copyUserPermissions(String fromUser, String toUser) {
		fromUser = fromUser.toLowerCase();
		toUser = toUser.toLowerCase();
		Map<String, Boolean> get = sql.getPlayerFlagsByResidence(fromUser, residence.getId());
		if(get != null) {
			for(Entry<String, Boolean> entry : get.entrySet()){
				this.setPlayerFlag(toUser, entry.getKey(), stringToFlagState(entry.getValue().toString()));
			}
		}
	}
}
