package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.cmd;
import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class rc implements cmd {

    @Override
    public boolean perform(String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return true;
	Player player = (Player) sender;
	String pname = player.getName();
	if (!Residence.getConfigManager().chatEnabled()) {
	    player.sendMessage(Residence.getLM().getMessage("Residence.ChatDisabled"));
	    return false;
	}
	if (args.length > 0)
	    args = Arrays.copyOfRange(args, 1, args.length);

	if (args.length == 0) {
	    ClaimedResidence res = Residence.getResidenceManager().getByLoc(player.getLocation());
	    if (res == null) {
		ChatChannel chat = Residence.getChatManager().getPlayerChannel(pname);
		if (chat != null) {
		    Residence.getChatManager().removeFromChannel(pname);
		    Residence.getPlayerListener().removePlayerResidenceChat(player);
		    return true;
		}
		player.sendMessage(Residence.getLM().getMessage("Residence.NotIn"));
		return true;
	    }
	    ChatChannel chat = Residence.getChatManager().getPlayerChannel(pname);
	    if (chat != null && chat.getChannelName().equals(res.getName())) {
		Residence.getChatManager().removeFromChannel(pname);
		Residence.getPlayerListener().removePlayerResidenceChat(player);
		return true;
	    }
	    if (!res.getPermissions().playerHas(player.getName(), "chat", true) && !Residence.getPermissionManager().isResidenceAdmin(player)) {
		player.sendMessage(Residence.getLM().getMessage("Residence.FlagDeny", "chat", res.getName()));
		return false;
	    }

	    Residence.getPlayerListener().tooglePlayerResidenceChat(player, res.getName());
	    Residence.getChatManager().setChannel(pname, res);
	    return true;
	} else if (args.length == 1) {
	    if (args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("leave")) {
		Residence.getChatManager().removeFromChannel(pname);
		Residence.getPlayerListener().removePlayerResidenceChat(player);
		return true;
	    }
	    ClaimedResidence res = Residence.getResidenceManager().getByName(args[0]);
	    if (res == null) {
		player.sendMessage(Residence.getLM().getMessage("Chat.InvalidChannel"));
		return true;
	    }

	    if (!res.getPermissions().playerHas(player.getName(), "chat", true) && !Residence.getPermissionManager().isResidenceAdmin(player)) {
		player.sendMessage(Residence.getLM().getMessage("Residence.FlagDeny", "chat", res.getName()));
		return false;
	    }
	    Residence.getPlayerListener().tooglePlayerResidenceChat(player, res.getName());
	    Residence.getChatManager().setChannel(pname, res);

	    return true;
	} else if (args.length == 2) {
	    if (args[0].equalsIgnoreCase("setcolor")) {

		ChatChannel chat = Residence.getChatManager().getPlayerChannel(pname);

		if (chat == null) {
		    player.sendMessage(Residence.getLM().getMessage("Chat.JoinFirst"));
		    return true;
		}

		ClaimedResidence res = Residence.getResidenceManager().getByName(chat.getChannelName());

		if (res == null)
		    return false;

		if (!res.isOwner(player) && !Residence.getPermissionManager().isResidenceAdmin(player)) {
		    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		    return true;
		}

		if (!player.hasPermission("residence.chatcolor")) {
		    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		    return true;
		}

		String posibleColor = args[1];

		if (!posibleColor.contains("&"))
		    posibleColor = "&" + posibleColor;

		if (posibleColor.length() != 2 || ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', posibleColor)).length() != 0) {
		    player.sendMessage(Residence.getLM().getMessage("Chat.InvalidColor"));
		    return true;
		}

		ChatColor color = ChatColor.getByChar(posibleColor.replace("&", ""));
		res.setChannelColor(color);
		chat.setChannelColor(color);
		player.sendMessage(Residence.getLM().getMessage("Chat.ChangedColor", color.name()));
		return true;
	    } else if (args[0].equalsIgnoreCase("setprefix")) {
		ChatChannel chat = Residence.getChatManager().getPlayerChannel(pname);

		if (chat == null) {
		    player.sendMessage(Residence.getLM().getMessage("Chat.JoinFirst"));
		    return true;
		}

		ClaimedResidence res = Residence.getResidenceManager().getByName(chat.getChannelName());

		if (res == null)
		    return false;

		if (!res.isOwner(player) && !Residence.getPermissionManager().isResidenceAdmin(player)) {
		    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		    return true;
		}

		if (!player.hasPermission("residence.chatprefix")) {
		    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		    return true;
		}

		String prefix = args[1];

		if (prefix.length() > Residence.getConfigManager().getChatPrefixLength()) {
		    player.sendMessage(Residence.getLM().getMessage("Chat.InvalidPrefixLength", Residence.getConfigManager()
			.getChatPrefixLength()));
		    return true;
		}

		res.setChatPrefix(prefix);
		chat.setChatPrefix(prefix);
		player.sendMessage(Residence.getLM().getMessage("Chat.ChangedPrefix", ChatColor.translateAlternateColorCodes('&', prefix)));
		return true;
	    } else if (args[0].equalsIgnoreCase("kick")) {
		ChatChannel chat = Residence.getChatManager().getPlayerChannel(pname);

		if (chat == null) {
		    player.sendMessage(Residence.getLM().getMessage("Chat.JoinFirst"));
		    return true;
		}

		ClaimedResidence res = Residence.getResidenceManager().getByName(chat.getChannelName());

		if (res == null)
		    return false;

		if (!res.getOwner().equals(player.getName()) && !Residence.getPermissionManager().isResidenceAdmin(player)) {
		    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		    return true;
		}

		if (!player.hasPermission("residence.chatkick")) {
		    player.sendMessage(Residence.getLM().getMessage("General.NoPermission"));
		    return true;
		}

		String targetName = args[1];
		if (!chat.hasMember(targetName)) {
		    player.sendMessage(Residence.getLM().getMessage("Chat.NotInChannel"));
		    return false;
		}

		chat.leave(targetName);
		Residence.getPlayerListener().removePlayerResidenceChat(targetName);
		player.sendMessage(Residence.getLM().getMessage("Chat.Kicked", targetName, chat.getChannelName()));
		return true;
	    }
	}
	return true;
    }
}
