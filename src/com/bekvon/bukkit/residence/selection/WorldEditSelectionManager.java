/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.selection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author Administrator
 */
public class WorldEditSelectionManager extends SelectionManager {
    
    public WorldEditSelectionManager(Server serv)
    {
        super(serv);
    }

    @Override
    public boolean worldEdit(Player player) {
        WorldEditPlugin wep = (WorldEditPlugin) server.getPluginManager().getPlugin("WorldEdit");
        Selection sel = wep.getSelection(player);
        if(sel!=null)
        {
            this.playerLoc1.put(player.getName(), sel.getMinimumPoint());
            this.playerLoc2.put(player.getName(), sel.getMaximumPoint());
            wep.setSelection(player, null);
            return true;
        }
        return false;
    }
    
    private void afterSelectionUpdate(Player player)
    {
    	if (hasPlacedBoth(player.getName()))
    	{
            WorldEditPlugin wep = (WorldEditPlugin) server.getPluginManager().getPlugin("WorldEdit");
            World world = playerLoc1.get(player.getName()).getWorld();
            CuboidArea area = new CuboidArea(playerLoc1.get(player.getName()), playerLoc2.get(player.getName()));
            Selection selection = new CuboidSelection(world, area.getLowLoc(), area.getHighLoc());
            wep.setSelection(player, selection);
    	}
    }

    @Override
    public void placeLoc1(Player player, Location loc) {
        this.worldEdit(player);
        super.placeLoc1(player, loc);
        this.afterSelectionUpdate(player);
    }

    @Override
    public void placeLoc2(Player player, Location loc) {
        this.worldEdit(player);
        super.placeLoc2(player, loc);
        this.afterSelectionUpdate(player);
    }

    @Override
    public void sky(Player player, boolean resadmin) {
        this.worldEdit(player);
        super.sky(player, resadmin);
        afterSelectionUpdate(player);
    }

    @Override
    public void bedrock(Player player, boolean resadmin) {
        this.worldEdit(player);
        super.bedrock(player, resadmin);
        afterSelectionUpdate(player);
    }

    @Override
    public void modify(Player player, boolean shift, int amount) {
        this.worldEdit(player);
        super.modify(player, shift, amount);
        afterSelectionUpdate(player);
    }

    @Override
    public void selectChunk(Player player) {
        this.worldEdit(player);
        super.selectChunk(player);
        afterSelectionUpdate(player);
    }
   
    
}
