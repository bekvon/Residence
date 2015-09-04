/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.protection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;

/**
 * 
 * @author Administrator
 */
public class FlagPermissions {

    protected static ArrayList<String> validFlags = new ArrayList<>();
    protected static ArrayList<String> validPlayerFlags = new ArrayList<>();
    protected static ArrayList<String> validAreaFlags = new ArrayList<>();
    final static Map<Material, String> matUseFlagList = new EnumMap<>(Material.class);
    protected Map<String, String> cachedPlayerNameUUIDs;
    protected Map<String, Map<String, Boolean>> playerFlags;
    protected Map<String, Map<String, Boolean>> groupFlags;
    protected Map<String, Boolean> cuboidFlags;
    protected FlagPermissions parent;

    public static void addMaterialToUseFlag(Material mat, String flag) {
	matUseFlagList.put(mat, flag);
    }

    public static void removeMaterialFromUseFlag(Material mat) {
	matUseFlagList.remove(mat);
    }

    public static EnumMap<Material, String> getMaterialUseFlagList() {
	return (EnumMap<Material, String>) matUseFlagList;
    }

    public static void addFlag(String flag) {
	flag = flag.toLowerCase();
	if (!validFlags.contains(flag)) {
	    validFlags.add(flag);
	}
	if (validFlagGroups.containsKey(flag)) {
	    validFlagGroups.remove(flag);
	}
    }

    public static void addPlayerOrGroupOnlyFlag(String flag) {
	flag = flag.toLowerCase();
	if (!validPlayerFlags.contains(flag)) {
	    validPlayerFlags.add(flag);
	}
	if (validFlagGroups.containsKey(flag)) {
	    validFlagGroups.remove(flag);
	}
    }

    public static void addResidenceOnlyFlag(String flag) {
	flag = flag.toLowerCase();
	if (!validAreaFlags.contains(flag)) {
	    validAreaFlags.add(flag);
	}
	if (validFlagGroups.containsKey(flag)) {
	    validFlagGroups.remove(flag);
	}
    }

    protected static HashMap<String, ArrayList<String>> validFlagGroups = new HashMap<>();

    public static void addFlagToFlagGroup(String group, String flag) {
	if (!FlagPermissions.validFlags.contains(group) && !FlagPermissions.validAreaFlags.contains(group) && !FlagPermissions.validPlayerFlags.contains(group)) {
	    if (!validFlagGroups.containsKey(group)) {
		validFlagGroups.put(group, new ArrayList<String>());
	    }
	    ArrayList<String> flags = validFlagGroups.get(group);
	    flags.add(flag);
	}
    }

    public static void removeFlagFromFlagGroup(String group, String flag) {
	if (validFlagGroups.containsKey(group)) {
	    ArrayList<String> flags = validFlagGroups.get(group);
	    flags.remove(flag);
	    if (flags.isEmpty()) {
		validFlagGroups.remove(group);
	    }
	}
    }

    public static boolean flagGroupExists(String group) {
	return validFlagGroups.containsKey(group);
    }

