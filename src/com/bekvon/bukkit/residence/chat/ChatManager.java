package com.bekvon.bukkit.residence.chat;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Server;

/**
 *
 * @author Administrator
 */
public class ChatManager {

    protected Map<String, ChatChannel> channelmap;
    protected Server server;

    public ChatManager() {
	server = Residence.getServ();
	channelmap = new HashMap<String, ChatChannel>();
    }

    public void setChannel(String player, ClaimedResidence res) {
	this.removeFromChannel(player);
	if (!channelmap.containsKey(res.getName()))
	    channelmap.put(res.getName(), new ChatChannel(res.getName(), res.getChatPrefix(), res.getChannelColor()));
	channelmap.get(res.getName()).join(player);
    }

    public void removeFromChannel(String player) {
	for (ChatChannel chan : channelmap.values()) {
	    if (chan.hasMember(player)) {
		chan.leave(player);
		break;
	    }
	}
    }

    public ChatChannel getChannel(String channel) {
	return channelmap.get(channel);
    }

    public ChatChannel getPlayerChannel(String player) {
	for (ChatChannel chan : channelmap.values()) {
	    if (chan.hasMember(player))
		return chan;
	}
	return null;
    }

}
