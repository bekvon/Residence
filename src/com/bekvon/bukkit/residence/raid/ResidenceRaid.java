package com.bekvon.bukkit.residence.raid;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.ConfigManager;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.BossBar.BossBarInfo;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class ResidenceRaid {

    private ClaimedResidence res;
    private Long startsAt = 0L;
    private Long endsAt = 0L;
//    private Long lastSiegeEnded = 0L;
    private HashMap<UUID, RaidAttacker> attackers = new HashMap<UUID, RaidAttacker>();
    private HashMap<UUID, RaidDefender> defenders = new HashMap<UUID, RaidDefender>();

    private int schedId = -1;

    public ResidenceRaid(ClaimedResidence res) {
	this.res = res;
    }

    public boolean onSameTeam(Player player1, Player player2) {
	return attackers.containsKey(player1.getUniqueId()) && attackers.containsKey(player2.getUniqueId()) || defenders.containsKey(player1.getUniqueId()) && defenders.containsKey(player2.getUniqueId());
    }

    public Long getEndsAt() {
	return endsAt;
    }

    public Long getCooldownEnd() {
	return endsAt + (ConfigManager.RaidCooldown * 1000);
    }

    public void setEndsAt(Long endsAt) {
	this.endsAt = endsAt;
    }

//    public Long getLastSiegeEnded() {
//	return lastSiegeEnded;
//    }
//
//    public void setLastSiegeEnded(Long lastSiegeEnded) {
//	this.lastSiegeEnded = lastSiegeEnded;
//    }

    public ClaimedResidence getRes() {
	return res;
    }

    public HashMap<UUID, RaidDefender> getDefenders() {
	return defenders;
    }

    public void addDefender(Player defender) {
	addDefender(Residence.getInstance().getPlayerManager().getResidencePlayer(defender));
    }

    public void addDefender(ResidencePlayer defender) {
	this.attackers.remove(defender.getUniqueId());
	this.defenders.put(defender.getUniqueId(), new RaidDefender(defender));
    }

    public boolean isDefender(Player player) {
	return isDefender(player.getUniqueId());
    }

    public boolean isDefender(UUID uuid) {
	return defenders.containsKey(uuid);
    }

    public void removeDefenders(Player defender) {
	this.defenders.remove(defender.getUniqueId());
    }

    public boolean isAttacker(Player player) {
	return isAttacker(player.getUniqueId());
    }

    public boolean isAttacker(UUID uuid) {
	return attackers.containsKey(uuid);
    }

    public HashMap<UUID, RaidAttacker> getAttackers() {
	return attackers;
    }

    public void addAttacker(Player attacker) {
	this.defenders.remove(attacker.getUniqueId());
	this.attackers.put(attacker.getUniqueId(), new RaidAttacker(Residence.getInstance().getPlayerManager().getResidencePlayer(attacker)));
    }

    public void addAttacker(ResidencePlayer attacker) {
	this.defenders.remove(attacker.getUniqueId());
	this.attackers.put(attacker.getUniqueId(), new RaidAttacker(attacker));
    }

    public void removeAttacker(Player attacker) {
	this.attackers.remove(attacker.getUniqueId());
    }

    public Long getStartsAt() {
	return startsAt;
    }

    public void setStartsAt(Long startsAt) {
	this.startsAt = startsAt;
    }

    public int getSchedId() {
	return schedId;
    }

    public void setSchedId(int schedId) {
	this.schedId = schedId;
    }

    public static final String bossBarPreRaidIdent = "ResidencePreRaid";
    public static final String bossBarRaidIdent = "ResidenceRaid";

    public void showBossBar() {

	if (res.isUnderRaid()) {
	    for (Entry<UUID, RaidAttacker> one : res.getRaid().getAttackers().entrySet()) {
		ResidencePlayer rPlayer = one.getValue().getPlayer();
		if (rPlayer.isOnline())
		    showBossbar(rPlayer, BarColor.BLUE, lm.Raid_EndsIn);
	    }
	    for (Entry<UUID, RaidDefender> one : res.getRaid().getDefenders().entrySet()) {
		ResidencePlayer rOwner = one.getValue().getPlayer();
		if (rOwner.isOnline())
		    showBossbar(rOwner, BarColor.BLUE, lm.Raid_EndsIn);
	    }
	} else if (res.isInPreRaid()) {
	    for (Entry<UUID, RaidAttacker> one : res.getRaid().getAttackers().entrySet()) {
		ResidencePlayer rPlayer = one.getValue().getPlayer();
		if (rPlayer.isOnline())
		    showBossbar(rPlayer, BarColor.GREEN, lm.Raid_StartsIn);
	    }
	    for (Entry<UUID, RaidDefender> one : res.getRaid().getDefenders().entrySet()) {
		ResidencePlayer rOwner = one.getValue().getPlayer();
		if (rOwner.isOnline())
		    showBossbar(rOwner, BarColor.GREEN, lm.Raid_StartsIn);
	    }
	}
    }

    private void showBossbar(ResidencePlayer rPlayer, BarColor color, lm msg) {
	BossBarInfo barInfo = rPlayer.getBossBar(res.isUnderRaid() ? bossBarRaidIdent : bossBarPreRaidIdent);
	if (barInfo == null) {
	    barInfo = new BossBarInfo(rPlayer, res.isUnderRaid() ? bossBarRaidIdent : bossBarPreRaidIdent) {
		@Override
		public void updateCycle() {
		    setTitleOfBar(Residence.getInstance().msg(msg, getDefenders().size(), getAttackers().size()));
		}
	    };
	    Double secLeft = ((res.isUnderRaid() ? res.getRaid().getEndsAt() : res.getRaid().getStartsAt()) - System.currentTimeMillis()) / 1000D;
	    barInfo.setKeepForTicks(22);
	    barInfo.setColor(color);
	    barInfo.setTitleOfBar(Residence.getInstance().msg(msg, getDefenders().size(), getAttackers().size()));
	    barInfo.setAdjustPerc(-(1D / secLeft));
	    barInfo.setPercentage(secLeft, secLeft);
	    barInfo.setStyle(BarStyle.SEGMENTED_20);
	    barInfo.setAuto(20);

	    rPlayer.addBossBar(barInfo);
	}
    }
}