    public static void initValidFlags() {
	validAreaFlags.clear();
	validPlayerFlags.clear();
	validFlags.clear();
	validFlagGroups.clear();
	addFlag("egg");
	addFlag("note");
	addFlag("pressure");
	addFlag("cake");
	addFlag("lever");
	addFlag("door");
	addFlag("button");
	addFlag("table");
	addFlag("brew");
	addFlag("bed");
	addFlag("commandblock");
	addFlag("anvil");
	addFlag("flowerpot");
	addFlag("enchant");
	addFlag("diode");
	addFlag("use");
	addFlag("move");
	addFlag("build");
	addFlag("tp");
	addFlag("ignite");
	addFlag("container");
	addFlag("subzone");
	addFlag("destroy");
	addFlag("place");
	addFlag("bucket");
	addFlag("bank");
	addFlag("beacon");

	/* New flags */
	addFlag("animalkilling");
	addFlag("mobkilling");
	addFlag("vehicledestroy");
	addFlag("trade");
	
	addFlag("leash");
	addFlag("shear");
	addFlag("nofly");
	
	addResidenceOnlyFlag("trample");
	addResidenceOnlyFlag("pvp");
	addResidenceOnlyFlag("fireball");
	addResidenceOnlyFlag("explode");
	addResidenceOnlyFlag("damage");
	addResidenceOnlyFlag("monsters");
	addResidenceOnlyFlag("firespread");
	addResidenceOnlyFlag("burn");
	addResidenceOnlyFlag("tnt");
	addResidenceOnlyFlag("creeper");
	addResidenceOnlyFlag("wither");
	addResidenceOnlyFlag("flow");
	addResidenceOnlyFlag("healing");
	addResidenceOnlyFlag("animals");
	addResidenceOnlyFlag("lavaflow");
	addResidenceOnlyFlag("waterflow");
	addResidenceOnlyFlag("physics");
	addResidenceOnlyFlag("piston");
	addResidenceOnlyFlag("spread");
	addResidenceOnlyFlag("hidden");
	addResidenceOnlyFlag("witherdamage");

	// prevents from mobs entering residence
	addResidenceOnlyFlag("nomobs");
	
	addPlayerOrGroupOnlyFlag("admin");
	addFlagToFlagGroup("redstone", "note");
	addFlagToFlagGroup("redstone", "pressure");
	addFlagToFlagGroup("redstone", "lever");
	addFlagToFlagGroup("redstone", "button");
	addFlagToFlagGroup("redstone", "diode");
	addFlagToFlagGroup("craft", "brew");
	addFlagToFlagGroup("craft", "table");
	addFlagToFlagGroup("craft", "enchant");
	addFlagToFlagGroup("trusted", "use");
	addFlagToFlagGroup("trusted", "tp");
	addFlagToFlagGroup("trusted", "build");
	addFlagToFlagGroup("trusted", "container");
	addFlagToFlagGroup("trusted", "bucket");
	addFlagToFlagGroup("trusted", "move");
	addFlagToFlagGroup("trusted", "leash");
	addFlagToFlagGroup("trusted", "animalkilling");
	addFlagToFlagGroup("trusted", "mobkilling");
	addFlagToFlagGroup("trusted", "shear");
	addFlagToFlagGroup("fire", "ignite");
	addFlagToFlagGroup("fire", "firespread");
	addMaterialToUseFlag(Material.DIODE, "diode");
	addMaterialToUseFlag(Material.DIODE_BLOCK_OFF, "diode");
	addMaterialToUseFlag(Material.DIODE_BLOCK_ON, "diode");
	addMaterialToUseFlag(Material.WORKBENCH, "table");
	addMaterialToUseFlag(Material.WOODEN_DOOR, "door");
	/* 1.8 Doors */
	addMaterialToUseFlag(Material.SPRUCE_DOOR, "door");
	addMaterialToUseFlag(Material.BIRCH_DOOR, "door");
	addMaterialToUseFlag(Material.JUNGLE_DOOR, "door");
	addMaterialToUseFlag(Material.ACACIA_DOOR, "door");
	addMaterialToUseFlag(Material.DARK_OAK_DOOR, "door");

	/* 1.8 Fence Gates */
	addMaterialToUseFlag(Material.SPRUCE_FENCE_GATE, "door");
	addMaterialToUseFlag(Material.BIRCH_FENCE_GATE, "door");
	addMaterialToUseFlag(Material.JUNGLE_FENCE_GATE, "door");
	addMaterialToUseFlag(Material.ACACIA_FENCE_GATE, "door");
	addMaterialToUseFlag(Material.DARK_OAK_FENCE_GATE, "door");

	addMaterialToUseFlag(Material.FENCE_GATE, "door");
	addMaterialToUseFlag(Material.NETHER_FENCE, "door");
	addMaterialToUseFlag(Material.TRAP_DOOR, "door");
	addMaterialToUseFlag(Material.IRON_TRAPDOOR, "door");
	addMaterialToUseFlag(Material.ENCHANTMENT_TABLE, "enchant");
	addMaterialToUseFlag(Material.STONE_BUTTON, "button");
	addMaterialToUseFlag(Material.LEVER, "lever");
	addMaterialToUseFlag(Material.BED_BLOCK, "bed");
	addMaterialToUseFlag(Material.BREWING_STAND, "brew");
	addMaterialToUseFlag(Material.CAKE, "cake");
	addMaterialToUseFlag(Material.NOTE_BLOCK, "note");
	addMaterialToUseFlag(Material.DRAGON_EGG, "egg");
	addMaterialToUseFlag(Material.COMMAND, "commandblock");
	addMaterialToUseFlag(Material.WOOD_BUTTON, "button");
	addMaterialToUseFlag(Material.ANVIL, "anvil");
	addMaterialToUseFlag(Material.FLOWER_POT, "flowerpot");
	addMaterialToUseFlag(Material.BEACON, "beacon");
	addMaterialToUseFlag(Material.JUKEBOX, "container");
	addMaterialToUseFlag(Material.CHEST, "container");
	addMaterialToUseFlag(Material.TRAPPED_CHEST, "container");
	addMaterialToUseFlag(Material.HOPPER, "container");
	addMaterialToUseFlag(Material.DROPPER, "container");
	addMaterialToUseFlag(Material.FURNACE, "container");
	addMaterialToUseFlag(Material.BURNING_FURNACE, "container");
	addMaterialToUseFlag(Material.DISPENSER, "container");
	addMaterialToUseFlag(Material.CAKE_BLOCK, "cake");
    }

