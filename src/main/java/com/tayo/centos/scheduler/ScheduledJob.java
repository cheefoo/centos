package com.tayo.centos.scheduler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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


    static void printAllAnswers(Date timeToSend) throws IOException
    {
        log.info("Time to send is : " + format.format(timeToSend));
        String dateToCompute = format.format(timeToSend);
        List<UserActivity> activities = null;
        List<ActivityTimestamp> activityList = null;
        java.sql.PreparedStatement ps = null;
        Connection conn = null;
        ResultSet rs = null;
        java.sql.PreparedStatement ps2 = null;
        ResultSet rs2 = null;
        try
        {
            conn = DbManager.getConnection();

        /*
		* Query to collect last activities
		 */
            String sql = "select activityType, activityTimestamp " +
                    " from user_events where activityTimestamp < current_time() and activityTimestamp >" + "'" + dateToCompute + "'" + " " + " limit 20";


            log.info("SQL is " + sql);
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            activityList = new ArrayList<ActivityTimestamp>();
            while (rs.next())
            {
                String activityType = rs.getString("activityType");
                String activityTime = rs.getString("activityTimestamp");
                ActivityTimestamp activity = new ActivityTimestamp(activityType, activityTime);
                //log.info("activityType is " + activityType);
                activityList.add(activity);
            }


		/*
		*  Query to collect top 10 activities by top users in the last 7 minutes
		 */

            String sql2 = "select userid, activityType, count(activityType) as activityCount  from user_events " +
                    "where activityTimestamp < current_time() and activityTimestamp >" + "'" + dateToCompute + "'" + " group by 2 order by 3 desc;";

            log.info("SQL is " + sql2);
            ps2 = conn.prepareStatement(sql2);
            rs2 = ps2.executeQuery();
            activities = new ArrayList<UserActivity>();
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

        }
        catch (SQLException e)
        {
            log.error(e.toString());
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                rs.close();
                ps.close();
                rs2.close();
                ps2.close();
                conn.close();
            }
            catch(Exception e)
            {
                log.error("Exception while closing DB resources " + e.toString());
            }

        }

        String outputFile = CentosUtils.getProperties().getProperty("indexfile");
        //Open index file to write html document
        Path path = Paths.get(outputFile);
        try (BufferedWriter writer = Files.newBufferedWriter(path))
        {
            writer.write(one);
            writer.write(two);
            writer.write(head1);
            writer.write("<p>");
            writer.write("<p>");
            for (UserActivity ua : activities)
            {
                writer.write("<p>");
                writer.write(ua.toString() + "\n");
                log.info(ua.toString());
                writer.write("<p>");
            }
            writer.write("<p>");
            writer.write(head2);
            writer.write("<p>");
            for (ActivityTimestamp a : activityList)
            {
                writer.write("<p>");
                writer.write(a.toString() + "\n");
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
