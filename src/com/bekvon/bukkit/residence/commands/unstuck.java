package com.bekvon.bukkit.residence.commands;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Zrips.CMILib.FileHandler.ConfigReader;
import net.Zrips.CMILib.Version.Teleporters.CMITeleporter;

import com.bekvon.bukkit.residence.LocaleManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class unstuck implements cmd {

    @Override
    @CommandAnnotation(simple = true, priority = 4000)
    public Boolean perform(Residence plugin, CommandSender sender, String[] args, boolean resadmin) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();

        if (!group.hasUnstuckAccess() && !plugin.isResAdminOn(player)) {
            plugin.msg(player, lm.General_NoPermission);
            return true;
        }
        ClaimedResidence res = plugin.getResidenceManager().getByLoc(player.getLocation());
        if (res == null) {
            plugin.msg(player, lm.Residence_NotIn);
        } else {
            plugin.msg(player, lm.General_Moved);
            Location loc = res.getOutsideFreeLoc(player.getLocation(), player, true);
            if (loc != null)
                CMITeleporter.teleportAsync(player, loc);
        }
        return true;
    }

    @Override
    public void getLocale() {
        ConfigReader c = Residence.getInstance().getLocaleManager().getLocaleConfig();
        c.get("Description", "Teleports outside of residence");
        c.get("Info", Arrays.asList("&eUsage: &6/res unstuck"));
        LocaleManager.addTabCompleteMain(this);
    }
}