    public static FlagPermissions parseFromConfigNode(String name, ConfigurationSection node) {
	FlagPermissions list = new FlagPermissions();
	Set<String> keys = node.getConfigurationSection(name).getKeys(false);
	if (keys != null) {
	    for (String key : keys) {
		boolean state = node.getBoolean(name + "." + key, false);
		key = key.toLowerCase();
		if (state) {
		    list.setFlag(key, FlagState.TRUE);
		} else {
		    list.setFlag(key, FlagState.FALSE);
		}
	    }
	}
	return list;
    }

    public static FlagPermissions parseFromConfigNodeAsList(String name, String node, String stage) {
	FlagPermissions list = new FlagPermissions();
	if (node.equalsIgnoreCase("true")) {
	    list.setFlag(node, FlagState.valueOf(stage));
	} else {
	    list.setFlag(node, FlagState.FALSE);
	}

	return list;
    }

    public FlagPermissions() {
	cuboidFlags = Collections.synchronizedMap(new HashMap<String, Boolean>());
	playerFlags = Collections.synchronizedMap(new HashMap<String, Map<String, Boolean>>());
	groupFlags = Collections.synchronizedMap(new HashMap<String, Map<String, Boolean>>());
	cachedPlayerNameUUIDs = Collections.synchronizedMap(new HashMap<String, String>());
    }

    protected Map<String, Boolean> getPlayerFlags(String player, boolean allowCreate)//this function works with uuid in string format as well, instead of player name
    {
	player = player.toLowerCase();
	String uuids;
	Map<String, Boolean> flags = null;

	if (player.length() == 36) {
	    uuids = player;
	    String resolvedName = Residence.getPlayerName(uuids);
	    if (resolvedName != null)
		player = resolvedName;
	    else if (cachedPlayerNameUUIDs.containsKey(player))
		player = cachedPlayerNameUUIDs.get(player);
	} else
	    uuids = Residence.getPlayerUUIDString(player);

	if (uuids == null) {
	    Set<Entry<String, String>> values = cachedPlayerNameUUIDs.entrySet();
	    for (Entry<String, String> value : values) {
		if (value.getValue().equals(player))
		    uuids = value.getKey();
	    }
	}

	if (uuids != null)
	    flags = playerFlags.get(uuids);
	if (flags == null) {
	    flags = playerFlags.get(player);
	    if (uuids != null && flags != null) {
		flags = playerFlags.remove(player);
		playerFlags.put(uuids, flags);
		cachedPlayerNameUUIDs.put(uuids, player);
	    }
	} else
	    cachedPlayerNameUUIDs.put(uuids, player);

	if (flags == null && allowCreate) {
	    if (uuids != null) {
		flags = Collections.synchronizedMap(new HashMap<String, Boolean>());
		playerFlags.put(uuids, flags);
		cachedPlayerNameUUIDs.put(uuids, player);
	    } else {
		flags = Collections.synchronizedMap(new HashMap<String, Boolean>());
		playerFlags.put(player, flags);
	    }
	}
	return flags;
    }

