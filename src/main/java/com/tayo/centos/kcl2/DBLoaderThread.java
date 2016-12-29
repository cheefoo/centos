package com.tayo.centos.kcl2;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kinesis.model.Record;

public class DBLoaderThread implements Runnable
{
	private static final Logger log = LoggerFactory.getLogger(DBLoaderThread.class);
    private List<Record> recordList;
    private static final CharsetDecoder DECODER = Charset.forName("UTF-8").newDecoder();

    public DBLoaderThread(List<Record> recordList)
    {
    	super();
        this.recordList = recordList;
    }

    public List<Record> getRecordList()
    {
        return recordList;
    }

    public void setRecordList(List<Record> recordList)
    {
        this.recordList = recordList;
    }

    @Override
    public void run()
    {
    	try 
		{
			Connection conn = getConnection();
			log.info("Connected to DB! smiles..." + conn.toString());
			persistRecords(conn, this.getRecordList());

		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
    }

    /*
    * This is a test
     */
    public static void main(String[] args)
    {
        try
        {
            Connection conn = getConnection();
            log.info("Connected to DB! smiles..." + conn.toString());
            List<Record> recordList = new ArrayList<Record>();

            Record record1 = new Record();
            Record record2 = new Record();
            Record record3 = new Record();
            record1.setData(ByteBuffer.wrap(String.format("1,jcarpenter0@cmu.edu,Temitayo Carpenter,Male,In Relationship,2016-11-25 11:52:30,TopicViewed,https://shutterfly.com/adipiscing/elit.xml?pretium=et&quis=ultrices&lectus=posuere&suspendisse=cubilia&potenti=curae&in=duis&eleifend=faucibus&quam=accumsan&a=odio&odio=curabitur&in=convallis&hac=duis&habitasse=consequat&platea=dui&dictumst=nec&maecenas=nisi&ut=volutpat&massa=eleifend&quis=donec&augue=ut&luctus=dolor&tincidunt=morbi&nulla=vel&mollis=lectus&molestie=in&lorem=quam&quisque=fringilla&ut=rhoncus&erat=mauris&curabitur=enim&gravida=leo&nisi=rhoncus&at=sed&nibh=vestibulum&in=sit&hac=amet&habitasse=cursus&platea=id&dictumst=turpis&aliquam=integer&augue=aliquet&quam=massa&sollicitudin=id&vitae=lobortis&consectetuer=convallis&eget=tortor&rutrum=risus&at=dapibus&lorem=augue&integer=vel&tincidunt=accumsan&ante=tellus&vel=nisi&ipsum=eu").getBytes()));
            record2.setData(ByteBuffer.wrap(String.format("2,mgilbert1@multiply.com,Matthew Gilbert,Male,Single,2016-11-23 06:41:28,CommentLiked,https://ow.ly/id/nulla/ultrices/aliquet.aspx?donec=nisi&pharetra=at&magna=nibh&vestibulum=in&aliquet=hac&ultrices=habitasse&erat=platea&tortor=dictumst&sollicitudin=aliquam&mi=augue&sit=quam&amet=sollicitudin&lobortis=vitae&sapien=consectetuer&sapien=eget&non=rutrum&mi=at&integer=lorem&ac=integer&neque=tincidunt&duis=ante&bibendum=vel&morbi=ipsum&non=praesent&quam=blandit&nec=lacinia&dui=erat&luctus=vestibulum&rutrum=sed&nulla=magna&tellus=at&in=nunc&sagittis=commodo&dui=placerat&vel=praesent&nisl=blandit&duis=nam&ac=nulla&nibh=integer&fusce=pede&lacus=justo&purus=lacinia&aliquet=eget&at=tincidunt&feugiat=eget&non=tempus&pretium=vel&quis=pede&lectus=morbi&suspendisse=porttitor&potenti=lorem&in=id&eleifend=ligula&quam=suspendisse&a=ornare&odio=consequat&in=lectus&hac=in&habitasse=est&platea=risus&dictumst=auctor&maecenas=sed&ut=tristique&massa=in&quis=tempus&augue=sit&luctus=amet&tincidunt=sem&nulla=fusce&mollis=consequat&molestie=nulla&lorem=nisl&quisque=nunc&ut=nisl&erat=duis&curabitur=bibendum&gravida=felis&nisi=sed&at=interdum&nibh=venenatis&in=turpis&hac=enim&habitasse=blandit&platea=mi&dictumst=in&aliquam=porttitor&augue=pede&quam=justo&sollicitudin=eu&vitae=massa&consectetuer=donec").getBytes()));
            record3.setData(ByteBuffer.wrap(String.format("3,rstanley2@ow.ly,Ralph Stanley,Male,In Relationship,2016-11-30 10:02:23,CommentRemoved,http://blogs.com/quisque.jsp?erat=cursus&eros=vestibulum&viverra=proin&eget=eu&congue=mi&eget=nulla&semper=ac&rutrum=enim&nulla=in&nunc=tempor&purus=turpis&phasellus=nec&in=euismod&felis=scelerisque&donec=quam&semper=turpis&sapien=adipiscing&a=lorem&libero=vitae&nam=mattis&dui=nibh&proin=ligula&leo=nec&odio=sem&porttitor=duis&id=aliquam&consequat=convallis&in=nunc&consequat=proin&ut=at&nulla=turpis&sed=a&accumsan=pede&felis=posuere&ut=nonummy&at=integer&dolor=non&quis=velit&odio=donec&consequat=diam&varius=neque&integer=vestibulum&ac=eget&leo=vulputate&pellentesque=ut&ultrices=ultrices&mattis=vel&odio=augue&donec=vestibulum&vitae=ante&nisi=ipsum&nam=primis&ultrices=in&libero=faucibus&non=orci&mattis=luctus&pulvinar=et&nulla=ultrices&pede=posuere&ullamcorper=cubilia&augue=curae&a=donec&suscipit=pharetra&nulla=magna&elit=vestibulum&ac=aliquet&nulla=ultrices&sed=erat&vel=tortor&enim=sollicitudin&sit=mi&amet=sit&nunc=amet&viverra=lobortis&dapibus=sapien&nulla=sapien&suscipit=non&ligula=mi&in=integer&lacus=ac&curabitur=neque&at=duis&ipsum=bibendum&ac=morbi&tellus=non&semper=quam&interdum=nec&mauris=dui&ullamcorper=luctus&purus=rutrum&sit=nulla&amet=tellus&nulla=in&quisque=sagittis&arcu=dui&libero=vel&rutrum=nisl&ac=duis").getBytes()));
            recordList.add(record1);
            recordList.add(record2);
            recordList.add(record3);

            persistRecords(conn, recordList);


        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private static void persistRecords(Connection conn, List<Record> recordsList) throws SQLException
    {

        PreparedStatement stmt = null;
        String id = null;
        String userId = null;
        String fullName = null;
        String gender = null;
        String relationshipStatus = null;
        String activityTimestamp = null;
        String activityType = null;
        String activityMetadata = null;

        log.info("In persistRecords");

       
            int k = 1;  // keep track of items added to batch
            String sql = "insert into event.user_events (id, userid ,fullName, gender,relationshipStatus,  activityTimestamp, activityType , activityMetadata) "
            		+ "       values (?, ?, ?, ?, ?, ?, ?, ?)ON DUPLICATE KEY UPDATE "
            		+ "userid=?,fullname=?,gender=?,relationshipstatus=?,"
            		+ "activitytimestamp=?,activitytype=?,activitymetadata=? ;";
            stmt = conn.prepareStatement(sql);
            for(Record record: recordsList)
            {
                try
                {
                    String recordValue =  DECODER.decode(record.getData()).toString();  ///decodes the value obtained
                    log.info("RECORD VALUE OBTAINED is : " + recordValue);
                    String[] rowValues = recordValue.toString().split(",");
                    id = rowValues[0];
                    userId = rowValues[1];
                    fullName = rowValues[2];
                    gender = rowValues[3];
                    relationshipStatus = rowValues[4];
                    activityTimestamp = rowValues[5];
                    activityType = rowValues[6];
                    activityMetadata = rowValues[7];
                    log.info("id = "+id + ",userId ="  +userId +",fullName =" + fullName + ",gender = "+gender + ",relationshipStatus= "
                                +relationshipStatus +",activityTimestamp =" + activityTimestamp  +",activityType =" + activityType + ",activityMetadata =" + activityMetadata);
                       
                        stmt.setLong(1, Long.valueOf(id));
                        stmt.setString(2, userId);
                        stmt.setString(3, fullName);
                        stmt.setString(4, gender);
                        stmt.setString(5, relationshipStatus);
                        stmt.setTimestamp(6, Timestamp.valueOf(activityTimestamp));
                        stmt.setString(7, activityType);
                        stmt.setString(8, "");
                        stmt.setString(9, userId);
                        stmt.setString(10, fullName);
                        stmt.setString(11, gender);
                        stmt.setString(12, relationshipStatus);
                        stmt.setTimestamp(13, Timestamp.valueOf(activityTimestamp));
                        stmt.setString(14, activityType);
                        stmt.setString(15, "");
                        stmt.addBatch();
                        log.info("Batch " + k + " added successfully" );
                        k++;
                }
                
                catch (SQLException e)
                {
                    log.error("Failed adding statement to batch " + e.toString());
                }
                catch (CharacterCodingException e)
                {
                    log.error("Record decoding error " + e.toString());
                }
            }
            try
            {
                int[] batchExec = stmt.executeBatch();
               // int batchExec = stmt.executeUpdate();

                log.info("Batch executed successfully for batchSize : " + batchExec.length);
               // log.info("Batch executed successfully for batchSize : " + batchExec);
                stmt.close();
            }
            catch (SQLException e)
            {
                log.error("Failed Batch Execution " + e.toString());
            }
            finally
            {
                try
                {
                    if (stmt != null)
                        stmt.close();
                }
                catch (Exception ex)
                {
                    log.error("Exception thrown in finally when closing connection " + ex.toString());
                }
            }  
        
    }

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

}
