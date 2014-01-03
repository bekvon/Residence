/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.t00thpick1.residence.event;

import net.t00thpick1.residence.protection.ClaimedResidence;
import net.t00thpick1.residence.protection.CuboidArea;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * @author Administrator
 */
public class ResidenceCreationEvent extends CancellableResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    protected String resname;
    CuboidArea area;

    public ResidenceCreationEvent(Player player, String newname, ClaimedResidence resref, CuboidArea resarea) {
        super("RESIDENCE_CREATE", resref, player);
        resname = newname;
        area = resarea;
    }

    public String getResidenceName() {
        return resname;
    }

    public void setResidenceName(String name) {
        resname = name;
    }

    public CuboidArea getPhysicalArea() {
        return area;
    }

    public void setPhysicalArea(CuboidArea newarea) {
        area = newarea;
    }
}