    public boolean setPlayerFlag(String player, String flag, FlagState state) {
	Map<String, Boolean> map = this.getPlayerFlags(player, state != FlagState.NEITHER);
	if (map == null)
	    return true;
	if (state == FlagState.FALSE) {
	    map.put(flag, false);
	} else if (state == FlagState.TRUE) {
	    map.put(flag, true);
	} else if (state == FlagState.NEITHER) {
	    if (map.containsKey(flag)) {
		map.remove(flag);
	    }
	}
	if (map.isEmpty())
	    this.removeAllPlayerFlags(player);
	return true;
    }

    public void removeAllPlayerFlags(String player) {//this function works with uuid in string format as well, instead of player name
	player = player.toLowerCase();
	String uuids = Residence.getPlayerUUIDString(player);
	if (uuids == null)
	    for (Entry<String, String> entry : cachedPlayerNameUUIDs.entrySet())
		if (entry.getValue().equals(player))
		    uuids = entry.getKey();

	if (uuids != null) {
	    playerFlags.remove(uuids);
	    cachedPlayerNameUUIDs.remove(uuids);
	}

	playerFlags.remove(player);
	cachedPlayerNameUUIDs.remove(player);
    }

    public void removeAllGroupFlags(String group) {
	groupFlags.remove(group);
    }

    public boolean setGroupFlag(String group, String flag, FlagState state) {
	group = group.toLowerCase();
	if (!groupFlags.containsKey(group)) {
	    groupFlags.put(group, Collections.synchronizedMap(new HashMap<String, Boolean>()));
	}
	Map<String, Boolean> map = groupFlags.get(group);
	if (state == FlagState.FALSE) {
	    map.put(flag, false);
	} else if (state == FlagState.TRUE) {
	    map.put(flag, true);
	} else if (state == FlagState.NEITHER) {
	    if (map.containsKey(flag)) {
		map.remove(flag);
	    }
	}
	if (map.isEmpty()) {
	    groupFlags.remove(group);
	}
	return true;
    }

    public boolean setFlag(String flag, FlagState state) {
	if (state == FlagState.FALSE) {
	    cuboidFlags.put(flag, false);
	} else if (state == FlagState.TRUE) {
	    cuboidFlags.put(flag, true);
	} else if (state == FlagState.NEITHER) {
	    if (cuboidFlags.containsKey(flag)) {
		cuboidFlags.remove(flag);
	    }
	}
	return true;
    }

    public static enum FlagState {

	TRUE, FALSE, NEITHER, INVALID
    }

    public static FlagState stringToFlagState(String flagstate) {
	if (flagstate.equalsIgnoreCase("true") || flagstate.equalsIgnoreCase("t")) {
	    return FlagState.TRUE;
	} else if (flagstate.equalsIgnoreCase("false") || flagstate.equalsIgnoreCase("f")) {
	    return FlagState.FALSE;
	} else if (flagstate.equalsIgnoreCase("remove") || flagstate.equalsIgnoreCase("r")) {
	    return FlagState.NEITHER;
	} else {
	    return FlagState.INVALID;
	}
    }

