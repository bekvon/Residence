package com.bekvon.bukkit.residence;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.event.ResidenceCommandEvent;

public class ResidenceCommandListener implements CommandExecutor {

    private static List<String> AdminCommands = new ArrayList<String>();

    public static List<String> getAdminCommands() {
	if (AdminCommands.size() == 0)
	    AdminCommands = Residence.getInstance().getCommandFiller().getCommands(false);
	return AdminCommands;
    }

    private Residence plugin;

    public ResidenceCommandListener(Residence plugin) {
	this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

//	if (args.length < 3)
//	    return true;

	ResidenceCommandEvent cevent = new ResidenceCommandEvent(command.getName(), args, sender);
	Bukkit.getPluginManager().callEvent(cevent);
	if (cevent.isCancelled()) {
	    return true;
	}

	if (sender instanceof Player && !plugin.getPermissionManager().isResidenceAdmin(sender) && plugin.isDisabledWorldCommand(((Player) sender)
	    .getWorld())) {
	    plugin.msg(sender, lm.General_DisabledWorld);
	    return true;
	}

	if (command.getName().equals("resreload") && args.length == 0) {
	    if (sender instanceof Player) {
		Player player = (Player) sender;
		if (plugin.getPermissionManager().isResidenceAdmin(player) && player.hasPermission("residence.topadmin")) {
		    plugin.reloadPlugin();
		    sender.sendMessage(ChatColor.GREEN + "[Residence] Reloaded config.");
		    System.out.println("[Residence] Reloaded by " + player.getName() + ".");
		} else
		    plugin.msg(player, lm.General_NoPermission);
	    } else {
		plugin.reloadPlugin();
		System.out.println("[Residence] Reloaded by console.");
	    }
	    return true;
	}
	if (command.getName().equals("resload")) {
	    if (!(sender instanceof Player) || sender instanceof Player && plugin.getPermissionManager().isResidenceAdmin(sender) && ((Player) sender).hasPermission(
		"residence.topadmin")) {
		try {
		    plugin.loadYml();
		    sender.sendMessage(ChatColor.GREEN + "[Residence] Reloaded save file...");
		} catch (Exception ex) {
		    sender.sendMessage(ChatColor.RED + "[Residence] Unable to reload the save file, exception occured!");
		    sender.sendMessage(ChatColor.RED + ex.getMessage());
		    Logger.getLogger(Residence.getInstance().getClass().getName()).log(Level.SEVERE, null, ex);
		}
	    } else
		plugin.msg(sender, lm.General_NoPermission);
	    return true;
	} else if (command.getName().equals("rc")) {
	    cmd cmdClass = getCmdClass(new String[] { "rc" });
	    if (cmdClass == null) {
		sendUsage(sender, command.getName());
		return true;
	    }
	    boolean respond = cmdClass.perform(Residence.getInstance(), args, false, command, sender);
	    if (!respond)
		sendUsage(sender, command.getName());
	    return true;
	} else if (command.getName().equals("res") || command.getName().equals("residence") || command.getName().equals("resadmin")) {
	    boolean resadmin = false;
	    if (sender instanceof Player) {
		if (command.getName().equals("resadmin") && plugin.getPermissionManager().isResidenceAdmin(sender)) {
		    resadmin = true;
		}
		if (command.getName().equals("resadmin") && !plugin.getPermissionManager().isResidenceAdmin(sender)) {
		    ((Player) sender).sendMessage(plugin.msg(lm.Residence_NonAdmin));
		    return true;
		}
		if (command.getName().equals("res") && plugin.getPermissionManager().isResidenceAdmin(sender) && plugin.getConfigManager().getAdminFullAccess()) {
		    resadmin = true;
		}
	    } else {
		resadmin = true;
	    }

	    if (args.length > 0 && args[args.length - 1].equalsIgnoreCase("?") || args.length > 1 && args[args.length - 2].equals("?")) {
		return commandHelp(args, resadmin, sender, command);
	    }

	    Player player = null;
	    if (sender instanceof Player) {
		player = (Player) sender;
	    } else {
		resadmin = true;
	    }
	    if (plugin.getConfigManager().allowAdminsOnly()) {
		if (!resadmin && player != null) {
		    plugin.msg(player, lm.General_AdminOnly);
		    return true;
		}
	    }
	    if (args.length == 0) {
		args = new String[1];
		args[0] = "?";
	    }

	    String cmd = args[0].toLowerCase();

	    switch (cmd) {
	    case "delete":
		cmd = "remove";
		break;
	    case "sz":
		cmd = "subzone";
		break;
	    }

	    cmd cmdClass = getCmdClass(args);
	    if (cmdClass == null) {
		return commandHelp(new String[] { "?" }, resadmin, sender, command);
	    }

	    if (!resadmin && !plugin.hasPermission(sender, "residence.command." + args[0].toLowerCase()))
		return true;

	    if (!resadmin && player != null && plugin.resadminToggle.contains(player.getName())) {
		if (!plugin.getPermissionManager().isResidenceAdmin(player)) {
		    plugin.resadminToggle.remove(player.getName());
		}
	    }
	    boolean respond = cmdClass.perform(Residence.getInstance(), args, resadmin, command, sender);
	    if (!respond) {
		String[] tempArray = new String[args.length + 1];
		for (int i = 0; i < args.length; i++) {
		    tempArray[i] = args[i];
		}
		tempArray[args.length] = "?";
		args = tempArray;
		return commandHelp(args, resadmin, sender, command);
	    }

	    return true;
	}
	return this.onCommand(sender, command, label, args);
    }

    private static cmd getCmdClass(String[] args) {
	cmd cmdClass = null;
	try {
	    Class<?> nmsClass;
	    nmsClass = Class.forName("com.bekvon.bukkit.residence.commands." + args[0].toLowerCase());
	    if (cmd.class.isAssignableFrom(nmsClass)) {
		cmdClass = (cmd) nmsClass.getConstructor().newInstance();
	    }
	} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
	    | SecurityException e) {
	}
	return cmdClass;
    }

    public void sendUsage(CommandSender sender, String command) {
	plugin.msg(sender, lm.General_DefaultUsage, command);
    }

    private boolean commandHelp(String[] args, boolean resadmin, CommandSender sender, Command command) {
	if (plugin.getHelpPages() == null)
	    return false;

	String helppath = getHelpPath(args);

	int page = 1;
	if (!args[args.length - 1].equalsIgnoreCase("?")) {
	    try {
		page = Integer.parseInt(args[args.length - 1]);
	    } catch (Exception ex) {
		plugin.msg(sender, lm.General_InvalidHelp);
	    }
	}

	if (command.getName().equalsIgnoreCase("res"))
	    resadmin = false;
	if (plugin.getHelpPages().containesEntry(helppath))
	    plugin.getHelpPages().printHelp(sender, page, helppath, resadmin);
	return true;
    }

    private String getHelpPath(String[] args) {
	String helppath = "res";
	for (int i = 0; i < args.length; i++) {
	    if (args[i].equalsIgnoreCase("?")) {
		break;
	    }
	    helppath = helppath + "." + args[i];
	}
	if (!plugin.getHelpPages().containesEntry(helppath) && args.length > 0)
	    return getHelpPath(Arrays.copyOf(args, args.length - 1));
	return helppath;
    }

}
