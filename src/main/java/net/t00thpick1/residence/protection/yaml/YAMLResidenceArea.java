package net.t00thpick1.residence.protection.yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import net.t00thpick1.residence.ConfigManager;
import net.t00thpick1.residence.Residence;
import net.t00thpick1.residence.api.ResidenceAPI;
import net.t00thpick1.residence.api.areas.CuboidArea;
import net.t00thpick1.residence.api.areas.ResidenceArea;
import net.t00thpick1.residence.api.events.ResidenceAreaCreatedEvent;
import net.t00thpick1.residence.api.events.ResidenceAreaDeletedEvent;
import net.t00thpick1.residence.api.events.ResidenceAreaRenamedEvent;
import net.t00thpick1.residence.api.flags.Flag;
import net.t00thpick1.residence.api.flags.FlagManager;
import net.t00thpick1.residence.api.flags.Flag.FlagType;
import net.t00thpick1.residence.protection.MemoryEconomyManager;
import net.t00thpick1.residence.protection.MemoryResidenceArea;
import net.t00thpick1.residence.protection.MemoryResidenceManager;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class YAMLResidenceArea extends MemoryResidenceArea {
    private ConfigurationSection section;

    public YAMLResidenceArea(ConfigurationSection section, YAMLResidenceArea parent) throws Exception {
        super(YAMLCuboidAreaSerializer.deserialize(section.getConfigurationSection("Data").getConfigurationSection("Area")));
        this.section = section;
        this.name = section.getName();
        if (parent == null) {
            this.fullName = name;
        } else {
            this.fullName = parent.fullName + "." + name;
        }
        this.parent = parent;
        ConfigurationSection data = section.getConfigurationSection("Data");
        this.owner = data.getString("Owner");
        this.creationDate = data.getLong("CreationDate");
        this.enterMessage = data.getString("EnterMessage");
        this.leaveMessage = data.getString("LeaveMessage");
        if (data.contains("ResidenceUUID")) {
            this.uuid = UUID.fromString(data.getString("ResidenceUUID"));
        } else {
            this.uuid = UUID.randomUUID();
        }
        this.teleportLocation = loadTeleportLocation(data.getConfigurationSection("TPLocation"));
        if (data.isConfigurationSection("RentData")) {
            ConfigurationSection rentData = data.getConfigurationSection("RentData");
            this.lastPayment = rentData.getLong("LastPayment");
            this.renter = rentData.getString("Renter");
            this.nextPayment = rentData.getLong("NextPayment");
            this.autoRenew = rentData.getBoolean("IsAutoRenew");
        } else {
            this.lastPayment = 0;
            this.renter = null;
            this.nextPayment = 0;
            this.autoRenew = false;
        }
        data = data.getConfigurationSection("MarketData");
        this.isRentable = data.getBoolean("ForRent");
        this.isBuyable = data.getBoolean("ForSale");
        this.cost = data.getInt("Cost");
        this.rentPeriod = data.getLong("RentPeriod", 0);
        initMarketState();
        areaFlags = new HashMap<Flag, Boolean>();
        boolean preserveFlags = ConfigManager.getInstance().preserveUnregisteredFlags();
        data = section.getConfigurationSection("Flags");
        for (String flagKey : data.getKeys(false)) {
            Flag flag = FlagManager.getFlag(flagKey);
            if (flag == null) {
                if (!preserveFlags) {
                    continue;
                }
                flag = new Flag(flagKey, FlagType.DUMMY, null, null);
                FlagManager.addFlag(flag);
            }
            areaFlags.put(flag, data.getBoolean(flagKey));
        }
        playerFlags = new HashMap<String, Map<Flag, Boolean>>();
        data = section.getConfigurationSection("Players");
        for (String player : data.getKeys(false)) {
            Map<Flag, Boolean> pFlags = new HashMap<Flag, Boolean>();
            ConfigurationSection flags = data.getConfigurationSection(player);
            for (String flagKey : flags.getKeys(false)) {
                Flag flag = FlagManager.getFlag(flagKey);
                if (flag == null) {
                    if (!preserveFlags) {
                        continue;
                    }
                    flag = new Flag(flagKey, FlagType.DUMMY, null, null);
                    FlagManager.addFlag(flag);
                }
                pFlags.put(flag, flags.getBoolean(flagKey));
            }
            playerFlags.put(player, pFlags);
        }
        rentFlags = new HashMap<Flag, Boolean>();
        data = section.getConfigurationSection("RentFlags");
        for (String flagKey : data.getKeys(false)) {
            Flag flag = FlagManager.getFlag(flagKey);
            if (flag == null) {
                if (!preserveFlags) {
                    continue;
                }
                flag = new Flag(flagKey, FlagType.DUMMY, null, null);
                FlagManager.addFlag(flag);
            }
            rentFlags.put(flag, data.getBoolean(flagKey));
        }
        subzones = new HashMap<String, ResidenceArea>();
        data = section.getConfigurationSection("Subzones");
        for (String subzone : data.getKeys(false)) {
            subzones.put(subzone, new YAMLResidenceArea(data.getConfigurationSection(subzone), this));
        }
        if (getParent() == null) {
            loadRentLinks();
        }
    }

    private Location loadTeleportLocation(ConfigurationSection section) {
        return new Location(getWorld(), section.getDouble("X"), section.getDouble("Y"), section.getDouble("Z"));
    }

    public YAMLResidenceArea(ConfigurationSection section, CuboidArea area, String owner, YAMLResidenceArea parent) {
        super(area);
        this.section = section;
        this.name = section.getName();
        this.parent = parent;
        if (parent == null) {
            this.fullName = name;
        } else {
            this.fullName = parent.fullName + "." + name;
        }
        ConfigurationSection data = section.createSection("Data");
        this.owner = owner;
        this.creationDate = System.currentTimeMillis();
        this.enterMessage = YAMLGroupManager.getDefaultEnterMessage(owner);
        this.leaveMessage = YAMLGroupManager.getDefaultLeaveMessage(owner);
        data.createSection("Area");
        data.createSection("MarketData");
        this.isBuyable = false;
        this.isRentable = false;
        this.cost = 0;
        this.autoRenewEnabled = ConfigManager.getInstance().isAutoRenewDefault();
        this.teleportLocation = getCenter();
        data.createSection("TPLocation");
        this.areaFlags = new HashMap<Flag, Boolean>();
        section.createSection("Flags");
        this.playerFlags = new HashMap<String, Map<Flag, Boolean>>();
        section.createSection("Players");
        this.rentFlags = new HashMap<Flag, Boolean>();
        section.createSection("RentFlags");
        this.subzones = new HashMap<String, ResidenceArea>();
        section.createSection("Subzones");
        this.rentLinks = new HashMap<String, ResidenceArea>();
        section.createSection("RentLinks");
        this.uuid = UUID.randomUUID();
        applyDefaultFlags();
    }

    private void loadRentLinks() {
        rentLinks = new HashMap<String, ResidenceArea>();
        ConfigurationSection links = section.getConfigurationSection("RentLinks");
        for (String rentLink : links.getStringList("Links")) {
            rentLinks.put(rentLink, getTopParent().getSubzoneByName(rentLink));
        }
        for (ResidenceArea subzone : subzones.values()) {
            ((YAMLResidenceArea) subzone).loadRentLinks();
        }
    }

    private void initMarketState() {
        MemoryEconomyManager econ = (MemoryEconomyManager) ResidenceAPI.getEconomyManager();
        if (isRented()) {
            econ.setRented(this);
        }
        if (isForRent()) {
            econ.setForRent(this);
        }
        if (isForSale()) {
            econ.setForSale(this);
        }
    }

    public boolean createSubzone(String name, String owner, CuboidArea area) {
        if (subzones.containsKey(name)) {
            return false;
        }
        if (!isAreaWithin(area)) {
            return false;
        }
        for (ResidenceArea subzone : getSubzoneList()) {
            if (subzone.checkCollision(area)) {
                return false;
            }
        }
        ConfigurationSection subzoneSection = section.getConfigurationSection("Subzones");
        try {
            ResidenceArea newRes = new YAMLResidenceArea(subzoneSection.createSection(name), area, owner, this);
            subzones.put(name.toLowerCase(), newRes);
            ((MemoryResidenceManager) ResidenceAPI.getResidenceManager()).addSubzoneUUID(newRes);
            Residence residence = Residence.getInstance();
            residence.getServer().getPluginManager().callEvent(new ResidenceAreaCreatedEvent(newRes));
        } catch (Exception e) {
            subzoneSection.set(name, null);
            subzones.remove(name.toLowerCase());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean removeSubzone(String subzone) {
        ResidenceArea sub = subzones.remove(subzone.toLowerCase());
        if (sub == null) {
            return false;
        }
        ConfigurationSection subzoneSection = section.getConfigurationSection("Subzones");
        subzoneSection.set(subzone, null);
        ((MemoryResidenceManager) ResidenceAPI.getResidenceManager()).removeSubzoneUUID(sub);
        Residence residence = Residence.getInstance();
        residence.getServer().getPluginManager().callEvent(new ResidenceAreaDeletedEvent(sub));
        return true;
    }

    public boolean rename(String newName) {
        if (parent == null) {
            if (((YAMLResidenceManager) ResidenceAPI.getResidenceManager()).rename(this, newName)) {
                String oldName = this.name;
                this.fullName = newName;
                this.name = newName;
                Residence residence = Residence.getInstance();
                residence.getServer().getPluginManager().callEvent(new ResidenceAreaRenamedEvent(this, oldName));
                return true;
            }
            return false;
        } else {
            ResidenceArea parent = getParent();
            if (parent.renameSubzone(name, newName)) {
                String oldName = this.name;
                this.fullName = parent.getFullName() + "." + newName;
                this.name = newName;
                Residence residence = Residence.getInstance();
                residence.getServer().getPluginManager().callEvent(new ResidenceAreaRenamedEvent(this, oldName));
                return true;
            }
            return false;
        }
    }

    public boolean renameSubzone(String oldName, String newName) {
        if (!subzones.containsKey(oldName.toLowerCase())) {
            return false;
        }
        if (subzones.containsKey(newName.toLowerCase())) {
            return false;
        }
        ConfigurationSection subzoneSection = section.getConfigurationSection("Subzones");
        ConfigurationSection newSection = subzoneSection.createSection(newName);
        subzoneSection.set(oldName, null);
        YAMLResidenceArea area = (YAMLResidenceArea) subzones.remove(oldName.toLowerCase());
        area.newSection(newSection);
        subzones.put(newName.toLowerCase(), area);
        return true;
    }

    public void save() {
        ConfigurationSection data = section.getConfigurationSection("Data");
        data.set("Owner", owner);
        data.set("CreationDate", creationDate);
        data.set("ResidenceUUID", uuid);
        data.set("EnterMessage", enterMessage);
        data.set("LeaveMessage", leaveMessage);
        ConfigurationSection tploc = data.getConfigurationSection("TPLocation");
        tploc.set("X", teleportLocation.getX());
        tploc.set("Y", teleportLocation.getY());
        tploc.set("Z", teleportLocation.getZ());
        YAMLCuboidAreaSerializer.serialize(this, data.getConfigurationSection("Area"));
        if (isRented()) {
            ConfigurationSection rentData = data.createSection("RentData");
            rentData.set("LastPayment", lastPayment);
            rentData.getString("Renter", renter);
            rentData.getLong("NextPayment", nextPayment);
            rentData.getBoolean("IsAutoRenew", autoRenew);
        } else {
            data.set("RentData", null);
        }
        data = data.getConfigurationSection("MarketData");
        data.set("ForRent", isRentable);
        data.set("ForSale", isBuyable);
        data.set("Cost", cost);
        data.set("RentPeriod", rentPeriod);
        section.set("Flags", null);
        data = section.createSection("Flags");
        for (Entry<Flag, Boolean> flag : areaFlags.entrySet()) {
            data.set(flag.getKey().getName(), flag.getValue());
        }
        section.set("Players", null);
        data = section.createSection("Players");
        for (Entry<String, Map<Flag, Boolean>> player : playerFlags.entrySet()) {
            ConfigurationSection playerSection = data.createSection(player.getKey());
            for (Entry<Flag, Boolean> flag : player.getValue().entrySet()) {
                playerSection.set(flag.getKey().getName(), flag.getValue());
            }
        }
        section.set("RentFlags", null);
        data = section.createSection("RentFlags");
        for (Entry<Flag, Boolean> flag : rentFlags.entrySet()) {
            data.set(flag.getKey().getName(), flag.getValue());
        }
        data = section.getConfigurationSection("Subzones");
        for (ResidenceArea subzone : subzones.values()) {
            ((YAMLResidenceArea) subzone).save();
        }
        data = section.getConfigurationSection("RentLinks");
        List<String> rentLink = new ArrayList<String>(rentLinks.keySet());
        data.set("Links", rentLink);
    }

    public void newSection(ConfigurationSection newSection) {
        if (parent != null) {
            this.fullName = parent.getFullName() + "." + newSection.getName();
        } else {
            this.fullName = newSection.getName();
        }
        this.section = newSection;
        ConfigurationSection data = section.createSection("Data");
        data.createSection("Area");
        data.createSection("MarketData");
        data.createSection("TPLocation");
        section.createSection("Flags");
        section.createSection("Players");
        section.createSection("RentFlags");
        section.createSection("Subzones");
        section.createSection("RentLinks");
        ConfigurationSection subzoneSection = section.createSection("Subzones");
        for (Entry<String, ResidenceArea> subzone : subzones.entrySet()) {
            ((YAMLResidenceArea) subzone.getValue()).newSection(subzoneSection.createSection(subzone.getKey()));
        }
    }
}
