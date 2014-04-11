package net.t00thpick1.residence.protection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableMap;

import net.milkbowl.vault.economy.Economy;
import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.CuboidArea;
import net.t00thpick1.residence.api.areas.ResidenceArea;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.locale.LocaleLoader;
import net.t00thpick1.residence.protection.yaml.YAMLGroupManager;
import net.t00thpick1.residence.utils.immutable.ImmutableWrapperCollection;
import net.t00thpick1.residence.utils.immutable.ImmutableWrapperMap;

public abstract class MemoryResidenceArea extends MemoryCuboidArea implements ResidenceArea {
    protected Map<Flag, Boolean> areaFlags;
    protected Map<String, Map<Flag, Boolean>> playerFlags;
    protected Map<Flag, Boolean> rentFlags;
    protected Map<String, ResidenceArea> rentLinks;
    protected Map<String, ResidenceArea> subzones;
    protected boolean isRentable;
    protected int cost;
    protected long rentPeriod;
    protected boolean autoRenewEnabled;
    protected String renter;
    protected boolean autoRenew;
    protected long lastPayment;
    protected boolean isBuyable;
    protected String owner;
    protected String enterMessage;
    protected String leaveMessage;
    protected Location teleportLocation;
    protected ResidenceArea parent;
    protected String name;
    protected String fullName;
    protected UUID uuid;
    protected long creationDate;
    protected long nextPayment;

    public MemoryResidenceArea(CuboidArea area) {
        super(area);
    }

    @Override
    public boolean allowAction(Flag flag) {
        Flag origFlag = flag;
        while (true) {
            Boolean bool = areaFlags.get(flag);
            if (bool != null) {
                return bool;
            }
            if (flag.getParent() != null) {
                flag = flag.getParent();
            } else {
                return ResidenceAPI.getResidenceWorld(getWorld()).allowAction(origFlag);
            }
        }
    }

    @Override
    public boolean allowAction(String player, Flag flag) {
        Flag origFlag = flag;
        if (flag == FlagManager.ADMIN && getOwner().equals(player)) {
            return true;
        }
        Map<Flag, Boolean> playerPerms = playerFlags.get(player);
        boolean skipRent = false;
        while (true) {
            Boolean bool = null;
            if (playerPerms != null) {
                bool = playerPerms.get(flag);
                if (bool != null) {
                    return bool;
                }
            }
            bool = rentFlags.get(flag);
            if (bool != null && !skipRent) {
                if (isRented() && getRenter().equals(player)) {
                    return bool;
                }
                for (ResidenceArea rentLink : rentLinks.values()) {
                    if (rentLink.getRenter().equals(player)) {
                        return bool;
                    }
                }
                skipRent = true;
            }
            bool = areaFlags.get(flag);
            if (bool != null) {
                return bool;
            }
            if (flag.getParent() == null) {
                return ResidenceAPI.getResidenceWorld(getWorld()).allowAction(origFlag);
            } else {
                flag = flag.getParent();
            }
        }
    }

    @Override
    public void setAreaFlag(Flag flag, Boolean value) {
        if (flag == null) {
            return;
        }
        if (value == null) {
            areaFlags.remove(flag);
            return;
        }
        areaFlags.put(flag, value);
    }

    @Override
    public Map<Flag, Boolean> getAreaFlags() {
        return new ImmutableWrapperMap<Flag, Boolean>(areaFlags);
    }

    @Override
    public void removeAllAreaFlags() {
        areaFlags.clear();
    }

    @Override
    public void clearFlags() {
        removeAllAreaFlags();
    }

    @Override
    public boolean isForRent() {
        return isRentable;
    }

    @Override
    public void setForRent(int cost, long rentPeriod, boolean autoRenewEnabled) {
        removeFromMarket();
        this.isRentable = true;
        this.cost = cost;
        this.rentPeriod = rentPeriod;
        this.autoRenewEnabled = autoRenewEnabled;
        ((MemoryEconomyManager) ResidenceAPI.getEconomyManager()).setForRent(this);
    }

    @Override
    public long getRentPeriod() {
        if (!isForRent() && !isRented()) {
            throw new IllegalStateException("Not for rent or being rented");
        }
        return rentPeriod;
    }

    @Override
    public boolean isAutoRenewEnabled() {
        return autoRenewEnabled;
    }

    @Override
    public void setAutoRenewEnabled(boolean autoRenew) {
        this.autoRenewEnabled = autoRenew;
    }

    @Override
    public boolean isRented() {
        return renter != null;
    }

    @Override
    public boolean rent(String renter, boolean autoRenew) {
        if (!isForRent()) {
            throw new IllegalStateException("ResidenceArea not for rent");
        }
        this.renter = renter;
        this.autoRenew = true;
        this.lastPayment = 0;
        if (!checkRent()) {
            evict();
            return false;
        }
        this.autoRenew = autoRenew;
        ((MemoryEconomyManager) ResidenceAPI.getEconomyManager()).setRented(this);
        return true;
    }

