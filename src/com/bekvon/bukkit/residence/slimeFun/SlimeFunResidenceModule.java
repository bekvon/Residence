package com.bekvon.bukkit.residence.slimeFun;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectableAction;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectionModule;

public class SlimeFunResidenceModule implements ProtectionModule {

    private final Residence residence;

    public SlimeFunResidenceModule(Plugin plugin) {
	this.residence = (Residence) plugin;
    }

    @Override
    public Plugin getPlugin() {
	return this.residence;
    }

    @Override
    public void load() {
    }

    @Override
    public boolean hasPermission(OfflinePlayer op, Location loc, ProtectableAction action) {

	if (op == null)
	    return false;

	switch (action) {
	case INTERACT_BLOCK:
	    ClaimedResidence res = residence.getResidenceManager().getByLoc(loc);
	    if (res != null) {
		boolean allow = res.getPermissions().playerHas(new ResidencePlayer(op), Flags.container, false);

		if (!allow)
		    residence.msg(op.getPlayer(), lm.Flag_Deny, Flags.container);

		return allow;
	    }
	    break;
	case BREAK_BLOCK:
	    Player player = Bukkit.getPlayer(op.getUniqueId());

	    if (player == null)
		return false;

	    return ResidenceBlockListener.canBreakBlock(player, loc.getBlock(), true);
	default:
	    break;
	}

	return true;
    }
}
