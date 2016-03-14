package com.bekvon.bukkit.residence.dynmap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.event.ResidenceAreaAddEvent;
import com.bekvon.bukkit.residence.event.ResidenceAreaDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceFlagChangeEvent;
import com.bekvon.bukkit.residence.event.ResidenceOwnerChangeEvent;
import com.bekvon.bukkit.residence.event.ResidenceRenameEvent;
import com.bekvon.bukkit.residence.event.ResidenceRentEvent;
import com.bekvon.bukkit.residence.event.ResidenceSizeChangeEvent;
import com.bekvon.bukkit.residence.event.ResidenceSubzoneCreationEvent;
import com.bekvon.bukkit.residence.utils.Debug;

public class DynMapListeners implements Listener {
//    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
//    public void onResidenceCreate(ResidenceCreationEvent event) {
//	Residence.getDynManager().fireUpdate(event.getResidence(), event.getResidence().getSubzoneDeep());
//    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceAreaAdd(ResidenceAreaAddEvent event) {
	Debug.D("Event : " + event.getEventName());
	Residence.getDynManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceAreaDelete(ResidenceAreaDeleteEvent event) {
	Debug.D("Event : " + event.getEventName());
	Residence.getDynManager().fireUpdateRemove(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceSubZoneCreate(ResidenceSubzoneCreationEvent event) {
	Debug.D("Event : " + event.getEventName());
	Residence.getDynManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceFlagChange(ResidenceFlagChangeEvent event) {
	Debug.D("Event : " + event.getEventName());
	Residence.getDynManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceDelete(ResidenceDeleteEvent event) {
	Debug.D("Event : " + event.getEventName());
	Residence.getDynManager().fireUpdateRemove(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceOwnerChange(ResidenceOwnerChangeEvent event) {
	Debug.D("Event : " + event.getEventName());
	Residence.getDynManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceRename(ResidenceRenameEvent event) {
	Debug.D("Event : " + event.getEventName());
	Residence.getDynManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceRent(ResidenceRentEvent event) {
	Debug.D("Event : " + event.getEventName());
	Residence.getDynManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResidenceSizeChange(ResidenceSizeChangeEvent event) {
	Debug.D("Event : " + event.getEventName());
	Residence.getDynManager().fireUpdateAdd(event.getResidence(), event.getResidence().getSubzoneDeep());
    }
}
