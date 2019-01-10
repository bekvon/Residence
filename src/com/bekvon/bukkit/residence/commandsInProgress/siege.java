package com.bekvon.bukkit.residence.commandsInProgress;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.event.ResidenceSiegeEndEvent;
import com.bekvon.bukkit.residence.event.ResidenceSiegePreStartEvent;
import com.bekvon.bukkit.residence.event.ResidenceSiegeStartEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.utils.Utils;

public class siege implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3100)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	final Player player = (Player) sender;

	if (args.length != 1 && args.length != 2)
	    return false;

	ClaimedResidence res = null;
	if (args.length == 2)
	    res = plugin.getResidenceManager().getByName(args[1]);
	else
	    res = plugin.getResidenceManager().getByLoc(player.getLocation());

	if (res == null) {
	    plugin.msg(player, lm.Invalid_Residence);
	    return true;
	}

	if (res.isOwner(player)) {
	    plugin.msg(player, lm.Siege_noSelf);
	    return true;
	}

	final ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(res.getOwnerUUID());
	if (!rPlayer.isOnline()) {
	    plugin.msg(player, lm.Siege_isOffline);
	    return true;
	}

	if (res.isUnderSiegeCooldown()) {
	    plugin.msg(player, lm.Siege_cooldown, Utils.to24hourShort(res.getSiege().getCooldownEnd() - System.currentTimeMillis() + 1000));
	    return true;
	}

	boolean started = res.startSiege(player);

	ResidenceSiegePreStartEvent pre = new ResidenceSiegePreStartEvent(res, player);

	Bukkit.getPluginManager().callEvent(pre);
	if (pre.isCancelled()) {
	    res.endSiege();
	    return true;
	}

	final ClaimedResidence r = res;
	if (started) {
	    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		@Override
		public void run() {
		    ResidenceSiegeStartEvent start = new ResidenceSiegeStartEvent(r, player);
		    Bukkit.getPluginManager().callEvent(start);
		}
	    }, ((res.getSiege().getStartsAt() - System.currentTimeMillis()) / 50));

	    res.getSiege().setSchedId(Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		@Override
		public void run() {
		    if (r.getSiege().getSchedId() > 0) {
			ResidenceSiegeEndEvent End = new ResidenceSiegeEndEvent(r);
			Bukkit.getPluginManager().callEvent(End);
		    }
		}
	    }, ((res.getSiege().getEndsAt() - System.currentTimeMillis()) / 50)));

	    return true;
	}
	plugin.msg(player, "Cant start siege");

	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Start siege on residence");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res siege [resName]"));
    }

}
