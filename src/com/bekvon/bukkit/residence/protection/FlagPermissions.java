/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.protection;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.permissions.Permission;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.persistance.SQLManager;

/**
 *
 * @author Administrator
 */
public class FlagPermissions {

	protected static ArrayList<String> validFlags = new ArrayList<String>();
	protected static ArrayList<String> validPlayerFlags = new ArrayList<String>();
	protected static ArrayList<String> validAreaFlags = new ArrayList<String>();
	protected static SQLManager sql;
	final static Map<Material,String> matUseFlagList = new EnumMap<Material,String>(Material.class) {
		{
			put(Material.DIODE,"diode");
			put(Material.DIODE_BLOCK_OFF, "diode");
			put(Material.DIODE_BLOCK_ON, "diode");
			put(Material.WORKBENCH, "table");
			put(Material.WOODEN_DOOR,"door");
			put(Material.FENCE_GATE, "door");
			put(Material.NETHER_FENCE, "door");
			put(Material.TRAP_DOOR, "door");
			put(Material.ENCHANTMENT_TABLE, "enchant");
			put(Material.STONE_BUTTON, "button");
			put(Material.LEVER, "lever");
			put(Material.BED_BLOCK, "bed");
			put(Material.BREWING_STAND, "brew");
			put(Material.CAKE, "cake");
			put(Material.NOTE_BLOCK, "note");
			put(Material.DRAGON_EGG, "egg");
			put(Material.EGG, "egg");
			put(Material.JUKEBOX, "container");
			put(Material.CHEST, "container");
			put(Material.FURNACE, "container");
			put(Material.BURNING_FURNACE, "container");
			put(Material.DISPENSER, "container");
			put(Material.CAKE_BLOCK, "container");
		}
	};

	public void addMaterialToUseFlag(Material mat, String flag) {
		matUseFlagList.put(mat, flag);
	}
	public void removeMaterialFromUseFlag(Material mat) {
		matUseFlagList.remove(mat);
	}
	public static EnumMap<Material,String> getMaterialToUseList(){
		return (EnumMap<Material, String>) matUseFlagList;
	}
	public static void addFlag(String flag) {
		flag = flag.toLowerCase();
		if(!validFlags.contains(flag)) {
			validFlags.add(flag);
		}
		if(ResidencePermissions.validFlagGroups.containsKey(flag)) {
			ResidencePermissions.validFlagGroups.remove(flag);
		}
		Residence.getServ().getPluginManager().addPermission(new Permission("residence."+flag));
		sql.addSQLFlag(true, true, true,flag);
	}
	public static void addPlayerOrGroupOnlyFlag(String flag) {
		flag = flag.toLowerCase();
		if(!validPlayerFlags.contains(flag)) {
			validPlayerFlags.add(flag);
		}
		if(ResidencePermissions.validFlagGroups.containsKey(flag)) {
			ResidencePermissions.validFlagGroups.remove(flag);
		}
		Residence.getServ().getPluginManager().addPermission(new Permission("residence."+flag));
		sql.addSQLFlag(true,true,false,flag);
	}
	public static void addResidenceOnlyFlag(String flag) {
		flag = flag.toLowerCase();
		if(!validAreaFlags.contains(flag)) {
			validAreaFlags.add(flag);
		}
		if(ResidencePermissions.validFlagGroups.containsKey(flag)) {
			ResidencePermissions.validFlagGroups.remove(flag);
		}
		Residence.getServ().getPluginManager().addPermission(new Permission("residence."+flag));
		sql.addSQLFlag(false,false,true,flag);
	}
	public static void initValidFlags() {
		validAreaFlags.clear();
		validPlayerFlags.clear();
		validFlags.clear();
		ResidencePermissions.validFlagGroups.clear();
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
		addResidenceOnlyFlag("flow");
		addResidenceOnlyFlag("healing");
		addResidenceOnlyFlag("animals");
		addResidenceOnlyFlag("lavaflow");
		addResidenceOnlyFlag("waterflow");
		addResidenceOnlyFlag("physics");
		addResidenceOnlyFlag("piston");
		addResidenceOnlyFlag("spread");
		addResidenceOnlyFlag("hidden");
		addPlayerOrGroupOnlyFlag("admin");
		ResidencePermissions.addFlagToFlagGroup("redstone", "note");
		ResidencePermissions.addFlagToFlagGroup("redstone", "pressure");
		ResidencePermissions.addFlagToFlagGroup("redstone", "lever");
		ResidencePermissions.addFlagToFlagGroup("redstone", "button");
		ResidencePermissions.addFlagToFlagGroup("redstone", "diode");
		ResidencePermissions.addFlagToFlagGroup("craft", "brew");
		ResidencePermissions.addFlagToFlagGroup("craft", "table");
		ResidencePermissions.addFlagToFlagGroup("craft", "enchant");
		ResidencePermissions.addFlagToFlagGroup("trusted", "use");
		ResidencePermissions.addFlagToFlagGroup("trusted", "move");
		ResidencePermissions.addFlagToFlagGroup("trusted", "tp");
		ResidencePermissions.addFlagToFlagGroup("trusted", "build");
		ResidencePermissions.addFlagToFlagGroup("trusted", "container");
		ResidencePermissions.addFlagToFlagGroup("trusted", "bucket");
		ResidencePermissions.addFlagToFlagGroup("fire", "ignite");
		ResidencePermissions.addFlagToFlagGroup("fire", "firespread");
	}
	protected FlagPermissions parent;
	protected Entry<Boolean, Object> type;

