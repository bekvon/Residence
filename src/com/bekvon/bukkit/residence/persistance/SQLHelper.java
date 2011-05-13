/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.persistance;

import java.sql.SQLException;

/**
 *
 * @author Administrator
 */
public class SQLHelper {

    public static void generateTables(SQLManager sql) throws SQLException
    {
        String query;
        query = "create table residences (name varchar(64) PRIMARY KEY NOT NULL, owner varchar(32), x1 int(8), y1 int(8), z1 int(8), x2 int(8), y2 int(8), z2 int(8));";
        sql.open();
        sql.runQuery(query);
        query = "create table flags (zonename varchar(64) PRIMARY KEY NOT NULL, flagname varchar(16), value boolean)";
        sql.runQuery(query);
        query = "create table groupflags (zonename varchar(64) PRIMARY KEY NOT NULL, flagname varchar(16), group varchar(32), value boolean)";
        sql.runQuery(query);
        query = "create table playerflags (zonename varchar(64) PRIMARY KEY NOT NULL, flagname varchar(16), player varchar(32), value boolean)";
        sql.runQuery(query);
        query = "create table sale (zonename varchar(64) PRIMARY KEY NOT NULL, amount int(8))";
        sql.runQuery(query);
        query = "create table rentable (zonename varchar(64) PRIMARY KEY NOT NULL, amount int(8), days int(8), repeat boolean)";
        sql.runQuery(query);
        query = "create table rented (zonename varchar(64) PRIMARY KEY NOT NULL, starttime int(16), endtime int(16), autorefresh boolean)";
        sql.close();
    }
}
