/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.chat;

import com.bekvon.bukkit.residence.Residence;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class ChatChannel {

    protected String name;
    protected List<String> members;

    public ChatChannel(String channelName)
    {
        name = channelName;
        members = new ArrayList<String>();
    }

    public void chat(String sourcePlayer, String message)
    {
        Server serv = Residence.getServ();
        ChatColor color = Residence.getConfig().getChatColor();
        for(String member : members)
        {
            Player player = serv.getPlayer(member);
            if(player!=null)
                player.sendMessage(color + sourcePlayer + ": " + message);
        }
        System.out.println("ResidentialChat[" + name + "] - " + sourcePlayer + ": " + message);
    }
    
    public void join(String player)
    {
        if(!members.contains(player))
            members.add(player);
    }
    
    public void leave(String player)
    {
        members.remove(player);
    }

    public boolean hasMember(String player)
    {
        return members.contains(player);
    }

    public int memberCount()
    {
        return members.size();
    }
}