    public boolean checkRent() {
        if (System.currentTimeMillis() >= nextPayment) {
            return true;
        }
        Economy econ = Residence.getInstance().getEconomy();
        if (econ.getBalance(getRenter()) < getCost()) {
            evict();
            return false;
        }
        econ.withdrawPlayer(getRenter(), getCost());
        econ.depositPlayer(getOwner(), getCost());
        nextPayment = System.currentTimeMillis() + getRentPeriod();
        return true;
    }

    @Override
    public String getRenter() {
        return renter;
    }

    @Override
    public boolean isAutoRenew() {
        if (!isRented()) {
            throw new IllegalStateException("Not being rented");
        }
        return autoRenew;
    }

    @Override
    public void setAutoRenew(boolean autoRenew) {
        if (!isRented()) {
            throw new IllegalStateException("Not being rented");
        }
        this.autoRenew = autoRenew;
    }

    @Override
    public long getLastPaymentDate() {
        if (!isRented()) {
            throw new IllegalStateException("Not being rented");
        }
        return lastPayment;
    }

    @Override
    public long getNextPaymentDate() {
        if (!isRented()) {
            throw new IllegalStateException("Not being rented");
        }
        return nextPayment;
    }

    @Override
    public void evict() {
        if (!isRented()) {
            throw new IllegalStateException("Not being rented");
        }
        renter = null;
        autoRenew = false;
        lastPayment = 0;
        ((MemoryEconomyManager) ResidenceAPI.getEconomyManager()).evict(this);
    }

    @Override
    public void removeFromMarket() {
        MemoryEconomyManager econ = (MemoryEconomyManager) ResidenceAPI.getEconomyManager();
        econ.removeFromRent(this);
        econ.removeFromSale(this);
    }

    @Override
    public int getCost() {
        if (!isForSale() && !isForRent() && !isRented()) {
            throw new IllegalStateException("Not for rent or being rented");
        }
        return cost;
    }

    @Override
    public boolean isForSale() {
        return isBuyable;
    }

    @Override
    public void setForSale(int cost) {
        removeFromMarket();
        this.isBuyable = true;
        this.cost = cost;
        ((MemoryEconomyManager) ResidenceAPI.getEconomyManager()).setForSale(this);
    }

    @Override
    public boolean buy(String buyer) {
        Economy econ = Residence.getInstance().getEconomy();
        if (!econ.has(buyer, cost)) {
            return false;
        }
        econ.withdrawPlayer(buyer, cost);
        if (getOwner() != null) {
            econ.depositPlayer(getOwner(), cost);
        }
        removeFromMarket();
        owner = buyer;
        return true;
    }

    @Override
    public String getOwner() {
        if (owner == null) {
            return LocaleLoader.getString("Info.ServerLand");
        }
        return owner;
    }

    @Override
    public void setOwner(String owner) {
        this.owner = owner;
        clearFlags();
        applyDefaultFlags();
    }

    @Override
    public List<Player> getPlayersInResidence() {
        List<Player> within = new ArrayList<Player>();
        Player[] players = Residence.getInstance().getServer().getOnlinePlayers();
        for (Player player : players) {
            if (containsLocation(player.getLocation())) {
                within.add(player);
            }
        }
        return within;
    }

    @Override
    public Location getOutsideFreeLoc(Location insideLoc) {
        if (!containsLocation(insideLoc)) {
            return insideLoc;
        }
        Location highLoc = getHighLocation();
        Location newLoc = new Location(highLoc.getWorld(), highLoc.getBlockX(), highLoc.getBlockY(), highLoc.getBlockZ());
        Location middleLoc = newLoc.clone().add(0, -1, 0);
        Location lowLoc = newLoc.clone().add(0, -2, 0);
        for (int i = 0; i < 100; i++) {
            newLoc.add(1, 0, 1);
            middleLoc.add(1, 0, 1);
            lowLoc.add(1, 0, 1);
            newLoc.setY(255);
            middleLoc.setY(254);
            lowLoc.setY(253);
            while ((newLoc.getBlock().getType() != Material.AIR || middleLoc.getBlock().getType() != Material.AIR || lowLoc.getBlock().getType() == Material.AIR) && lowLoc.getBlockY() > 0) {
                newLoc.add(0, -1, 0);
                middleLoc.add(0, -1, 0);
                lowLoc.add(0, -1, 0);
            }
            if (lowLoc.getBlockY() > 0) {
                return newLoc;
            }
        }
        return getWorld().getSpawnLocation();
    }

    @Override
    public String getEnterMessage() {
        return enterMessage;
    }

    @Override
    public void setEnterMessage(String string) {
        this.enterMessage = string;
    }

    @Override
    public String getLeaveMessage() {
        return leaveMessage;
    }

    @Override
    public void setLeaveMessage(String string) {
        this.leaveMessage = string;
    }

    @Override
    public Location getTeleportLocation() {
        return teleportLocation;
    }

    @Override
    public boolean setTeleportLocation(Location location) {
        if (!containsLocation(location)) {
            return false;
        }
        teleportLocation = location;
        return true;
    }

