package com.bekvon.bukkit.residence.Siege;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class ResidenceSiege {

    private ClaimedResidence res;
    private Long startsAt = 0L;
    private Long endsAt = 0L;
//    private Long lastSiegeEnded = 0L;
    private Set<Player> attackers = new HashSet<Player>();
    private Set<Player> defenders = new HashSet<Player>();
    
    private int schedId = -1;

    int siegeCooldown = 5;

    public ResidenceSiege() {
    }

    public Long getEndsAt() {
	return endsAt;
    }

    public Long getCooldownEnd() {
	return endsAt + (siegeCooldown * 1000);
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

    public void setRes(ClaimedResidence res) {
	this.res = res;
    }

    public Set<Player> getDefenders() {
	return defenders;
    }

    public void addDefender(Player defender) {
	this.defenders.add(defender);
    }

    public void removeDefenders(Player defender) {
	this.defenders.remove(defender);
    }

    public Set<Player> getAttackers() {
	return attackers;
    }

    public void addAttacker(Player attacker) {
	this.attackers.add(attacker);
    }

    public void removeAttacker(Player attacker) {
	this.attackers.remove(attacker);
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

}
