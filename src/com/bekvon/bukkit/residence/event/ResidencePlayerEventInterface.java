/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.event;

import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public interface ResidencePlayerEventInterface {
    public boolean isAdmin();
    public boolean isPlayer();
    public Player getPlayer();
}
