package com.bekvon.bukkit.residence.Siege;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.BossBar.BossBarInfo;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class ResidenceSiegeListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void ResidenceSiegePreStartEvent(com.bekvon.bukkit.residence.event.ResidenceSiegePreStartEvent event) {
	ClaimedResidence res = event.getRes();
	Player player = event.getAttacker();

	ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
	Residence.getInstance().msg(player, lm.Siege_Started);
	BossBarInfo barInfo = new BossBarInfo(rPlayer, "ResidenceSiege");
	Double secLeft = (res.getSiege().getStartsAt() - System.currentTimeMillis()) / 1000D;
	barInfo.setKeepForTicks(22);
	barInfo.setColor(BarColor.GREEN);
	barInfo.setTitleOfBar("&7Siege starts in: [autoTimeLeft]");
	barInfo.setAdjustPerc(-(1D / secLeft));
	barInfo.setPercentage(secLeft, secLeft);
	barInfo.setStyle(BarStyle.SEGMENTED_20);
	rPlayer.removeBossBar(rPlayer.getBossBar("ResidenceSiege"));
	barInfo.setAuto(20);

	rPlayer.addBossBar(barInfo);

	ResidencePlayer rOwner = Residence.getInstance().getPlayerManager().getResidencePlayer(res.getOwnerUUID());
	Residence.getInstance().msg(rOwner.getPlayer(), lm.Siege_Started);
	barInfo = new BossBarInfo(rOwner, "ResidenceSiege");
	secLeft = (res.getSiege().getStartsAt() - System.currentTimeMillis()) / 1000D;
	barInfo.setKeepForTicks(22);
	barInfo.setColor(BarColor.GREEN);
	barInfo.setTitleOfBar("&7Siege starts in: [autoTimeLeft]");
	barInfo.setAdjustPerc(-(1D / secLeft));
	barInfo.setPercentage(secLeft, secLeft);
	barInfo.setStyle(BarStyle.SEGMENTED_20);
	barInfo.setAuto(20);
	rOwner.removeBossBar(rOwner.getBossBar("ResidenceSiege"));
	rOwner.addBossBar(barInfo);

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void ResidenceSiegeStartEvent(com.bekvon.bukkit.residence.event.ResidenceSiegeStartEvent event) {

	ClaimedResidence res = event.getRes();
	Player player = event.getAttacker();

	ResidencePlayer rPlayer = Residence.getInstance().getPlayerManager().getResidencePlayer(player);
	BossBarInfo barInfo = new BossBarInfo(rPlayer, "ResidenceSiege");
	Double secLeft = (res.getSiege().getEndsAt() - System.currentTimeMillis()) / 1000D - 1;
	barInfo.setKeepForTicks(22);
	barInfo.setColor(BarColor.RED);
	barInfo.setTitleOfBar("&cSiege ends in: [autoTimeLeft]");
	barInfo.setAdjustPerc(-(1D / secLeft));
	barInfo.setPercentage(secLeft, secLeft);
	barInfo.setStyle(BarStyle.SEGMENTED_20);
	barInfo.setAuto(20);
	rPlayer.removeBossBar(rPlayer.getBossBar("ResidenceSiege"));
	rPlayer.addBossBar(barInfo);

	ResidencePlayer rOwner = Residence.getInstance().getPlayerManager().getResidencePlayer(res.getOwnerUUID());
	barInfo = new BossBarInfo(rOwner, "ResidenceSiege");
	secLeft = (res.getSiege().getEndsAt() - System.currentTimeMillis()) / 1000D - 1;
	barInfo.setKeepForTicks(22);
	barInfo.setColor(BarColor.RED);
	barInfo.setTitleOfBar("&cSiege ends in: [autoTimeLeft]");
	barInfo.setAdjustPerc(-(1D / secLeft));
	barInfo.setPercentage(secLeft, secLeft);
	barInfo.setStyle(BarStyle.SEGMENTED_20);
	barInfo.setAuto(20);
	rOwner.removeBossBar(rOwner.getBossBar("ResidenceSiege"));
	rOwner.addBossBar(barInfo);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void ResidenceSiegeEndEvent(com.bekvon.bukkit.residence.event.ResidenceSiegeEndEvent event) {
	event.getRes().endSiege();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void ResidenceFlagCheckEvent(com.bekvon.bukkit.residence.event.ResidenceFlagCheckEvent event) {

    }
}
