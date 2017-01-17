package com.tayo.centos.scheduler;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tayo.centos.util.DbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tayo.centos.util.CentosUtils;

/*
* Job that will be executed periodically.
* Job contains top user activities and last activities on the site
* *
* Also (re-)creates the index file  that is loaded by the nodejs server
 */


public class ScheduledJob 
{
	private static final Logger log = LoggerFactory.getLogger(ScheduledJob.class);
	private static final String pattern = "yyyy-MM-dd HH:mm:ss ";
	static SimpleDateFormat format = new SimpleDateFormat(pattern);
	private static final String one = "<!DOCTYPE html><head><meta http-equiv=\"Refresh\" content=\"5\"></head><body>";
    private static final String two = "<h1>My First Heading</h1>";
    private static final String three = "</body></html>";
    private static final String head1 = "<p>Top Users and Top Activities<p>";
    private static final String head2 = "<p>Last Activities<p>";
    //private static final String outputFile = "index.html";


	
	static void printAllAnswers(Date timeToSend)throws Exception
	{
		log.info("Time to send is : " + format.format(timeToSend));
		String dateToCompute = format.format(timeToSend);
		Connection conn = DbManager.getConnection();
		java.sql.PreparedStatement ps = null;
		/*
		* Query to collect last activities
		 */
		String sql = "select activityType, activityTimestamp "+ 
				" from user_events where activityTimestamp < current_time() and activityTimestamp >" + "'"+dateToCompute+"'";

		
		log.info("SQL is " + sql);
		ps = conn.prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		List<ActivityTimestamp> activityList = new ArrayList<ActivityTimestamp>();
		while (rs.next())
		{
			String activityType = rs.getString("activityType");
			String activityTime = rs.getString("activityTimestamp");
			ActivityTimestamp activity = new ActivityTimestamp(activityType, activityTime);
			//log.info("activityType is " + activityType);
			activityList.add(activity);
		}	
		java.sql.PreparedStatement ps2 = null;
		/*
		*  Query to collect top 10 activities by top users in the last 7 minutes
		 */

		String sql2 = "select userid, activityType, count(activityType) as activityCount  from from user_events "+
				"where activityTimestamp < current_time() and activityTimestamp >" + "'"+dateToCompute+"'" + " group by 2 order by 3;";
		
		log.info("SQL is " + sql2);
		ps2 = conn.prepareStatement(sql2);
		ResultSet rs2 = ps2.executeQuery();
		List<UserActivity> activities = new ArrayList<UserActivity>();
		while (rs2.next())
		{
			String activityType = rs2.getString("activityType");
			log.info("activityType is " + activityType);
			String userId = rs2.getString("userid");
			String activityCount = rs2.getString("activityCount");
			UserActivity ua = new UserActivity(userId, activityType, activityCount);
			log.info("userId is " + userId);
			activities.add(ua);
		}
		String outputFile = CentosUtils.getProperties().getProperty("indexfile");
		//Open index file to write html document
		Path path = Paths.get(outputFile);
		try(BufferedWriter writer = Files.newBufferedWriter(path))
		{
			writer.write(one);
			writer.write(two);
			writer.write(head1);
			writer.write("<p>");
			writer.write("<p>");
			for(UserActivity ua: activities)
			{
				writer.write("<p>");
				writer.write(ua.toString()+"\n");
				log.info(ua.toString());
				writer.write("<p>");
			}
			writer.write("<p>");
			writer.write(head2);
			writer.write("<p>");
			for(ActivityTimestamp a: activityList)
			{
				writer.write("<p>");
				writer.write(a.toString()+"\n");
				writer.write("<p>");
				log.info(a.toString());
			}
			writer.write("<p>");
			writer.write(three);
			writer.flush();
			writer.close();
		}
		
		System.out.println("printed ...");
	}
	
}
