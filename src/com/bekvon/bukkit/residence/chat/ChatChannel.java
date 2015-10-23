/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.chat;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceChatEvent;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class ChatChannel {

    protected String channelName;
    protected List<String> members;
    protected String ChatPrefix = "";
    protected ChatColor ChannelColor = ChatColor.WHITE;

    public ChatChannel(String channelName, String ChatPrefix, ChatColor chatColor) {
	this.channelName = channelName;
	this.ChatPrefix = ChatPrefix;
	this.ChannelColor = chatColor;
	members = new ArrayList<String>();
    }

    public String getChannelName() {
	return channelName;
    }

    public void setChatPrefix(String ChatPrefix) {
	this.ChatPrefix = ChatPrefix;
    }

    public void setChannelColor(ChatColor ChannelColor) {
	this.ChannelColor = ChannelColor;
    }

    public void chat(String sourcePlayer, String message) {
	Server serv = Residence.getServ();
	ResidenceChatEvent cevent = new ResidenceChatEvent(Residence.getResidenceManager().getByName(channelName), serv.getPlayer(sourcePlayer), this.ChatPrefix, message,
	    this.ChannelColor);
	Residence.getServ().getPluginManager().callEvent(cevent);
	if (cevent.isCancelled())
	    return;
	for (String member : members) {
	    Player player = serv.getPlayer(member);
	    if (player != null)
		player.sendMessage(cevent.getChatprefix() + " " + Residence.getConfigManager().getChatColor() + sourcePlayer + ": " + cevent.getColor() + cevent.getChatMessage());
	}
	Bukkit.getConsoleSender().sendMessage("ResidentialChat[" + channelName + "] - " + sourcePlayer + ": " + ChatColor.stripColor(cevent.getChatMessage()));
    }

    public void join(String player) {
	if (!members.contains(player))
	    members.add(player);
    }

    public void leave(String player) {
	members.remove(player);
    }

    public boolean hasMember(String player) {
	return members.contains(player);
    }

    public int memberCount() {
	return members.size();
    }
}
