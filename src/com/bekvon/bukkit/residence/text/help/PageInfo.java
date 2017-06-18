package com.bekvon.bukkit.residence.text.help;

public class PageInfo {

    private int totalEntries = 0;
    private int totalPages = 0;
    private int start = 0;
    private int end = 0;
    private int currentPage = 0;

    private int currentPlace = -1;

    private int perPage = 6;

    public PageInfo(int perPage, int totalEntries, int currentPage) {
	this.perPage = perPage;
	this.totalEntries = totalEntries;
	this.currentPage = currentPage;
	calculate();
    }

    public int getPositionForOutput(int place) {
	return this.start + place + 1;
    }

    public int getPositionForOutput() {
	return currentPlace + 1;
    }

    private void calculate() {
	this.start = (this.currentPage - 1) * this.perPage;
	this.end = this.start + this.perPage - 1;
	if (this.end + 1 > this.totalEntries)
	    this.end = this.totalEntries - 1;
	this.totalPages = (int) Math.ceil((double) this.totalEntries / (double) this.perPage);
    }

    public boolean isInRange(int place) {
	if (place >= start && place <= end)
	    return true;
	return false;
    }

    public boolean isInRange() {
	currentPlace++;
	return isInRange(currentPlace);
    }

    public boolean isPageOk() {
	return isPageOk(this.currentPage);
    }

    public boolean isPageOk(int page) {
	if (this.totalPages < page)
	    return false;
	if (page < 1)
	    return false;
	return true;
    }

    public int getStart() {
	return start;
    }

    public int getEnd() {
	return end;
    }

    public int getTotalPages() {
	return totalPages;
    }

    public int getCurrentPage() {
	return currentPage;
    }

    public int getTotalEntries() {
	return totalEntries;
    }

    public int getCurrentPlace() {
	return currentPlace;
    }

    public void setCurrentPlace(int currentPlace) {
	this.currentPlace = currentPlace;
    }
}
