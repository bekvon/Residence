package com.bekvon.bukkit.residence.containers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class ResPlayer {

    private String userName = null;
//    private UUID uuid = null;
    private Player player = null;
    private Map<String, ClaimedResidence> ResidenceList = new HashMap<String, ClaimedResidence>();
    private int currentRes = -1;

    private PermissionGroup group = null;

    private int maxRes = -1;
    private int maxRents = -1;
    private int maxSubzones = -1;

//    private int maxEastWest = -1;
//    private int MaxNorthSouth = -1;

    public ResPlayer(String userName) {
	this.userName = userName;
//	this.uuid = uuid;
	RecalculatePermissions();
    }

    public void RecalculatePermissions() {
	this.player = Bukkit.getPlayerExact(userName);
	if (this.player != null) {
	    recountMaxRes();
	    recountMaxRents();
	    recountMaxSubzones();
	    recountGroup();
	}
    }

    public void recountMaxRes() {
	for (int i = 1; i <= 250; i++) {
	    if (this.player.isPermissionSet("residence.max.res." + i))
		this.maxRes = i;
	}
    }

    public void recountMaxRents() {
	for (int i = 1; i <= 250; i++) {
	    if (this.player.isPermissionSet("residence.max.rents." + i))
		this.maxRents = i;
	}
    }

    public void recountMaxSubzones() {
	for (int i = 1; i <= 250; i++) {
	    if (this.player.isPermissionSet("residence.max.subzones." + i))
		this.maxSubzones = i;
	}
    }

    public void recountGroup() {
	for (Entry<String, PermissionGroup> one : Residence.getPermissionManager().getGroups().entrySet()) {
	    if (this.player.isPermissionSet("residence.group." + one.getKey()))
		this.group = one.getValue();
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
	this.ResidenceList = Residence.getResidenceManager().getResidenceMapList(this.userName, false);
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
