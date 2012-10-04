/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.event;

import org.bukkit.event.HandlerList;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 *
 * @author Administrator
 */
public class ResidenceOwnerChangeEvent extends ResidenceEvent {

	private static final HandlerList handlers = new HandlerList();
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	protected String newowner;

	public ResidenceOwnerChangeEvent(ClaimedResidence resref, String newOwner) {
		super("RESIDENCE_OWNER_CHANGE",resref);
		newowner = newOwner;
	}

	public String getNewOwner() {
		return newowner;
	}
}
