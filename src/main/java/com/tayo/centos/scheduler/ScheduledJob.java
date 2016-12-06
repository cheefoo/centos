package com.tayo.centos.scheduler;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;


public class ScheduledJob 
{
	private static final Logger log = LoggerFactory.getLogger(ScheduledJob.class);
	
	private static Connection getConnection() throws Exception
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
            
            log.info("Connected to DB...");
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
	
	
	static void printTop10UserActivitiesByTop10Users() throws Exception
	{
		Connection conn = getConnection();
		java.sql.PreparedStatement ps = null;
		String sql = "select activityType, userid from ("+
					"select userid, count(userid), activityType from user_events "+
					"where activityTimestamp < current_time() and activityTimestamp >'2016-11-30 20:15:07') as t;";
		
		ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		List<UserActivity> activityList = new ArrayList<UserActivity>();
		while (rs.next())
		{
			String activityType = rs.getString("activityType");
			String userId = rs.getString("userid");
			UserActivity ua = new UserActivity(activityType, userId);
			activityList.add(ua);
		}
		ObjectMapper mapper = new ObjectMapper();
		try
		{
			for(UserActivity user: activityList)
			{
				//mapper.writeValue(new File("/"), user);
				log.info(mapper.writeValueAsString(user));
			}
		}
		catch(Exception e)
		{
			log.error(e.toString());
		}
		System.out.println("printTop10UserActivitiesByTop10Users");
		
	}
	
	static void printLast20Activities() throws Exception
	{
		Connection conn = getConnection();
		java.sql.PreparedStatement ps = null;
		String sql = "select activityType, count(activityType) from user_events " +
				"where activityTimestamp < current_time() and activityTimestamp >'2016-11-30 20:15:07'"+
				"group by activityType order by count(activityType) desc;";
		
		ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		List<String> activityList = new ArrayList<String>();
		while (rs.next())
		{
			String activityType = rs.getString("activityType");
			activityList.add(activityType);
		}
		
		ObjectMapper mapper = new ObjectMapper();
		try
		{
			for(String user: activityList)
			{
				//mapper.writeValue(new File("/"), user);
				log.info(mapper.writeValueAsString(user));
			}
		}
		catch(Exception e)
		{
			log.error(e.toString());
		}
		
		System.out.println("printLast20Activities");
	}
	
}
