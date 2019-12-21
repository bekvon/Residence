package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.event.ResidenceRaidEndEvent;
import com.bekvon.bukkit.residence.event.ResidenceRaidPreStartEvent;
import com.bekvon.bukkit.residence.event.ResidenceRaidStartEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.utils.Debug;
import com.bekvon.bukkit.residence.utils.Utils;

public class attack implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3100)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	final Player player = (Player) sender;

	if (args.length != 1 && args.length != 2)
	    return false;

	if (!ConfigManager.RaidEnabled) {
	    plugin.msg(player, lm.Raid_NotEnabled);
	    return true;
	}

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
	    plugin.msg(player, lm.Raid_noSelf);
	    return true;
	}

	final ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(res.getOwnerUUID());
	if (!rPlayer.isOnline()) {
	    plugin.msg(player, lm.Raid_isOffline);
	    return true;
	}

	if (res.isUnderRaidCooldown() && !res.isInPreRaid() && !res.isUnderRaid()) {
	    plugin.msg(player, lm.Raid_cooldown, Utils.to24hourShort(res.getRaid().getCooldownEnd() - System.currentTimeMillis() + 1000));
	    return true;
	}

	if (res.isUnderRaid() || res.isInPreRaid()) {
	    if (!res.getRaid().isAttacker(player))
		res.getRaid().addAttacker(player);

	    plugin.msg(player, lm.Raid_Joined, res.getName());

	    return true;
	}

	boolean started = res.preStartRaid(player);

	if (started) {
	    res.startRaid();
	    return true;
	}

	plugin.msg(player, "Cant start raid");

	return false;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Start raid on residence");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res attack [resName]"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[cresidence]"));
    }

}
