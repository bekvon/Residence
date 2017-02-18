package com.bekvon.bukkit.residence.containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.vaultinterface.ResidenceVaultAdapter;

public class ResidencePlayer {

    private String userName = null;
    private Player player = null;
    private OfflinePlayer ofPlayer = null;
    private UUID uuid = null;

    private Map<String, ClaimedResidence> ResidenceList = new HashMap<String, ClaimedResidence>();
    private ClaimedResidence mainResidence = null;

    private PlayerGroup groups = null;

    private int maxRes = -1;
    private int maxRents = -1;
    private int maxSubzones = -1;
    private int maxSubzoneDepth = -1;

    public ResidencePlayer(Player player) {
	if (player == null)
	    return;
	Residence.getInstance().getOfflinePlayerMap().put(player.getName(), player);
	Residence.getInstance().addCachedPlayerNameUUIDs(player.getUniqueId(), player.getName());
	this.updatePlayer(player);
	this.RecalculatePermissions();
    }

    public boolean isOnline() {
	this.updatePlayer();
	if (this.player != null && this.player.isOnline())
	    return true;
	return false;
    }

    public ResidencePlayer(String userName) {
	this.userName = userName;
	if (this.isOnline())
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
	    for (String one : Residence.getInstance().getRentManager().getRentedLands(this.userName)) {
		ClaimedResidence res = Residence.getInstance().getResidenceManager().getByName(one);
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
	getGroup();
	recountMaxRes();
	recountMaxRents();
	recountMaxSubzones();
    }

    public void recountMaxRes() {
	if (this.getGroup() != null)
	    this.maxRes = this.getGroup().getMaxZones();
	for (int i = 1; i <= Residence.getInstance().getConfigManager().getMaxResCount(); i++) {
	    if (player != null && player.isOnline()) {
		if (this.player.hasPermission("residence.max.res." + i))
		    this.maxRes = i;
	    } else if (ofPlayer != null) {
		if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.res." + i, Residence.getInstance().getConfigManager().getDefaultWorld()))
		    this.maxRes = i;
	    }
	}
    }

    public void recountMaxRents() {
	for (int i = 1; i <= Residence.getInstance().getConfigManager().getMaxRentCount(); i++) {
	    if (player != null) {
		if (this.player.isPermissionSet("residence.max.rents." + i))
		    this.maxRents = i;
	    } else {
		if (ofPlayer != null)
		    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.rents." + i, Residence.getInstance().getConfigManager().getDefaultWorld()))
			this.maxRents = i;
	    }
	}

	int m = this.getGroup().getMaxRents();
	if (this.maxRents < m)
	    this.maxRents = m;
    }

    public int getMaxRents() {
	recountMaxRents();
	return this.maxRents;
    }

    public void recountMaxSubzones() {
	for (int i = 1; i <= Residence.getInstance().getConfigManager().getMaxSubzonesCount(); i++) {
	    if (player != null) {
		if (this.player.isPermissionSet("residence.max.subzones." + i))
		    this.maxSubzones = i;
	    } else {
		if (ofPlayer != null)
		    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.subzones." + i, Residence.getInstance().getConfigManager().getDefaultWorld()))
			this.maxSubzones = i;
	    }
	}

	int m = this.getGroup().getMaxSubzones();
	if (this.maxSubzones < m)
	    this.maxSubzones = m;
    }

    public int getMaxSubzones() {
	recountMaxSubzones();
	return this.maxSubzones;
    }

    public void recountMaxSubzoneDepth() {
	for (int i = 1; i <= Residence.getInstance().getConfigManager().getMaxSubzoneDepthCount(); i++) {
	    if (player != null) {
		if (this.player.isPermissionSet("residence.max.subzonedepth." + i))
		    this.maxSubzoneDepth = i;
	    } else {
		if (ofPlayer != null)
		    if (ResidenceVaultAdapter.hasPermission(this.ofPlayer, "residence.max.subzonedepth." + i, Residence.getInstance().getConfigManager().getDefaultWorld()))
			this.maxSubzoneDepth = i;
	    }
	}

	int m = this.getGroup().getMaxSubzoneDepth();
	if (this.maxSubzoneDepth < m)
	    this.maxSubzoneDepth = m;
    }

    public int getMaxSubzoneDepth() {
	recountMaxSubzoneDepth();
	return this.maxSubzoneDepth;
    }

    public int getMaxRes() {
	recountMaxRes();
	PermissionGroup g = getGroup();
	if (this.maxRes < g.getMaxZones()) {
	    return g.getMaxZones();
	}
	return this.maxRes;
    }

    public PermissionGroup getGroup() {
	updatePlayer();
	return getGroup(this.player != null ? player.getWorld().getName() : Residence.getInstance().getConfigManager().getDefaultWorld());
    }

    public PermissionGroup getGroup(String world) {
	if (groups == null)
	    groups = new PlayerGroup(this);
	groups.updateGroup(world, false);
	PermissionGroup group = groups.getGroup(world);
	return group;
    }

    public ResidencePlayer updatePlayer(Player player) {
	this.player = player;
	this.uuid = player.getUniqueId();
	this.userName = player.getName();
	this.ofPlayer = player;
	return this;
    }

    private void updatePlayer() {
	player = Bukkit.getPlayer(this.uuid);
	if (player != null)
	    updatePlayer(player);
	if (player != null && player.isOnline())
	    return;
	if (this.uuid != null && Bukkit.getPlayer(this.uuid) != null) {
	    player = Bukkit.getPlayer(this.uuid);
	    this.userName = player.getName();
	    return;
	}

	if (this.userName != null) {
	    player = Bukkit.getPlayer(this.userName);
	}
	if (player != null) {
	    this.userName = player.getName();
	    this.uuid = player.getUniqueId();
	    this.ofPlayer = player;
	    return;
	}
	if (this.player == null && ofPlayer == null)
	    ofPlayer = Residence.getInstance().getOfflinePlayer(userName);
	if (ofPlayer != null) {
	    this.userName = ofPlayer.getName();
	    this.uuid = ofPlayer.getUniqueId();
	    return;
	}
    }

    public void addResidence(ClaimedResidence residence) {
	if (residence == null)
	    return;
	this.ResidenceList.put(residence.getName().toLowerCase(), residence);
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

	ClaimedResidence res = ResidenceList.get(oldResidence);
	if (res != null) {
	    removeResidence(oldResidence);
	    ResidenceList.put(newResidence.toLowerCase(), res);
	}
    }

    public int getResAmount() {
	return ResidenceList.size();
    }

    public List<ClaimedResidence> getResList() {
	List<ClaimedResidence> temp = new ArrayList<ClaimedResidence>();
	for (Entry<String, ClaimedResidence> one : this.ResidenceList.entrySet()) {
	    temp.add(one.getValue());
	}
	return temp;
    }

    public Map<String, ClaimedResidence> getResidenceMap() {
	return this.ResidenceList;
    }

    public String getPlayerName() {
	this.updatePlayer();
	return userName;
    }

    public UUID getUuid() {
	return uuid;
    }

    public Player getPlayer() {
	this.updatePlayer();
	return player;
    }
}
