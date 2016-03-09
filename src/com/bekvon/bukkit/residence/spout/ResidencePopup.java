package com.bekvon.bukkit.residence.spout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.bukkit.plugin.Plugin;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.Screen;
import org.getspout.spoutapi.gui.Widget;

public class ResidencePopup extends GenericPopup {
    int xspacing;
    int yspacing;
    int xsize;
    int ysize;
    HashMap<String, Widget> wigs = new HashMap<String, Widget>();
    HashMap<String, Object> metaData = new HashMap<String, Object>();
    public String type;

    public enum PopupType {
	GENERIC, FLAG_GUI, INFO_GUI
    }

    public ResidencePopup(PopupType ptype) {
	xspacing = 15;
	yspacing = 5;
	xsize = 70;
	ysize = 15;
	type = ptype.toString();
    }

    public HashMap<String, Object> getMetaData() {
	return metaData;
    }

    public void setPopupType(String t) {
	type = t;
    }

    public String getPopupType() {
	return type;
    }

    public Screen gridAttachWidget(Plugin plugin, Widget widget, int column, int row) {
	return this.gridAttachWidget(null, plugin, widget, column, row);
    }

    public Screen gridAttachWidget(String wID, Plugin plugin, Widget widget, int column, int row) {
	widget.setX(xspacing + (column * xspacing) + (column * xsize));
	widget.setY(yspacing + (row * yspacing) + (row * ysize));
	widget.setWidth(xsize);
	widget.setHeight(ysize);
	if (wID != null)
	    wigs.put(wID, widget);
	this.setDirty(true);
	return super.attachWidget(plugin, widget);
    }

    public Widget getWidget(String wID) {
	return wigs.get(wID);
    }

    public ArrayList<String> getWidgetIDs() {
	ArrayList<String> ids = new ArrayList<String>();
	for (String id : wigs.keySet()) {
	    ids.add(id);
	}
	return ids;
    }

    @Override
    public Screen removeWidget(Widget widget) {
	wigs.values().remove(widget);
	return super.removeWidget(widget);
    }

    @Override
    public Screen removeWidgets(Plugin p) {
	Iterator<Widget> it = wigs.values().iterator();
	while (it.hasNext()) {
	    Widget next = it.next();
	    if (next.getPlugin() == p)
		it.remove();
	}
	return super.removeWidgets(p);
    }

    public void setGridXSpacing(int xspace) {
	xspacing = xspace;
    }

    public void setGridYSpacing(int yspace) {
	yspacing = yspace;
    }

    public void setGridXSize(int xs) {
	xsize = xs;
    }

    public void setGridYSize(int ys) {
	ysize = ys;
    }
}
