package com.bekvon.bukkit.residence.persistance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

public class SQLManager {
	protected String url;
	protected String user;
	protected String pass;
	protected Connection conn;
	public SQLManager(){
		url = Residence.getConfigManager().getSQLUrl();
		user = Residence.getConfigManager().getSQLUser();
		pass = Residence.getConfigManager().getSQLPass();
		try {
			connect();
		} catch (SQLException e) {
			Residence.getServ().getLogger().severe("Cannot connect to SQL Database");
			e.printStackTrace();
		}
	}
	private void connect() throws SQLException {
		if(conn!=null){
			conn.close();
		}
		conn = DriverManager.getConnection(url, user, pass);
	}
	public int insertSQL(String query){
		//TODO Run insert queries here

		//return row id of inserted row
		return 0;
	}
	public void deleteSQL(String query){
		//TODO run delete queries here
	}
	public void alterSQL(String query){
		//TODO run update queries here
	}
	public void addSQLFlag(boolean player, boolean group, boolean area, String flag) {
		// TODO Create new columns default null if not exists
	}
	public void removeSQLFlag(boolean player, boolean group, boolean area, String flag){
		//TODO Remove unused column
		//ALTER TABLE 'table_things' DROP 'col_stuff'
	}
	public void setPlayerResFlag(String player, String Residence, String flag, boolean value) {
		// TODO update player insert if not exists
	}
	public void setGroupResFlag(String group, String Residence, String flag, boolean value) {
		// TODO update group insert if not exists
	}
	public void alterResPlayer(String player, String Residence, boolean Action) {
		if(Action==true){
			//TODO add player to player flag table for that residence
		} else {
			//TODO remove player from player flag table for that residence
		}
	}
	public void alterResGroup(String group, String Residence, boolean Action) {
		if(Action==true){
			//TODO add group to group flag table for that residence
		} else {
			//TODO remove group from group flag table for that residence
		}
	}
	public void setRegionResFlag(String Residence, String flag, boolean value) {
		// TODO Change value of flag for residence
	}
	public void addResidence(ClaimedResidence newRes){
		// TODO add residence
	}
	public void addSubzone(String name, ClaimedResidence res, int parentid){
		// TODO add residence
	}
	public void alterResidence(Location tpLoc, String enterMessage, String leaveMessage, int bank, String owner){
		//TODO alter Residence based on which values are not null
	}
	public void removeResidence(String name){
		//TODO remove residence
	}
	public void addArea(String name, CuboidArea area, int parentid){
		//TODO add area
	}
	public void removeArea(String areaid, int parentid){
		//TODO remove area
	}
	public CuboidArea getArea(String areaname, int parentid){
		//TODO get area
		return null;
	}
	public void createMarketInfo(){
		//TODO create market info
	}
	public void modifyMarketInfo(){
		//TODO modify market info
	}
	public void addLease(){
		//TODO add lease
	}
	public void modifyLease(){
		//TODO modify lease
	}
	public void deleteLease(){
		//TODO delete lease
	}
	public void addRentData(){
		//TODO add rent data
	}
	public void addResidenceInfo(){
		//TODO add residence info
	}
	public void addBlackList(){
		//TODO add blacklist
	}
	public void alterBlackList(String list, int parentid){
		//TODO alter blacklist
	}
	public void addWhiteList(){
		//TODO add whitelist
	}
	public void alterWhiteList(String list, int parentid){
		//TODO alter whitelist
	}
	public void clearFlags(ClaimedResidence residence) {
		// TODO Clear all flags from residence
	}
	public void clearPlayerFlags(ClaimedResidence residence, String player) {
		// TODO Clear all flags for player from residence
	}
	public ClaimedResidence getResByLocation(Location loc){
		return getResByLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
	}
	public ClaimedResidence getResByLocation(int x, int y, int z, String world){
		//TODO
		return null;
	}
	public ClaimedResidence getResByName(String name){
		//TODO
		return null;
	}
	public ClaimedResidence getSubzoneByLocation(int x, int y, int z, String world, int id){
		//TODO
		return null;
	}
	public ClaimedResidence getSubzoneByName(String name, int id){
		//TODO
		return null;
	}
	public void removeSubzone(String name, int id) {
		// TODO remove subzone

	}
	public boolean isInArea(int X, int Y, int Z, String world, int parentid) {
		// TODO check if location is inside an area
		return false;
	}
	public Collection<CuboidArea> getAreas(int parentid) {
		// TODO return physical areas
		return null;
	}
	public CuboidArea getAreaByLoc(Location loc, int parentid) {
		return getAreaByLoc(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName(), parentid);
	}
	public CuboidArea getAreaByLoc(int blockX, int blockY, int blockZ, String name, int parentid) {
		// TODO Auto-generated method stub
		return null;
	}
	public void setTpLoc(int blockX, int blockY, int blockZ, World world, int parentid) {
		// TODO Auto-generated method stub

	}
	public int removeAllResidencesIn(String world) {
		// TODO Remove all residences and return amount removed
		return 0;
	}
	public List<ClaimedResidence> getAllResidences(String owner) {
		// TODO Auto-generated method stub
		return null;
	}
	public void renameSubzone(String oldName, String newName, int parentid) {
		// TODO Auto-generated method stub

	}
	public void renameArea(String oldName, String newName, int parentid) {
		// TODO Auto-generated method stub

	}
	public void removeAllWorldGroupFlags(String group, String world) {
		// TODO Auto-generated method stub

	}
	public void setWorldGroupFlag(String group, String flag, boolean value, String world) {
		// TODO Auto-generated method stub

	}
	public void setWorldFlag(String flag, boolean value, String world) {
		// TODO Auto-generated method stub

	}
	public boolean getWorldGroupFlag(String group, String flag, String world) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean getWorldFlag(String flag, String world) {
		// TODO Auto-generated method stub
		return false;
	}
	public HashMap<String, Boolean> getWorldFlags(String world) {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean getResPlayerFlag(String player, String flag, int parentid) {
		// TODO Auto-generated method stub
		return false;
	}
	public boolean getResidenceFlag(String flag, int parentid) {
		// TODO Auto-generated method stub
		return false;
	}
	public Map<String, Boolean> getPlayerFlagsByResidence(String player, int parentid) {
		// TODO Auto-generated method stub
		return null;
	}
	public Map<String, Boolean> getGroupFlagsByResidence(String group, int parentid) {
		// TODO Auto-generated method stub
		return null;
	}
	public String[] getAllResNames() {
		// TODO Auto-generated method stub
		return null;
	}
	public void renameResidence(String newName, String oldName) {
		// TODO Auto-generated method stub

	}
	public Map<String, Boolean> getAreaFlagsByRes(int parentid) {
		// TODO Auto-generated method stub
		return null;
	}
	public Map<String, Map<String, Boolean>> getAllPlayerFlagsByRes(int parentid) {
		// TODO Auto-generated method stub
		return null;
	}
	public Map<String, Map<String, Boolean>> getAllGroupFlagsByRes(int parentid) {
		// TODO Auto-generated method stub
		return null;
	}
	public String getMetaData(String plugin, String name, int id) {
		// TODO Regain metadata
		return null;
	}
	public void registerMetaData(String plugin, String name) {
		//TODO Add column for plugin with name "plugin+name"
	}
	public void setMetaData(String plugin, String name, String data, int id) {
		//TODO Store metadata
	}

}