    public boolean playerHas(String player, String world, String flag, boolean def) {
	String group = Residence.getPermissionManager().getGroupNameByPlayer(player, world);
	return this.playerCheck(player, flag, this.groupCheck(group, flag, this.has(flag, def)));
    }

    public boolean groupHas(String group, String flag, boolean def) {
	return this.groupCheck(group, flag, this.has(flag, def));
    }

    private boolean playerCheck(String player, String flag, boolean def) {
	Map<String, Boolean> pmap = this.getPlayerFlags(player, false);
	if (pmap != null) {
	    if (pmap.containsKey(flag)) {
		return pmap.get(flag);
	    }
	}
	if (parent != null) {
	    return parent.playerCheck(player, flag, def);
	}
	return def;
    }

    private boolean groupCheck(String group, String flag, boolean def) {
	if (groupFlags.containsKey(group)) {
	    Map<String, Boolean> gmap = groupFlags.get(group);
	    if (gmap.containsKey(flag)) {
		return gmap.get(flag);
	    }
	}
	if (parent != null) {
	    return parent.groupCheck(group, flag, def);
	}
	return def;
    }

    public boolean has(String flag, boolean def) {
	if (cuboidFlags.containsKey(flag)) {
	    return cuboidFlags.get(flag);
	}
	if (parent != null) {
	    return parent.has(flag, def);
	}
	return def;
    }

    public boolean isPlayerSet(String player, String flag) {
	Map<String, Boolean> flags = this.getPlayerFlags(player, false);
	if (flags == null)
	    return false;
	return flags.containsKey(flag);
    }

    public boolean inheritanceIsPlayerSet(String player, String flag) {
	Map<String, Boolean> flags = this.getPlayerFlags(player, false);
	if (flags == null) {
	    return parent == null ? false : parent.inheritanceIsPlayerSet(player, flag);
	}
	return flags.containsKey(flag) ? true : parent == null ? false : parent.inheritanceIsPlayerSet(player, flag);
    }

    public boolean isGroupSet(String group, String flag) {
	group = group.toLowerCase();
	Map<String, Boolean> flags = groupFlags.get(group);
	if (flags == null) {
	    return false;
	}
	return flags.containsKey(flag);
    }

    public boolean inheritanceIsGroupSet(String group, String flag) {
	group = group.toLowerCase();
	Map<String, Boolean> flags = groupFlags.get(group);
	if (flags == null) {
	    return parent == null ? false : parent.inheritanceIsGroupSet(group, flag);
	}
	return flags.containsKey(flag) ? true : parent == null ? false : parent.inheritanceIsGroupSet(group, flag);
    }

    public boolean isSet(String flag) {
	return cuboidFlags.containsKey(flag);
    }

    public boolean inheritanceIsSet(String flag) {
	return cuboidFlags.containsKey(flag) ? true : parent == null ? false : parent.inheritanceIsSet(flag);
    }

    public boolean checkValidFlag(String flag, boolean globalflag) {
	if (validFlags.contains(flag)) {
	    return true;
	}
	if (globalflag) {
	    if (validAreaFlags.contains(flag)) {
		return true;
	    }
	} else {
	    if (validPlayerFlags.contains(flag)) {
		return true;
	    }
	}
	return false;
    }

    public Map<String, Object> save() {
	Map<String, Object> root = new LinkedHashMap<>();
	root.put("LastKnownPlayerNames", cachedPlayerNameUUIDs);
	root.put("PlayerFlags", playerFlags);
	root.put("GroupFlags", groupFlags);
	root.put("AreaFlags", cuboidFlags);
	return root;
    }