    @Override
    public void applyDefaultFlags() {
        for (Entry<Flag, Boolean> defaultFlag : YAMLGroupManager.getDefaultAreaFlags(getOwner()).entrySet()) {
            setAreaFlag(defaultFlag.getKey(), defaultFlag.getValue());
        }
        if (owner != null) {
            for (Entry<Flag, Boolean> defaultFlag : YAMLGroupManager.getDefaultOwnerFlags(getOwner()).entrySet()) {
                setPlayerFlag(getOwner(), defaultFlag.getKey(), defaultFlag.getValue());
            }
        }
    }

    @Override
    public void copyFlags(ResidenceArea mirror) {
        this.areaFlags = new HashMap<Flag, Boolean>(mirror.getAreaFlags());
        this.playerFlags = new HashMap<String, Map<Flag, Boolean>>();
        Map<String, Map<Flag, Boolean>> flags = mirror.getPlayerFlags();
        for (String player : flags.keySet()) {
            playerFlags.put(player, new HashMap<Flag, Boolean>(flags.get(player)));
        }
        this.rentFlags = new HashMap<Flag, Boolean>(mirror.getRentFlags());
    }

    @Override
    public Map<Flag, Boolean> getPlayerFlags(String player) {
        Map<Flag, Boolean> flags = playerFlags.get(player);
        if (flags == null) {
            return ImmutableMap.of();
        }
        return new ImmutableWrapperMap<Flag, Boolean>(flags);
    }

    @Override
    public Map<String, Map<Flag, Boolean>> getPlayerFlags() {
        ImmutableMap.Builder<String, Map<Flag, Boolean>> builder = ImmutableMap.builder();
        for (String player : playerFlags.keySet()) {
            builder.put(player, new ImmutableWrapperMap<Flag, Boolean>(playerFlags.get(player)));
        }
        return builder.build();
    }

    @Override
    public void removeAllPlayerFlags(String player) {
        playerFlags.remove(player);
    }

    @Override
    public void setPlayerFlag(String player, Flag flag, Boolean value) {
        Map<Flag, Boolean> flags = playerFlags.get(player);
        if (flags == null) {
            if (value == null) {
                return;
            }
            flags = new HashMap<Flag, Boolean>();
            playerFlags.put(player, flags);
        }
        if (value == null) {
            flags.remove(flag);
            if (flags.isEmpty()) {
                playerFlags.remove(player);
            }
            return;
        }
        flags.put(flag, value);
    }

    @Override
    public void rentLink(ResidenceArea link) {
        if (!link.getTopParent().equals(getTopParent())) {
            return;
        }
        rentLinks.put(link.getFullName(), link);
    }

    @Override
    public Collection<ResidenceArea> getRentLinks() {
        return new ImmutableWrapperCollection<ResidenceArea>(rentLinks.values());
    }

    @Override
    public void setRentFlag(Flag flag, Boolean value) {
        if (value == null) {
            rentFlags.remove(flag);
            return;
        }
        rentFlags.put(flag, value);
    }

    @Override
    public Map<Flag, Boolean> getRentFlags() {
        return new ImmutableWrapperMap<Flag, Boolean>(rentFlags);
    }

    @Override
    public Collection<ResidenceArea> getSubzoneList() {
        return new ImmutableWrapperCollection<ResidenceArea>(subzones.values());
    }

    @Override
    public ResidenceArea getSubzoneByLocation(Location loc) {
        for (ResidenceArea subzone : subzones.values()) {
            if (subzone.containsLocation(loc)) {
                return subzone.getSubzoneByLocation(loc);
            }
        }
        return this;
    }

    @Override
    public ResidenceArea getSubzoneByName(String subzonename) {
        subzonename = subzonename.toLowerCase();
        if (!subzonename.contains(".")) {
            return subzones.get(subzonename);
        }
        String split[] = subzonename.split("\\.");
        ResidenceArea get = subzones.get(split[0]);
        for (int i = 1; i < split.length; i++) {
            if (get == null) {
                return null;
            }
            get = get.getSubzoneByName(split[i]);
        }
        return get;
    }

    @Override
    public boolean removeSubzone(ResidenceArea subzone) {
        if (!subzone.getTopParent().equals(getTopParent())) {
            return false;
        }
        if (!subzone.getParent().equals(this)) {
            return false;
        }
        return removeSubzone(subzone.getName());
    }

    @Override
    public ResidenceArea getParent() {
        return parent;
    }

    @Override
    public ResidenceArea getTopParent() {
        if (parent == null) {
            return this;
        }
        return parent.getTopParent();
    }

    @Override
    public int getSubzoneDepth() {
        int count = 0;
        ResidenceArea res = parent;
        while (res != null) {
            count++;
            res = res.getParent();
        }
        return count;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public long getCreationDate() {
        return creationDate;
    }

    @Override
    public UUID getResidenceUUID() {
        return uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ResidenceArea) {
            ResidenceArea other = ((ResidenceArea) obj);
            return other.getResidenceUUID().equals(getResidenceUUID()) && other.getCreationDate() == this.getCreationDate();
        }
        return false;
    }
}
