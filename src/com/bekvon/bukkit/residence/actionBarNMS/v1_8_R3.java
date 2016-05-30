package com.bekvon.bukkit.residence.actionBarNMS;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import com.bekvon.bukkit.residence.AB;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

public class v1_8_R3 implements AB {

    @Override
    public void send(CommandSender sender, String msg) {
	if (sender instanceof Player)
	    send((Player) sender, msg);
	else
	    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    @Override
    public void send(Player player, String msg) {
	CraftPlayer p = (CraftPlayer) player;
	IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + ChatColor.translateAlternateColorCodes('&', msg) + "\"}");
	PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
	p.getHandle().playerConnection.sendPacket(ppoc);
    }
}
