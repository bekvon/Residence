package com.bekvon.bukkit.residence.text.help;

public class PageInfo {

    private int totalEntries = 0;
    private int totalPages = 0;
    private int start = 0;
    private int end = 0;
    private int currentPage = 0;

    private int perPage = 6;

    public PageInfo(int perPage, int totalEntries, int currentPage) {
	this.perPage = perPage;
	this.totalEntries = totalEntries;
	this.currentPage = currentPage;
	calculate();
    }

    private void calculate() {
	this.start = (this.currentPage - 1) * this.perPage;
	this.end = this.start + this.perPage;
	this.totalPages = (int) Math.ceil((double) this.totalEntries / (double) this.perPage);
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
}
