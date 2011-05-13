/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.persistance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class SQLManager {

    String connector;
    String user;
    String pass;
    Connection con;

    boolean useUserPass;

    public SQLManager(String connection, String usr, String pw)
    {
        connector = connection;
        user = usr;
        pass = pw;
        useUserPass = false;
        if(!usr.equals("") && !pw.equals(""))
        {
            useUserPass = true;
        }
    }

    public synchronized boolean runQuery(String query) throws SQLException
    {
        if(isOpen())
        {
            PreparedStatement statement = con.prepareStatement(query);
            return statement.execute();
        }
        return false;
    }

    public synchronized ResultSet resultsQuery(String query)
    {
        if(isOpen())
        {
            try {
                PreparedStatement statement = con.prepareStatement(query);
                return statement.executeQuery();
            } catch (SQLException ex) {
                Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    public synchronized void open()
    {
        if(!isOpen())
        {
            try {
                if(useUserPass)
                {
                    con = DriverManager.getConnection(connector);
                }
                else
                {
                    con = DriverManager.getConnection(connector, user, pass);
                }
                con.setAutoCommit(false);
            } catch (SQLException ex) {
                Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public synchronized void close()
    {
        if(isOpen())
        {
            try {
                con.commit();
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public synchronized boolean isOpen()
    {
        try {
            if (con != null && !con.isClosed()) {
                return true;
            }
            return false;
        } catch (SQLException ex) {
            Logger.getLogger(SQLManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
