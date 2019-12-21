package com.bekvon.bukkit.residence.raid;

import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.BossBar.BossBarInfo;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;

public class ResidenceRaidListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void ResidenceSiegePreStartEvent(com.bekvon.bukkit.residence.event.ResidenceRaidPreStartEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void ResidenceSiegeStartEvent(com.bekvon.bukkit.residence.event.ResidenceRaidStartEvent event) {

//	for (UUID one : event.getRes().getRaid().getAttackers()) {
//	    ResidencePlayer RPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(one);
//	    if (RPlayer != null) {
//		BossBarInfo barInfo = RPlayer.getBossBar(ResidenceRaid.bossBarPreRaidIdent);
//		if (barInfo != null)
//		    RPlayer.removeBossBar(barInfo);
//	    }
//	}
//	for (UUID one : event.getRes().getRaid().getDefenders()) {
//	    ResidencePlayer RPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(one);
//	    if (RPlayer != null) {
//		BossBarInfo barInfo = RPlayer.getBossBar(ResidenceRaid.bossBarPreRaidIdent);
//		if (barInfo != null)
//		    RPlayer.removeBossBar(barInfo);
//	    }
//	}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void ResidenceSiegeEndEvent(com.bekvon.bukkit.residence.event.ResidenceRaidEndEvent event) {

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void ResidenceFlagCheckEvent(com.bekvon.bukkit.residence.event.ResidenceFlagCheckEvent event) {

    }
}
