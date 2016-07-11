package com.bekvon.bukkit.residence.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidenceManager;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;

public class ResidencePlayer {

    private String userName = null;
    private Player player = null;
    private OfflinePlayer ofPlayer = null;

    private Map<String, ClaimedResidence> ResidenceList = new HashMap<String, ClaimedResidence>();
    private ClaimedResidence mainResidence = null;
    private int currentRes = -1;

    private PermissionGroup group = null;

    private int maxRes = -1;
    private int maxRents = -1;
    private int maxSubzones = -1;

    public ResidencePlayer(Player player) {
	this.player = player;
	updateName();
	RecalculatePermissions();
    }

    public ResidencePlayer(OfflinePlayer player) {
	this.ofPlayer = player;
	updateName();
	RecalculatePermissions();
    }

    public ResidencePlayer(String userName) {
	this.userName = userName;
	RecalculatePermissions();
    }

    public void setMainResidence(ClaimedResidence res) {
	if (mainResidence != null)
	    mainResidence.setMainResidence(false);
	mainResidence = res;
    }

    public ClaimedResidence getMainResidence() {
	if (mainResidence == null) {
	    for (Entry<String, ClaimedResidence> one : ResidenceList.entrySet()) {
		if (one.getValue() == null)
		    continue;
		if (one.getValue().isMainResidence()) {
		    mainResidence = one.getValue();
		    return mainResidence;
		}
	    }
	    for (String one : Residence.getRentManager().getRentedLands(this.player.getName())) {
		ClaimedResidence res = Residence.getResidenceManager().getByName(one);
		if (res != null) {
		    mainResidence = res;
		    return mainResidence;
		}
	    }
	    for (Entry<String, ClaimedResidence> one : ResidenceList.entrySet()) {
		if (one.getValue() == null)
		    continue;
		mainResidence = one.getValue();
		return mainResidence;
	    }
	}
	return mainResidence;
    }

    public void RecalculatePermissions() {
	this.player = Bukkit.getPlayerExact(userName);

	if (this.player == null)
	    ofPlayer = Residence.getOfflinePlayer(userName);

	recountMaxRes();
	recountMaxRents();
	recountMaxSubzones();
    }

    public void recountMaxRes() {
	for (int i = 1; i <= Residence.getConfigManager().getMaxResCount(); i++) {
	    if (player != null) {
		if (this.player.hasPermission("residence.max.res." + i))
		    this.maxRes = i;
	    } else {
		if (ofPlayer != null)
		    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.res." + i, Residence.getConfigManager().getDefaultWorld()))
			this.maxRes = i;
	    }
	}
    }

    public void recountMaxRents() {
	for (int i = 1; i <= Residence.getConfigManager().getMaxRentCount(); i++) {
	    if (player != null) {
		if (this.player.isPermissionSet("residence.max.rents." + i))
		    this.maxRents = i;
	    } else {
		if (ofPlayer != null)
		    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.rents." + i, Residence.getConfigManager().getDefaultWorld()))
			this.maxRents = i;
	    }
	}
    }

    public void recountMaxSubzones() {
	for (int i = 1; i <= Residence.getConfigManager().getMaxSubzonesCount(); i++) {
	    if (player != null) {
		if (this.player.isPermissionSet("residence.max.subzones." + i))
		    this.maxSubzones = i;
	    } else {
		if (ofPlayer != null)
		    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.subzones." + i, Residence.getConfigManager().getDefaultWorld()))
			this.maxSubzones = i;
	    }
	}
    }

    public int getMaxRes() {
	recountMaxRes();
	if (this.player != null) {
	    Residence.getPermissionManager().updateGroupNameForPlayer(this.player.getName(), this.player.isOnline() ? this.player.getPlayer().getLocation().getWorld()
		.getName() : Residence.getConfigManager().getDefaultWorld(), true);
	    PermissionGroup g = Residence.getPermissionManager().getGroup(this.player);
	    if (this.maxRes < g.getMaxZones())
		return g.getMaxZones();
	}

	return this.maxRes;
    }

    public int getMaxRents() {
	recountMaxRents();
	return this.maxRents;
    }

    public int getMaxSubzones() {
	recountMaxSubzones();
	return this.maxSubzones;
    }

    public PermissionGroup getGroup() {
	Player player = Bukkit.getPlayer(userName);
	if (player != null) {
	    String gp = Residence.getPermissionManager().getGroupNameByPlayer(player.getName(), player.getWorld().getName());
	    this.group = Residence.getPermissionManager().getGroupByName(gp);
	} else {
	    String gp = Residence.getPermissionManager().getGroupNameByPlayer(userName, Residence.getConfigManager().getDefaultWorld());
	    this.group = Residence.getPermissionManager().getGroupByName(gp);
	}
	return this.group;
    }

    public void recountRes() {
	updateName();
	if (this.userName != null) {
	    ResidenceManager m = Residence.getResidenceManager();
	    this.ResidenceList = m.getResidenceMapList(this.userName, true);
	}
	recountResAmount();
    }

    private void updateName() {
	if (this.userName != null)
	    return;
	if (player != null) {
	    this.userName = player.getName();
	    return;
	}
	if (ofPlayer != null) {
	    this.userName = ofPlayer.getName();
	    return;
	}
    }

    public void recountResAmount() {
	this.currentRes = this.ResidenceList.size();
    }

    public void addResidence(ClaimedResidence residence) {
	if (residence == null)
	    return;
	String name = residence.getName();
	name = name.toLowerCase();
	this.ResidenceList.put(name, residence);
    }

    public void removeResidence(String residence) {
	if (residence == null)
	    return;
	residence = residence.toLowerCase();
	this.ResidenceList.remove(residence);
    }

    public void renameResidence(String oldResidence, String newResidence) {
	if (oldResidence == null)
	    return;
	if (newResidence == null)
	    return;
	oldResidence = oldResidence.toLowerCase();
	newResidence = newResidence.toLowerCase();

	ClaimedResidence res = ResidenceList.get(oldResidence);
	if (res != null) {
	    removeResidence(oldResidence);
	    ResidenceList.put(newResidence, res);
	}
    }

    public int getResAmount() {
	if (currentRes == -1)
	    recountResAmount();
	return currentRes;
    }

    public List<ClaimedResidence> getResList() {
	List<ClaimedResidence> temp = new ArrayList<ClaimedResidence>();
	for (Entry<String, ClaimedResidence> one : this.ResidenceList.entrySet()) {
	    temp.add(one.getValue());
	}
	return temp;
    }

}