    public static FlagPermissions load(Map<String, Object> root) throws Exception {
	FlagPermissions newperms = new FlagPermissions();
	return FlagPermissions.load(root, newperms);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected static FlagPermissions load(Map<String, Object> root, FlagPermissions newperms) throws Exception {
	if (root.containsKey("LastKnownPlayerNames"))
	    newperms.cachedPlayerNameUUIDs = (Map) root.get("LastKnownPlayerNames");
	newperms.playerFlags = (Map) root.get("PlayerFlags");
	newperms.groupFlags = (Map) root.get("GroupFlags");
	newperms.cuboidFlags = (Map) root.get("AreaFlags");

	String ownerName = null;
	String uuid = null;

	if (root.containsKey("OwnerLastKnownName")) {
	    ownerName = (String) root.get("OwnerLastKnownName");
	    uuid = (String) root.get("OwnerUUID");
	}

	newperms.convertPlayerNamesToUUIDs(ownerName, uuid);
	return newperms;
    }

    private void convertPlayerNamesToUUIDs(String OwnerName, String owneruuid) {
	HashMap<String, String> converts = new HashMap<>();

	List<String> Toremove = new ArrayList<String>();

	for (String keyset : playerFlags.keySet()) {
	    if (keyset.length() != 36) {
		String uuid = null;
		if (OwnerName != null && OwnerName.equalsIgnoreCase(keyset) && !owneruuid.equalsIgnoreCase("ffffffff-ffff-ffff-ffff-ffffffffffff"))
		    uuid = owneruuid;
		else
		    uuid = Residence.getPlayerUUIDString(keyset);
		//				if (OwnerName.equals(keyset)) {
		if (uuid != null)
		    converts.put(keyset, uuid);
		else if (OwnerName != null && !OwnerName.equalsIgnoreCase(keyset))
		    Toremove.add(keyset);
		//				}
	    } else {
		String pname = Residence.getPlayerName(keyset);
		if (pname != null)
		    this.cachedPlayerNameUUIDs.put(keyset, pname);
	    }

	}
	for (String one : Toremove) {
	    playerFlags.remove(one);
	}
	for (Entry<String, String> convert : converts.entrySet()) {
	    playerFlags.put(convert.getValue(), playerFlags.remove(convert.getKey()));
	    cachedPlayerNameUUIDs.put(convert.getValue(), convert.getKey());
	}
    }

    public String listFlags() {
	StringBuilder sbuild = new StringBuilder();
	Set<Entry<String, Boolean>> set = cuboidFlags.entrySet();
	synchronized (set) {
	    Iterator<Entry<String, Boolean>> it = set.iterator();
	    while (it.hasNext()) {
		Entry<String, Boolean> next = it.next();
		if (next.getValue()) {
		    sbuild.append("+").append(next.getKey());
		    if (it.hasNext()) {
			sbuild.append(" ");
		    }
		} else {
		    sbuild.append("-").append(next.getKey());
		    if (it.hasNext()) {
			sbuild.append(" ");
		    }
		}
	    }
	}
	if (sbuild.length() == 0) {
	    sbuild.append("none");
	}
	return sbuild.toString();
    }

    public String listPlayerFlags(String player) {
	Map<String, Boolean> flags = this.getPlayerFlags(player, false);
	if (flags != null) {
	    return this.printPlayerFlags(flags);
	} else {
	    return "none";
	}
    }

    protected String printPlayerFlags(Map<String, Boolean> flags) {
	StringBuilder sbuild = new StringBuilder();
	Set<Entry<String, Boolean>> set = flags.entrySet();
	synchronized (flags) {
	    Iterator<Entry<String, Boolean>> it = set.iterator();
	    while (it.hasNext()) {
		Entry<String, Boolean> next = it.next();
		if (next.getValue()) {
		    sbuild.append("+").append(next.getKey());
		    if (it.hasNext()) {
			sbuild.append(" ");
		    }
		} else {
		    sbuild.append("-").append(next.getKey());
		    if (it.hasNext()) {
			sbuild.append(" ");
		    }
		}
	    }
	}
	if (sbuild.length() == 0) {
	    sbuild.append("none");
	}
	return sbuild.toString();
    }

    public String listOtherPlayersFlags(String player) {
	player = player.toLowerCase();
	String uuids = Residence.getPlayerUUIDString(player);
	StringBuilder sbuild = new StringBuilder();
	Set<Entry<String, Map<String, Boolean>>> set = playerFlags.entrySet();
	synchronized (set) {
	    Iterator<Entry<String, Map<String, Boolean>>> it = set.iterator();
	    while (it.hasNext()) {
		Entry<String, Map<String, Boolean>> nextEnt = it.next();
		String next = nextEnt.getKey();
		if (!next.equals(player) && !next.equals(uuids)) {
		    String perms = printPlayerFlags(nextEnt.getValue());
		    if (next.length() == 36) {
			String resolvedName = Residence.getPlayerName(next);
			if (resolvedName != null) {
			    this.cachedPlayerNameUUIDs.put(next, resolvedName);
			    next = resolvedName;
			} else if (this.cachedPlayerNameUUIDs.containsKey(next))
			    next = this.cachedPlayerNameUUIDs.get(next);
		    }
		    if (!perms.equals("none")) {
			sbuild.append(next).append("[").append(ChatColor.DARK_AQUA).append(perms).append(ChatColor.RED).append("] ");
		    }
		}
	    }
	}
	return sbuild.toString();
    }

    public String listGroupFlags() {
	StringBuilder sbuild = new StringBuilder();
	Set<String> set = groupFlags.keySet();
	synchronized (set) {
	    Iterator<String> it = set.iterator();
	    while (it.hasNext()) {
		String next = it.next();
		String perms = listGroupFlags(next);
		if (!perms.equals("none")) {
		    sbuild.append(next).append("[").append(ChatColor.DARK_AQUA).append(perms).append(ChatColor.RED).append("] ");
		}
	    }
	}
	return sbuild.toString();
    }

    public String listGroupFlags(String group) {
	group = group.toLowerCase();
	if (groupFlags.containsKey(group)) {
	    StringBuilder sbuild = new StringBuilder();
	    Map<String, Boolean> get = groupFlags.get(group);
	    Set<Entry<String, Boolean>> set = get.entrySet();
	    synchronized (get) {
		Iterator<Entry<String, Boolean>> it = set.iterator();
		while (it.hasNext()) {
		    Entry<String, Boolean> next = it.next();
		    if (next.getValue()) {
			sbuild.append("+").append(next.getKey());
			if (it.hasNext()) {
			    sbuild.append(" ");
			}
		    } else {
			sbuild.append("-").append(next.getKey());
			if (it.hasNext()) {
			    sbuild.append(" ");
			}
		    }
		}
	    }
	    if (sbuild.length() == 0) {
		groupFlags.remove(group);
		sbuild.append("none");
	    }
	    return sbuild.toString();
	} else {
	    return "none";
	}
    }

    public void clearFlags() {
	groupFlags.clear();
	playerFlags.clear();
	cuboidFlags.clear();
    }

    public void printFlags(Player player) {
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Flags") + ":" + ChatColor.BLUE + " " + listFlags());
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Your.Flags") + ":" + ChatColor.GREEN + " " + listPlayerFlags(player.getName()));
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Group.Flags") + ":" + ChatColor.RED + " " + listGroupFlags());
	player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Others.Flags") + ":" + ChatColor.RED + " " + listOtherPlayersFlags(player.getName()));
    }

    public void copyUserPermissions(String fromUser, String toUser) {
	Map<String, Boolean> get = this.getPlayerFlags(fromUser, false);
	if (get != null) {
	    Map<String, Boolean> targ = this.getPlayerFlags(toUser, true);
	    for (Entry<String, Boolean> entry : get.entrySet()) {
		targ.put(entry.getKey(), entry.getValue());
	    }
	}
    }

    @Deprecated //Seemed to be a duplicate function of removeAllPlayerFlags()... deprecating
    public void clearPlayersFlags(String user) {
	this.removeAllPlayerFlags(user);
    }

    public void setParent(FlagPermissions p) {
	parent = p;
    }

    public FlagPermissions getParent() {
	return parent;
    }
}
