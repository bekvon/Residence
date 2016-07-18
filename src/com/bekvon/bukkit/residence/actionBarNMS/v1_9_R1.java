package com.bekvon.bukkit.residence.actionBarNMS;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_9_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.containers.ABInterface;

import net.minecraft.server.v1_9_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_9_R1.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_9_R1.IChatBaseComponent;
import net.minecraft.server.v1_9_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_9_R1.PacketPlayOutChat;

public class v1_9_R1 implements ABInterface {

    @Override
    public void send(CommandSender sender, String msg) {
	if (sender instanceof Player)
	    send((Player) sender, msg);
	else
	    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    @Override
    public void send(Player player, String msg) {
	try {
	    CraftPlayer p = (CraftPlayer) player;
	    IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + ChatColor.translateAlternateColorCodes('&', msg) + "\"}");
	    PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
	    p.getHandle().playerConnection.sendPacket(ppoc);
	} catch (Exception e) {
	    player.sendMessage(msg);
	}
    }
    
    @Override
    public void sendTitle(Player player, Object title, Object subtitle) {
	CraftPlayer Cplayer = (CraftPlayer) player;
	if (title != null) {
	    PacketPlayOutTitle packetTitle = new PacketPlayOutTitle(EnumTitleAction.TITLE, CraftChatMessage.fromString(ChatColor.translateAlternateColorCodes('&', String
		.valueOf(title)))[0]);
	    Cplayer.getHandle().playerConnection.sendPacket(packetTitle);
	}
	if (subtitle != null) {
	    PacketPlayOutTitle packetSubtitle = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, CraftChatMessage.fromString(ChatColor.translateAlternateColorCodes('&',
		String.valueOf(subtitle)))[0]);
	    Cplayer.getHandle().playerConnection.sendPacket(packetSubtitle);
	}
    }
}
