package com.bekvon.bukkit.residence;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.bekvon.bukkit.residence.event.ResidenceCommandEvent;

public class ResidenceCommandListener extends Residence {

    public static List<String> AdminCommands = Arrays.asList("removeworld", "setowner", "removeall", "signupdate", "listhidden", "listallhidden", "server", "clearflags",
	"resreload", "resload", "signconvert");

    public static List<String> getAdminCommands() {
	return AdminCommands;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
	ResidenceCommandEvent cevent = new ResidenceCommandEvent(command.getName(), args, sender);
	Residence.getServ().getPluginManager().callEvent(cevent);
	if (cevent.isCancelled()) {
	    return true;
	}
	
	if (sender instanceof Player && !Residence.getPermissionManager().isResidenceAdmin((Player) sender) && Residence.isDisabledWorldCommand(((Player) sender).getWorld())){
	    sender.sendMessage(Residence.getLM().getMessage("General.DisabledWorld"));
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
		    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    } else {
		this.reloadPlugin();
		System.out.println("[Residence] Reloaded by console.");
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
		sender.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
	    return true;
	} else if (command.getName().equals("rc")) {
	    cmd cmdClass = getCmdClass(sender, "rc", new String[] { "rc" });
	    if (cmdClass == null) {
		sendUsage(sender, command.getName());
		return true;
	    }
	    boolean respond = cmdClass.perform(args, false, command, sender);
	    if (!respond)
		sendUsage(sender, command.getName());
	    return true;
	} else if (command.getName().equals("res") || command.getName().equals("residence") || command.getName().equals("resadmin")) {
	    boolean resadmin = false;
	    if (sender instanceof Player) {
		if (command.getName().equals("resadmin") && Residence.gmanager.isResidenceAdmin((Player) sender)) {
		    resadmin = true;
		}
		if (command.getName().equals("resadmin") && !Residence.gmanager.isResidenceAdmin((Player) sender)) {
		    ((Player) sender).sendMessage(Residence.getLM().getMessage("Residence.NonAdmin"));
		    return true;
		}
		if (command.getName().equals("res") && Residence.gmanager.isResidenceAdmin((Player) sender) && Residence.getConfigManager().getAdminFullAccess()) {
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
	    if (Residence.cmanager.allowAdminsOnly()) {
		if (!resadmin) {
		    player.sendMessage(Residence.getLM().getMessage("General.AdminOnly"));
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

	    switch (cmd) {
	    case "delete":
		cmd = "remove";
		break;
	    case "sz":
		cmd = "subzone";
		break;
	    }

	    cmd cmdClass = getCmdClass(sender, command.getName(), args);
	    if (cmdClass == null) {
		sendUsage(sender, command.getName());
		return true;
	    }

	    if (!resadmin && Residence.resadminToggle.contains(player.getName())) {
		if (!Residence.gmanager.isResidenceAdmin(player)) {
		    Residence.resadminToggle.remove(player.getName());
		}
	    }
	    boolean respond = cmdClass.perform(args, resadmin, command, sender);
	    if (!respond)
		sendUsage(sender, command.getName());

	    return true;
	}
	return this.onCommand(sender, command, label, args);
    }

    private cmd getCmdClass(CommandSender sender, String command, String[] args) {
	cmd cmdClass = null;
	try {
	    Class<?> nmsClass;
	    nmsClass = Class.forName("com.bekvon.bukkit.residence.commands." + args[0].toLowerCase());
	    if (cmd.class.isAssignableFrom(nmsClass)) {
		cmdClass = (cmd) nmsClass.getConstructor().newInstance();
	    }
	} catch (ClassNotFoundException e) {
	} catch (InstantiationException e) {
	} catch (IllegalAccessException e) {
	} catch (IllegalArgumentException e) {
	} catch (InvocationTargetException e) {
	} catch (NoSuchMethodException e) {
	} catch (SecurityException e) {
	}
	return cmdClass;
    }

    private void sendUsage(CommandSender sender, String command) {
	sender.sendMessage(Residence.getLM().getMessage("General.DefaultUsage", command));
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
		sender.sendMessage(Residence.getLM().getMessage("General.InvalidHelp"));
	    }
	}

	if (command.getName().equalsIgnoreCase("res"))
	    resadmin = false;

	if (Residence.helppages.containesEntry(helppath))
	    Residence.helppages.printHelp(sender, page, helppath, resadmin);

	return true;
    }

}
