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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class ResidencePermissions extends FlagPermissions {

    protected UUID ownerUUID;
    protected String ownerLastKnownName;
    protected String world;
    protected ClaimedResidence residence;

    private ResidencePermissions(ClaimedResidence res) {
	super();
	residence = res;
    }

    public ResidencePermissions(ClaimedResidence res, String creator, String inworld) {
	this(res);
	ownerUUID = Residence.getPlayerUUID(creator);
	if (ownerUUID == null)
	    ownerUUID = UUID.fromString(Residence.getTempUserUUID());
	this.ownerLastKnownName = creator;
	world = inworld;
    }

    public boolean playerHas(String player, String flag, boolean def) {
	return this.playerHas(player, world, flag, def);
    }

    @Override
    public boolean playerHas(String player, String world, String flag, boolean def) {
	ResidenceFlagCheckEvent fc = new ResidenceFlagCheckEvent(residence, flag, FlagType.PLAYER, player, def);

	Residence.getServ().getPluginManager().callEvent(fc);
	if (fc.isOverriden())
	    return fc.getOverrideValue();
	return super.playerHas(player, world, flag, def);
    }

    @Override
    public boolean groupHas(String group, String flag, boolean def) {
	ResidenceFlagCheckEvent fc = new ResidenceFlagCheckEvent(residence, flag, FlagType.GROUP, group, def);
	Residence.getServ().getPluginManager().callEvent(fc);
	if (fc.isOverriden())
	    return fc.getOverrideValue();
	return super.groupHas(group, flag, def);
    }

    @Override
    public boolean has(String flag, boolean def) {
	ResidenceFlagCheckEvent fc = new ResidenceFlagCheckEvent(residence, flag, FlagType.RESIDENCE, null, def);
	Residence.getServ().getPluginManager().callEvent(fc);
	if (fc.isOverriden())
	    return fc.getOverrideValue();
	return super.has(flag, def);
    }

    public boolean hasApplicableFlag(String player, String flag) {
	return super.inheritanceIsPlayerSet(player, flag) || super.inheritanceIsGroupSet(Residence.getPermissionManager().getGroupNameByPlayer(player, world), flag)
	    || super.inheritanceIsSet(flag);
    }

    public void applyTemplate(Player player, FlagPermissions list, boolean resadmin) {
	if (player != null) {
	    if (!resadmin) {
		if (!Residence.getConfigManager().isOfflineMode() && !player.getUniqueId().toString().equals(ownerUUID.toString())) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		    return;
		} else if (!player.getName().equals(ownerLastKnownName)) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		    return;
		}
	    }
	} else {
	    resadmin = true;
	}
	PermissionGroup group = Residence.getPermissionManager().getGroup(this.getOwner(), world);
	for (Entry<String, Boolean> flag : list.cuboidFlags.entrySet()) {
	    if (group.hasFlagAccess(flag.getKey()) || resadmin) {
		this.cuboidFlags.put(flag.getKey(), flag.getValue());
	    } else {
		if (player != null)
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagSetDeny", ChatColor.YELLOW + flag.getKey() + ChatColor.RED));
	    }
	}
	for (Entry<String, Map<String, Boolean>> plists : list.playerFlags.entrySet()) {
	    Map<String, Boolean> map = this.getPlayerFlags(plists.getKey(), true);
	    for (Entry<String, Boolean> flag : plists.getValue().entrySet()) {
		if (group.hasFlagAccess(flag.getKey()) || resadmin) {
		    map.put(flag.getKey(), flag.getValue());
		} else {
		    if (player != null)
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagSetDeny", ChatColor.YELLOW + flag.getKey() + ChatColor.RED));
		}
	    }
	}
	for (Entry<String, Map<String, Boolean>> glists : list.groupFlags.entrySet()) {
	    for (Entry<String, Boolean> flag : glists.getValue().entrySet()) {
		if (group.hasFlagAccess(flag.getKey()) || resadmin) {
		    if (!this.groupFlags.containsKey(glists.getKey()))
			this.groupFlags.put(glists.getKey(), Collections.synchronizedMap(new HashMap<String, Boolean>()));
		    this.groupFlags.get(glists.getKey()).put(flag.getKey(), flag.getValue());
		} else {
		    if (player != null)
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagSetDeny", ChatColor.YELLOW + flag.getKey() + ChatColor.RED));
		}
	    }
	}
	if (player != null)
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("PermissionsApply"));
    }

    public boolean hasResidencePermission(Player player, boolean requireOwner) {
	if (Residence.getConfigManager().enabledRentSystem()) {
	    String resname = residence.getName();
	    if (Residence.getRentManager().isRented(resname)) {
		if (requireOwner) {
		    return false;
		}
		String renter = Residence.getRentManager().getRentingPlayer(resname);
		if (player.getName().equals(renter)) {
		    return true;
		} else {
		    return (playerHas(player.getName(), "admin", false));
		}
	    }
	}
	if (requireOwner) {
	    return (this.getOwner().equals(player.getName()));
	}
	return (playerHas(player.getName(), "admin", false) || this.getOwner().equals(player.getName()));
    }

    private boolean checkCanSetFlag(Player player, String flag, FlagState state, boolean globalflag, boolean resadmin) {
	if (!checkValidFlag(flag, globalflag)) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidFlag"));
	    return false;
	}
	if (state == FlagState.INVALID) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidFlagState"));
	    return false;
	}
	if (!resadmin) {
	    if (!this.hasResidencePermission(player, false)) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return false;
	    }
	    if (!hasFlagAccess(this.getOwner(), flag)) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagSetFailed", flag));
		return false;
	    }
	}
	return true;
    }

    private boolean hasFlagAccess(String player, String flag) {
	PermissionGroup group = Residence.getPermissionManager().getGroup(player, world);
	return group.hasFlagAccess(flag);
    }

    public boolean setPlayerFlag(Player player, String targetPlayer, String flag, String flagstate, boolean resadmin, boolean Show) {

	if (Residence.getPlayerUUID(targetPlayer) == null) {
	    player.sendMessage("no player by this name");
	    return false;
	}

	if (validFlagGroups.containsKey(flag))
	    return this.setFlagGroupOnPlayer(player, targetPlayer, flag, flagstate, resadmin);
	FlagState state = FlagPermissions.stringToFlagState(flagstate);
	if (checkCanSetFlag(player, flag, state, false, resadmin)) {
	    ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, player, flag, ResidenceFlagChangeEvent.FlagType.PLAYER, state, targetPlayer);
	    Residence.getServ().getPluginManager().callEvent(fc);
	    if (fc.isCancelled())
		return false;
	    if (super.setPlayerFlag(targetPlayer, flag, state)) {
		if (Show)
		    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("FlagSet", flag + "|" + residence.getName() + "|" + flagstate));
		return true;
	    }
	}
	return false;
    }

    public boolean setGroupFlag(Player player, String group, String flag, String flagstate, boolean resadmin) {
	group = group.toLowerCase();
	if (validFlagGroups.containsKey(flag))
	    return this.setFlagGroupOnGroup(player, flag, group, flagstate, resadmin);
	FlagState state = FlagPermissions.stringToFlagState(flagstate);
	if (checkCanSetFlag(player, flag, state, false, resadmin)) {
	    if (Residence.getPermissionManager().hasGroup(group)) {
		ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, player, flag, ResidenceFlagChangeEvent.FlagType.GROUP, state, group);
		Residence.getServ().getPluginManager().callEvent(fc);
		if (fc.isCancelled())
		    return false;
		if (super.setGroupFlag(group, flag, state)) {
		    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("FlagSet", flag + "|" + residence.getName() + "|" + flagstate));
		    return true;
		}
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidGroup"));
		return false;
	    }
	}
	return false;
    }

    public boolean setFlag(Player player, String flag, String flagstate, boolean resadmin) {
	if (validFlagGroups.containsKey(flag))
	    return this.setFlagGroup(player, flag, flagstate, resadmin);

	FlagState state = FlagPermissions.stringToFlagState(flagstate);

	if (Residence.getConfigManager().isPvPFlagPrevent()) {
	    for (String oneFlag : Residence.getConfigManager().getProtectedFlagsList()) {
		if (!flag.equalsIgnoreCase(oneFlag))
		    continue;

		ArrayList<Player> players = this.residence.getPlayersInResidence();
		if (!resadmin && (players.size() > 1 || players.size() == 1 && !players.get(0).getName().equals(this.getOwner()))) {
		    int size = 0;
		    for (Player one : players) {
			if (!one.getName().equals(this.getOwner()))
			    size++;
		    }
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("FlagChangeDeny", flag + "|" + size));
		    return false;
		}
	    }
	}

	if (checkCanSetFlag(player, flag, state, true, resadmin)) {
	    ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, player, flag, ResidenceFlagChangeEvent.FlagType.RESIDENCE, state, null);
	    Residence.getServ().getPluginManager().callEvent(fc);
	    if (fc.isCancelled())
		return false;
	    if (super.setFlag(flag, state)) {
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("FlagSet", flag + "|" + residence.getName() + "|" + flagstate));
		return true;
	    }
	}
	return false;
    }

    public boolean removeAllPlayerFlags(Player player, String targetPlayer, boolean resadmin) {
	if (this.hasResidencePermission(player, false) || resadmin) {
	    ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, player, "ALL", ResidenceFlagChangeEvent.FlagType.RESIDENCE, FlagState.NEITHER, null);
	    Residence.getServ().getPluginManager().callEvent(fc);
	    if (fc.isCancelled()) {
		return false;
	    }
	    super.removeAllPlayerFlags(targetPlayer);
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("FlagSet"));
	    return true;
	}
	return false;
    }

    public boolean removeAllGroupFlags(Player player, String group, boolean resadmin) {
	if (this.hasResidencePermission(player, false) || resadmin) {
	    ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, player, "ALL", ResidenceFlagChangeEvent.FlagType.GROUP, FlagState.NEITHER, null);
	    Residence.getServ().getPluginManager().callEvent(fc);
	    if (fc.isCancelled()) {
		return false;
	    }
	    super.removeAllGroupFlags(group);
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("FlagSet"));
	    return true;
	}
	return false;
    }

    @Override
    public boolean setFlag(String flag, FlagState state) {
	ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, null, flag, ResidenceFlagChangeEvent.FlagType.RESIDENCE, state, null);
	Residence.getServ().getPluginManager().callEvent(fc);
	if (fc.isCancelled())
	    return false;
	return super.setFlag(flag, state);
    }

    @Override
    public boolean setGroupFlag(String group, String flag, FlagState state) {
	ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, null, flag, ResidenceFlagChangeEvent.FlagType.GROUP, state, group);
	Residence.getServ().getPluginManager().callEvent(fc);
	if (fc.isCancelled())
	    return false;
	return super.setGroupFlag(group, flag, state);
    }

    @Override
    public boolean setPlayerFlag(String player, String flag, FlagState state) {
	ResidenceFlagChangeEvent fc = new ResidenceFlagChangeEvent(residence, null, flag, ResidenceFlagChangeEvent.FlagType.PLAYER, state, player);
	Residence.getServ().getPluginManager().callEvent(fc);
	if (fc.isCancelled())
	    return false;
	return super.setPlayerFlag(player, flag, state);
    }

    public void applyDefaultFlags(Player player, boolean resadmin) {
	if (this.hasResidencePermission(player, true) || resadmin) {
	    this.applyDefaultFlags();
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("FlagsDefault"));
	} else
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("NoPermission"));
    }

    public void applyDefaultFlags() {
	PermissionManager gm = Residence.getPermissionManager();
	PermissionGroup group = gm.getGroup(this.getOwner(), world);
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
		    this.setPlayerFlag(this.getOwner(), next.getKey(), FlagState.TRUE);
		} else {
		    this.setPlayerFlag(this.getOwner(), next.getKey(), FlagState.FALSE);
		}
	    }
	}
	for (Entry<String, Map<String, Boolean>> entry : dgflags) {
	    Map<String, Boolean> value = entry.getValue();
	    for (Entry<String, Boolean> flag : value.entrySet()) {
		if (flag.getValue()) {
		    this.setGroupFlag(entry.getKey(), flag.getKey(), FlagState.TRUE);
		} else {
		    this.setGroupFlag(entry.getKey(), flag.getKey(), FlagState.FALSE);
		}
	    }
	}
    }

    public void setOwner(String newOwner, boolean resetFlags) {
	ownerLastKnownName = newOwner;

	ResidenceOwnerChangeEvent ownerchange = new ResidenceOwnerChangeEvent(residence, newOwner);
	Residence.getServ().getPluginManager().callEvent(ownerchange);

	if (newOwner.equalsIgnoreCase("Server Land") || newOwner.equalsIgnoreCase(Residence.getServerLandname())) {
	    ownerUUID = UUID.fromString(Residence.getServerLandUUID());// the UUID for server owned land
	} else {
	    UUID playerUUID = Residence.getPlayerUUID(newOwner);
	    if (playerUUID != null)
		ownerUUID = playerUUID;
	    else
		ownerUUID = UUID.fromString(Residence.getTempUserUUID());//the fake UUID used when unable to find the real one, will be updated with players real UUID when its possible to find it
	}
	if (resetFlags)
	    this.applyDefaultFlags();
    }

    public String getOwner() {
	if (Residence.getConfigManager().isOfflineMode())
	    return ownerLastKnownName;
	if (ownerUUID.toString().equals(Residence.getServerLandUUID())) //check for server land
	    return Residence.getServerLandname();
	String name = Residence.getPlayerName(ownerUUID);//try to find the owner's name
	if (name == null)
	    return ownerLastKnownName;//return last known if we cannot find it
	else
	    ownerLastKnownName = name;//update last known if we did find it
	return name;
    }

    public UUID getOwnerUUID() {
	return ownerUUID;
    }

    public String getWorld() {
	return world;
    }

    @Override
    public Map<String, Object> save() {
	Map<String, Object> root = super.save();
	root.put("OwnerUUID", ownerUUID.toString());
	root.put("OwnerLastKnownName", ownerLastKnownName);
	root.put("World", world);
	return root;
    }

    public static ResidencePermissions load(ClaimedResidence res, Map<String, Object> root) throws Exception {
	ResidencePermissions newperms = new ResidencePermissions(res);
	//newperms.owner = (String) root.get("Owner");
	if (root.containsKey("OwnerUUID")) {
	    newperms.ownerUUID = UUID.fromString((String) root.get("OwnerUUID"));//get owner UUID
	    //			String name = Residence.getPlayerName(newperms.ownerUUID); //try to find the current name of the owner
	    newperms.ownerLastKnownName = (String) root.get("OwnerLastKnownName");//otherwise load last known name from file

	    if (newperms.ownerLastKnownName.equalsIgnoreCase("Server land") || newperms.ownerLastKnownName.equalsIgnoreCase(Residence.getServerLandname())) {
		newperms.ownerUUID = UUID.fromString(Residence.getServerLandUUID());//UUID for server land
		newperms.ownerLastKnownName = Residence.getServerLandname();
	    } else if (newperms.ownerUUID.toString().equals(Residence.getTempUserUUID())) //check for fake UUID
	    {
		UUID realUUID = Residence.getPlayerUUID(newperms.ownerLastKnownName);//try to find the real UUID of the player if possible now
		if (realUUID != null)
		    newperms.ownerUUID = realUUID;
	    }
	} else if (root.containsKey("Owner")) //convert old owner name save format into uuid format
	{
	    String owner = (String) root.get("Owner");
	    newperms.ownerLastKnownName = owner;
	    newperms.ownerUUID = Residence.getPlayerUUID(owner);
	    if (newperms.ownerUUID == null)
		newperms.ownerUUID = UUID.fromString(Residence.getTempUserUUID());//set fake UUID until we can find real one for last known player
	} else {
	    newperms.ownerUUID = UUID.fromString(Residence.getServerLandUUID());//cant determine owner name or UUID... setting zero UUID which is server land
	    newperms.ownerLastKnownName = Residence.getServerLandname();
	}
	newperms.world = (String) root.get("World");
	FlagPermissions.load(root, newperms);
	if (newperms.getOwner() == null || newperms.world == null || newperms.playerFlags == null || newperms.groupFlags == null || newperms.cuboidFlags == null)
	    throw new Exception("Invalid Residence Permissions...");
	return newperms;
    }

    public void applyGlobalDefaults() {
	this.clearFlags();
	FlagPermissions gRD = Residence.getConfigManager().getGlobalResidenceDefaultFlags();
	FlagPermissions gCD = Residence.getConfigManager().getGlobalCreatorDefaultFlags();
	Map<String, FlagPermissions> gGD = Residence.getConfigManager().getGlobalGroupDefaultFlags();
	for (Entry<String, Boolean> entry : gRD.cuboidFlags.entrySet()) {
	    if (entry.getValue())
		this.setFlag(entry.getKey(), FlagState.TRUE);
	    else
		this.setFlag(entry.getKey(), FlagState.FALSE);
	}
	for (Entry<String, Boolean> entry : gCD.cuboidFlags.entrySet()) {
	    if (entry.getValue())
		this.setPlayerFlag(this.getOwner(), entry.getKey(), FlagState.TRUE);
	    else
		this.setPlayerFlag(this.getOwner(), entry.getKey(), FlagState.FALSE);
	}
	for (Entry<String, FlagPermissions> entry : gGD.entrySet()) {
	    for (Entry<String, Boolean> flag : entry.getValue().cuboidFlags.entrySet()) {
		if (flag.getValue())
		    this.setGroupFlag(entry.getKey(), flag.getKey(), FlagState.TRUE);
		else
		    this.setGroupFlag(entry.getKey(), flag.getKey(), FlagState.FALSE);
	    }
	}
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
	    String flagString = "";
	    int i = 0;
	    for (String flag : flags) {
		i++;
		if (this.setPlayerFlag(player, target, flag, state, resadmin, false)) {
		    changed = true;
		    flagString += flag;
		    if (i < flags.size() - 1)
			flagString += ", ";
		}
	    }
	    if (flagString.length() > 0)
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("FlagSet", flagString + "|" + target + "|" + state));
	    return changed;
	}
	return false;
    }
}
