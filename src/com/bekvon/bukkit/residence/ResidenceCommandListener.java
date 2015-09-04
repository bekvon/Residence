/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;

import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.event.ResidenceCommandEvent;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.selection.WorldGuardUtil;
import com.bekvon.bukkit.residence.spout.ResidenceSpout;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ResidenceCommandListener extends Residence {

    public static HashMap<String, ClaimedResidence> teleportMap = new HashMap<String, ClaimedResidence>();
    public static List<String> teleportDelayMap = new ArrayList<String>();

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	ResidenceCommandEvent cevent = new ResidenceCommandEvent(command.getName(), args, sender);
	server.getPluginManager().callEvent(cevent);
	if (cevent.isCancelled()) {
	    return true;
	}
	if (command.getName().equals("resreload") && args.length == 0) {
	    if (sender instanceof Player) {
		Player player = (Player) sender;
		if (Residence.getPermissionManager().isResidenceAdmin(player)) {
		    this.reloadPlugin();
		    sender.sendMessage(ChatColor.GREEN + "[Residence] Reloaded config.");
		    System.out.println("[Residence] Reloaded by " + player.getName() + ".");
		}
	    } else {
		this.reloadPlugin();
		System.out.println("[Residence] Reloaded by console.");
	    }
	    return true;
	}
	if (command.getName().equals("resload")) {
	    if (!(sender instanceof Player) || sender instanceof Player && gmanager.isResidenceAdmin((Player) sender)) {
		try {
		    this.loadYml();
		    sender.sendMessage(ChatColor.GREEN + "[Residence] Reloaded save file...");
		} catch (Exception ex) {
		    sender.sendMessage(ChatColor.RED + "[Residence] Unable to reload the save file, exception occured!");
		    sender.sendMessage(ChatColor.RED + ex.getMessage());
		    Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
		}
	    }
	    return true;
	} else if (command.getName().equals("resworld")) {
	    if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
		if (sender instanceof ConsoleCommandSender) {
		    rmanager.removeAllFromWorld(sender, args[1]);
		    return true;
		} else {
		    sender.sendMessage(ChatColor.RED + "MUST be run from console.");
		}
	    }
	    return false;
	} else if (command.getName().equals("rc")) {
	    if (sender instanceof Player) {
		Player player = (Player) sender;
		String pname = player.getName();
		if (cmanager.chatEnabled()) {
		    if (args.length == 0) {
			plistener.tooglePlayerResidenceChat(player);
		    } else {
			String area = plistener.getCurrentResidenceName(pname);
			if (area != null) {
			    ChatChannel channel = chatmanager.getChannel(area);
			    if (channel != null) {
				String message = "";
				for (String arg : args) {
				    message = message + " " + arg;
				}
				channel.chat(pname, message);
			    } else {
				player.sendMessage(ChatColor.RED + language.getPhrase("InvalidChannel"));
			    }
			} else {
			    player.sendMessage(ChatColor.RED + language.getPhrase("NotInResidence"));
			}
		    }
		} else {
		    player.sendMessage(ChatColor.RED + language.getPhrase("ChatDisabled"));
		}
	    }
	    return true;
	} else if (command.getName().equals("res") || command.getName().equals("residence") || command.getName().equals("resadmin")) {
	    boolean resadmin = false;
	    if (sender instanceof Player) {
		if (command.getName().equals("resadmin") && gmanager.isResidenceAdmin((Player) sender)) {
		    resadmin = true;
		}
		if (command.getName().equals("resadmin") && !gmanager.isResidenceAdmin((Player) sender)) {
		    ((Player) sender).sendMessage(ChatColor.RED + language.getPhrase("NonAdmin"));
		    return true;
		}
	    } else {
		resadmin = true;
	    }
	    return commandRes(args, resadmin, command, sender);
	}
	return super.onCommand(sender, command, label, args);
    }

    @SuppressWarnings("deprecation")
    private boolean commandRes(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (args.length > 0 && args[args.length - 1].equalsIgnoreCase("?") || args.length > 1 && args[args.length - 2].equals("?")) {
	    return commandHelp(args, resadmin, sender);
	}
	int page = 1;
	try {
	    if (args.length > 0) {
		page = Integer.parseInt(args[args.length - 1]);
	    }
	} catch (Exception ex) {
	}
	Player player = null;
	PermissionGroup group = null;
	String pname = null;
	if (sender instanceof Player) {
	    player = (Player) sender;
	    group = Residence.getPermissionManager().getGroup(player);
	    pname = player.getName();
	} else {
	    resadmin = true;
	}
	if (cmanager.allowAdminsOnly()) {
	    if (!resadmin) {
		player.sendMessage(ChatColor.RED + language.getPhrase("AdminOnly"));
		return true;
	    }
	}
	if (args.length == 0) {
	    return false;
	}
	if (args.length == 0) {
	    args = new String[1];
	    args[0] = "?";
	}
	String cmd = args[0].toLowerCase();
	if (cmd.equals("remove") || cmd.equals("delete")) {
	    return commandResRemove(args, resadmin, sender, page);
	}
	if (cmd.equals("confirm")) {
	    return commandResConfirm(args, resadmin, sender, page);
	}
	if (cmd.equals("version")) {
	    sender.sendMessage(ChatColor.GRAY + "------------------------------------");
	    sender.sendMessage(ChatColor.RED + "This server running " + ChatColor.GOLD + "Residence" + ChatColor.RED + " version: " + ChatColor.BLUE + this
		.getDescription().getVersion());
	    sender.sendMessage(ChatColor.GREEN + "Created by: " + ChatColor.YELLOW + "bekvon");
	    sender.sendMessage(ChatColor.GREEN + "Updated to 1.8 by: " + ChatColor.YELLOW + "DartCZ");
	    String names = null;
	    List<String> authlist = this.getDescription().getAuthors();
	    for (String auth : authlist) {
		if (names == null)
		    names = auth;
		else
		    names = names + ", " + auth;
	    }
	    sender.sendMessage(ChatColor.GREEN + "Authors: " + ChatColor.YELLOW + names);
	    sender.sendMessage(ChatColor.DARK_AQUA + "For a command list, and help, see the wiki:");
	    sender.sendMessage(ChatColor.GREEN + "http://residencebukkitmod.wikispaces.com/");
	    sender.sendMessage(ChatColor.AQUA + "Visit the Spigot Resource page at:");
	    sender.sendMessage(ChatColor.BLUE + "http://www.spigotmc.org/resources/residence-reloaded-1-8.2697/");
	    sender.sendMessage(ChatColor.GRAY + "------------------------------------");
	    return true;
	}
	if (cmd.equals("setowner") && args.length == 3) {
	    if (!resadmin) {
		sender.sendMessage(ChatColor.RED + language.getPhrase("NoPermission"));
		return true;
	    }
	    ClaimedResidence area = rmanager.getByName(args[1]);
	    if (area != null) {
		area.getPermissions().setOwner(args[2], true);
		if (area.getParent() == null) {
		    sender.sendMessage(ChatColor.GREEN + language.getPhrase("ResidenceOwnerChange", ChatColor.YELLOW + " " + args[1] + " " + ChatColor.GREEN + "."
			+ ChatColor.YELLOW + args[2] + ChatColor.GREEN));
		} else {
		    sender.sendMessage(ChatColor.GREEN + language.getPhrase("SubzoneOwnerChange", ChatColor.YELLOW + " " + args[1].split("\\.")[args[1].split(
			"\\.").length - 1] + " " + ChatColor.GREEN + "." + ChatColor.YELLOW + args[2] + ChatColor.GREEN));
		}
	    } else {
		sender.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    }
	    return true;
	}
	if (player == null) {
	    return true;
	}
	if (command.getName().equals("resadmin")) {
	    if (args.length == 1 && args[0].equals("on")) {
		resadminToggle.add(player.getName());
		player.sendMessage(ChatColor.YELLOW + language.getPhrase("AdminToggle", language.getPhrase("TurnOn")));
		return true;
	    } else if (args.length == 1 && args[0].equals("off")) {
		resadminToggle.remove(player.getName());
		player.sendMessage(ChatColor.YELLOW + language.getPhrase("AdminToggle", language.getPhrase("TurnOff")));
		return true;
	    }
	}
	if (!resadmin && resadminToggle.contains(player.getName())) {
	    if (!gmanager.isResidenceAdmin(player)) {
		resadminToggle.remove(player.getName());
	    }
	}
	if (cmd.equals("select")) {
	    return commandResSelect(args, resadmin, player, page);
	}

	if (cmd.equals("expand")) {
	    return commandResExpand(args, resadmin, player, page);
	}

	if (cmd.equals("contract")) {
	    return commandResContract(args, resadmin, player, page);
	}

	if (cmd.equals("create")) {
	    return commandResCreate(args, resadmin, player, page);
	}
	if (cmd.equals("subzone") || cmd.equals("sz")) {
	    return commandResSubzone(args, resadmin, player, page);
	}
	if (cmd.equals("gui")) {
	    return commandResGui(args, resadmin, player, page);
	}
	if (cmd.equals("sublist")) {
	    return commandResSublist(args, resadmin, player, page);
	}
	if (cmd.equals("removeall")) {
	    if (args.length != 2) {
		return false;
	    }
	    if (resadmin || args[1].endsWith(pname)) {
		rmanager.removeAllByOwner(player, args[1]);
		player.sendMessage(ChatColor.GREEN + language.getPhrase("RemovePlayersResidences", ChatColor.YELLOW + args[1] + ChatColor.GREEN));
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("NoPermission"));
	    }
	    return true;
	}
	if (cmd.equals("compass")) {
	    return commandResCompass(args, resadmin, player, page);
	}
	if (cmd.equals("area")) {
	    return commandResArea(args, resadmin, player, page);
	}
	if (cmd.equals("lists")) {
	    return commandResList(args, resadmin, player, page);
	}
	if (cmd.equals("default")) {
	    if (args.length == 2) {
		ClaimedResidence res = rmanager.getByName(args[1]);
		res.getPermissions().applyDefaultFlags(player, resadmin);
		return true;
	    }
	    return false;
	}
	if (cmd.equals("limits")) {
	    if (args.length == 1) {
		gmanager.getGroup(player).printLimits(player);
		return true;
	    }
	    return false;
	}
	if (cmd.equals("info")) {
	    if (args.length == 1) {
		String area = rmanager.getNameByLoc(player.getLocation());
		if (area != null) {
		    rmanager.printAreaInfo(area, player);
		} else {
		    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
		}
		return true;
	    } else if (args.length == 2) {
		rmanager.printAreaInfo(args[1], player);
		return true;
	    }
	    return false;
	}
	if (cmd.equals("check")) {
	    if (args.length == 3 || args.length == 4) {
		if (args.length == 4) {
		    pname = args[3];
		}
		ClaimedResidence res = rmanager.getByName(args[1]);
		if (res == null) {
		    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
		    return true;
		}
		if (!res.getPermissions().hasApplicableFlag(pname, args[2])) {
		    player.sendMessage(language.getPhrase("FlagCheckFalse", ChatColor.YELLOW + args[2] + ChatColor.RED + "." + ChatColor.YELLOW + pname + ChatColor.RED
			+ "." + ChatColor.YELLOW + args[1] + ChatColor.RED));
		} else {
		    player.sendMessage(language.getPhrase("FlagCheckTrue", ChatColor.GREEN + args[2] + ChatColor.YELLOW + "." + ChatColor.GREEN + pname + ChatColor.YELLOW
			+ "." + ChatColor.YELLOW + args[1] + ChatColor.RED + "." + (res.getPermissions().playerHas(pname, res.getPermissions().getWorld(), args[2], false)
			    ? ChatColor.GREEN + "TRUE" : ChatColor.RED + "FALSE")));
		}
		return true;
	    }
	    return false;
	}
	if (cmd.equals("current")) {
	    if (args.length != 1) {
		return false;
	    }
	    String res = rmanager.getNameByLoc(player.getLocation());
	    if (res == null) {
		player.sendMessage(ChatColor.RED + language.getPhrase("NotInResidence"));
	    } else {
		player.sendMessage(ChatColor.GREEN + language.getPhrase("InResidence", ChatColor.YELLOW + res + ChatColor.GREEN));
	    }
	    return true;
	}
	if (cmd.equals("set")) {
	    return commandResSet(args, resadmin, player, page);
	}
	if (cmd.equals("pset")) {
	    return commandResPset(args, resadmin, player, page);
	}
	if (cmd.equals("gset")) {
	    return commandResGset(args, resadmin, player, page);
	}
	if (cmd.equals("lset")) {
	    return commandResLset(args, resadmin, player, page);
	}
	if (cmd.equals("list")) {
	    if (args.length == 1) {
		rmanager.listResidences(player);
		return true;
	    } else if (args.length == 2) {
		try {
		    Integer.parseInt(args[1]);
		    rmanager.listResidences(player, page);
		} catch (Exception ex) {
		    rmanager.listResidences(player, args[1]);
		}
		return true;
	    } else if (args.length == 3) {
		rmanager.listResidences(player, args[1], page);
		return true;
	    }
	    return false;
	}
	if (cmd.equals("listhidden")) {
	    if (!resadmin) {
		player.sendMessage(ChatColor.RED + language.getPhrase("NoPermission"));
		return true;
	    }
	    if (args.length == 1) {
		rmanager.listResidences(player, 1, true);
		return true;
	    } else if (args.length == 2) {
		try {
		    Integer.parseInt(args[1]);
		    rmanager.listResidences(player, page, true);
		} catch (Exception ex) {
		    rmanager.listResidences(player, args[1], 1, true);
		}
		return true;
	    } else if (args.length == 3) {
		rmanager.listResidences(player, args[1], page, true);
		return true;
	    }
	    return false;
	}
	if (cmd.equals("rename")) {
	    if (args.length == 3) {
		rmanager.renameResidence(player, args[1], args[2], resadmin);
		return true;
	    }
	    return false;
	}
	if (cmd.equals("renamearea")) {
	    if (args.length == 4) {
		ClaimedResidence res = rmanager.getByName(args[1]);
		if (res == null) {
		    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
		    return true;
		}
		res.renameArea(player, args[2], args[3], resadmin);
		return true;
	    }
	    return false;
	}
	if (cmd.equals("unstuck")) {
	    if (args.length != 1) {
		return false;
	    }
	    group = gmanager.getGroup(player);
	    if (!group.hasUnstuckAccess()) {
		player.sendMessage(ChatColor.RED + language.getPhrase("NoPermission"));
		return true;
	    }
	    ClaimedResidence res = rmanager.getByLoc(player.getLocation());
	    if (res == null) {
		player.sendMessage(ChatColor.RED + language.getPhrase("NotInResidence"));
	    } else {
		player.sendMessage(ChatColor.YELLOW + language.getPhrase("Moved") + "...");
		player.teleport(res.getOutsideFreeLoc(player.getLocation()));
	    }
	    return true;
	}
	if (cmd.equals("kick")) {
	    if (args.length != 2) {
		return false;
	    }
	    Player targetplayer = Bukkit.getPlayer(args[1]);
	    if (targetplayer == null) {

	    }
	    group = gmanager.getGroup(player);
	    if (!group.hasKickAccess()) {
		player.sendMessage(ChatColor.RED + language.getPhrase("NoPermission"));
		return true;
	    }
	    ClaimedResidence res = rmanager.getByLoc(targetplayer.getLocation());
	    if (res.getOwner().equals(player.getName())) {
		if (res.getPlayersInResidence().contains(targetplayer)) {
		    targetplayer.teleport(res.getOutsideFreeLoc(player.getLocation()));
		    targetplayer.sendMessage(ChatColor.RED + language.getPhrase("Kicked") + "!");
		}
	    }

	}
	if (cmd.equals("mirror")) {
	    if (args.length != 3) {
		return false;
	    }
	    rmanager.mirrorPerms(player, args[2], args[1], resadmin);
	    return true;
	}
	if (cmd.equals("listall")) {
	    if (args.length == 1) {
		rmanager.listAllResidences(player, 1);
	    } else if (args.length == 2) {
		try {
		    rmanager.listAllResidences(player, page);
		} catch (Exception ex) {
		}
	    } else {
		return false;
	    }
	    return true;
	}
	if (cmd.equals("listallhidden")) {
	    if (!resadmin) {
		player.sendMessage(ChatColor.RED + language.getPhrase("NoPermission"));
		return true;
	    }
	    if (args.length == 1) {
		rmanager.listAllResidences(player, 1, true);
	    } else if (args.length == 2) {
		try {
		    rmanager.listAllResidences(player, page, true);
		} catch (Exception ex) {
		}
	    } else {
		return false;
	    }
	    return true;
	}
	if (cmd.equals("material")) {
	    if (args.length != 2) {
		return false;
	    }
	    try {
		player.sendMessage(ChatColor.GREEN + language.getPhrase("MaterialGet", ChatColor.GOLD + args[1] + ChatColor.GREEN + "." + ChatColor.RED + Material
		    .getMaterial(Integer.parseInt(args[1])).name() + ChatColor.GREEN));
	    } catch (Exception ex) {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidMaterial"));
	    }
	    return true;
	}
	if (cmd.equals("tpset")) {
	    ClaimedResidence res = rmanager.getByLoc(player.getLocation());
	    if (res != null) {
		res.setTpLoc(player, resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    }
	    return true;
	}
	if (cmd.equals("tp")) {
	    if (args.length != 2) {
		return false;
	    }
	    ClaimedResidence res = rmanager.getByName(args[1]);
	    if (res == null) {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
		return true;
	    }
	    res.tpToResidence(player, player, resadmin);
	    return true;
	}

	if (cmd.equals("tpconfirm")) {
	    if (args.length != 1) {
		return false;
	    }
	    if (teleportMap.containsKey(player.getName())) {
		teleportMap.get(player.getName()).tpToResidence(player, player, resadmin);
		teleportMap.remove(player.getName());
	    } else
		player.sendMessage(ChatColor.RED + language.getPhrase("NoTeleportConfirm"));
	    return true;
	}

	if (cmd.equals("lease")) {
	    return commandResLease(args, resadmin, player, page);
	}
	if (cmd.equals("bank")) {
	    return commandResBank(args, resadmin, player, page);
	}
	if (cmd.equals("market")) {
	    return commandResMarket(args, resadmin, player, page);
	}
	if (cmd.equals("message")) {
	    return commandResMessage(args, resadmin, player, page);
	}
	if (cmd.equals("give") && args.length == 3) {
	    rmanager.giveResidence(player, args[2], args[1], resadmin);
	    return true;
	}
	if (cmd.equals("server")) {
	    if (!resadmin) {
		player.sendMessage(ChatColor.RED + language.getPhrase("NoPermission"));
		return true;
	    }
	    if (args.length == 2) {
		ClaimedResidence res = rmanager.getByName(args[1]);
		if (res == null) {
		    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
		    return true;
		}
		res.getPermissions().setOwner("Server Land", false);
		player.sendMessage(ChatColor.GREEN + language.getPhrase("ResidenceOwnerChange", ChatColor.YELLOW + args[1] + ChatColor.GREEN + "." + ChatColor.YELLOW
		    + "Server Land" + ChatColor.GREEN));
		return true;
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
		return true;
	    }
	}
	if (cmd.equals("clearflags")) {
	    if (!resadmin) {
		player.sendMessage(ChatColor.RED + language.getPhrase("NoPermission"));
		return true;
	    }
	    ClaimedResidence area = rmanager.getByName(args[1]);
	    if (area != null) {
		area.getPermissions().clearFlags();
		player.sendMessage(ChatColor.GREEN + language.getPhrase("FlagsCleared"));
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    }
	    return true;
	}
	if (cmd.equals("tool")) {
	    player.sendMessage(ChatColor.YELLOW + language.getPhrase("SelectionTool") + ":" + ChatColor.GREEN + Material.getMaterial(cmanager.getSelectionTooldID()));
	    player.sendMessage(ChatColor.YELLOW + language.getPhrase("InfoTool") + ": " + ChatColor.GREEN + Material.getMaterial(cmanager.getInfoToolID()));
	    return true;
	}
	return false;
    }

    private boolean commandHelp(String[] args, boolean resadmin, CommandSender sender) {
	if (helppages != null) {
	    String helppath = "res";
	    for (int i = 0; i < args.length; i++) {
		if (args[i].equalsIgnoreCase("?")) {
		    break;
		}
		helppath = helppath + "." + args[i];
	    }
	    int page = 1;
	    if (!args[args.length - 1].equalsIgnoreCase("?")) {
		try {
		    page = Integer.parseInt(args[args.length - 1]);
		} catch (Exception ex) {
		    sender.sendMessage(ChatColor.RED + language.getPhrase("InvalidHelp"));
		}
	    }
	    if (helppages.containesEntry(helppath)) {
		helppages.printHelp(sender, page, helppath);
		return true;
	    }
	}
	return false;
    }

    private boolean commandResExpand(String[] args, boolean resadmin, Player player, int page) {
	String resName;
	String areaName;
	ClaimedResidence res = null;
	res = rmanager.getByLoc(player.getLocation());
	if (res == null) {
	    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    return true;
	}
	resName = res.getName();
	CuboidArea area = null;
	areaName = res.getAreaIDbyLoc(player.getLocation());
	area = res.getArea(areaName);
	if (area != null) {
	    smanager.placeLoc1(player, area.getHighLoc());
	    smanager.placeLoc2(player, area.getLowLoc());
	    player.sendMessage(ChatColor.GREEN + language.getPhrase("SelectionArea", ChatColor.GOLD + areaName + ChatColor.GREEN + "." + ChatColor.GOLD + resName
		+ ChatColor.GREEN));
	} else {
	    player.sendMessage(ChatColor.RED + language.getPhrase("AreaNonExist"));
	}
	int amount;
	try {
	    amount = Integer.parseInt(args[1]);
	} catch (Exception ex) {
	    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidAmount"));
	    return true;
	}

	if (amount < 0)
	    amount = amount * -1;

	smanager.modify(player, false, amount);

	if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
	    if (Residence.wep != null) {
		if (Residence.wepid == Residence.getConfigManager().selectionToolId) {
		    smanager.worldEdit(player);
		}
	    }
	    res.replaceArea(player, new CuboidArea(Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager().getPlayerLoc2(
		player.getName())), areaName,
		resadmin);
	    return true;
	} else {
	    player.sendMessage(ChatColor.RED + language.getPhrase("SelectPoints"));
	}
	return false;
    }

    private boolean commandResContract(String[] args, boolean resadmin, Player player, int page) {
	String resName;
	String areaName;
	ClaimedResidence res = null;
	res = rmanager.getByLoc(player.getLocation());
	if (res == null) {
	    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    return true;
	}
	resName = res.getName();
	CuboidArea area = null;
	areaName = res.getAreaIDbyLoc(player.getLocation());
	area = res.getArea(areaName);
	if (area != null) {
	    smanager.placeLoc1(player, area.getHighLoc());
	    smanager.placeLoc2(player, area.getLowLoc());
	    player.sendMessage(ChatColor.GREEN + language.getPhrase("SelectionArea", ChatColor.GOLD + areaName + ChatColor.GREEN + "." + ChatColor.GOLD + resName
		+ ChatColor.GREEN));
	} else {
	    player.sendMessage(ChatColor.RED + language.getPhrase("AreaNonExist"));
	}
	int amount;
	try {
	    amount = Integer.parseInt(args[1]);
	} catch (Exception ex) {
	    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidAmount"));
	    return true;
	}

	if (amount < 0)
	    amount = amount * -1;

	if (!smanager.contract(player, amount, resadmin))
	    return true;

	if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
	    if (Residence.wep != null) {
		if (Residence.wepid == Residence.getConfigManager().selectionToolId) {
		    smanager.worldEdit(player);
		}
	    }
	    res.replaceArea(player, new CuboidArea(Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager().getPlayerLoc2(
		player.getName())), areaName, resadmin);
	    Residence.getSelectionManager().MakeBorders(player, Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager()
		.getPlayerLoc2(player.getName()), false);
	    return true;
	} else {
	    player.sendMessage(ChatColor.RED + language.getPhrase("SelectPoints"));
	}
	return false;
    }

    private boolean commandResSelect(String[] args, boolean resadmin, Player player, int page) {
	PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	if (!group.selectCommandAccess() && !resadmin) {
	    player.sendMessage(ChatColor.RED + language.getPhrase("SelectDiabled"));
	    return true;
	}
	if (!group.canCreateResidences() && group.getMaxSubzoneDepth() <= 0 && !resadmin) {
	    player.sendMessage(ChatColor.RED + language.getPhrase("SelectDiabled"));
	    return true;
	}
	if ((!player.hasPermission("residence.create") && player.isPermissionSet("residence.create") && !player.hasPermission("residence.select") && player
	    .isPermissionSet("residence.select")) && !resadmin) {
	    player.sendMessage(ChatColor.RED + language.getPhrase("SelectDiabled"));
	    return true;
	}
	if (args.length == 2) {
	    if (args[1].equals("size") || args[1].equals("cost")) {
		if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
		    try {
			smanager.showSelectionInfo(player);
			return true;
		    } catch (Exception ex) {
			Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
			return true;
		    }
		} else if (smanager.worldEdit(player)) {
		    try {
			smanager.showSelectionInfo(player);
			return true;
		    } catch (Exception ex) {
			Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
			return true;
		    }
		}
	    } else if (args[1].equals("vert")) {
		smanager.vert(player, resadmin);
		return true;
	    } else if (args[1].equals("sky")) {
		smanager.sky(player, resadmin);
		return true;
	    } else if (args[1].equals("bedrock")) {
		smanager.bedrock(player, resadmin);
		return true;
	    } else if (args[1].equals("coords")) {
		Location playerLoc1 = Residence.getSelectionManager().getPlayerLoc1(player.getName());
		if (playerLoc1 != null) {
		    player.sendMessage(ChatColor.GREEN + language.getPhrase("Primary.Selection") + ":" + ChatColor.AQUA + " (" + playerLoc1.getBlockX() + ", "
			+ playerLoc1.getBlockY() + ", " + playerLoc1.getBlockZ() + ")");
		}
		Location playerLoc2 = Residence.getSelectionManager().getPlayerLoc2(player.getName());
		if (playerLoc2 != null) {
		    player.sendMessage(ChatColor.GREEN + language.getPhrase("Secondary.Selection") + ":" + ChatColor.AQUA + " (" + playerLoc2.getBlockX() + ", "
			+ playerLoc2.getBlockY() + ", " + playerLoc2.getBlockZ() + ")");
		}
		return true;
	    } else if (args[1].equals("chunk")) {
		smanager.selectChunk(player);
		return true;
	    } else if (args[1].equals("worldedit")) {
		if (smanager.worldEdit(player)) {
		    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SelectionSuccess"));
		}
		return true;
	    }
	} else if (args.length == 3) {
	    if (args[1].equals("expand")) {
		int amount;
		try {
		    amount = Integer.parseInt(args[2]);
		} catch (Exception ex) {
		    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidAmount"));
		    return true;
		}
		smanager.modify(player, false, amount);
		return true;
	    } else if (args[1].equals("shift")) {
		int amount;
		try {
		    amount = Integer.parseInt(args[2]);
		} catch (Exception ex) {
		    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidAmount"));
		    return true;
		}
		smanager.modify(player, true, amount);
		return true;
	    }
	}
	if (args.length > 1 && args[1].equals("residence")) {
	    String resName;
	    String areaName;
	    ClaimedResidence res = null;
	    if (args.length > 2) {
		res = rmanager.getByName(args[2]);
	    } else {
		res = rmanager.getByLoc(player.getLocation());
	    }
	    if (res == null) {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
		return true;
	    }
	    resName = res.getName();
	    CuboidArea area = null;
	    if (args.length > 3) {
		area = res.getArea(args[3]);
		areaName = args[3];
	    } else {
		areaName = res.getAreaIDbyLoc(player.getLocation());
		area = res.getArea(areaName);
	    }
	    if (area != null) {
		smanager.placeLoc1(player, area.getHighLoc());
		smanager.placeLoc2(player, area.getLowLoc());
		player.sendMessage(ChatColor.GREEN + language.getPhrase("SelectionArea", ChatColor.GOLD + areaName + ChatColor.GREEN + "." + ChatColor.GOLD + resName
		    + ChatColor.GREEN));
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("AreaNonExist"));
	    }
	    return true;
	} else {
	    try {
		smanager.selectBySize(player, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
		return true;
	    } catch (Exception ex) {
		player.sendMessage(ChatColor.RED + language.getPhrase("SelectionFail"));
		return true;
	    }
	}
    }

    private boolean commandResCreate(String[] args, boolean resadmin, Player player, int page) {
	if (args.length != 2) {
	    return false;
	}
	WorldEditPlugin wep = (WorldEditPlugin) server.getPluginManager().getPlugin("WorldEdit");
	if (wep != null) {
	    if (wep.getConfig().getInt("wand-item") == Residence.getConfigManager().selectionToolId) {
		smanager.worldEdit(player);
	    }
	}
	if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
	    if (Residence.wg != null && WorldGuardUtil.isSelectionInRegion(player) == null) {
		rmanager.addResidence(player, args[1], smanager.getPlayerLoc1(player.getName()), smanager.getPlayerLoc2(
		    player.getName()), resadmin);
		return true;
	    } else if (Residence.wg != null && WorldGuardUtil.isSelectionInRegion(player) != null) {
		ProtectedRegion Region = WorldGuardUtil.isSelectionInRegion(player);
		player.sendMessage(ChatColor.RED + language.getPhrase("SelectOverlap").replace("%1", Region.getId()));

		Location lowLoc = new Location(Residence.getSelectionManager().getPlayerLoc1(player.getName()).getWorld(),
		    Region.getMinimumPoint().getBlockX(), Region.getMinimumPoint().getBlockY(), Region.getMinimumPoint().getBlockZ());

		Location highLoc = new Location(Residence.getSelectionManager().getPlayerLoc1(player.getName()).getWorld(),
		    Region.getMaximumPoint().getBlockX(), Region.getMaximumPoint().getBlockY(), Region.getMaximumPoint().getBlockZ());

		Residence.getSelectionManager().NewMakeBorders(player, lowLoc, highLoc, true);
		Residence.getSelectionManager().NewMakeBorders(player, Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager()
		    .getPlayerLoc2(player.getName()), false);
	    } else if (Residence.wg == null) {
		rmanager.addResidence(player, args[1], smanager.getPlayerLoc1(player.getName()), smanager.getPlayerLoc2(player.getName()), resadmin);
		return true;
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("SelectPoints"));
		return true;
	    }
	} else {
	    player.sendMessage(ChatColor.RED + language.getPhrase("SelectPoints"));
	    return true;
	}
	return true;
    }

    private boolean commandResSubzone(String[] args, boolean resadmin, Player player, int page) {
	if (args.length != 2 && args.length != 3) {
	    return false;
	}
	String zname;
	String parent;
	if (args.length == 2) {
	    parent = rmanager.getNameByLoc(player.getLocation());
	    zname = args[1];
	} else {
	    parent = args[1];
	    zname = args[2];
	}
	WorldEditPlugin wep = (WorldEditPlugin) server.getPluginManager().getPlugin("WorldEdit");
	if (wep != null) {
	    if (wep.getConfig().getInt("wand-item") == Residence.getConfigManager().selectionToolId) {
		smanager.worldEdit(player);
	    }
	}
	if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
	    ClaimedResidence res = rmanager.getByName(parent);
	    if (res == null) {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
		return true;
	    }
	    res.addSubzone(player, Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager().getPlayerLoc2(player.getName()),
		zname, resadmin);
	    return true;
	} else {
	    player.sendMessage(ChatColor.RED + language.getPhrase("SelectPoints"));
	    return true;
	}
    }

    private boolean commandResArea(String[] args, boolean resadmin, Player player, int page) {
	if (args.length == 4) {
	    if (args[1].equals("remove")) {
		ClaimedResidence res = rmanager.getByName(args[2]);
		if (res != null) {
		    res.removeArea(player, args[3], resadmin);
		} else {
		    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
		}
		return true;
	    } else if (args[1].equals("add")) {
		WorldEditPlugin wep = (WorldEditPlugin) server.getPluginManager().getPlugin("WorldEdit");
		if (wep != null) {
		    if (wep.getConfig().getInt("wand-item") == Residence.getConfigManager().selectionToolId) {
			smanager.worldEdit(player);
		    }
		}
		if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
		    ClaimedResidence res = rmanager.getByName(args[2]);
		    if (res != null) {
			res.addArea(player, new CuboidArea(Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager().getPlayerLoc2(
			    player.getName())), args[3],
			    resadmin);
		    } else {
			player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
		    }
		} else {
		    player.sendMessage(ChatColor.RED + language.getPhrase("SelectPoints"));
		}
		return true;
	    } else if (args[1].equals("replace")) {
		WorldEditPlugin wep = (WorldEditPlugin) server.getPluginManager().getPlugin("WorldEdit");
		if (wep != null) {
		    if (wep.getConfig().getInt("wand-item") == Residence.getConfigManager().selectionToolId) {
			smanager.worldEdit(player);
		    }
		}
		if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
		    ClaimedResidence res = rmanager.getByName(args[2]);
		    if (res != null) {
			res.replaceArea(player, new CuboidArea(Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager()
			    .getPlayerLoc2(player.getName())),
			    args[3], resadmin);
		    } else {
			player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
		    }
		} else {
		    player.sendMessage(ChatColor.RED + language.getPhrase("SelectPoints"));
		}
		return true;
	    }
	}
	if ((args.length == 3 || args.length == 4) && args[1].equals("list")) {
	    ClaimedResidence res = rmanager.getByName(args[2]);
	    if (res != null) {
		res.printAreaList(player, page);
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    }
	    return true;
	} else if ((args.length == 3 || args.length == 4) && args[1].equals("listall")) {
	    ClaimedResidence res = rmanager.getByName(args[2]);
	    if (res != null) {
		res.printAdvancedAreaList(player, page);
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    }
	    return true;
	}
	return false;
    }

    private boolean commandResRemove(String[] args, boolean resadmin, CommandSender sender, int page) {
	Player player = null;
	if (sender instanceof Player) {
	    player = (Player) sender;
	    if (args.length == 1) {
		String area = rmanager.getNameByLoc(player.getLocation());
		if (area != null) {
		    ClaimedResidence res = rmanager.getByName(area);
		    if (res.getParent() != null) {
			String[] split = area.split("\\.");
			String words = split[split.length - 1];
			if (!deleteConfirm.containsKey(player.getName()) || !area.equalsIgnoreCase(deleteConfirm.get(player.getName()))) {
			    player.sendMessage(ChatColor.RED + language.getPhrase("DeleteSubzoneConfirm", ChatColor.YELLOW + words + ChatColor.RED));
			    deleteConfirm.put(player.getName(), area);
			} else {
			    rmanager.removeResidence(player, area, resadmin);
			}
			return true;
		    } else {
			if (!deleteConfirm.containsKey(player.getName()) || !area.equalsIgnoreCase(deleteConfirm.get(player.getName()))) {
			    player.sendMessage(ChatColor.RED + language.getPhrase("DeleteConfirm", ChatColor.YELLOW + area + ChatColor.RED));
			    deleteConfirm.put(player.getName(), area);
			} else {
			    rmanager.removeResidence(player, area, resadmin);
			}
			return true;
		    }
		}
		return false;
	    }
	}
	if (args.length != 2) {
	    return false;
	}
	if (player != null) {
	    if (!deleteConfirm.containsKey(player.getName()) || !args[1].equalsIgnoreCase(deleteConfirm.get(player.getName()))) {
		String words = "";
		if (rmanager.getByName(args[1]) != null) {
		    ClaimedResidence res = rmanager.getByName(args[1]);
		    if (res.getParent() != null) {
			String[] split = args[1].split("\\.");
			words = split[split.length - 1];
		    }
		}
		if (words == "") {
		    player.sendMessage(ChatColor.RED + language.getPhrase("DeleteConfirm", ChatColor.YELLOW + args[1] + ChatColor.RED));
		} else {
		    player.sendMessage(ChatColor.RED + language.getPhrase("DeleteSubzoneConfirm", ChatColor.YELLOW + words + ChatColor.RED));
		}
		deleteConfirm.put(player.getName(), args[1]);
	    } else {
		rmanager.removeResidence(player, args[1], resadmin);
	    }
	} else {
	    if (!deleteConfirm.containsKey("Console") || !args[1].equalsIgnoreCase(deleteConfirm.get("Console"))) {
		String words = "";
		if (rmanager.getByName(args[1]) != null) {
		    ClaimedResidence res = rmanager.getByName(args[1]);
		    if (res.getParent() != null) {
			String[] split = args[1].split("\\.");
			words = split[split.length - 1];
		    }
		}
		if (words == "") {
		    server.getConsoleSender().sendMessage(ChatColor.RED + language.getPhrase("DeleteConfirm", ChatColor.YELLOW + args[1] + ChatColor.RED));
		} else {
		    server.getConsoleSender().sendMessage(ChatColor.RED + language.getPhrase("DeleteSubzoneConfirm", ChatColor.YELLOW + words + ChatColor.RED));
		}
		deleteConfirm.put("Console", args[1]);
	    } else {
		rmanager.removeResidence(args[1]);
	    }
	}
	return true;
    }

    private boolean commandResConfirm(String[] args, boolean resadmin, CommandSender sender, int page) {
	Player player = null;
	String name = "Console";
	if (sender instanceof Player) {
	    player = (Player) sender;
	    name = player.getName();
	}
	if (args.length == 1) {
	    String area = deleteConfirm.get(name);
	    if (area == null) {
		sender.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    } else {
		rmanager.removeResidence(player, area, resadmin);
		deleteConfirm.remove(name);
		if (player == null) {
		    sender.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("ResidenceRemove", ChatColor.YELLOW + name + ChatColor.GREEN));
		}
	    }
	}
	return true;
    }

    private boolean commandResSet(String[] args, boolean resadmin, Player player, int page) {
	if (args.length == 3) {
	    String area = rmanager.getNameByLoc(player.getLocation());
	    if (area != null) {
		rmanager.getByName(area).getPermissions().setFlag(player, args[1], args[2], resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    }
	    return true;
	} else if (args.length == 4) {
	    ClaimedResidence area = rmanager.getByName(args[1]);
	    if (area != null) {
		area.getPermissions().setFlag(player, args[2], args[3], resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    }
	    return true;
	}
	return false;
    }

    private boolean commandResPset(String[] args, boolean resadmin, Player player, int page) {
	if (args.length == 3 && args[2].equalsIgnoreCase("removeall")) {
	    ClaimedResidence area = rmanager.getByLoc(player.getLocation());
	    if (area != null) {
		area.getPermissions().removeAllPlayerFlags(player, args[1], resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    }
	    return true;
	} else if (args.length == 4 && args[3].equalsIgnoreCase("removeall")) {
	    ClaimedResidence area = rmanager.getByName(args[1]);
	    if (area != null) {
		area.getPermissions().removeAllPlayerFlags(player, args[2], resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    }
	    return true;
	} else if (args.length == 4) {
	    ClaimedResidence area = rmanager.getByLoc(player.getLocation());
	    if (area != null) {
		area.getPermissions().setPlayerFlag(player, args[1], args[2], args[3], resadmin, true);
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    }
	    return true;
	} else if (args.length == 5) {
	    ClaimedResidence area = rmanager.getByName(args[1]);
	    if (area != null) {
		area.getPermissions().setPlayerFlag(player, args[2], args[3], args[4], resadmin, true);
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    }
	    return true;
	}
	return false;
    }

    private boolean commandResGset(String[] args, boolean resadmin, Player player, int page) {
	if (args.length == 4) {
	    ClaimedResidence area = rmanager.getByLoc(player.getLocation());
	    if (area != null) {
		area.getPermissions().setGroupFlag(player, args[1], args[2], args[3], resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidArea"));
	    }
	    return true;
	} else if (args.length == 5) {
	    ClaimedResidence area = rmanager.getByName(args[1]);
	    if (area != null) {
		area.getPermissions().setGroupFlag(player, args[2], args[3], args[4], resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    }
	    return true;
	}
	return false;
    }

    private boolean commandResLset(String[] args, boolean resadmin, Player player, int page) {
	ClaimedResidence res = null;
	Material mat = null;
	String listtype = null;
	boolean showinfo = false;
	if (args.length == 2 && args[1].equals("info")) {
	    res = rmanager.getByLoc(player.getLocation());
	    showinfo = true;
	} else if (args.length == 3 && args[2].equals("info")) {
	    res = rmanager.getByName(args[1]);
	    showinfo = true;
	}
	if (showinfo) {
	    if (res == null) {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
		return true;
	    }
	    player.sendMessage(ChatColor.RED + "Blacklist:");
	    res.getItemBlacklist().printList(player);
	    player.sendMessage(ChatColor.GREEN + "Ignorelist:");
	    res.getItemIgnoreList().printList(player);
	    return true;
	} else if (args.length == 4) {
	    res = rmanager.getByName(args[1]);
	    listtype = args[2];
	    try {
		mat = Material.valueOf(args[3].toUpperCase());
	    } catch (Exception ex) {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidMaterial"));
		return true;
	    }
	} else if (args.length == 3) {
	    res = rmanager.getByLoc(player.getLocation());
	    listtype = args[1];
	    try {
		mat = Material.valueOf(args[2].toUpperCase());
	    } catch (Exception ex) {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidMaterial"));
		return true;
	    }
	}
	if (res != null) {
	    if (listtype.equalsIgnoreCase("blacklist")) {
		res.getItemBlacklist().playerListChange(player, mat, resadmin);
	    } else if (listtype.equalsIgnoreCase("ignorelist")) {
		res.getItemIgnoreList().playerListChange(player, mat, resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidList"));
	    }
	    return true;
	} else {
	    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    return true;
	}
    }

    private boolean commandResBank(String[] args, boolean resadmin, Player player, int page) {
	if (args.length != 3) {
	    return false;
	}
	ClaimedResidence res = rmanager.getByName(plistener.getCurrentResidenceName(player.getName()));
	if (res == null) {
	    player.sendMessage(ChatColor.RED + language.getPhrase("NotInResidence"));
	    return true;
	}
	int amount = 0;
	try {
	    amount = Integer.parseInt(args[2]);
	} catch (Exception ex) {
	    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidAmount"));
	    return true;
	}
	if (args[1].equals("deposit")) {
	    res.getBank().deposit(player, amount, resadmin);
	} else if (args[1].equals("withdraw")) {
	    res.getBank().withdraw(player, amount, resadmin);
	} else {
	    return false;
	}
	return true;
    }

    private boolean commandResLease(String[] args, boolean resadmin, Player player, int page) {
	if (args.length == 2 || args.length == 3) {
	    if (args[1].equals("renew")) {
		if (args.length == 3) {
		    leasemanager.renewArea(args[2], player);
		} else {
		    leasemanager.renewArea(rmanager.getNameByLoc(player.getLocation()), player);
		}
		return true;
	    } else if (args[1].equals("cost")) {
		if (args.length == 3) {
		    ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);
		    if (res == null || leasemanager.leaseExpires(args[2])) {
			int cost = leasemanager.getRenewCost(res);
			player.sendMessage(ChatColor.YELLOW + language.getPhrase("LeaseRenewalCost", ChatColor.RED + args[2] + ChatColor.YELLOW + "." + ChatColor.RED
			    + cost + ChatColor.YELLOW));
		    } else {
			player.sendMessage(ChatColor.RED + language.getPhrase("LeaseNotExpire"));
		    }
		    return true;
		} else {
		    String area = rmanager.getNameByLoc(player.getLocation());
		    ClaimedResidence res = rmanager.getByName(area);
		    if (area == null || res == null) {
			player.sendMessage(ChatColor.RED + language.getPhrase("InvalidArea"));
			return true;
		    }
		    if (leasemanager.leaseExpires(area)) {
			int cost = leasemanager.getRenewCost(res);
			player.sendMessage(ChatColor.YELLOW + language.getPhrase("LeaseRenewalCost", ChatColor.RED + area + ChatColor.YELLOW + "." + ChatColor.RED + cost
			    + ChatColor.YELLOW));
		    } else {
			player.sendMessage(ChatColor.RED + language.getPhrase("LeaseNotExpire"));
		    }
		    return true;
		}
	    }
	} else if (args.length == 4) {
	    if (args[1].equals("set")) {
		if (!resadmin) {
		    player.sendMessage(ChatColor.RED + language.getPhrase("NoPermission"));
		    return true;
		}
		if (args[3].equals("infinite")) {
		    if (leasemanager.leaseExpires(args[2])) {
			leasemanager.removeExpireTime(args[2]);
			player.sendMessage(ChatColor.GREEN + language.getPhrase("LeaseInfinite"));
		    } else {
			player.sendMessage(ChatColor.RED + language.getPhrase("LeaseNotExpire"));
		    }
		    return true;
		} else {
		    int days;
		    try {
			days = Integer.parseInt(args[3]);
		    } catch (Exception ex) {
			player.sendMessage(ChatColor.RED + language.getPhrase("InvalidDays"));
			return true;
		    }
		    leasemanager.setExpireTime(player, args[2], days);
		    return true;
		}
	    }
	}
	return false;
    }

    private boolean commandResMarket(String[] args, boolean resadmin, Player player, int page) {
	if (args.length == 1) {
	    return false;
	}
	String command = args[1].toLowerCase();
	if (command.equals("list")) {
	    return commandResMarketList(args, resadmin, player, page);
	}
	if (command.equals("autorenew")) {
	    return commandResMarketAutorenew(args, resadmin, player, page);
	}
	if (command.equals("rentable")) {
	    return commandResMarketRentable(args, resadmin, player, page);
	}
	if (command.equals("rent")) {
	    return commandResMarketRent(args, resadmin, player, page);
	}
	if (command.equals("release")) {
	    if (args.length != 3) {
		return false;
	    }
	    if (rentmanager.isRented(args[2])) {
		rentmanager.removeFromForRent(player, args[2], resadmin);
	    } else {
		rentmanager.unrent(player, args[2], resadmin);
	    }
	    return true;
	}
	if (command.equals("info")) {
	    if (args.length == 2) {
		String areaname = rmanager.getNameByLoc(player.getLocation());
		tmanager.viewSaleInfo(areaname, player);
		if (cmanager.enabledRentSystem() && rentmanager.isForRent(areaname)) {
		    rentmanager.printRentInfo(player, areaname);
		}
	    } else if (args.length == 3) {
		tmanager.viewSaleInfo(args[2], player);
		if (cmanager.enabledRentSystem() && rentmanager.isForRent(args[2])) {
		    rentmanager.printRentInfo(player, args[2]);
		}
	    } else {
		return false;
	    }
	    return true;
	}
	if (command.equals("buy")) {
	    if (args.length != 3) {
		return false;
	    }
	    tmanager.buyPlot(args[2], player, resadmin);
	    return true;
	}
	if (command.equals("unsell")) {
	    if (args.length != 3) {
		return false;
	    }
	    tmanager.removeFromSale(player, args[2], resadmin);
	    return true;
	}
	if (command.equals("sell")) {
	    if (args.length != 4) {
		return false;
	    }
	    int amount;
	    try {
		amount = Integer.parseInt(args[3]);
	    } catch (Exception ex) {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidAmount"));
		return true;
	    }
	    tmanager.putForSale(args[2], player, amount, resadmin);
	    return true;
	}
	return false;
    }

    private boolean commandResMarketRent(String[] args, boolean resadmin, Player player, int page) {
	if (args.length < 3 || args.length > 4) {
	    return false;
	}
	boolean repeat = false;
	if (args.length == 4) {
	    if (args[3].equalsIgnoreCase("t") || args[3].equalsIgnoreCase("true")) {
		repeat = true;
	    } else if (!args[3].equalsIgnoreCase("f") && !args[3].equalsIgnoreCase("false")) {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidBoolean"));
		return true;
	    }
	}
	rentmanager.rent(player, args[2], repeat, resadmin);
	return true;
    }

    private boolean commandResMarketRentable(String[] args, boolean resadmin, Player player, int page) {
	if (args.length < 5 || args.length > 6) {
	    return false;
	}
	if (!cmanager.enabledRentSystem()) {
	    player.sendMessage(ChatColor.RED + language.getPhrase("RentDisabled"));
	    return true;
	}
	int days;
	int cost;
	try {
	    cost = Integer.parseInt(args[3]);
	} catch (Exception ex) {
	    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidCost"));
	    return true;
	}
	try {
	    days = Integer.parseInt(args[4]);
	} catch (Exception ex) {
	    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidDays"));
	    return true;
	}
	boolean repeat = false;
	if (args.length == 6) {
	    if (args[5].equalsIgnoreCase("t") || args[5].equalsIgnoreCase("true")) {
		repeat = true;
	    } else if (!args[5].equalsIgnoreCase("f") && !args[5].equalsIgnoreCase("false")) {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidBoolean"));
		return true;
	    }
	}
	rentmanager.setForRent(player, args[2], cost, days, repeat, resadmin);
	return true;
    }

    private boolean commandResMarketAutorenew(String[] args, boolean resadmin, Player player, int page) {
	if (!cmanager.enableEconomy()) {
	    player.sendMessage(ChatColor.RED + language.getPhrase("MarketDisabled"));
	    return true;
	}
	if (args.length != 4) {
	    return false;
	}
	boolean value;
	if (args[3].equalsIgnoreCase("true") || args[3].equalsIgnoreCase("t")) {
	    value = true;
	} else if (args[3].equalsIgnoreCase("false") || args[3].equalsIgnoreCase("f")) {
	    value = false;
	} else {
	    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidBoolean"));
	    return true;
	}
	if (rentmanager.isRented(args[2]) && rentmanager.getRentingPlayer(args[2]).equalsIgnoreCase(player.getName())) {
	    rentmanager.setRentedRepeatable(player, args[2], value, resadmin);
	} else if (rentmanager.isForRent(args[2])) {
	    rentmanager.setRentRepeatable(player, args[2], value, resadmin);
	} else {
	    player.sendMessage(ChatColor.RED + language.getPhrase("RentReleaseInvalid", ChatColor.YELLOW + args[2] + ChatColor.RED));
	}
	return true;
    }

    private boolean commandResMarketList(String[] args, boolean resadmin, Player player, int page) {
	if (!cmanager.enableEconomy()) {
	    player.sendMessage(ChatColor.RED + language.getPhrase("MarketDisabled"));
	    return true;
	}
	player.sendMessage(ChatColor.BLUE + "---" + language.getPhrase("MarketList") + "---");
	tmanager.printForSaleResidences(player);
	if (cmanager.enabledRentSystem()) {
	    rentmanager.printRentableResidences(player);
	}
	return true;
    }

    private boolean commandResMessage(String[] args, boolean resadmin, Player player, int page) {
	ClaimedResidence res = null;
	int start = 0;
	boolean enter = false;
	if (args.length < 2) {
	    return false;
	}
	if (args[1].equals("enter")) {
	    enter = true;
	    res = rmanager.getByLoc(player.getLocation());
	    start = 2;
	} else if (args[1].equals("leave")) {
	    res = rmanager.getByLoc(player.getLocation());
	    start = 2;
	} else if (args[1].equals("remove")) {
	    if (args.length > 2 && args[2].equals("enter")) {
		res = rmanager.getByLoc(player.getLocation());
		if (res != null) {
		    res.setEnterLeaveMessage(player, null, true, resadmin);
		} else {
		    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
		}
		return true;
	    } else if (args.length > 2 && args[2].equals("leave")) {
		res = rmanager.getByLoc(player.getLocation());
		if (res != null) {
		    res.setEnterLeaveMessage(player, null, false, resadmin);
		} else {
		    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
		}
		return true;
	    }
	    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidMessageType"));
	    return true;
	} else if (args.length > 2 && args[2].equals("enter")) {
	    enter = true;
	    res = rmanager.getByName(args[1]);
	    start = 3;
	} else if (args.length > 2 && args[2].equals("leave")) {
	    res = rmanager.getByName(args[1]);
	    start = 3;
	} else if (args.length > 2 && args[2].equals("remove")) {
	    res = rmanager.getByName(args[1]);
	    if (args.length != 4) {
		return false;
	    }
	    if (args[3].equals("enter")) {
		if (res != null) {
		    res.setEnterLeaveMessage(player, null, true, resadmin);
		}
		return true;
	    } else if (args[3].equals("leave")) {
		if (res != null) {
		    res.setEnterLeaveMessage(player, null, false, resadmin);
		}
		return true;
	    }
	    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidMessageType"));
	    return true;
	} else {
	    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidMessageType"));
	    return true;
	}
	if (start == 0) {
	    return false;
	}
	String message = "";
	for (int i = start; i < args.length; i++) {
	    message = message + args[i] + " ";
	}
	if (res != null) {
	    res.setEnterLeaveMessage(player, message, enter, resadmin);
	} else {
	    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	}
	return true;
    }

    private boolean commandResSublist(String[] args, boolean resadmin, Player player, int page) {
	if (args.length == 1 || args.length == 2 || args.length == 3) {
	    ClaimedResidence res;
	    if (args.length == 1) {
		res = rmanager.getByLoc(player.getLocation());
	    } else {
		res = rmanager.getByName(args[1]);
	    }
	    if (res != null) {
		res.printSubzoneList(player, page);
	    } else {
		player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	    }
	    return true;
	}
	return false;
    }

    private boolean commandResCompass(String[] args, boolean resadmin, Player player, int page) {
	if (args.length != 2) {
	    player.setCompassTarget(player.getWorld().getSpawnLocation());
	    player.sendMessage(ChatColor.GREEN + language.getPhrase("CompassTargetReset"));
	    return true;
	}
	if (rmanager.getByName(args[1]) != null) {
	    if (rmanager.getByName(args[1]).getWorld().equalsIgnoreCase(player.getWorld().getName())) {
		Location low = rmanager.getByName(args[1]).getArea("main").getLowLoc();
		Location high = rmanager.getByName(args[1]).getArea("main").getHighLoc();
		Location mid = new Location(low.getWorld(), (low.getBlockX() + high.getBlockX()) / 2, (low.getBlockY() + high.getBlockY()) / 2, (low.getBlockZ() + high
		    .getBlockZ()) / 2);
		player.setCompassTarget(mid);
		player.sendMessage(ChatColor.GREEN + language.getPhrase("CompassTargetSet", ChatColor.YELLOW + args[1] + ChatColor.GREEN));
	    }
	} else {
	    player.sendMessage(ChatColor.RED + language.getPhrase("InvalidResidence"));
	}
	return true;
    }

    private boolean commandResGui(String[] args, boolean resadmin, Player player, int page) {
	if (slistener != null) {
	    if (args.length == 1) {
		ResidenceSpout.showResidenceFlagGUI(SpoutManager.getPlayer(player), this, rmanager.getNameByLoc(player.getLocation()), resadmin);
	    } else if (args.length == 2) {
		ResidenceSpout.showResidenceFlagGUI(SpoutManager.getPlayer(player), this, args[1], resadmin);
	    }
	}
	return true;
    }

    private boolean commandResList(String[] args, boolean resadmin, Player player, int page) {
	if (args.length == 2) {
	    if (args[1].equals("list")) {
		pmanager.printLists(player);
		return true;
	    }
	} else if (args.length == 3) {
	    if (args[1].equals("view")) {
		pmanager.printList(player, args[2]);
		return true;
	    } else if (args[1].equals("remove")) {
		pmanager.removeList(player, args[2]);
		return true;
	    } else if (args[1].equals("add")) {
		pmanager.makeList(player, args[2]);
		return true;
	    }
	} else if (args.length == 4) {
	    if (args[1].equals("apply")) {
		pmanager.applyListToResidence(player, args[2], args[3], resadmin);
		return true;
	    }
	} else if (args.length == 5) {
	    if (args[1].equals("set")) {
		pmanager.getList(player.getName(), args[2]).setFlag(args[3], FlagPermissions.stringToFlagState(args[4]));
		player.sendMessage(ChatColor.GREEN + language.getPhrase("FlagSet"));
		return true;
	    }
	} else if (args.length == 6) {
	    if (args[1].equals("gset")) {
		pmanager.getList(player.getName(), args[2]).setGroupFlag(args[3], args[4], FlagPermissions.stringToFlagState(args[5]));
		player.sendMessage(ChatColor.GREEN + language.getPhrase("FlagSet"));
		return true;
	    } else if (args[1].equals("pset")) {
		pmanager.getList(player.getName(), args[2]).setPlayerFlag(args[3], args[4], FlagPermissions.stringToFlagState(args[5]));
		player.sendMessage(ChatColor.GREEN + language.getPhrase("FlagSet"));
		return true;
	    }
	}
	return false;
    }
}
