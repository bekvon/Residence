package com.bekvon.bukkit.residence.containers;

public class CommandStatus {

    private Boolean simple;
    private Integer priority;

    public CommandStatus(Boolean simple, Integer priority) {
	this.simple = simple;
	this.priority = priority;
    }

    public Integer getPriority() {
	return priority;
    }

    public void setPriority(Integer priority) {
	this.priority = priority;
    }

    public Boolean getSimple() {
	return simple;
    }

    public void setSimple(Boolean simple) {
	this.simple = simple;
    }
}
