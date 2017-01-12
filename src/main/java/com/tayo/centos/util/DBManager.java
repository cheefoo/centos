package com.tayo.centos.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by temitayo on 1/12/17.
 */
public class DbManager
{
    private static final Logger log = LoggerFactory.getLogger(DbManager.class);

    public static Connection getConnection() throws Exception
    {
        Connection conn = null;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("db.properties");
        Properties prop = new Properties();
        log.info("Input from classloader is :" + input.toString());
        prop.load(input);
        try
        {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            log.info("Connecting to database...");
            Properties props = new Properties();
            props.setProperty("user", prop.getProperty("mysqldbuser"));
            props.setProperty("password", prop.getProperty("mysqldbpwd"));
            conn = DriverManager.getConnection(prop.getProperty("mysqldburl"), props);
            log.info("Connected to DB... wink wink");
        }
        catch (ClassNotFoundException e1)
        {
            log.error("Encountered a ClassNotFoundException : " + e1.toString());
            e1.printStackTrace();
            throw new ClassNotFoundException();
        }
        catch (SQLException e1)
        {
            log.error("Encountered an SQL Exception :" + e1.toString());
            e1.printStackTrace();
            throw new SQLException();
        }

        return conn;
    }
}
