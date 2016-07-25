package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.economy.EconomyInterface;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent.DeleteCause;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.utils.GetTime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.entity.Player;

public class LeaseManager {

    private Map<String, Long> leaseExpireTime;

    ResidenceManager manager;

    public LeaseManager(ResidenceManager m) {
	manager = m;
	leaseExpireTime = Collections.synchronizedMap(new HashMap<String, Long>());
    }

    public boolean leaseExpires(String area) {
	return leaseExpireTime.containsKey(area);
    }

    public String getExpireTime(String area) {
	if (leaseExpireTime.containsKey(area)) {
	    return GetTime.getTime(leaseExpireTime.get(area));
	}
	return null;
    }

    public void removeExpireTime(String area) {
	leaseExpireTime.remove(area);
    }

    public void setExpireTime(String area, int days) {
	this.setExpireTime(null, area, days);
    }

    public void setExpireTime(Player player, String area, int days) {
	if (manager.getByName(area) != null) {
	    leaseExpireTime.put(area, daysToMs(days) + System.currentTimeMillis());
	    if (player != null)
		Residence.msg(player, lm.Economy_LeaseRenew, getExpireTime(area));
	} else {
	    if (player != null)
		Residence.msg(player, lm.Invalid_Area);
	}
    }

    public void renewArea(String area, Player player) {
	if (!leaseExpires(area)) {
	    Residence.msg(player, lm.Economy_LeaseNotExpire);
	    return;
	}
	ResidencePlayer rPlayer = Residence.getPlayerManager().getResidencePlayer(player);
	PermissionGroup group = rPlayer.getGroup();
	int max = group.getMaxLeaseTime();
	int add = group.getLeaseGiveTime();
	int rem = daysRemaining(area);
	EconomyInterface econ = Residence.getEconomyManager();
	if (econ != null) {
	    double cost = group.getLeaseRenewCost();
	    ClaimedResidence res = manager.getByName(area);
	    area = res.getName();
	    int amount = (int) Math.ceil(res.getTotalSize() * cost);
	    if (cost != 0D) {
		//Account account = iConomy.getBank().getAccount(player.getName());
		if (econ.canAfford(player.getName(), amount)/*account.hasEnough(amount)*/) {
		    econ.subtract(player.getName(), amount);
		    econ.add("Lease Money", amount);
		    Residence.msg(player, lm.Economy_MoneyCharged, String.format("%d", amount), econ.getName());
		} else {
		    Residence.msg(player, lm.Economy_NotEnoughMoney);
		    return;
		}
	    }
	}
	if (rem + add > max) {
	    setExpireTime(player, area, max);
	    Residence.msg(player, lm.Economy_LeaseRenewMax);
	    Residence.msg(player, lm.Economy_LeaseRenew, getExpireTime(area));
	    return;
	}
	Long get = leaseExpireTime.get(area);
	if (get != null) {
	    get = get + daysToMs(add);
	    leaseExpireTime.put(area, get);
	} else
	    leaseExpireTime.put(area, daysToMs(add));
	Residence.msg(player, lm.Economy_LeaseRenew, getExpireTime(area));
    }

    public int getRenewCost(ClaimedResidence res) {
	double cost = res.getOwnerGroup().getLeaseRenewCost();
	int amount = (int) Math.ceil(res.getTotalSize() * cost);
	return amount;
    }

    private static long daysToMs(int days) {
	return ((days) * 24L * 60L * 60L * 1000L);
    }

    private static int msToDays(long ms) {
	return (int) Math.ceil((((ms / 1000D) / 60D) / 60D) / 24D);
    }

    private int daysRemaining(String area) {
	Long get = leaseExpireTime.get(area);
	if (get <= System.currentTimeMillis())
	    return 0;
	return msToDays((int) (get - System.currentTimeMillis()));
    }

    public void doExpirations() {
	Set<Entry<String, Long>> set = leaseExpireTime.entrySet();
	Iterator<Entry<String, Long>> it = set.iterator();
	while (it.hasNext()) {
	    Entry<String, Long> next = it.next();
	    if (next.getValue() <= System.currentTimeMillis()) {
		String resname = next.getKey();
		ClaimedResidence res = Residence.getResidenceManager().getByName(resname);
		if (res == null) {
		    it.remove();
		} else {
		    resname = res.getName();
		    boolean renewed = false;
		    String owner = res.getPermissions().getOwner();

		    PermissionGroup group = res.getOwnerGroup();

		    int cost = this.getRenewCost(res);
		    if (Residence.getConfigManager().enableEconomy() && Residence.getConfigManager().autoRenewLeases()) {
			if (cost == 0) {
			    renewed = true;
			} else if (res.getBank().hasEnough(cost)) {
			    res.getBank().subtract(cost);
			    renewed = true;
			    if (Residence.getConfigManager().debugEnabled())
				System.out.println("Lease Renewed From Residence Bank: " + resname);
			} else if (Residence.getEconomyManager().canAfford(owner, cost)) {
			    if (Residence.getEconomyManager().subtract(owner, cost)) {
				renewed = true;
				if (Residence.getConfigManager().debugEnabled())
				    System.out.println("Lease Renewed From Economy: " + resname);
			    }
			}
		    }
		    if (!renewed) {
			if (!Residence.getConfigManager().enabledRentSystem() || !Residence.getRentManager().isRented(resname)) {
			    ResidenceDeleteEvent resevent = new ResidenceDeleteEvent(null, res, DeleteCause.LEASE_EXPIRE);
			    Residence.getServ().getPluginManager().callEvent(resevent);
			    if (!resevent.isCancelled()) {
				manager.removeResidence(next.getKey());
				it.remove();
				if (Residence.getConfigManager().debugEnabled())
				    System.out.println("Lease NOT removed, Removing: " + resname);
			    }
			}
		    } else {
			if (Residence.getConfigManager().enableEconomy() && Residence.getConfigManager().enableLeaseMoneyAccount()) {
			    Residence.getEconomyManager().add("Lease Money", cost);
			}
			if (Residence.getConfigManager().debugEnabled())
			    System.out.println("Lease Renew Old: " + next.getValue());
			next.setValue(System.currentTimeMillis() + daysToMs(group.getLeaseGiveTime()));
			if (Residence.getConfigManager().debugEnabled())
			    System.out.println("Lease Renew New: " + next.getValue());
		    }
		}
	    }
	}
    }

    public void resetLeases() {
	leaseExpireTime.clear();
	String[] list = manager.getResidenceList();
	for (String item : list) {
	    if (item != null) {
		ClaimedResidence res = Residence.getResidenceManager().getByName(item);
		if (res != null)
		    this.setExpireTime(null, item, res.getOwnerGroup().getLeaseGiveTime());
	    }
	}
	System.out.println("[Residence] - Set default leases.");
    }

    public Map<String, Long> save() {
	return leaseExpireTime;
    }

    public void updateLeaseName(String oldName, String newName) {
	if (leaseExpireTime.containsKey(oldName)) {
	    leaseExpireTime.put(newName, leaseExpireTime.get(oldName));
	    leaseExpireTime.remove(oldName);
	}
    }

    @SuppressWarnings("unchecked")
    public static LeaseManager load(@SuppressWarnings("rawtypes") Map root, ResidenceManager m) {
	LeaseManager l = new LeaseManager(m);
	if (root != null) {
	    for (Object val : root.values()) {
		if (!(val instanceof Long)) {
		    root.remove(val);
		}
	    }
	    l.leaseExpireTime = Collections.synchronizedMap(root);
	}
	return l;
    }
}
