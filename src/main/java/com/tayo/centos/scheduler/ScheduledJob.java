package com.tayo.centos.scheduler;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;


public class ScheduledJob 
{
	private static final Logger log = LoggerFactory.getLogger(ScheduledJob.class);
	private static final String pattern = "yyyy-MM-dd HH:mm:ss ";
	static SimpleDateFormat format = new SimpleDateFormat(pattern);
	private static final String one = "<!DOCTYPE html><html><head></head><body>";
    private static final String two = "<h1>My First Heading</h1>";
    private static final String three = "</body></html>";
    private static final String head1 = "<p>Top Users and Top Activities<p>";
    private static final String head2 = "<p>Last Activities<p>";
    private static final String outputfile = "index.html";


	
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
	static void printTop10UserActivitiesByTop10Users(Date timeToSend) throws Exception
	{
		log.info("Time to send is : " + format.format(timeToSend));
		String dateToCompute = format.format(timeToSend);
		Connection conn = getConnection();
		java.sql.PreparedStatement ps = null;
		/*String sql = "select activityType, userid from ("+
					"select userid, count(userid), activityType from user_events "+
					"where activityTimestamp < current_time() and activityTimestamp >'2016-11-30 20:15:07') as t;";*/
		String sql = "select activityType, userid from ("+
				"select userid, count(userid), activityType from user_events "+
				"where activityTimestamp < current_time() and activityTimestamp >" + "'"+dateToCompute+"'"+") as t;";
		
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
	
	static void printLast20Activities(Date timeToSend) throws Exception
	{
		log.info("Time to send is : " + format.format(timeToSend));
		String dateToCompute = format.format(timeToSend);
		Connection conn = getConnection();
		java.sql.PreparedStatement ps = null;
		String sql = "select activityType, count(activityType) from user_events " +
				"where activityTimestamp < current_time() and activityTimestamp >"+"'"+dateToCompute+"'"+
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
	
	static void printAllAnswers(Date timeToSend)throws Exception
	{
		log.info("Time to send is : " + format.format(timeToSend));
		String dateToCompute = format.format(timeToSend);
		Connection conn = getConnection();
		java.sql.PreparedStatement ps = null;
		String sql = "select activityType, count(activityType) from user_events " +
				"where activityTimestamp < current_time() and activityTimestamp >"+"'"+dateToCompute+"'"+
				"group by activityType order by count(activityType) desc;";
		
		ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		List<String> activityList = new ArrayList<String>();
		while (rs.next())
		{
			String activityType = rs.getString("activityType");
			activityList.add(activityType);
		}	
		java.sql.PreparedStatement ps2 = null;
		String sql2 = "select activityType, userid from ("+
				"select userid, count(userid), activityType from user_events "+
				"where activityTimestamp < current_time() and activityTimestamp >" + "'"+dateToCompute+"'"+") as t;";
		ps2 = conn.prepareStatement(sql2);
		ResultSet rs2 = ps.executeQuery();
		List<UserActivity> activities = new ArrayList<UserActivity>();
		while (rs2.next())
		{
			String activityType = rs2.getString("activityType");
			String userId = rs2.getString("userid");
			UserActivity ua = new UserActivity(activityType, userId);
			activities.add(ua);
		}
		
		//Open index file to write html document
		Path path = Paths.get(outputfile);
		try(BufferedWriter writer = Files.newBufferedWriter(path))
		{
			writer.write(one);
			writer.write(two);
			writer.write(head1);
			writer.write("<p>");
			for(UserActivity ua: activities)
			{
				writer.write(ua.toString());
			}
			writer.write("<p>");
			writer.write(head2);
			writer.write("<p>");
			for(String a: activityList)
			{
				writer.write(a);
			}
			writer.write("<p>");
			writer.write(three);
		}
		
		System.out.println("printLast20Activities");
	}
	
}
