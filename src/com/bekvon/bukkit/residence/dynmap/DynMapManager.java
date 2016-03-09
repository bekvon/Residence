package com.bekvon.bukkit.residence.dynmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.economy.rent.RentManager;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import com.bekvon.bukkit.residence.utils.GetTime;

public class DynMapManager {
    Residence plugin;

    public DynmapAPI api;
    MarkerAPI markerapi;
    MarkerSet set;
    private Map<String, AreaMarker> resareas = new HashMap<String, AreaMarker>();

    public DynMapManager(Residence plugin) {
	this.plugin = plugin;
    }

    public MarkerSet getMarkerSet() {
	return set;
    }

    public void fireUpdate(final ClaimedResidence res, final int deep) {
	if (api == null || set == null)
	    return;
	if (res == null)
	    return;
	final String name = res.getName();
	Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
	    public void run() {
		handleResidence(name, res, resareas, deep);
		return;
	    }
	}, 20L);
    }

    private String formatInfoWindow(String resid, ClaimedResidence res) {
	if (res == null)
	    return null;
	String v =
	    "<div class=\"regioninfo\"><div class=\"infowindow\"><span style=\"font-size:140%;font-weight:bold;\">%regionname%</span><br /> "
		+ ChatColor.stripColor(Residence.getLM().getMessage("General.Owner", "")) + "<span style=\"font-weight:bold;\">%playerowners%</span><br />"
		+ ChatColor.stripColor(Residence.getLM().getMessage("General.Flags", "")) + "<br /><span style=\"font-weight:bold;\">%flags%</span></div></div>";

	if (Residence.getRentManager().isForRent(res.getName()))
	    v = "<div class=\"regioninfo\"><div class=\"infowindow\">"
		+ ChatColor.stripColor(Residence.getLM().getMessage("Rentable.Land", "")) + "<span style=\"font-size:140%;font-weight:bold;\">%regionname%</span><br />"
		+ ChatColor.stripColor(Residence.getLM().getMessage("General.Owner", "")) + "<span style=\"font-weight:bold;\">%playerowners%</span><br />"
		+ ChatColor.stripColor(Residence.getLM().getMessage("Residence.RentedBy", "")) + "<span style=\"font-weight:bold;\">%renter%</span><br /> "
		+ ChatColor.stripColor(Residence.getLM().getMessage("General.LandCost", "")) + "<span style=\"font-weight:bold;\">%rent%</span><br /> "
		+ ChatColor.stripColor(Residence.getLM().getMessage("Rent.Days", "")) + "<span style=\"font-weight:bold;\">%rentdays%</span><br /> "
		+ ChatColor.stripColor(Residence.getLM().getMessage("Rentable.AutoRenew", "")) + "<span style=\"font-weight:bold;\">%renew%</span><br /> "
		+ ChatColor.stripColor(Residence.getLM().getMessage("Rent.Expire", "")) + "<span style=\"font-weight:bold;\">%expire%</span></div></div>";

	if (Residence.getTransactionManager().isForSale(res.getName()))
	    v = "<div class=\"regioninfo\"><div class=\"infowindow\">"
		+ ChatColor.stripColor(Residence.getLM().getMessage("Economy.LandForSale", " "))
		+ "<span style=\"font-size:140%;font-weight:bold;\">%regionname%</span><br /> "
		+ ChatColor.stripColor(Residence.getLM().getMessage("General.Owner", "")) + "<span style=\"font-weight:bold;\">%playerowners%</span><br />"
		+ ChatColor.stripColor(Residence.getLM().getMessage("Economy.SellAmount", "")) + "<span style=\"font-weight:bold;\">%price%</span><br /></div></div>";

	v = v.replace("%regionname%", res.getName());
	v = v.replace("%playerowners%", res.getOwner());
	String m = res.getEnterMessage();
	v = v.replace("%entermsg%", (m != null) ? m : "");
	m = res.getLeaveMessage();
	v = v.replace("%leavemsg%", (m != null) ? m : "");
	ResidencePermissions p = res.getPermissions();
	String flgs = "";

	// remake
	Map<String, Boolean> all = Residence.getPermissionManager().getAllFlags().getFlags();
	String[] FLAGS = new String[all.size()];
	int ii = 0;
	for (Entry<String, Boolean> one : all.entrySet()) {
	    FLAGS[ii] = one.getKey();
	    ii++;
	}

	for (int i = 0; i < FLAGS.length; i++) {
	    if (p.isSet(FLAGS[i])) {
		if (flgs.length() > 0)
		    flgs += "<br/>";
		boolean f = p.has(FLAGS[i], false);
		flgs += FLAGS[i] + ": " + f;
		v = v.replace("%flag." + FLAGS[i] + "%", Boolean.toString(f));
	    } else
		v = v.replace("%flag." + FLAGS[i] + "%", "");
	}
	v = v.replace("%flags%", flgs);
	RentManager rentmgr = Residence.getRentManager();
	TransactionManager transmgr = Residence.getTransactionManager();

	if (rentmgr.isForRent(res.getName())) {
	    boolean isrented = rentmgr.isRented(resid);
	    v = v.replace("%isrented%", Boolean.toString(isrented));
	    String id = "";
	    if (isrented)
		id = rentmgr.getRentingPlayer(resid);
	    v = v.replace("%renter%", id);

	    v = v.replace("%rent%", rentmgr.getCostOfRent(resid) + "");
	    v = v.replace("%rentdays%", rentmgr.getRentDays(resid) + "");
	    boolean renew = rentmgr.getRentableRepeatable(resid);
	    v = v.replace("%renew%", renew + "");
	    String expire = "";
	    if (isrented) {
		long time = rentmgr.getRentedLand(resid).endTime;
		if (time != 0L)
		    expire = GetTime.getTime(time);
	    }
	    v = v.replace("%expire%", expire);
	}

	if (transmgr.isForSale(res.getName())) {
	    boolean forsale = transmgr.isForSale(resid);
	    v = v.replace("%isforsale%", Boolean.toString(transmgr.isForSale(resid)));
	    String price = "";
	    if (forsale)
		price = Integer.toString(transmgr.getSaleAmount(resid));
	    v = v.replace("%price%", price);
	}

	return v;
    }

    private boolean isVisible(String id, String worldname) {
	List<String> visible = Residence.getConfigManager().DynMapVisibleRegions;
	List<String> hidden = Residence.getConfigManager().DynMapHiddenRegions;
	if (visible != null && visible.size() > 0) {
	    if ((visible.contains(id) == false) && (visible.contains("world:" + worldname) == false)) {
		return false;
	    }
	}
	if (hidden != null && hidden.size() > 0) {
	    if (hidden.contains(id) || hidden.contains("world:" + worldname))
		return false;
	}
	return true;
    }

    private void addStyle(String resid, AreaMarker m) {
	AreaStyle as = new AreaStyle();
	int sc = 0xFF0000;
	int fc = 0xFF0000;
	try {
	    sc = Integer.parseInt(as.strokecolor.substring(1), 16);
	    if (Residence.getRentManager().isForRent(resid) && !Residence.getRentManager().isRented(resid))
		fc = Integer.parseInt(as.forrentstrokecolor.substring(1), 16);
	    else if (Residence.getRentManager().isForRent(resid) && Residence.getRentManager().isRented(resid))
		fc = Integer.parseInt(as.rentedstrokecolor.substring(1), 16);
	    else if (Residence.getTransactionManager().isForSale(resid))
		fc = Integer.parseInt(as.forsalestrokecolor.substring(1), 16);
	    else
		fc = Integer.parseInt(as.fillcolor.substring(1), 16);
	} catch (NumberFormatException nfx) {
	}
	m.setLineStyle(as.strokeweight, as.strokeopacity, sc);
	m.setFillStyle(as.fillopacity, fc);
	m.setRangeY(as.y, as.y);
    }

    private void handleResidence(String resid, ClaimedResidence res, Map<String, AreaMarker> newmap, int depth) {

	String id = resid + "%" + depth;
	if (Residence.getResidenceManager().getByName(resid) == null) {
	    if (resareas.containsKey(id)) {
		AreaMarker marker = resareas.remove(id);
		marker.deleteMarker();
		return;
	    }
	}

	String name = res.getName();
	double[] x = new double[2];
	double[] z = new double[2];

	String desc = formatInfoWindow(resid, res);

	if (!isVisible(resid, res.getWorld()))
	    return;

	Location l0 = res.getAreaArray()[0].getLowLoc();
	Location l1 = res.getAreaArray()[0].getHighLoc();

	x[0] = l0.getX();
	z[0] = l0.getZ();
	x[1] = l1.getX() + 1.0;
	z[1] = l1.getZ() + 1.0;

	AreaMarker marker = resareas.remove(id);
	if (Residence.getResidenceManager().getByName(res.getName()) == null) {
	    marker.deleteMarker();
	    return;
	}
	if (marker == null) {
	    marker = set.createAreaMarker(id, name, false, res.getWorld(), x, z, false);
	    if (marker == null)
		return;
	} else {
	    marker.setCornerLocations(x, z);
	    marker.setLabel(name);
	}

	if (Residence.getConfigManager().DynMapLayer3dRegions)
	    marker.setRangeY(l1.getY() + 1.0, l0.getY());

	marker.setDescription(desc);
	addStyle(resid, marker);
	newmap.put(id, marker);

	if (depth < Residence.getConfigManager().DynMapLayerSubZoneDepth) {
	    List<ClaimedResidence> subids = res.getSubzones();
	    for (ClaimedResidence one : subids) {
		id = resid + "." + one.getSubzoneDeep();
		handleResidence(id, one, newmap, depth + 1);
	    }
	}
    }

    public void activate() {
	markerapi = api.getMarkerAPI();
	if (markerapi == null) {
	    Bukkit.getConsoleSender().sendMessage("[Residence] Error loading dynmap marker API!");
	    return;
	}

	if (set != null) {
	    set.deleteMarkerSet();
	    set = null;
	}
	set = markerapi.getMarkerSet("residence.markerset");
	if (set == null)
	    set = markerapi.createMarkerSet("residence.markerset", "Residence", null, false);
	else
	    set.setMarkerSetLabel("Residence");

	if (set == null) {
	    Bukkit.getConsoleSender().sendMessage("Error creating marker set");
	    return;
	}
	set.setLayerPriority(1);
	set.setHideByDefault(false);

	Bukkit.getConsoleSender().sendMessage("[Residence] DynMap residence activated!");

	for (Entry<String, ClaimedResidence> one : Residence.getResidenceManager().getResidences().entrySet()) {
	    Residence.getDynManager().fireUpdate(one.getValue(), one.getValue().getSubzoneDeep());
	}
    }
}
