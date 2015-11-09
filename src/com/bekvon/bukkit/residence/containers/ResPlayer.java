package com.bekvon.bukkit.residence.containers;

import java.util.HashMap;
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

public class ResPlayer {

    private String userName = null;
    private Player player = null;
    private OfflinePlayer ofPlayer = null;

    private Map<String, ClaimedResidence> ResidenceList = new HashMap<String, ClaimedResidence>();
    private int currentRes = -1;

    private PermissionGroup group = null;

    private int maxRes = -1;
    private int maxRents = -1;
    private int maxSubzones = -1;

    public ResPlayer(String userName) {
	this.userName = userName;
	RecalculatePermissions();
    }

    public void RecalculatePermissions() {
	this.player = Bukkit.getPlayerExact(userName);

	if (this.player == null)
	    ofPlayer = Residence.getOfflinePlayer(userName);

	recountMaxRes();
	recountMaxRents();
	recountMaxSubzones();
	recountGroup();
    }

    public void recountMaxRes() {
	for (int i = 1; i <= Residence.getConfigManager().getMaxResCount(); i++) {
	    if (player != null) {
		if (this.player.isPermissionSet("residence.max.res." + i))
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

    public void recountGroup() {
	for (Entry<String, PermissionGroup> one : Residence.getPermissionManager().getGroups().entrySet()) {
	    if (player != null) {
		if (this.player.hasPermission("residence.group." + one.getKey()))
		    this.group = one.getValue();
	    } else {
		if (ofPlayer != null)
		    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.group." + one.getKey(), Residence.getConfigManager().getDefaultWorld()))
			this.group = one.getValue();
	    }
	}
    }

    public int getMaxRes() {
	recountMaxRes();
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
	recountGroup();
	return this.group;
    }

    public void recountRes() {
	if (this.userName != null) {
	    ResidenceManager m = Residence.getResidenceManager();
	    this.ResidenceList = m.getResidenceMapList(this.userName, true);
	}
	recountResAmount();
    }

    public void recountResAmount() {
	this.currentRes = this.ResidenceList.size();
    }

    public void addResidence(ClaimedResidence residence) {
	this.ResidenceList.put(residence.getName(), residence);
    }

    public void removeResidence(String residence) {
	this.ResidenceList.remove(residence);
    }

    public void renameResidence(String oldResidence, String newResidence) {
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

    public Map<String, String> getResList() {
	Map<String, String> temp = new HashMap<String, String>();
	for (Entry<String, ClaimedResidence> one : this.ResidenceList.entrySet()) {
	    temp.put(one.getValue().getName(), one.getValue().getWorld());
	}
	return temp;
    }
}
