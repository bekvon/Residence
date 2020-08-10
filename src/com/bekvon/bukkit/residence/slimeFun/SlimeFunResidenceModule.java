package com.bekvon.bukkit.residence.slimeFun;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;

import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectableAction;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectionModule;

public class SlimeFunResidenceModule implements ProtectionModule {
    private Residence residence;

    private final Plugin plugin;

    public SlimeFunResidenceModule(Plugin plugin) {
	this.plugin = plugin;
    }

    @Override
    public Plugin getPlugin() {
	return this.plugin;
    }

    @Override
    public void load() {
	this.residence = Residence.getInstance();
    }

    @Override
    public boolean hasPermission(OfflinePlayer p, Location l, ProtectableAction action) {
	if (!action.isBlockAction())
	    return true;

	switch (action) {
	case ACCESS_INVENTORIES:
	    break;
	case BREAK_BLOCK:
	    Player player = Bukkit.getPlayer(p.getUniqueId());
	    if (player == null)
		return false;
	    if (ResidenceBlockListener.cancelBlockBreak(player, l.getBlock()))
		return false;
	    break;
	case PLACE_BLOCK:
	    break;
	case PVP:
	    break;
	default:
	    break;
	}
	return true;
    }
}
