package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class rc implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 1100)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return true;
	Player player = (Player) sender;
	String pname = player.getName();
	if (!plugin.getConfigManager().chatEnabled()) {
	    plugin.msg(player, lm.Residence_ChatDisabled);
	    return false;
	}
	if (args.length > 0)
	    args = Arrays.copyOfRange(args, 1, args.length);

	if (args.length == 0) {
	    ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
	    if (res == null) {
		ChatChannel chat = plugin.getChatManager().getPlayerChannel(pname);
		if (chat != null) {
		    plugin.getChatManager().removeFromChannel(pname);
		    plugin.getPlayerListener().removePlayerResidenceChat(player);
		    return true;
		}
		plugin.msg(player, lm.Residence_NotIn);
		return true;
	    }
	    ChatChannel chat = plugin.getChatManager().getPlayerChannel(pname);
	    if (chat != null && chat.getChannelName().equals(res.getName())) {
		plugin.getChatManager().removeFromChannel(pname);
		plugin.getPlayerListener().removePlayerResidenceChat(player);
		return true;
	    }
	    if (!res.getPermissions().playerHas(player.getName(), Flags.chat, true) && !plugin.getPermissionManager().isResidenceAdmin(player)) {
		plugin.msg(player, lm.Residence_FlagDeny, Flags.chat, res.getName());
		return false;
	    }

	    plugin.getPlayerListener().tooglePlayerResidenceChat(player, res.getName());
	    plugin.getChatManager().setChannel(pname, res);
	    return true;
	} else if (args.length == 1) {
	    if (args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("leave")) {
		plugin.getChatManager().removeFromChannel(pname);
		plugin.getPlayerListener().removePlayerResidenceChat(player);
		return true;
	    }
	    ClaimedResidence res = plugin.getResidenceManager().getByName(args[0]);
	    if (res == null) {
		plugin.msg(player, lm.Chat_InvalidChannel);
		return true;
	    }

	    if (!res.getPermissions().playerHas(player.getName(), Flags.chat, true) && !plugin.getPermissionManager().isResidenceAdmin(player)) {
		plugin.msg(player, lm.Residence_FlagDeny, Flags.chat, res.getName());
		return false;
	    }
	    plugin.getPlayerListener().tooglePlayerResidenceChat(player, res.getName());
	    plugin.getChatManager().setChannel(pname, res);

	    return true;
	} else if (args.length == 2) {
	    if (args[0].equalsIgnoreCase("setcolor")) {

		ChatChannel chat = plugin.getChatManager().getPlayerChannel(pname);

		if (chat == null) {
		    plugin.msg(player, lm.Chat_JoinFirst);
		    return true;
		}

		ClaimedResidence res = plugin.getResidenceManager().getByName(chat.getChannelName());

		if (res == null)
		    return false;

		if (!res.isOwner(player) && !plugin.getPermissionManager().isResidenceAdmin(player)) {
		    plugin.msg(player, lm.General_NoPermission);
		    return true;
		}

		if (!plugin.hasPermission(player, "residence.chatcolor"))
		    return true;

		String posibleColor = args[1];

		if (!posibleColor.contains("&"))
		    posibleColor = "&" + posibleColor;

		if (posibleColor.length() != 2 || ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', posibleColor)).length() != 0) {
		    plugin.msg(player, lm.Chat_InvalidColor);
		    return true;
		}

		ChatColor color = ChatColor.getByChar(posibleColor.replace("&", ""));
		res.setChannelColor(color);
		chat.setChannelColor(color);
		plugin.msg(player, lm.Chat_ChangedColor, color.name());
		return true;
	    } else if (args[0].equalsIgnoreCase("setprefix")) {
		ChatChannel chat = plugin.getChatManager().getPlayerChannel(pname);

		if (chat == null) {
		    plugin.msg(player, lm.Chat_JoinFirst);
		    return true;
		}

		ClaimedResidence res = plugin.getResidenceManager().getByName(chat.getChannelName());

		if (res == null)
		    return false;

		if (!res.isOwner(player) && !plugin.getPermissionManager().isResidenceAdmin(player)) {
		    plugin.msg(player, lm.General_NoPermission);
		    return true;
		}

		if (!plugin.hasPermission(player, "residence.chatprefix"))
		    return true;

		String prefix = args[1];

		if (prefix.length() > plugin.getConfigManager().getChatPrefixLength()) {
		    plugin.msg(player, lm.Chat_InvalidPrefixLength, plugin.getConfigManager()
			.getChatPrefixLength());
		    return true;
		}

		res.setChatPrefix(prefix);
		chat.setChatPrefix(prefix);
		plugin.msg(player, lm.Chat_ChangedPrefix, ChatColor.translateAlternateColorCodes('&', prefix));
		return true;
	    } else if (args[0].equalsIgnoreCase("kick")) {
		ChatChannel chat = plugin.getChatManager().getPlayerChannel(pname);

		if (chat == null) {
		    plugin.msg(player, lm.Chat_JoinFirst);
		    return true;
		}

		ClaimedResidence res = plugin.getResidenceManager().getByName(chat.getChannelName());

		if (res == null)
		    return false;

		if (!res.getOwner().equals(player.getName()) && !plugin.getPermissionManager().isResidenceAdmin(player)) {
		    plugin.msg(player, lm.General_NoPermission);
		    return true;
		}

		if (!plugin.hasPermission(player, "residence.chatkick"))
		    return true;

		String targetName = args[1];
		if (!chat.hasMember(targetName)) {
		    plugin.msg(player, lm.Chat_NotInChannel);
		    return false;
		}

		chat.leave(targetName);
		plugin.getPlayerListener().removePlayerResidenceChat(targetName);
		plugin.msg(player, lm.Chat_Kicked, targetName, chat.getChannelName());
		return true;
	    }
	}
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Joins current or defined residence chat channel");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res rc (residence)", "Join residence chat channel."));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]"));

	path += "SubCommands.";
	c.get(path + "leave.Description", "Leaves current residence chat channel");
	c.get(path + "leave.Info", Arrays.asList("&eUsage: &6/res rc leave", "If you are in residence chat channel then you will leave it"));

	c.get(path + "setcolor.Description", "Sets residence chat channel text color");
	c.get(path + "setcolor.Info", Arrays.asList("&eUsage: &6/res rc setcolor [colorCode]", "Sets residence chat channel text color"));

	c.get(path + "setprefix.Description", "Sets residence chat channel prefix");
	c.get(path + "setprefix.Info", Arrays.asList("&eUsage: &6/res rc setprefix [newName]", "Sets residence chat channel prefix"));

	c.get(path + "kick.Description", "Kicks player from channel");
	c.get(path + "kick.Info", Arrays.asList("&eUsage: &6/res rc kick [player]", "Kicks player from channel"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName(), "kick"), Arrays.asList("[playername]"));
    }
}
