/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Administrator
 */
public class ResidenceChatEvent extends CancellableResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    protected String message;
    ChatColor color;
    
    public ResidenceChatEvent(ClaimedResidence resref, Player player, String message, ChatColor color) {
        super("RESIDENCE_CHAT_EVENT", resref, player);
        this.message = message;
        this.color = color;
    }
    
    public String getChatMessage()
    {
        return message;
    }
    
    public void setChatMessage(String newmessage)
    {
        message = newmessage;
    }
    
    public ChatColor getColor()
    {
        return color;
    }
    
    public void setColor(ChatColor c)
    {
        color = c;
    }
}
