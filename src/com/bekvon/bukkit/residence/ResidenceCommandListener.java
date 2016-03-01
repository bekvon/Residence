package com.bekvon.bukkit.residence;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;

import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.event.ResidenceCommandEvent;
import com.bekvon.bukkit.residence.gui.SetFlag;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.selection.AutoSelection;
import com.bekvon.bukkit.residence.selection.WorldGuardUtil;
import com.bekvon.bukkit.residence.shopStuff.ShopListener;
import com.bekvon.bukkit.residence.shopStuff.Board;
import com.bekvon.bukkit.residence.shopStuff.ShopVote;
import com.bekvon.bukkit.residence.shopStuff.Vote;
import com.bekvon.bukkit.residence.signsStuff.Signs;
import com.bekvon.bukkit.residence.spout.ResidenceSpout;
import com.bekvon.bukkit.residence.utils.RandomTp;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ResidenceCommandListener extends Residence {

    public static HashMap<String, ClaimedResidence> teleportMap = new HashMap<String, ClaimedResidence>();
    public static HashMap<String, Long> rtMap = new HashMap<String, Long>();
    public static List<String> teleportDelayMap = new ArrayList<String>();

    public static List<String> AdminCommands = Arrays.asList("setowner", "removeall", "signupdate", "listhidden", "listallhidden", "server", "clearflags", "resreload",
	"resload", "ressignconvert");

    public static HashMap<String, ClaimedResidence> getTeleportMap() {
	return teleportMap;
    }

    public static List<String> getTeleportDelayMap() {
	return teleportDelayMap;
    }

    public static List<String> getAdminCommands() {
	return AdminCommands;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	ResidenceCommandEvent cevent = new ResidenceCommandEvent(command.getName(), args, sender);
	Residence.getServ().getPluginManager().callEvent(cevent);
	if (cevent.isCancelled()) {
	    return true;
	}
	if (command.getName().equals("resreload") && args.length == 0) {
	    if (sender instanceof Player) {
		Player player = (Player) sender;
		if (Residence.getPermissionManager().isResidenceAdmin(player) && player.hasPermission("residence.topadmin")) {
		    this.reloadPlugin();
		    sender.sendMessage(ChatColor.GREEN + "[Residence] Reloaded config.");
		    System.out.println("[Residence] Reloaded by " + player.getName() + ".");
		} else
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    } else {
		this.reloadPlugin();
		System.out.println("[Residence] Reloaded by console.");
	    }
	    return true;
	}
	if (command.getName().equals("ressignconvert") && args.length == 0) {
	    if (sender instanceof Player) {
		Player player = (Player) sender;
		if (Residence.getPermissionManager().isResidenceAdmin(player)) {
		    Residence.getSignUtil().convertSigns(sender);
		} else
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    } else {
		Residence.getSignUtil().convertSigns(sender);
	    }
	    return true;
	}
	if (command.getName().equals("resload")) {
	    if (!(sender instanceof Player) || sender instanceof Player && Residence.gmanager.isResidenceAdmin((Player) sender) && ((Player) sender).hasPermission(
		"residence.topadmin")) {
		try {
		    this.loadYml();
		    sender.sendMessage(ChatColor.GREEN + "[Residence] Reloaded save file...");
		} catch (Exception ex) {
		    sender.sendMessage(ChatColor.RED + "[Residence] Unable to reload the save file, exception occured!");
		    sender.sendMessage(ChatColor.RED + ex.getMessage());
		    Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
		}
	    } else
		sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    return true;
	} else if (command.getName().equals("resworld")) {
	    if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
		if (sender instanceof ConsoleCommandSender) {
		    Residence.getResidenceManager().removeAllFromWorld(sender, args[1]);
		    return true;
		} else {
		    sender.sendMessage(ChatColor.RED + "MUST be run from console.");
		}
	    }
	    return false;
	} else if (command.getName().equals("rc")) {
	    if (!(sender instanceof Player))
		return true;
	    Player player = (Player) sender;
	    String pname = player.getName();
	    if (Residence.cmanager.chatEnabled()) {
		if (args.length == 0) {
		    ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
		    if (res == null) {
			ChatChannel chat = Residence.getChatManager().getPlayerChannel(pname);
			if (chat != null) {
			    Residence.getChatManager().removeFromChannel(pname);
			    Residence.plistener.removePlayerResidenceChat(player);
			    return true;
			}
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NotInResidence"));
			return true;
		    } else {
			ChatChannel chat = Residence.getChatManager().getPlayerChannel(pname);
			if (chat != null && chat.getChannelName().equals(res.getName())) {
			    Residence.getChatManager().removeFromChannel(pname);
			    Residence.plistener.removePlayerResidenceChat(player);
			    return true;
			}
		    }
		    if (!res.getPermissions().playerHas(player.getName(), "chat", true) && !Residence.gmanager.isResidenceAdmin(player)) {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceFlagDeny", "chat|" + res.getName()));
			return false;
		    }

		    Residence.plistener.tooglePlayerResidenceChat(player, res.getName());
		    Residence.getChatManager().setChannel(pname, res);
		    return true;
		} else if (args.length == 1) {
		    if (args[0].equalsIgnoreCase("l")) {
			Residence.getChatManager().removeFromChannel(pname);
			Residence.plistener.removePlayerResidenceChat(player);
			return true;
		    }
		    ClaimedResidence res = Residence.getResidenceManager().getByName(args[0]);
		    if (res == null) {
			player.sendMessage(ChatColor.RED + Residence.getLM().getMessage("Language.Chat.InvalidChannel"));
			return true;
		    }

		    if (!res.getPermissions().playerHas(player.getName(), "chat", true) && !Residence.gmanager.isResidenceAdmin(player)) {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ResidenceFlagDeny", "chat|" + res.getName()));
			return false;
		    }
		    Residence.plistener.tooglePlayerResidenceChat(player, res.getName());
		    Residence.getChatManager().setChannel(pname, res);

		    return true;
		} else if (args.length == 2) {
		    if (args[0].equalsIgnoreCase("setcolor")) {

			ChatChannel chat = Residence.getChatManager().getPlayerChannel(pname);

			if (chat == null) {
			    player.sendMessage(ChatColor.RED + Residence.getLM().getMessage("Language.Chat.JoinFirst"));
			    return true;
			}

			ClaimedResidence res = Residence.getResidenceManager().getByName(chat.getChannelName());

			if (res == null)
			    return false;

			if (!res.isOwner(player) && !Residence.gmanager.isResidenceAdmin(player)) {
			    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
			    return true;
			}

			if (!player.hasPermission("residence.chatcolor")) {
			    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
			    return true;
			}

			String posibleColor = args[1];

			if (!posibleColor.contains("&"))
			    posibleColor = "&" + posibleColor;

			if (posibleColor.length() != 2 || ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', posibleColor)).length() != 0) {
			    player.sendMessage(ChatColor.RED + Residence.getLM().getMessage("Language.Chat.InvalidColor"));
			    return true;
			}

			ChatColor color = ChatColor.getByChar(posibleColor.replace("&", ""));
			res.setChannelColor(color);
			chat.setChannelColor(color);
			player.sendMessage(ChatColor.GOLD + Residence.getLM().getMessage("Language.Chat.ChangedColor", color.name()));
			return true;
		    } else if (args[0].equalsIgnoreCase("setprefix")) {
			ChatChannel chat = Residence.getChatManager().getPlayerChannel(pname);

			if (chat == null) {
			    player.sendMessage(ChatColor.RED + Residence.getLM().getMessage("Language.Chat.JoinFirst"));
			    return true;
			}

			ClaimedResidence res = Residence.getResidenceManager().getByName(chat.getChannelName());

			if (res == null)
			    return false;

			if (!res.isOwner(player) && !Residence.gmanager.isResidenceAdmin(player)) {
			    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
			    return true;
			}

			if (!player.hasPermission("residence.chatprefix")) {
			    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
			    return true;
			}

			String prefix = args[1];

			if (prefix.length() > Residence.getConfigManager().getChatPrefixLength()) {
			    player.sendMessage(ChatColor.RED + Residence.getLM().getMessage("Language.Chat.InvalidPrefixLength", Residence.getConfigManager()
				.getChatPrefixLength()));
			    return true;
			}

			res.setChatPrefix(prefix);
			chat.setChatPrefix(prefix);
			player.sendMessage(ChatColor.GOLD + Residence.getLM().getMessage("Language.Chat.ChangedPrefix", ChatColor.translateAlternateColorCodes('&',
			    prefix)));
			return true;
		    } else if (args[0].equalsIgnoreCase("kick")) {
			ChatChannel chat = Residence.getChatManager().getPlayerChannel(pname);

			if (chat == null) {
			    player.sendMessage(ChatColor.RED + Residence.getLM().getMessage("Language.Chat.JoinFirst"));
			    return true;
			}

			ClaimedResidence res = Residence.getResidenceManager().getByName(chat.getChannelName());

			if (res == null)
			    return false;

			if (!res.getOwner().equals(player.getName()) && !Residence.gmanager.isResidenceAdmin(player)) {
			    player.sendMessage(ChatColor.RED + Residence.getLM().getMessage("NoPermission"));
			    return true;
			}

			if (!player.hasPermission("residence.chatkick")) {
			    player.sendMessage(ChatColor.RED + Residence.getLM().getMessage("NoPermission"));
			    return true;
			}

			String targetName = args[1];
			if (!chat.hasMember(targetName)) {
			    player.sendMessage(ChatColor.RED + Residence.getLM().getMessage("Language.Chat.NotInChannel"));
			    return false;
			}

			chat.leave(targetName);
			Residence.plistener.removePlayerResidenceChat(targetName);
			player.sendMessage(ChatColor.RED + Residence.getLM().getMessage("Language.Chat.Kicked", targetName + "%" + chat.getChannelName()));
			return true;
		    }
		}
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("ChatDisabled"));
	    }
	} else if (command.getName().equals("res") || command.getName().equals("residence") || command.getName().equals("resadmin")) {
	    boolean resadmin = false;
	    if (sender instanceof Player) {
		if (command.getName().equals("resadmin") && Residence.gmanager.isResidenceAdmin((Player) sender)) {
		    resadmin = true;
		}
		if (command.getName().equals("resadmin") && !Residence.gmanager.isResidenceAdmin((Player) sender)) {
		    ((Player) sender).sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NonAdmin"));
		    return true;
		}
		if (command.getName().equals("res") && Residence.gmanager.isResidenceAdmin((Player) sender) && Residence.getConfigManager().getAdminFullAccess()) {
		    resadmin = true;
		}
	    } else {
		resadmin = true;
	    }

	    boolean respond = commandRes(args, resadmin, command, sender);

	    if (!respond)
		sendUsage(sender, command.getName());

	    return true;
	}
	return this.onCommand(sender, command, label, args);
    }

    private void sendUsage(CommandSender sender, String command) {

	sender.sendMessage(Residence.getLM().getMessage("DefaultUsage", command));

    }

    @SuppressWarnings("deprecation")
    private boolean commandRes(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (args.length > 0 && args[args.length - 1].equalsIgnoreCase("?") || args.length > 1 && args[args.length - 2].equals("?")) {
	    return commandHelp(args, resadmin, sender, command);
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
	if (Residence.cmanager.allowAdminsOnly()) {
	    if (!resadmin) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AdminOnly"));
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

	// Test code for area regeneration with WE plugin
//	if (cmd.equals("test")) {
//	    final long time = System.currentTimeMillis();
//
//	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
//
//	    final CuboidArea area = res.getAreaArray()[0];
//
//	    final int startX = area.getLowLoc().getBlockX();
//	    final int startY = area.getLowLoc().getBlockY();
//	    final int startZ = area.getLowLoc().getBlockZ();
//
//	    final int lX = area.getXSize();
//	    final int lY = area.getYSize();
//	    final int lZ = area.getZSize();
//	    Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
//		@Override
//		public void run() {
//		    int i = 0;
//
//		    List<Block> blocks = new ArrayList<Block>();
//
//		    for (int x = startX; x < startX + lX; x++) {
//			for (int y = startY; y < startY + lY; y++) {
//			    for (int z = startZ; z < startZ + lZ; z++) {
//
//				Protection prot = Residence.getLwc().getProtectionCache().getProtection(area.getWorld().getName() + ":" + x + ":" + y + ":" + z);
//
//				if (prot == null)
//				    continue;
//				i++;
//
//				Debug.D("" + prot.getOwner() + "  " + prot.getFormattedOwnerPlayerName());
//				Debug.D("" + prot.getX() + ":" + prot.getY() + ":" + prot.getZ());
//			    }
//			}
//		    }
//
//		    Debug.D("baigtas test " + (System.currentTimeMillis() - time) + "   " + i + "   " + blocks.size());
//
//		    return;
//		}
//	    });
//	    
//	    Selection selection = new CuboidSelection(this.getC1().getWorld(),this.getC1(), this.getC2());
//
//		CuboidSelection selection = new CuboidSelection(player.getWorld(), area.getLowLoc(), area.getHighLoc());
//		
//	        try {
//	            Region region = selection.getRegionSelector().getRegion();
//	            region.getWorld().regenerate(region, WorldEdit.getInstance().getEditSessionFactory().getEditSession(region.getWorld(), -1));
//	        } catch (IncompleteRegionException e) {
//	        }      
//	    
//	    return true;
//	}
	if (cmd.equals("version")) {
	    sender.sendMessage(ChatColor.GRAY + "------------------------------------");
	    sender.sendMessage(ChatColor.RED + "This server running " + ChatColor.GOLD + "Residence" + ChatColor.RED + " version: " + ChatColor.BLUE + this
		.getDescription().getVersion());
	    sender.sendMessage(ChatColor.GREEN + "Created by: " + ChatColor.YELLOW + "bekvon");
	    sender.sendMessage(ChatColor.GREEN + "Updated to 1.8 by: " + ChatColor.YELLOW + "DartCZ");
	    sender.sendMessage(ChatColor.GREEN + "Currently maintained by: " + ChatColor.YELLOW + "Zrips");
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
	    sender.sendMessage(ChatColor.GREEN + "https://github.com/bekvon/Residence/wiki");
	    sender.sendMessage(ChatColor.AQUA + "Visit the Spigot Resource page at:");
	    sender.sendMessage(ChatColor.BLUE + "https://www.spigotmc.org/resources/residence.11480/");
	    sender.sendMessage(ChatColor.GRAY + "------------------------------------");
	    return true;
	}
	if (cmd.equals("setowner") && args.length == 3) {
	    if (!resadmin) {
		sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return true;
	    }
	    ClaimedResidence area = Residence.getResidenceManager().getByName(args[1]);
	    if (area != null) {
		area.getPermissions().setOwner(args[2], true);
		if (Residence.getRentManager().isForRent(area.getName()))
		    Residence.getRentManager().removeRentable(area.getName());
		if (Residence.tmanager.isForSale(area.getName()))
		    Residence.tmanager.removeFromSale(area.getName());
		area.getPermissions().applyDefaultFlags();

		if (area.getParent() == null) {
		    sender.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("ResidenceOwnerChange", ChatColor.YELLOW + " " + args[1] + " "
			+ ChatColor.GREEN + "|" + ChatColor.YELLOW + args[2] + ChatColor.GREEN));
		} else {
		    sender.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SubzoneOwnerChange", ChatColor.YELLOW + " " + args[1].split("\\.")[args[1]
			.split("\\.").length - 1] + " " + ChatColor.GREEN + "|" + ChatColor.YELLOW + args[2] + ChatColor.GREEN));
		}
	    } else {
		sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    }
	    return true;
	}

	if (cmd.equals("bank")) {
	    return commandResBank(args, resadmin, sender, page);
	}

	if (player == null) {
	    return true;
	}
	if (command.getName().equals("resadmin")) {
	    if (args.length == 1 && args[0].equals("on")) {
		Residence.resadminToggle.add(player.getName());
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("AdminToggle", Residence.getLanguage().getPhrase("TurnOn")));
		return true;
	    } else if (args.length == 1 && args[0].equals("off")) {
		Residence.resadminToggle.remove(player.getName());
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("AdminToggle", Residence.getLanguage().getPhrase("TurnOff")));
		return true;
	    }
	}
	if (!resadmin && Residence.resadminToggle.contains(player.getName())) {
	    if (!Residence.gmanager.isResidenceAdmin(player)) {
		Residence.resadminToggle.remove(player.getName());
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
		Residence.getResidenceManager().removeAllByOwner(player, args[1]);
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("RemovePlayersResidences", ChatColor.YELLOW + args[1] + ChatColor.GREEN));
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
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
		ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);
		res.getPermissions().applyDefaultFlags(player, resadmin);
		return true;
	    }
	    return false;
	}
	if (cmd.equals("limits")) {
	    if (args.length == 1 || args.length == 2) {
		final String[] tempArgs = args;
		final Player p = player;
		Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
		    @Override
		    public void run() {
			OfflinePlayer target;
			boolean rsadm = false;
			if (tempArgs.length == 1) {
			    target = p;
			    rsadm = true;
			} else
			    target = Residence.getOfflinePlayer(tempArgs[1]);
			if (target == null)
			    return;
			Residence.gmanager.getGroup(target.getName(), Residence.getConfigManager().getDefaultWorld()).printLimits(p, target, rsadm);
			return;
		    }
		});
		return true;
	    }
	    return false;
	}
	if (cmd.equals("signupdate")) {
	    if (args.length == 1) {
		if (!resadmin) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		    return true;
		}
		int number = Residence.getSignUtil().updateAllSigns();
		player.sendMessage(Residence.getLanguage().getPhrase("SignsUpdated", String.valueOf(number)));
		return true;
	    }
	    return false;
	}
	if (cmd.equals("info")) {
	    if (args.length == 1) {
		String area = Residence.getResidenceManager().getNameByLoc(player.getLocation());
		if (area != null) {
		    Residence.getResidenceManager().printAreaInfo(area, player);
		} else {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
		}
		return true;
	    } else if (args.length == 2) {
		Residence.getResidenceManager().printAreaInfo(args[1], player);
		return true;
	    }
	    return false;
	}
	if (cmd.equals("padd")) {
	    String baseCmd = "res";
	    if (resadmin)
		baseCmd = "resadmin";
	    if (args.length == 2) {
		if (!Residence.isPlayerExist(player, args[1], true))
		    return false;

		Bukkit.dispatchCommand(player, baseCmd + " pset " + args[1] + " trusted true");
		return true;
	    }
	    if (args.length == 3) {
		if (!Residence.isPlayerExist(player, args[2], true))
		    return false;
		Bukkit.dispatchCommand(player, baseCmd + " pset " + args[1] + " " + args[2] + " trusted true");
		return true;
	    }
	    return false;
	}
	if (cmd.equals("pdel")) {
	    String baseCmd = "res";
	    if (resadmin)
		baseCmd = "resadmin";
	    if (args.length == 2) {
		Bukkit.dispatchCommand(player, baseCmd + " pset " + args[1] + " trusted remove");
		return true;
	    }
	    if (args.length == 3) {
		Bukkit.dispatchCommand(player, baseCmd + " pset " + args[1] + " " + args[2] + " trusted remove");
		return true;
	    }
	    return false;
	}
	if (cmd.equals("check")) {
	    if (args.length == 3 || args.length == 4) {
		if (args.length == 4) {
		    pname = args[3];
		}
		ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);
		if (res == null) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
		    return true;
		}
		if (!res.getPermissions().hasApplicableFlag(pname, args[2])) {
		    player.sendMessage(Residence.getLanguage().getPhrase("FlagCheckFalse", ChatColor.YELLOW + args[2] + ChatColor.RED + "|" + ChatColor.YELLOW + pname
			+ ChatColor.RED
			+ "|" + ChatColor.YELLOW + args[1] + ChatColor.RED));
		} else {
		    player.sendMessage(Residence.getLanguage().getPhrase("FlagCheckTrue", ChatColor.GREEN + args[2] + ChatColor.YELLOW + "|" + ChatColor.GREEN + pname
			+ ChatColor.YELLOW
			+ "|" + ChatColor.YELLOW + args[1] + ChatColor.RED + "|" + (res.getPermissions().playerHas(pname, res.getPermissions().getWorld(), args[2], false)
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
	    String res = Residence.getResidenceManager().getNameByLoc(player.getLocation());
	    if (res == null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NotInResidence"));
	    } else {
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("InResidence", ChatColor.YELLOW + res + ChatColor.GREEN));
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
		Residence.getResidenceManager().listResidences(player);
		return true;
	    } else if (args.length == 2) {
		try {
		    Integer.parseInt(args[1]);
		    Residence.getResidenceManager().listResidences(player, page);
		} catch (Exception ex) {
		    Residence.getResidenceManager().listResidences(player, args[1]);
		}
		return true;
	    } else if (args.length == 3) {
		Residence.getResidenceManager().listResidences(player, args[1], page);
		return true;
	    }
	    return false;
	}
	if (cmd.equals("shop")) {
	    if ((args.length == 2 || args.length == 3 || args.length == 4) && (args[1].equalsIgnoreCase("votes") || args[1].equalsIgnoreCase("likes"))) {

		int VotePage = 1;

		ClaimedResidence res = null;
		if (args.length == 2) {
		    res = Residence.getResidenceManager().getByLoc(player.getLocation());
		    if (res == null) {
			player.sendMessage(Residence.getLanguage().getPhrase("NotInResidence"));
			return true;
		    }
		} else if (args.length == 3) {
		    res = Residence.getResidenceManager().getByName(args[2]);
		    if (res == null) {
			try {
			    VotePage = Integer.parseInt(args[2]);
			    res = Residence.getResidenceManager().getByLoc(player.getLocation());
			    if (res == null) {
				player.sendMessage(Residence.getLanguage().getPhrase("NotInResidence"));
				return true;
			    }
			} catch (Exception ex) {
			    player.sendMessage(Residence.getLanguage().getPhrase("UseNumbers"));
			    return true;
			}
		    }

		} else if (args.length == 4) {
		    res = Residence.getResidenceManager().getByName(args[2]);
		    if (res == null) {
			player.sendMessage(Residence.getLanguage().getPhrase("NotInResidence"));
			return true;
		    }
		    try {
			VotePage = Integer.parseInt(args[3]);
		    } catch (Exception ex) {
			player.sendMessage(Residence.getLanguage().getPhrase("UseNumbers"));
			return true;
		    }
		}

		if (res == null) {
		    player.sendMessage(Residence.getLanguage().getPhrase("NotInResidence"));
		    return true;
		}

		Map<String, List<ShopVote>> ShopList = Residence.getShopSignUtilManager().GetAllVoteList();

		List<ShopVote> VoteList = new ArrayList<ShopVote>();
		if (ShopList.containsKey(res.getName())) {
		    VoteList = ShopList.get(res.getName());
		}

		String separator = ChatColor.GOLD + "";
		String simbol = "\u25AC";
		for (int i = 0; i < 5; i++) {
		    separator += simbol;
		}
		int pagecount = (int) Math.ceil((double) VoteList.size() / (double) 10);
		if (page > pagecount || page < 1) {
		    sender.sendMessage(ChatColor.RED + Residence.getLM().getMessage("Language.Shop.NoVotes"));
		    return true;
		}

		player.sendMessage(Residence.getLM().getMessage("Language.Shop.VotesTopLine", separator + "%" + res.getName() + "%" + VotePage + "%" + pagecount + "%"
		    + separator));

		int start = VotePage * 10 - 9;
		int end = VotePage * 10 + 1;
		int position = 0;
		int i = start;
		for (ShopVote one : VoteList) {
		    position++;

		    if (position < start)
			continue;

		    if (position >= end)
			break;

		    Date dNow = new Date(one.getTime());
		    SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
		    ft.setTimeZone(TimeZone.getTimeZone(Residence.getConfigManager().getTimeZone()));
		    String timeString = ft.format(dNow);

		    String message = Residence.getLM().getMessage("Language.Shop.VotesList", i + "%" + one.getName() + "%" + (Residence.getConfigManager().isOnlyLike()
			? "" : one.getVote()) + "%" + timeString);
		    player.sendMessage(message);
		    i++;
		}

		if (pagecount == 1)
		    return true;

		int NextPage = page + 1;
		NextPage = page < pagecount ? NextPage : page;
		int Prevpage = page - 1;
		Prevpage = page > 1 ? Prevpage : page;

		String prevCmd = "/res shop votes " + res.getName() + " " + Prevpage;
		String prev = "[\"\",{\"text\":\"" + separator + " " + Residence.getLanguage().getPhrase("PrevInfoPage")
		    + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + prevCmd
		    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + "<<<" + "\"}]}}}";
		String nextCmd = "/res shop votes " + res.getName() + " " + NextPage;
		String next = " {\"text\":\"" + Residence.getLanguage().getPhrase("NextInfoPage") + " " + separator
		    + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + nextCmd
		    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ">>>" + "\"}]}}}]";

		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + prev + "," + next);

		return true;
	    }
	    if ((args.length == 2 || args.length == 3) && args[1].equalsIgnoreCase("list")) {

		int Shoppage = 1;

		if (args.length == 3) {
		    try {
			Shoppage = Integer.parseInt(args[2]);
		    } catch (Exception ex) {
			player.sendMessage(Residence.getLanguage().getPhrase("UseNumbers"));
			return true;
		    }
		}

		Map<String, Double> ShopList = Residence.getShopSignUtilManager().getSortedShopList();

		String separator = ChatColor.GOLD + "";
		String simbol = "\u25AC";
		for (int i = 0; i < 5; i++) {
		    separator += simbol;
		}
		int pagecount = (int) Math.ceil((double) ShopList.size() / (double) 10);
		if (page > pagecount || page < 1) {
		    sender.sendMessage(ChatColor.RED + Residence.getLM().getMessage("Language.Shop.NoVotes"));
		    return true;
		}

		player.sendMessage(Residence.getLM().getMessage("Language.Shop.ListTopLine", separator + "%" + Shoppage + "%" + pagecount + "%" + separator));

		int start = Shoppage * 10 - 9;
		int end = Shoppage * 10 + 1;
		int position = 0;
		int i = start;
		for (Entry<String, Double> one : ShopList.entrySet()) {
		    position++;

		    if (position < start)
			continue;

		    if (position >= end)
			break;

		    Vote vote = Residence.getShopSignUtilManager().getAverageVote(one.getKey());
		    String votestat = "";

		    if (Residence.getConfigManager().isOnlyLike()) {
			votestat = vote.getAmount() == 0 ? "" : Residence.getLM().getMessage("Language.Shop.ListLiked", Residence.getShopSignUtilManager().getLikes(one
			    .getKey()));
		    } else
			votestat = vote.getAmount() == 0 ? "" : Residence.getLM().getMessage("Language.Shop.ListVoted", vote.getVote() + "%" + vote.getAmount());
		    ClaimedResidence res = Residence.getResidenceManager().getByName(one.getKey());
		    String message = Residence.getLM().getMessage("Language.Shop.List", i + "%" + one.getKey() + "%" + Residence.getResidenceManager().getByName(one
			.getKey()).getOwner() + "%" + votestat);

		    String desc = res.getShopDesc() == null ? Residence.getLM().getMessage("Language.Shop.NoDesc") : Residence.getLM().getMessage(
			"Language.Shop.Desc", ChatColor.translateAlternateColorCodes('&', res.getShopDesc().replace("/n", "\n")));

		    String prev = "[\"\",{\"text\":\"" + ChatColor.GOLD + " " + message
			+ "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/res tp " + one.getKey()
			+ " \"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + desc + "\"}]}}}]";

		    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + prev);

		    i++;
		}

		if (pagecount == 1)
		    return true;

		int NextPage = page + 1;
		NextPage = page < pagecount ? NextPage : page;
		int Prevpage = page - 1;
		Prevpage = page > 1 ? Prevpage : page;

		String prevCmd = "/res shop list " + Prevpage;
		String prev = "[\"\",{\"text\":\"" + separator + " " + Residence.getLanguage().getPhrase("PrevInfoPage")
		    + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + prevCmd
		    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + "<<<" + "\"}]}}}";
		String nextCmd = "/res shop list " + NextPage;
		String next = " {\"text\":\"" + Residence.getLanguage().getPhrase("NextInfoPage") + " " + separator
		    + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + nextCmd
		    + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ">>>" + "\"}]}}}]";

		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + prev + "," + next);

		return true;
	    }

	    if (args.length == 2 && args[1].equalsIgnoreCase("DeleteBoard")) {

		if (!resadmin) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		    return true;
		}

		ShopListener.Delete.add(player.getName());
		player.sendMessage(Residence.getLM().getMessage("Language.Shop.DeleteBoard"));
		return true;
	    }
	    if (args.length > 2 && args[1].equalsIgnoreCase("setdesc")) {

		ClaimedResidence res = null;

		String desc = "";
		if (args.length >= 2) {
		    res = Residence.getResidenceManager().getByLoc(player.getLocation());
		    if (res == null) {
			player.sendMessage(Residence.getLanguage().getPhrase("NotInResidence"));
			return true;
		    } else {
			for (int i = 2; i < args.length; i++) {
			    desc += args[i];
			    if (i < args.length - 1)
				desc += " ";
			}
		    }
		}

		if (res == null)
		    return true;

		if (!res.isOwner(player) && !resadmin) {
		    player.sendMessage(Residence.getLanguage().getPhrase("NonAdmin"));
		    return true;
		}

		res.setShopDesc(desc);
		player.sendMessage(Residence.getLM().getMessage("Language.Shop.DescChange", ChatColor.translateAlternateColorCodes('&', desc)));
		return true;
	    }
	    if (args.length == 3 && args[1].equalsIgnoreCase("createboard")) {

		if (!resadmin) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		    return true;
		}

		if (!Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
		    player.sendMessage(Residence.getLanguage().getPhrase("SelectPoints"));
		    return true;
		}

		int place = 1;
		try {
		    place = Integer.parseInt(args[2]);
		} catch (Exception ex) {
		    player.sendMessage(Residence.getLanguage().getPhrase("UseNumbers"));
		    return true;
		}

		if (place < 1)
		    place = 1;

		Location loc1 = Residence.getSelectionManager().getPlayerLoc1(player.getName());
		Location loc2 = Residence.getSelectionManager().getPlayerLoc2(player.getName());

		if (loc1.getBlockY() < loc2.getBlockY()) {
		    player.sendMessage(Residence.getLM().getMessage("Language.Shop.InvalidSelection"));
		    return true;
		}

		Board newTemp = new Board();
		newTemp.setStartPlace(place);
		newTemp.setWorld(loc1.getWorld().getName());
		newTemp.setTX(loc1.getBlockX());
		newTemp.setTY(loc1.getBlockY());
		newTemp.setTZ(loc1.getBlockZ());
		newTemp.setBX(loc2.getBlockX());
		newTemp.setBY(loc2.getBlockY());
		newTemp.setBZ(loc2.getBlockZ());

		newTemp.GetTopLocation();
		newTemp.GetBottomLocation();

		newTemp.GetLocations();

		Residence.getShopSignUtilManager().addBoard(newTemp);
		player.sendMessage(Residence.getLM().getMessage("Language.Shop.NewBoard"));

		Residence.getShopSignUtilManager().BoardUpdate();
		Residence.getShopSignUtilManager().saveSigns();

		return true;

	    }
	    if ((args.length == 2 || args.length == 3 || args.length == 4) && (args[1].equalsIgnoreCase("vote") || args[1].equalsIgnoreCase("like"))) {
		String resName = "";
		int vote = 5;
		ClaimedResidence res = null;
		if (args.length == 3) {

		    if (Residence.getConfigManager().isOnlyLike()) {

			res = Residence.getResidenceManager().getByName(args[2]);
			if (res == null) {
			    player.sendMessage(Residence.getLanguage().getPhrase("InvalidResidence"));
			    return true;
			}
			vote = Residence.getConfigManager().getVoteRangeTo();

		    } else {
			res = Residence.getResidenceManager().getByLoc(player.getLocation());
			if (res == null) {
			    player.sendMessage(Residence.getLanguage().getPhrase("NotInResidence"));
			    return true;
			}

			try {
			    vote = Integer.parseInt(args[2]);
			} catch (Exception ex) {
			    player.sendMessage(Residence.getLanguage().getPhrase("UseNumbers"));
			    return true;
			}
		    }
		} else if (args.length == 2 && Residence.getConfigManager().isOnlyLike()) {
		    res = Residence.getResidenceManager().getByLoc(player.getLocation());
		    if (res == null) {
			player.sendMessage(Residence.getLanguage().getPhrase("NotInResidence"));
			return true;
		    }
		    vote = Residence.getConfigManager().getVoteRangeTo();
		} else if (args.length == 4 && !Residence.getConfigManager().isOnlyLike()) {
		    res = Residence.getResidenceManager().getByName(args[2]);
		    if (res == null) {
			player.sendMessage(Residence.getLanguage().getPhrase("InvalidResidence"));
			return true;
		    }
		    try {
			vote = Integer.parseInt(args[3]);
		    } catch (Exception ex) {
			player.sendMessage(Residence.getLanguage().getPhrase("UseNumbers"));
			return true;
		    }
		} else if (args.length == 3 && !Residence.getConfigManager().isOnlyLike()) {
		    res = Residence.getResidenceManager().getByLoc(player.getLocation());
		    if (res == null) {
			player.sendMessage(Residence.getLanguage().getPhrase("InvalidResidence"));
			return true;
		    }
		    try {
			vote = Integer.parseInt(args[3]);
		    } catch (Exception ex) {
			player.sendMessage(Residence.getLanguage().getPhrase("UseNumbers"));
			return true;
		    }
		} else {
		    return false;
		}

		resName = res.getName();

		if (!res.getPermissions().has("shop", false)) {
		    player.sendMessage(Residence.getLM().getMessage("Language.Shop.CantVote"));
		    return true;
		}

		if (vote < Residence.getConfigManager().getVoteRangeFrom() || vote > Residence.getConfigManager().getVoteRangeTo()) {
		    player.sendMessage(Residence.getLM().getMessage("Language.Shop.VotedRange", Residence.getConfigManager().getVoteRangeFrom() + "%" + Residence
			.getConfigManager().getVoteRangeTo()));
		    return true;
		}

		ConcurrentHashMap<String, List<ShopVote>> VoteList = Residence.getShopSignUtilManager().GetAllVoteList();

		if (VoteList.containsKey(resName)) {
		    List<ShopVote> list = VoteList.get(resName);
		    boolean found = false;
		    for (ShopVote OneVote : list) {
			if (OneVote.getName().equalsIgnoreCase(player.getName())) {

			    if (Residence.getConfigManager().isOnlyLike()) {
				player.sendMessage(Residence.getLM().getMessage("Language.Shop.AlreadyLiked", resName));
				return true;
			    }

			    player.sendMessage(Residence.getLM().getMessage("Language.Shop.VoteChanged", OneVote.getVote() + "%" + vote + "%" + resName));
			    OneVote.setVote(vote);
			    OneVote.setTime(System.currentTimeMillis());
			    found = true;
			    break;
			}
		    }
		    if (!found) {
			ShopVote newVote = new ShopVote(player.getName(), vote, System.currentTimeMillis());
			list.add(newVote);

			if (Residence.getConfigManager().isOnlyLike())
			    player.sendMessage(Residence.getLM().getMessage("Language.Shop.Liked", resName));
			else
			    player.sendMessage(Residence.getLM().getMessage("Language.Shop.Voted", vote + "%" + resName));
		    }
		} else {
		    List<ShopVote> list = new ArrayList<ShopVote>();
		    ShopVote newVote = new ShopVote(player.getName(), vote, System.currentTimeMillis());
		    list.add(newVote);
		    VoteList.put(resName, list);
		    if (Residence.getConfigManager().isOnlyLike())
			player.sendMessage(Residence.getLM().getMessage("Language.Shop.Liked", resName));
		    else
			player.sendMessage(Residence.getLM().getMessage("Language.Shop.Voted", vote + "%" + resName));
		}
		Residence.getShopSignUtilManager().saveShopVotes();
		Residence.getShopSignUtilManager().BoardUpdate();
		return true;
	    }
	    return false;
	}
	if (cmd.equals("listhidden")) {
	    if (!resadmin) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return true;
	    }
	    if (args.length == 1) {
		Residence.getResidenceManager().listResidences(player, 1, true);
		return true;
	    } else if (args.length == 2) {
		try {
		    Integer.parseInt(args[1]);
		    Residence.getResidenceManager().listResidences(player, page, true);
		} catch (Exception ex) {
		    Residence.getResidenceManager().listResidences(player, args[1], 1, true);
		}
		return true;
	    } else if (args.length == 3) {
		Residence.getResidenceManager().listResidences(player, args[1], page, true);
		return true;
	    }
	    return false;
	}
	if (cmd.equals("rename")) {
	    if (args.length == 3) {
		Residence.getResidenceManager().renameResidence(player, args[1], args[2], resadmin);
		return true;
	    }
	    return false;
	}
	if (cmd.equals("renamearea")) {
	    if (args.length == 4) {
		ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);
		if (res == null) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
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
	    group = Residence.gmanager.getGroup(player);
	    if (!group.hasUnstuckAccess()) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return true;
	    }
	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    if (res == null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NotInResidence"));
	    } else {
		player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("Moved") + "...");
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
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NotOnline"));
		return true;
	    }
	    group = Residence.gmanager.getGroup(player);
	    if (!group.hasKickAccess() && !resadmin) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return true;
	    }
	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(targetplayer.getLocation());

	    if (res == null || res != null && !res.isOwner(player) && !resadmin) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("PlayerNotInResidence"));
		return true;
	    }

	    if (res.isOwner(player)) {
		if (res.getPlayersInResidence().contains(targetplayer)) {

		    Location loc = Residence.getConfigManager().getKickLocation();
		    if (loc != null)
			targetplayer.teleport(loc);
		    else
			targetplayer.teleport(res.getOutsideFreeLoc(player.getLocation()));
		    targetplayer.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("Kicked") + "!");
		}
	    }

	}
	if (cmd.equals("mirror")) {
	    if (args.length != 3) {
		return false;
	    }
	    Residence.getResidenceManager().mirrorPerms(player, args[2], args[1], resadmin);
	    return true;
	}
	if (cmd.equals("listall")) {
	    if (args.length == 1) {
		Residence.getResidenceManager().listAllResidences(player, 1);
	    } else if (args.length == 2) {
		try {
		    Residence.getResidenceManager().listAllResidences(player, page);
		} catch (Exception ex) {
		}
	    } else {
		return false;
	    }
	    return true;
	}
	if (cmd.equals("listallhidden")) {
	    if (!resadmin) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return true;
	    }
	    if (args.length == 1) {
		Residence.getResidenceManager().listAllResidences(player, 1, true);
	    } else if (args.length == 2) {
		try {
		    Residence.getResidenceManager().listAllResidences(player, page, true);
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
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("MaterialGet", ChatColor.GOLD + args[1] + ChatColor.GREEN + "|" + ChatColor.RED
		    + Material
			.getMaterial(Integer.parseInt(args[1])).name() + ChatColor.GREEN));
	    } catch (Exception ex) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidMaterial"));
	    }
	    return true;
	}
	if (cmd.equals("tpset")) {
	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    if (res != null) {
		res.setTpLoc(player, resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    }
	    return true;
	}
	if (cmd.equals("tp")) {
	    if (args.length != 2) {
		return false;
	    }
	    ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);

	    if (res == null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
		return true;
	    }
	    res.tpToResidence(player, player, resadmin);
	    return true;
	}

	if (cmd.equals("rt")) {
	    if (args.length != 1) {
		return false;
	    }

	    int sec = Residence.getConfigManager().getrtCooldown();
	    if (rtMap.containsKey(player.getName()) && !resadmin) {
		if (rtMap.get(player.getName()) + (sec * 1000) > System.currentTimeMillis()) {
		    int left = (int) (sec - ((System.currentTimeMillis() - rtMap.get(player.getName())) / 1000));
		    player.sendMessage(ChatColor.RED + Residence.getLM().getMessage("Language.RandomTeleport.TpLimit", String.valueOf(left)));
		    return true;
		}
	    }

	    if (!player.hasPermission("residence.randomtp") && !resadmin) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return true;
	    }

	    Location loc = RandomTp.getRandomlocation(player.getLocation().getWorld().getName());
	    rtMap.put(pname, System.currentTimeMillis());

	    if (loc == null) {
		player.sendMessage(Residence.getLM().getMessage("Language.RandomTeleport.IncorrectLocation", String.valueOf(sec)));
		return true;
	    }

	    if (Residence.getConfigManager().getTeleportDelay() > 0 && !resadmin) {
		player.sendMessage(ChatColor.GREEN + Residence.getLM().getMessage("Language.RandomTeleport.TeleportStarted", loc.getX() + "%" + loc.getY() + "%" + loc
		    .getZ() + "%" + Residence.getConfigManager().getTeleportDelay()));
		teleportDelayMap.add(player.getName());
		Residence.getRandomTpManager().performDelaydTp(loc, player);
	    } else
		RandomTp.performInstantTp(loc, player);

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
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoTeleportConfirm"));
	    return true;
	}

	if (cmd.equals("lease")) {
	    return commandResLease(args, resadmin, player, page);
	}

	if (cmd.equals("market")) {
	    return commandResMarket(args, resadmin, player, page);
	}

	if (cmd.equals("message")) {
	    return commandResMessage(args, resadmin, player, page);
	}
	if (cmd.equals("give") && args.length == 3) {
	    Residence.getResidenceManager().giveResidence(player, args[2], args[1], resadmin);
	    return true;
	}
	if (cmd.equals("server")) {
	    if (!resadmin) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return true;
	    }
	    if (args.length == 2) {
		ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);
		if (res == null) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
		    return true;
		}
		res.getPermissions().setOwner(Residence.getServerLandname(), false);
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("ResidenceOwnerChange", ChatColor.YELLOW + args[1] + ChatColor.GREEN + "|"
		    + ChatColor.YELLOW
		    + Residence.getServerLandname() + ChatColor.GREEN));
		return true;
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
		return true;
	    }
	}
	if (cmd.equals("clearflags")) {
	    if (!resadmin) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		return true;
	    }
	    ClaimedResidence area = Residence.getResidenceManager().getByName(args[1]);
	    if (area != null) {
		area.getPermissions().clearFlags();
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("FlagsCleared"));
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    }
	    return true;
	}
	if (cmd.equals("tool")) {
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("SelectionTool") + ":" + ChatColor.GREEN + Material.getMaterial(Residence.cmanager
		.getSelectionTooldID()));
	    player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("InfoTool") + ": " + ChatColor.GREEN + Material.getMaterial(Residence.cmanager
		.getInfoToolID()));
	    return true;
	}
	return false;
    }

    private boolean commandHelp(String[] args, boolean resadmin, CommandSender sender, Command command) {
	if (Residence.helppages == null)
	    return false;

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
		sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidHelp"));
	    }
	}

	if (command.getName().equalsIgnoreCase("res"))
	    resadmin = false;

	if (Residence.helppages.containesEntry(helppath))
	    Residence.helppages.printHelp(sender, page, helppath, resadmin);

	return true;
    }

    private boolean commandResExpand(String[] args, boolean resadmin, Player player, int page) {
	String resName;
	String areaName = null;
	ClaimedResidence res = null;
	if (args.length == 2)
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	else if (args.length == 3) {
	    res = Residence.getResidenceManager().getByName(args[1]);
	} else
	    return false;

	if (res == null) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    return true;
	}

	if (res.isSubzone() && !player.hasPermission("residence.expand.subzone") && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Language.CantExpandSubzone"));
	    return false;
	}

	if (!res.isSubzone() && !player.hasPermission("residence.expand") && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Language.CantExpandResidence"));
	    return false;
	}

	resName = res.getName();
	CuboidArea area = null;

	if (args.length == 2) {
	    areaName = res.getAreaIDbyLoc(player.getLocation());
	    area = res.getArea(areaName);
	} else if (args.length == 3) {
	    areaName = res.isSubzone() ? Residence.getResidenceManager().getSubzoneNameByRes(res) : "main";
	    area = res.getCuboidAreabyName(areaName);
	}

	if (area != null) {
	    Residence.smanager.placeLoc1(player, area.getHighLoc(), false);
	    Residence.smanager.placeLoc2(player, area.getLowLoc(), false);
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SelectionArea", ChatColor.GOLD + areaName + ChatColor.GREEN + "|" + ChatColor.GOLD
		+ resName + ChatColor.GREEN));
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaNonExist"));
	    return true;
	}
	int amount = -1;
	try {
	    if (args.length == 2)
		amount = Integer.parseInt(args[1]);
	    else if (args.length == 3)
		amount = Integer.parseInt(args[2]);
	} catch (Exception ex) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidAmount"));
	    return true;
	}

	if (amount > 1000) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidAmount"));
	    return true;
	}

	if (amount < 0)
	    amount = 1;

	Residence.smanager.modify(player, false, amount);

	if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
	    if (Residence.wep != null) {
		if (Residence.wepid == Residence.getConfigManager().selectionToolId) {
		    Residence.smanager.worldEdit(player);
		}
	    }
	    res.replaceArea(player, new CuboidArea(Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager().getPlayerLoc2(player
		.getName())), areaName, resadmin);
	    return true;
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
	}
	return false;
    }

    private boolean commandResContract(String[] args, boolean resadmin, Player player, int page) {
	String resName;
	String areaName = null;
	ClaimedResidence res = null;
	if (args.length == 2)
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	else if (args.length == 3)
	    res = Residence.getResidenceManager().getByName(args[1]);
	else
	    return false;
	if (res == null) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    return true;
	}

	if (res.isSubzone() && !player.hasPermission("residence.contract.subzone") && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Language.CantContractSubzone"));
	    return false;
	}

	if (!res.isSubzone() && !player.hasPermission("residence.contract") && !resadmin) {
	    player.sendMessage(Residence.getLM().getMessage("Language.CantContractResidence"));
	    return false;
	}

	resName = res.getName();
	CuboidArea area = null;

	if (args.length == 2) {
	    areaName = res.getAreaIDbyLoc(player.getLocation());
	    area = res.getArea(areaName);
	} else if (args.length == 3) {
	    areaName = res.isSubzone() ? Residence.getResidenceManager().getSubzoneNameByRes(res) : "main";
	    area = res.getCuboidAreabyName(areaName);
	}

	if (area != null) {
	    Residence.smanager.placeLoc1(player, area.getHighLoc(), false);
	    Residence.smanager.placeLoc2(player, area.getLowLoc(), false);
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SelectionArea", ChatColor.GOLD + areaName + ChatColor.GREEN + "|" + ChatColor.GOLD
		+ resName + ChatColor.GREEN));
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaNonExist"));
	    return true;
	}
	int amount = -1;
	try {
	    if (args.length == 2)
		amount = Integer.parseInt(args[1]);
	    else if (args.length == 3)
		amount = Integer.parseInt(args[2]);
	} catch (Exception ex) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidAmount"));
	    return true;
	}

	if (amount > 1000) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidAmount"));
	    return true;
	}

	if (amount < 0)
	    amount = 1;

	if (!Residence.smanager.contract(player, amount, resadmin))
	    return true;

	if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
	    if (Residence.wep != null) {
		if (Residence.wepid == Residence.getConfigManager().selectionToolId) {
		    Residence.smanager.worldEdit(player);
		}
	    }
	    res.replaceArea(player, new CuboidArea(Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager().getPlayerLoc2(player
		.getName())), areaName, resadmin);
	    return true;
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
	}
	return false;
    }

    private boolean commandResSelect(String[] args, boolean resadmin, Player player, int page) {
	PermissionGroup group = Residence.getPermissionManager().getGroup(player);
	if (!group.selectCommandAccess() && !resadmin) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectDiabled"));
	    return true;
	}
	if (!group.canCreateResidences() && group.getMaxSubzoneDepth(player.getName()) <= 0 && !resadmin) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectDiabled"));
	    return true;
	}
	if ((!player.hasPermission("residence.create") && player.isPermissionSet("residence.create") && !player.hasPermission("residence.select") && player
	    .isPermissionSet("residence.select")) && !resadmin) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectDiabled"));
	    return true;
	}
	if (args.length == 2) {
	    if (args[1].equals("size") || args[1].equals("cost")) {
		if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
		    try {
			Residence.smanager.showSelectionInfo(player);
			return true;
		    } catch (Exception ex) {
			Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
			return true;
		    }
		} else if (Residence.smanager.worldEdit(player)) {
		    try {
			Residence.smanager.showSelectionInfo(player);
			return true;
		    } catch (Exception ex) {
			Logger.getLogger(Residence.class.getName()).log(Level.SEVERE, null, ex);
			return true;
		    }
		}
	    } else if (args[1].equals("vert")) {
		Residence.smanager.vert(player, resadmin);
		return true;
	    } else if (args[1].equals("sky")) {
		Residence.smanager.sky(player, resadmin);
		return true;
	    } else if (args[1].equals("bedrock")) {
		Residence.smanager.bedrock(player, resadmin);
		return true;
	    } else if (args[1].equals("coords")) {
		Location playerLoc1 = Residence.getSelectionManager().getPlayerLoc1(player.getName());
		if (playerLoc1 != null) {
		    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("Primary.Selection") + ":" + ChatColor.AQUA + " (" + playerLoc1.getBlockX()
			+ ", "
			+ playerLoc1.getBlockY() + ", " + playerLoc1.getBlockZ() + ")");
		}
		Location playerLoc2 = Residence.getSelectionManager().getPlayerLoc2(player.getName());
		if (playerLoc2 != null) {
		    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("Secondary.Selection") + ":" + ChatColor.AQUA + " (" + playerLoc2.getBlockX()
			+ ", "
			+ playerLoc2.getBlockY() + ", " + playerLoc2.getBlockZ() + ")");
		}
		return true;
	    } else if (args[1].equals("chunk")) {
		Residence.smanager.selectChunk(player);
		return true;
	    } else if (args[1].equals("worldedit")) {
		if (Residence.smanager.worldEdit(player)) {
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
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidAmount"));
		    return true;
		}
		Residence.smanager.modify(player, false, amount);
		return true;
	    } else if (args[1].equals("shift")) {
		int amount;
		try {
		    amount = Integer.parseInt(args[2]);
		} catch (Exception ex) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidAmount"));
		    return true;
		}
		Residence.smanager.modify(player, true, amount);
		return true;
	    }
	}
	if ((args.length == 2 || args.length == 3) && args[1].equals("auto")) {
	    Player target = player;
	    if (args.length == 3) {
		if (!player.hasPermission("residence.select.auto.others")) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		    return true;
		}
		target = Bukkit.getPlayer(args[2]);
		if (target == null) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NotOnline"));
		    return true;
		}
	    }
	    AutoSelection.switchAutoSelection(target);
	    return true;
	}
	if (args.length > 1 && args[1].equals("residence")) {
	    String resName;
	    String areaName;
	    ClaimedResidence res = null;
	    if (args.length > 2) {
		res = Residence.getResidenceManager().getByName(args[2]);
	    } else {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    }
	    if (res == null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
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
		Residence.smanager.placeLoc1(player, area.getHighLoc(), true);
		Residence.smanager.placeLoc2(player, area.getLowLoc(), true);
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("SelectionArea", ChatColor.GOLD + areaName + ChatColor.GREEN + "|" + ChatColor.GOLD
		    + resName
		    + ChatColor.GREEN));
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("AreaNonExist"));
	    }
	    return true;
	} else {
	    try {
		Residence.smanager.selectBySize(player, Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
		return true;
	    } catch (Exception ex) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectionFail"));
		return true;
	    }
	}
    }

    private boolean commandResCreate(String[] args, boolean resadmin, Player player, int page) {
	if (args.length != 2) {
	    return false;
	}

	WorldEditPlugin wep = (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
	if (wep != null) {
	    if (wep.getConfig().getInt("wand-item") == Residence.getConfigManager().selectionToolId) {
		Residence.smanager.worldEdit(player);
	    }
	}
	if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
	    if (Residence.wg != null && WorldGuardUtil.isSelectionInRegion(player) == null) {
		Residence.getResidenceManager().addResidence(player, args[1], Residence.smanager.getPlayerLoc1(player.getName()), Residence.smanager.getPlayerLoc2(player
		    .getName()), resadmin);
		return true;
	    } else if (Residence.wg != null && WorldGuardUtil.isSelectionInRegion(player) != null) {
		ProtectedRegion Region = WorldGuardUtil.isSelectionInRegion(player);
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectOverlap", String.valueOf(Region.getId())));

		Location lowLoc = new Location(Residence.getSelectionManager().getPlayerLoc1(player.getName()).getWorld(), Region.getMinimumPoint().getBlockX(), Region
		    .getMinimumPoint().getBlockY(), Region.getMinimumPoint().getBlockZ());

		Location highLoc = new Location(Residence.getSelectionManager().getPlayerLoc1(player.getName()).getWorld(), Region.getMaximumPoint().getBlockX(), Region
		    .getMaximumPoint().getBlockY(), Region.getMaximumPoint().getBlockZ());

		Residence.getSelectionManager().NewMakeBorders(player, lowLoc, highLoc, true);
		Residence.getSelectionManager().NewMakeBorders(player, Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager()
		    .getPlayerLoc2(player.getName()), false);
	    } else if (Residence.wg == null) {
		Residence.getResidenceManager().addResidence(player, args[1], Residence.smanager.getPlayerLoc1(player.getName()), Residence.smanager.getPlayerLoc2(player
		    .getName()),
		    resadmin);
		return true;
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
		return true;
	    }
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
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
	    parent = Residence.getResidenceManager().getNameByLoc(player.getLocation());
	    zname = args[1];
	} else {
	    parent = args[1];
	    zname = args[2];
	}
	WorldEditPlugin wep = (WorldEditPlugin) Residence.server.getPluginManager().getPlugin("WorldEdit");
	if (wep != null) {
	    if (wep.getConfig().getInt("wand-item") == Residence.getConfigManager().selectionToolId) {
		Residence.smanager.worldEdit(player);
	    }
	}
	if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
	    ClaimedResidence res = Residence.getResidenceManager().getByName(parent);
	    if (res == null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
		return true;
	    }

	    if (!player.hasPermission("residence.create.subzone") && !resadmin) {
		player.sendMessage(Residence.getLM().getMessage("Language.CantCreateSubzone"));
		return false;
	    }

	    res.addSubzone(player, Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager().getPlayerLoc2(player.getName()),
		zname, resadmin);
	    return true;
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
	    return true;
	}
    }

    private boolean commandResArea(String[] args, boolean resadmin, Player player, int page) {
	if (args.length == 4) {
	    if (args[1].equals("remove")) {
		ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);
		if (res != null) {
		    res.removeArea(player, args[3], resadmin);
		} else {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
		}
		return true;
	    } else if (args[1].equals("add")) {
		WorldEditPlugin wep = (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
		if (wep != null) {
		    if (wep.getConfig().getInt("wand-item") == Residence.getConfigManager().selectionToolId) {
			Residence.smanager.worldEdit(player);
		    }
		}
		if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
		    ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);
		    if (res != null) {
			res.addArea(player, new CuboidArea(Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager().getPlayerLoc2(
			    player.getName())), args[3], resadmin);
		    } else {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
		    }
		} else {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
		}
		return true;
	    } else if (args[1].equals("replace")) {
		WorldEditPlugin wep = (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
		if (wep != null) {
		    if (wep.getConfig().getInt("wand-item") == Residence.getConfigManager().selectionToolId) {
			Residence.smanager.worldEdit(player);
		    }
		}
		if (Residence.getSelectionManager().hasPlacedBoth(player.getName())) {
		    ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);
		    if (res != null) {
			res.replaceArea(player, new CuboidArea(Residence.getSelectionManager().getPlayerLoc1(player.getName()), Residence.getSelectionManager()
			    .getPlayerLoc2(player.getName())), args[3], resadmin);
		    } else {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
		    }
		} else {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("SelectPoints"));
		}
		return true;
	    }
	}
	if ((args.length == 3 || args.length == 4) && args[1].equals("list")) {
	    ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);
	    if (res != null) {
		res.printAreaList(player, page);
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    }
	    return true;
	} else if ((args.length == 3 || args.length == 4) && args[1].equals("listall")) {
	    ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);
	    if (res != null) {
		res.printAdvancedAreaList(player, page);
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    }
	    return true;
	}
	return false;
    }

    private boolean commandResRemove(String[] args, boolean resadmin, CommandSender sender, int page) {

	Player player = null;
	if (sender instanceof Player) {
	    player = (Player) sender;

	    if (Residence.deleteConfirm.containsKey(player.getName()))
		Residence.deleteConfirm.remove(player.getName());

	    if (args.length == 1) {

		ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());

		if (res == null) {
		    player.sendMessage(Residence.getLM().getMessage("Language.InvalidResidence"));
		    return false;
		}

		if (res.isSubzone() && !player.hasPermission("residence.delete.subzone") && !resadmin) {
		    player.sendMessage(Residence.getLM().getMessage("Language.CantDeleteSubzone"));
		    return false;
		}

		if (res.isSubzone() && player.hasPermission("residence.delete.subzone") && !resadmin && Residence.getConfigManager().isPreventSubZoneRemoval() && !res
		    .getParent().isOwner(player)) {
		    player.sendMessage(Residence.getLM().getMessage("Language.CantDeleteSubzoneNotOwnerOfParent"));
		    return false;
		}

		if (!res.isSubzone() && !player.hasPermission("residence.delete") && !resadmin) {
		    player.sendMessage(Residence.getLM().getMessage("Language.CantDeleteResidence"));
		    return false;
		}

		if (res.isSubzone()) {
		    String area = Residence.getResidenceManager().getNameByLoc(player.getLocation());
		    String[] split = area.split("\\.");
		    String words = split[split.length - 1];
		    if (!Residence.deleteConfirm.containsKey(player.getName()) || !area.equalsIgnoreCase(Residence.deleteConfirm.get(player.getName()))) {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("DeleteSubzoneConfirm", ChatColor.YELLOW + words + ChatColor.RED));
			Residence.deleteConfirm.put(player.getName(), area);
		    } else {
			Residence.getResidenceManager().removeResidence(player, area, resadmin);
		    }
		    return true;
		} else {
		    if (!Residence.deleteConfirm.containsKey(player.getName()) || !res.getName().equalsIgnoreCase(Residence.deleteConfirm.get(player.getName()))) {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("DeleteConfirm", ChatColor.YELLOW + res.getName() + ChatColor.RED));
			Residence.deleteConfirm.put(player.getName(), res.getName());
		    } else {
			Residence.getResidenceManager().removeResidence(player, res.getName(), resadmin);
		    }
		    return true;
		}

	    }
	}
	if (args.length != 2) {
	    return false;
	}
	if (player != null) {
	    if (!Residence.deleteConfirm.containsKey(player.getName()) || !args[1].equalsIgnoreCase(Residence.deleteConfirm.get(player.getName()))) {
		String words = "";
		if (Residence.getResidenceManager().getByName(args[1]) != null) {
		    ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);
		    if (res.getParent() != null) {
			String[] split = args[1].split("\\.");
			words = split[split.length - 1];
		    }
		}
		if (words == "") {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("DeleteConfirm", ChatColor.YELLOW + args[1] + ChatColor.RED));
		} else {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("DeleteSubzoneConfirm", ChatColor.YELLOW + words + ChatColor.RED));
		}
		Residence.deleteConfirm.put(player.getName(), args[1]);
	    } else {
		Residence.getResidenceManager().removeResidence(player, args[1], resadmin);
	    }
	} else {
	    if (!Residence.deleteConfirm.containsKey("Console") || !args[1].equalsIgnoreCase(Residence.deleteConfirm.get("Console"))) {
		String words = "";
		if (Residence.getResidenceManager().getByName(args[1]) != null) {
		    ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);
		    if (res.getParent() != null) {
			String[] split = args[1].split("\\.");
			words = split[split.length - 1];
		    }
		}
		if (words == "") {
		    this.getServer().getConsoleSender().sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("DeleteConfirm", ChatColor.YELLOW + args[1]
			+ ChatColor.RED));
		} else {
		    this.getServer().getConsoleSender().sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("DeleteSubzoneConfirm", ChatColor.YELLOW + words
			+ ChatColor.RED));
		}
		Residence.deleteConfirm.put("Console", args[1]);
	    } else {
		Residence.getResidenceManager().removeResidence(args[1]);
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
	    String area = Residence.deleteConfirm.get(name);
	    if (area == null) {
		sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    } else {
		Residence.getResidenceManager().removeResidence(player, area, resadmin);
		Residence.deleteConfirm.remove(name);
		if (player == null) {
		    sender.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("ResidenceRemove", ChatColor.YELLOW + name + ChatColor.GREEN));
		}
	    }
	}
	return true;
    }

    private boolean commandResSet(String[] args, boolean resadmin, Player player, int page) {
	if (args.length == 3) {
	    String area = Residence.getResidenceManager().getNameByLoc(player.getLocation());
	    if (area != null) {
		Residence.getResidenceManager().getByName(area).getPermissions().setFlag(player, args[1], args[2], resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    }
	    return true;
	} else if (args.length == 4) {
	    ClaimedResidence area = Residence.getResidenceManager().getByName(args[1]);
	    if (area != null) {
		area.getPermissions().setFlag(player, args[2], args[3], resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    }
	    return true;
	} else if (args.length == 1 && Residence.getConfigManager().useFlagGUI) {
	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    if (res != null) {
		if (!res.isOwner(player) && !resadmin) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		    return true;
		}
		SetFlag flag = new SetFlag(res.getName(), player, resadmin);
		flag.recalculateResidence(res);
		ResidencePlayerListener.GUI.put(player.getName(), flag);
		player.openInventory(flag.getInventory());
	    } else
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    return true;
	} else if (args.length == 2 && Residence.getConfigManager().useFlagGUI) {
	    ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);
	    if (res != null) {
		if (!res.isOwner(player) && !resadmin) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		    return true;
		}
		SetFlag flag = new SetFlag(res.getName(), player, resadmin);
		flag.recalculateResidence(res);
		ResidencePlayerListener.GUI.put(player.getName(), flag);
		player.openInventory(flag.getInventory());
	    } else
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    return true;
	}
	return false;
    }

    private boolean commandResPset(String[] args, boolean resadmin, Player player, int page) {
	if (args.length == 3 && args[2].equalsIgnoreCase("removeall")) {
	    ClaimedResidence area = Residence.getResidenceManager().getByLoc(player.getLocation());
	    if (area != null) {
		area.getPermissions().removeAllPlayerFlags(player, args[1], resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    }
	    return true;
	} else if (args.length == 4 && args[3].equalsIgnoreCase("removeall")) {
	    ClaimedResidence area = Residence.getResidenceManager().getByName(args[1]);
	    if (area != null) {
		area.getPermissions().removeAllPlayerFlags(player, args[2], resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    }
	    return true;
	} else if (args.length == 4) {
	    ClaimedResidence area = Residence.getResidenceManager().getByLoc(player.getLocation());

	    if (!Residence.isPlayerExist(player, args[1], true))
		return false;

	    if (area != null) {
		area.getPermissions().setPlayerFlag(player, args[1], args[2], args[3], resadmin, true);
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    }
	    return true;
	} else if (args.length == 5) {
	    ClaimedResidence area = Residence.getResidenceManager().getByName(args[1]);
	    if (!Residence.isPlayerExist(player, args[2], true))
		return false;
	    if (area != null) {
		area.getPermissions().setPlayerFlag(player, args[2], args[3], args[4], resadmin, true);
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    }
	    return true;
	} else if (args.length == 2) {
	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    if (res != null) {
		if (!Residence.isPlayerExist(player, args[1], true))
		    return false;
		if (!res.isOwner(player) && !resadmin) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		    return true;
		}
		SetFlag flag = new SetFlag(res.getName(), player, resadmin);
		flag.setTargePlayer(args[1]);
		flag.recalculatePlayer(res);
		ResidencePlayerListener.GUI.put(player.getName(), flag);
		player.openInventory(flag.getInventory());
	    } else
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    return true;
	} else if (args.length == 3) {
	    ClaimedResidence res = Residence.getResidenceManager().getByName(args[1]);
	    if (res != null) {
		if (!Residence.isPlayerExist(player, args[2], true))
		    return false;
		if (!res.isOwner(player) && !resadmin) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		    return true;
		}

		SetFlag flag = new SetFlag(res.getName(), player, resadmin);
		flag.setTargePlayer(args[2]);
		flag.recalculatePlayer(res);
		ResidencePlayerListener.GUI.put(player.getName(), flag);
		player.openInventory(flag.getInventory());
	    } else
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    return true;
	}
	return false;
    }

    private boolean commandResGset(String[] args, boolean resadmin, Player player, int page) {
	if (args.length == 4) {
	    ClaimedResidence area = Residence.getResidenceManager().getByLoc(player.getLocation());
	    if (area != null) {
		area.getPermissions().setGroupFlag(player, args[1], args[2], args[3], resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidArea"));
	    }
	    return true;
	} else if (args.length == 5) {
	    ClaimedResidence area = Residence.getResidenceManager().getByName(args[1]);
	    if (area != null) {
		area.getPermissions().setGroupFlag(player, args[2], args[3], args[4], resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
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
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    showinfo = true;
	} else if (args.length == 3 && args[2].equals("info")) {
	    res = Residence.getResidenceManager().getByName(args[1]);
	    showinfo = true;
	}
	if (showinfo) {
	    if (res == null) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
		return true;
	    }
	    player.sendMessage(ChatColor.RED + "Blacklist:");
	    res.getItemBlacklist().printList(player);
	    player.sendMessage(ChatColor.GREEN + "Ignorelist:");
	    res.getItemIgnoreList().printList(player);
	    return true;
	} else if (args.length == 4) {
	    res = Residence.getResidenceManager().getByName(args[1]);
	    listtype = args[2];
	    try {
		mat = Material.valueOf(args[3].toUpperCase());
	    } catch (Exception ex) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidMaterial"));
		return true;
	    }
	} else if (args.length == 3) {
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    listtype = args[1];
	    try {
		mat = Material.valueOf(args[2].toUpperCase());
	    } catch (Exception ex) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidMaterial"));
		return true;
	    }
	}
	if (res != null) {
	    if (listtype.equalsIgnoreCase("blacklist")) {
		res.getItemBlacklist().playerListChange(player, mat, resadmin);
	    } else if (listtype.equalsIgnoreCase("ignorelist")) {
		res.getItemIgnoreList().playerListChange(player, mat, resadmin);
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidList"));
	    }
	    return true;
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    return true;
	}
    }

    private boolean commandResBank(String[] args, boolean resadmin, CommandSender sender, int page) {
	if (args.length != 3 && args.length != 4) {
	    return false;
	}
	ClaimedResidence res = null;

	if (args.length == 4) {
	    res = Residence.getResidenceManager().getByName(args[2]);
	    if (res == null) {
		sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
		return true;
	    }
	} else {
	    if (sender instanceof Player)
		res = Residence.getResidenceManager().getByLoc(((Player) sender).getLocation());
	}
	if (res == null) {
	    sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NotInResidence"));
	    return true;
	}
	int amount = 0;
	try {
	    if (args.length == 3)
		amount = Integer.parseInt(args[2]);
	    else
		amount = Integer.parseInt(args[3]);
	} catch (Exception ex) {
	    sender.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidAmount"));
	    return true;
	}
	if (args[1].equals("deposit")) {
	    res.getBank().deposit(sender, amount, resadmin);
	} else if (args[1].equals("withdraw")) {
	    res.getBank().withdraw(sender, amount, resadmin);
	} else {
	    return false;
	}
	return true;
    }

    private boolean commandResLease(String[] args, boolean resadmin, Player player, int page) {
	if (args.length == 2 || args.length == 3) {
	    if (args[1].equals("expires")) {
		ClaimedResidence res = null;
		if (args.length == 2) {
		    res = Residence.getResidenceManager().getByLoc(player.getLocation());
		    if (res == null) {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NotInResidence"));
			return true;
		    }
		} else {
		    res = Residence.getResidenceManager().getByName(args[2]);
		    if (res == null) {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
			return true;
		    }
		}

		String until = Residence.getLeaseManager().getExpireTime(res.getName());
		if (until != null)
		    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("LeaseRenew", until));
		return true;
	    }
	    if (args[1].equals("renew")) {
		if (args.length == 3) {
		    Residence.leasemanager.renewArea(args[2], player);
		} else {
		    Residence.leasemanager.renewArea(Residence.getResidenceManager().getNameByLoc(player.getLocation()), player);
		}
		return true;
	    } else if (args[1].equals("cost")) {
		if (args.length == 3) {
		    ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);
		    if (res == null || Residence.leasemanager.leaseExpires(args[2])) {
			int cost = Residence.leasemanager.getRenewCost(res);
			player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LeaseRenewalCost", ChatColor.RED + args[2] + ChatColor.YELLOW + "|"
			    + ChatColor.RED
			    + cost + ChatColor.YELLOW));
		    } else {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("LeaseNotExpire"));
		    }
		    return true;
		} else {
		    String area = Residence.getResidenceManager().getNameByLoc(player.getLocation());
		    ClaimedResidence res = Residence.getResidenceManager().getByName(area);
		    if (area == null || res == null) {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidArea"));
			return true;
		    }
		    if (Residence.leasemanager.leaseExpires(area)) {
			int cost = Residence.leasemanager.getRenewCost(res);
			player.sendMessage(ChatColor.YELLOW + Residence.getLanguage().getPhrase("LeaseRenewalCost", ChatColor.RED + area + ChatColor.YELLOW + "|"
			    + ChatColor.RED + cost
			    + ChatColor.YELLOW));
		    } else {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("LeaseNotExpire"));
		    }
		    return true;
		}
	    }
	} else if (args.length == 4) {
	    if (args[1].equals("set")) {
		if (!resadmin) {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
		    return true;
		}
		if (args[3].equals("infinite")) {
		    if (Residence.leasemanager.leaseExpires(args[2])) {
			Residence.leasemanager.removeExpireTime(args[2]);
			player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("LeaseInfinite"));
		    } else {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("LeaseNotExpire"));
		    }
		    return true;
		} else {
		    int days;
		    try {
			days = Integer.parseInt(args[3]);
		    } catch (Exception ex) {
			player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidDays"));
			return true;
		    }
		    Residence.leasemanager.setExpireTime(player, args[2], days);
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
	    if (Residence.rentmanager.isRented(args[2])) {
		Residence.rentmanager.removeFromForRent(player, args[2], resadmin);
	    } else {
		Residence.rentmanager.unrent(player, args[2], resadmin);
	    }
	    return true;
	}
	if (command.equals("sign")) {
	    if (args.length != 3) {
		return false;
	    }
	    Block block = player.getTargetBlock((Set<Material>) null, 10);

	    if (!(block.getState() instanceof Sign)) {
		player.sendMessage(Residence.getLanguage().getPhrase("LookAtSign"));
		return true;
	    }

	    Sign sign = (Sign) block.getState();

	    Signs signInfo = new Signs();

	    Signs oldSign = Residence.getSignUtil().getSignFromLoc(sign.getLocation());

	    if (oldSign != null)
		signInfo = oldSign;

	    Location loc = sign.getLocation();

	    String landName = null;

	    ClaimedResidence CurrentRes = Residence.getResidenceManager().getByLoc(sign.getLocation());

	    if (CurrentRes == null) {
		player.sendMessage(Residence.getLanguage().getPhrase("InvalidResidence"));
		return true;
	    }

	    if (!CurrentRes.isOwner(player) && !resadmin) {
		player.sendMessage(Residence.getLanguage().getPhrase("NotOwner"));
		return true;
	    }

	    final ClaimedResidence res = Residence.getResidenceManager().getByName(args[2]);

	    if (res == null) {
		player.sendMessage(Residence.getLanguage().getPhrase("InvalidResidence"));
		return true;
	    }

	    landName = res.getName();

	    boolean ForSale = Residence.getTransactionManager().isForSale(landName);
	    boolean ForRent = Residence.getRentManager().isForRent(landName);

	    int category = 1;
	    if (Residence.getSignUtil().getSigns().GetAllSigns().size() > 0)
		category = Residence.getSignUtil().getSigns().GetAllSigns().get(Residence.getSignUtil().getSigns().GetAllSigns().size() - 1).GetCategory() + 1;

	    if (ForSale || ForRent) {
		signInfo.setCategory(category);
		signInfo.setResidence(landName);
		signInfo.setWorld(loc.getWorld().getName());
		signInfo.setX(loc.getBlockX());
		signInfo.setY(loc.getBlockY());
		signInfo.setZ(loc.getBlockZ());
		signInfo.setLocation(loc);
		Residence.getSignUtil().getSigns().addSign(signInfo);
		Residence.getSignUtil().saveSigns();
	    } else {
		player.sendMessage(Residence.getLanguage().getPhrase("ResidenceNotForRentOrSell"));
		return true;
	    }
	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		public void run() {
		    Residence.getSignUtil().CheckSign(res);
		}
	    }, 5L);

	    return true;
	}
	if (command.equals("info")) {
	    if (args.length == 2) {
		String areaname = Residence.getResidenceManager().getNameByLoc(player.getLocation());
		Residence.tmanager.viewSaleInfo(areaname, player);
		if (Residence.cmanager.enabledRentSystem() && Residence.rentmanager.isForRent(areaname)) {
		    Residence.rentmanager.printRentInfo(player, areaname);
		}
	    } else if (args.length == 3) {
		Residence.tmanager.viewSaleInfo(args[2], player);
		if (Residence.cmanager.enabledRentSystem() && Residence.rentmanager.isForRent(args[2])) {
		    Residence.rentmanager.printRentInfo(player, args[2]);
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
	    Residence.tmanager.buyPlot(args[2], player, resadmin);
	    return true;
	}
	if (command.equals("unsell")) {
	    if (args.length != 3) {
		return false;
	    }
	    Residence.tmanager.removeFromSale(player, args[2], resadmin);
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
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidAmount"));
		return true;
	    }
	    Residence.tmanager.putForSale(args[2], player, amount, resadmin);
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
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidBoolean"));
		return true;
	    }
	}
	Residence.rentmanager.rent(player, args[2], repeat, resadmin);
	return true;
    }

    private boolean commandResMarketRentable(String[] args, boolean resadmin, Player player, int page) {
	if (args.length < 5 || args.length > 6) {
	    return false;
	}
	if (!Residence.cmanager.enabledRentSystem()) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentDisabled"));
	    return true;
	}
	int days;
	int cost;
	try {
	    cost = Integer.parseInt(args[3]);
	} catch (Exception ex) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidCost"));
	    return true;
	}
	try {
	    days = Integer.parseInt(args[4]);
	} catch (Exception ex) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidDays"));
	    return true;
	}
	boolean repeat = false;
	if (args.length == 6) {
	    if (args[5].equalsIgnoreCase("t") || args[5].equalsIgnoreCase("true")) {
		repeat = true;
	    } else if (!args[5].equalsIgnoreCase("f") && !args[5].equalsIgnoreCase("false")) {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidBoolean"));
		return true;
	    }
	}
	Residence.rentmanager.setForRent(player, args[2], cost, days, repeat, resadmin);
	return true;
    }

    private boolean commandResMarketAutorenew(String[] args, boolean resadmin, Player player, int page) {
	if (!Residence.cmanager.enableEconomy()) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("MarketDisabled"));
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
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidBoolean"));
	    return true;
	}
	if (Residence.rentmanager.isRented(args[2]) && Residence.rentmanager.getRentingPlayer(args[2]).equalsIgnoreCase(player.getName())) {
	    Residence.rentmanager.setRentedRepeatable(player, args[2], value, resadmin);
	} else if (Residence.rentmanager.isForRent(args[2])) {
	    Residence.rentmanager.setRentRepeatable(player, args[2], value, resadmin);
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("RentReleaseInvalid", ChatColor.YELLOW + args[2] + ChatColor.RED));
	}
	return true;
    }

    private boolean commandResMarketList(String[] args, boolean resadmin, Player player, int page) {
	if (!Residence.cmanager.enableEconomy()) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("MarketDisabled"));
	    return true;
	}
	player.sendMessage(ChatColor.BLUE + "---" + Residence.getLanguage().getPhrase("MarketList") + "---");
	Residence.tmanager.printForSaleResidences(player);
	if (Residence.cmanager.enabledRentSystem()) {
	    Residence.rentmanager.printRentableResidences(player);
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
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    start = 2;
	} else if (args[1].equals("leave")) {
	    res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    start = 2;
	} else if (args[1].equals("remove")) {
	    if (args.length > 2 && args[2].equals("enter")) {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
		if (res != null) {
		    res.setEnterLeaveMessage(player, null, true, resadmin);
		} else {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
		}
		return true;
	    } else if (args.length > 2 && args[2].equals("leave")) {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
		if (res != null) {
		    res.setEnterLeaveMessage(player, null, false, resadmin);
		} else {
		    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
		}
		return true;
	    }
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidMessageType"));
	    return true;
	} else if (args.length > 2 && args[2].equals("enter")) {
	    enter = true;
	    res = Residence.getResidenceManager().getByName(args[1]);
	    start = 3;
	} else if (args.length > 2 && args[2].equals("leave")) {
	    res = Residence.getResidenceManager().getByName(args[1]);
	    start = 3;
	} else if (args.length > 2 && args[2].equals("remove")) {
	    res = Residence.getResidenceManager().getByName(args[1]);
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
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidMessageType"));
	    return true;
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidMessageType"));
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
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	}
	return true;
    }

    private boolean commandResSublist(String[] args, boolean resadmin, Player player, int page) {
	if (args.length == 1 || args.length == 2 || args.length == 3) {
	    ClaimedResidence res;
	    if (args.length == 1) {
		res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    } else {
		res = Residence.getResidenceManager().getByName(args[1]);
	    }
	    if (res != null) {
		res.printSubzoneList(player, page);
	    } else {
		player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	    }
	    return true;
	}
	return false;
    }

    private boolean commandResCompass(String[] args, boolean resadmin, Player player, int page) {
	if (args.length != 2) {
	    player.setCompassTarget(player.getWorld().getSpawnLocation());
	    player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("CompassTargetReset"));
	    return true;
	}

	if (!player.hasPermission("residence.compass")) {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("NoPermission"));
	    return true;
	}

	if (Residence.getResidenceManager().getByName(args[1]) != null) {
	    if (Residence.getResidenceManager().getByName(args[1]).getWorld().equalsIgnoreCase(player.getWorld().getName())) {
		Location low = Residence.getResidenceManager().getByName(args[1]).getArea("main").getLowLoc();
		Location high = Residence.getResidenceManager().getByName(args[1]).getArea("main").getHighLoc();
		Location mid = new Location(low.getWorld(), (low.getBlockX() + high.getBlockX()) / 2, (low.getBlockY() + high.getBlockY()) / 2, (low.getBlockZ() + high
		    .getBlockZ()) / 2);
		player.setCompassTarget(mid);
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("CompassTargetSet", ChatColor.YELLOW + args[1] + ChatColor.GREEN));
	    }
	} else {
	    player.sendMessage(ChatColor.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
	}
	return true;
    }

    private boolean commandResGui(String[] args, boolean resadmin, Player player, int page) {
	if (Residence.slistener != null) {
	    if (args.length == 1) {
		ResidenceSpout.showResidenceFlagGUI(SpoutManager.getPlayer(player), this, Residence.getResidenceManager().getNameByLoc(player.getLocation()), resadmin);
	    } else if (args.length == 2) {
		ResidenceSpout.showResidenceFlagGUI(SpoutManager.getPlayer(player), this, args[1], resadmin);
	    }
	}
	return true;
    }

    private boolean commandResList(String[] args, boolean resadmin, Player player, int page) {
	if (args.length == 2) {
	    if (args[1].equals("list")) {
		Residence.pmanager.printLists(player);
		return true;
	    }
	} else if (args.length == 3) {
	    if (args[1].equals("view")) {
		Residence.pmanager.printList(player, args[2]);
		return true;
	    } else if (args[1].equals("remove")) {
		Residence.pmanager.removeList(player, args[2]);
		return true;
	    } else if (args[1].equals("add")) {
		Residence.pmanager.makeList(player, args[2]);
		return true;
	    }
	} else if (args.length == 4) {
	    if (args[1].equals("apply")) {
		Residence.pmanager.applyListToResidence(player, args[2], args[3], resadmin);
		return true;
	    }
	} else if (args.length == 5) {
	    if (args[1].equals("set")) {
		Residence.pmanager.getList(player.getName(), args[2]).setFlag(args[3], FlagPermissions.stringToFlagState(args[4]));
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("FlagSet"));
		return true;
	    }
	} else if (args.length == 6) {
	    if (args[1].equals("gset")) {
		Residence.pmanager.getList(player.getName(), args[2]).setGroupFlag(args[3], args[4], FlagPermissions.stringToFlagState(args[5]));
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("FlagSet"));
		return true;
	    } else if (args[1].equals("pset")) {
		Residence.pmanager.getList(player.getName(), args[2]).setPlayerFlag(args[3], args[4], FlagPermissions.stringToFlagState(args[5]));
		player.sendMessage(ChatColor.GREEN + Residence.getLanguage().getPhrase("FlagSet"));
		return true;
	    }
	}
	return false;
    }
}