	public FlagPermissions() {
		sql = Residence.getSQLManager();
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

	public String listFlags() {
		StringBuilder sbuild = new StringBuilder();
		Set<Entry<String, Boolean>> set = getAreaFlags().entrySet();
		Iterator<Entry<String, Boolean>> it = set.iterator();
		while(it.hasNext()) {
			Entry<String, Boolean> next = it.next();
			if(next.getValue()) {
				sbuild.append("+").append(next.getKey());
				if(it.hasNext()) {
					sbuild.append(" ");
				}
			} else {
				sbuild.append("-").append(next.getKey());
				if(it.hasNext()) {
					sbuild.append(" ");
				}
			}
		}
		if(sbuild.length() == 0) {
			sbuild.append("none");
		}
		return sbuild.toString();
	}

	public String listPlayerFlags(String player) {
		player = player.toLowerCase();
		StringBuilder sbuild = new StringBuilder();
		Map<String, Boolean> get = getPlayerFlags().get(player);
		if(get != null) {
			Set<Entry<String, Boolean>> set = get.entrySet();
			Iterator<Entry<String, Boolean>> it = set.iterator();
			while(it.hasNext()) {
				Entry<String, Boolean> next = it.next();
				if(next.getValue()) {
					sbuild.append("+").append(next.getKey());
					if(it.hasNext()) {
						sbuild.append(" ");
					}
				} else {
					sbuild.append("-").append(next.getKey());
					if(it.hasNext()) {
						sbuild.append(" ");
					}
				}
			}
			if(sbuild.length()==0) {
				sbuild.append("none");
			}
			return sbuild.toString();
		} else {
			return "none";
		}
	}

	public String listOtherPlayersFlags(String player) {
		player = player.toLowerCase();
		StringBuilder sbuild = new StringBuilder();
		Set<String> set = getPlayerFlags().keySet();
		Iterator<String> it = set.iterator();
		while(it.hasNext()) {
			String next = it.next();
			if(!next.equals(player)) {
				String perms = listPlayerFlags(next);
				if(!perms.equals("none")) {
					sbuild.append(next).append("["+ChatColor.DARK_AQUA).append(perms).append(ChatColor.RED+"] ");
				}
			}
		}
		return sbuild.toString();
	}

	public String listGroupFlags() {
		StringBuilder sbuild = new StringBuilder();
		Set<String> set = getGroupFlags().keySet();
		Iterator<String> it = set.iterator();
		while(it.hasNext()) {
			String next = it.next();
			String perms = listGroupFlags(next);
			if(!perms.equals("none")) {
				sbuild.append(next).append("["+ChatColor.DARK_AQUA).append(perms).append(ChatColor.RED+"] ");
			}
		}
		return sbuild.toString();
	}

	public String listGroupFlags(String group) {
		group = group.toLowerCase();
		Map<String, Boolean> get = getGroupFlags().get(group);
		if(get != null) {
			StringBuilder sbuild = new StringBuilder();
			Set<Entry<String, Boolean>> set = get.entrySet();
			Iterator<Entry<String, Boolean>> it = set.iterator();
			while(it.hasNext()) {
				Entry<String, Boolean> next = it.next();
				if(next.getValue()) {
					sbuild.append("+").append(next.getKey());
					if(it.hasNext()) {
						sbuild.append(" ");
					}
				} else {
					sbuild.append("-").append(next.getKey());
					if(it.hasNext()) {
						sbuild.append(" ");
					}
				}
			}
			if(sbuild.length()==0) {
				sbuild.append("none");
			}
			return sbuild.toString();
		} else {
			return "none";
		}
	}

	public void setParent(FlagPermissions p) {
		parent = p;
	}

	public FlagPermissions getParent() {
		return parent;
	}

	public void clearFlags() {
		//TODO DEBUG
		System.out.println("Shouldn't see this when done");
	}

	public boolean isGroupSet(String group, String flag) {
		//TODO DEBUG
		System.out.println("Shouldn't see this when done");
		return false;
	}
	public boolean playerHas(String player, String flag, boolean def) {
		//TODO DEBUG
		System.out.println("Shouldn't see this when done");
		return false;
	}
	public boolean groupHas(String group, String flag, boolean def) {
		//TODO DEBUG
		System.out.println("Shouldn't see this when done");
		return false;
	}
	public boolean has(String flag, boolean def) {
		//TODO DEBUG
		System.out.println("Shouldn't see this when done");
		return false;
	}
	public boolean setFlag(String flag, FlagState state) {
		//TODO DEBUG
		System.out.println("Shouldn't see this when done");
		return false;
	}
	public boolean setGroupFlag(String group, String flag, FlagState state) {
		//TODO DEBUG
		System.out.println("Shouldn't see this when done");
		return false;
	}
	public void removeAllGroupFlags(String group) {
		//TODO DEBUG
		System.out.println("Shouldn't see this when done");
	}
	public boolean setPlayerFlag(String string, String string2, FlagState state) {
		//TODO DEBUG
		System.out.println("Shouldn't see this when done");
		return false;
	}
	public Map<String, Boolean> getAreaFlags() {
		//TODO DEBUG
		System.out.println("Shouldn't see this when done");
		return null;
	}
	public Map<String, Map<String, Boolean>> getPlayerFlags() {
		//TODO DEBUG
		System.out.println("Shouldn't see this when done");
		return null;
	}
	public Map<String, Map<String, Boolean>> getGroupFlags() {
		//TODO DEBUG
		System.out.println("Shouldn't see this when done");
		return null;
	}
}
