package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ResidenceSiegePreStartEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
	return handlers;
    }

    public static HandlerList getHandlerList() {
	return handlers;
    }

    private ClaimedResidence res;
    private Player attacker;
    protected boolean cancelled;

    public ResidenceSiegePreStartEvent(ClaimedResidence res, Player attacker) {
	this.res = res;
	this.attacker = attacker;
    }

    @Override
    public boolean isCancelled() {
	return cancelled;
    }

    @Override
    public void setCancelled(boolean bln) {
	cancelled = bln;
    }

    public ClaimedResidence getRes() {
	return res;
    }

    public Player getAttacker() {
	return attacker;
    }
}
