package com.bekvon.bukkit.residence.selection;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;

@SuppressWarnings("deprecation")
public class SchematicsManager {
    public boolean save(ClaimedResidence res) {
	if (Residence.getWEplugin() == null)
	    return false;
	if (res == null)
	    return false;

	CuboidArea area = res.getAreaArray()[0];

	Vector bvmin = new Vector(area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc().getBlockZ());
	Vector bvmax = new Vector(area.getHighLoc().getBlockX(), area.getHighLoc().getBlockY(), area.getHighLoc().getBlockZ());
	Vector origin = new Vector(area.getLowLoc().getBlockX(), area.getLowLoc().getBlockY(), area.getLowLoc().getBlockZ());

	World bworld = Bukkit.getWorld(res.getWorld());

	if (bworld == null)
	    return false;

	EditSession editSession = new EditSession(new BukkitWorld(bworld), Integer.MAX_VALUE);
	editSession.enableQueue();
	CuboidClipboard clipboard = new CuboidClipboard(bvmax.subtract(bvmin).add(new Vector(1, 1, 1)), origin);
	clipboard.setOrigin(origin);
	clipboard.copy(editSession);

	File dir = new File(Residence.getDataLocation(), "Schematics");
	if (!dir.exists())
	    try {
		dir.mkdir();
	    } catch (SecurityException se) {
	    }
	dir = new File(Residence.getDataLocation(), "Schematics" + File.separator + res.getWorld());
	if (!dir.exists())
	    try {
		dir.mkdir();
	    } catch (SecurityException se) {
	    }
	
	File file = new File(Residence.getDataLocation(), "Schematics" + File.separator + res.getWorld() + File.separator + res.getName() + ".schematic");
	try {
	    SchematicFormat.MCEDIT.save(clipboard, file);
	} catch (com.sk89q.worldedit.world.DataException | IOException e) {
	    return false;
	}
	editSession.flushQueue();
	return true;
    }

    public boolean load(ClaimedResidence res) {

	if (Residence.getWEplugin() == null)
	    return false;

	if (res == null)
	    return false;

	World bworld = Bukkit.getWorld(res.getWorld());

	if (bworld == null)
	    return false;

	EditSession es = new EditSession(new BukkitWorld(bworld), Integer.MAX_VALUE);
	File file = new File(Residence.getDataLocation(), "Schematics" + File.separator + res.getWorld() + File.separator + res.getName() + ".schematic");

	if (!file.exists())
	    return false;

	CuboidClipboard cc = null;
	try {
	    cc = CuboidClipboard.loadSchematic(file);
	} catch (com.sk89q.worldedit.world.DataException e1) {
	    e1.printStackTrace();
	} catch (IOException e1) {
	    e1.printStackTrace();
	    return false;
	}

	if (cc == null)
	    return false;
	Vector or = cc.getOrigin();
	ClaimedResidence r1 = Residence.getResidenceManager().getByLoc(new Location(bworld, or.getBlockX(), or.getBlockY(), or.getBlockZ()));
	ClaimedResidence r2 = Residence.getResidenceManager().getByLoc(new Location(bworld, or.getBlockX() + cc.getWidth() - 1, or.getBlockY() + cc.getHeight() - 1, or
	    .getBlockZ() + cc.getLength() - 1));
	if (r1 == null || r2 == null)
	    return false;

	if (!r1.getName().equalsIgnoreCase(r2.getName()))
	    return false;

//	int totalBlocks = cc.getHeight() * cc.getLength() * cc.getWidth();

	try {
	    cc.paste(es, cc.getOrigin(), false);
	} catch (MaxChangedBlocksException e) {
	    e.printStackTrace();
	    return false;
	}
	return true;
    }

    public boolean delete(ClaimedResidence res) {
	if (Residence.getWEplugin() == null)
	    return false;
	if (res == null)
	    return false;

	File file = new File(Residence.getDataLocation(), "Schematics" + File.separator + res.getWorld() + File.separator + res.getName() + ".schematic");
	if (!file.exists())
	    return false;

	return file.delete();
    }

    public boolean rename(ClaimedResidence res, String newName) {
	if (Residence.getWEplugin() == null)
	    return false;
	if (res == null)
	    return false;

	File oldFile = new File(Residence.getDataLocation(), "Schematics" + File.separator + res.getWorld() + File.separator + res.getName() + ".schematic");

	if (!oldFile.exists())
	    return false;

	File newFile = new File(Residence.getDataLocation(), "Schematics" + File.separator + res.getWorld() + File.separator + newName + ".schematic");
	return oldFile.renameTo(newFile);
    }
}
