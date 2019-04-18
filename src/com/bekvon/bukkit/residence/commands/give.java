package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.cmiLib.ConfigReader;
import com.bekvon.bukkit.cmiLib.RawMessage;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class give implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 3800)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
	if (!(sender instanceof Player))
	    return false;

	Player player = (Player) sender;

	boolean includeSubzones = false;
	boolean confirmed = false;

	if (args.length != 3 && args.length != 4 && args.length != 5)
	    return false;

	for (String one : args) {
	    if (one.equalsIgnoreCase("-s"))
		includeSubzones = true;
	    if (one.equalsIgnoreCase("-confirmed"))
		confirmed = true;
	}
	if (!confirmed) {

	    RawMessage rm = new RawMessage();

	    ClaimedResidence res = plugin.getResidenceManager().getByName(args[1]);

	    if (res == null) {
		plugin.msg(sender, lm.Invalid_Residence);
		return false;
	    }

	    rm.add(plugin.getLM().getMessage(lm.Residence_GiveConfirm, args[1], res.getOwner(), args[2]), plugin.getLM().getMessage(lm.info_click), (resadmin ? "resadmin" : "res") + " give " + args[1]
		+ " " + args[2] + (includeSubzones ? " -s" : "") + " -confirmed");
	    rm.show(sender);

	    return true;
	}
	plugin.getResidenceManager().giveResidence(player, args[2], args[1], resadmin, includeSubzones);
	return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
	c.get(path + "Description", "Give residence to player.");
	c.get(path + "Info", Arrays.asList("&eUsage: &6/res give <residence name> [player] <-s>", "Gives your owned residence to target player"));
	Residence.getInstance().getLocaleManager().CommandTab.put(Arrays.asList(this.getClass().getSimpleName()), Arrays.asList("[residence]", "[playername]"));
    }
}
