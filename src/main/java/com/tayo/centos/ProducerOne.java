package com.tayo.centos;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.producer.*;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.tayo.centos.util.CentosUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


/**
 * Created by temitayo on 11/3/16.
 */
public class ProducerOne
{
    private static final Logger log = LoggerFactory.getLogger(ProducerOne.class);
    private static long id = 1;  //Id used to create unique objects to be sent to Kinesis



    private static KinesisProducer getKinesisProducer() throws IOException
    {
    	String kpltempdir = CentosUtils.getProperties().getProperty("kpltempdir");
        KinesisProducerConfiguration config = KinesisProducerConfiguration.fromPropertiesFile("default_config.properties");
        config.setCredentialsProvider(new DefaultAWSCredentialsProviderChain());
        config.setTempDirectory(kpltempdir);
        return new KinesisProducer(config);
    }


    public static void main (String [] args) throws IOException
    {

        String streamName = CentosUtils.getProperties().getProperty("streamname");
        KinesisProducer producer = getKinesisProducer();

        final FutureCallback<UserRecordResult> callback = new FutureCallback<UserRecordResult>()
        {
            @Override
            public void onFailure(Throwable t)
            {

                if (t instanceof UserRecordFailedException)
                {
                   UserRecordFailedException e =
                                (UserRecordFailedException) t;
                        UserRecordResult result = e.getResult();

                        String errorList =
                                StringUtils.join(result.getAttempts().stream()
                                        .map(a -> String.format(
                                                "Delay after prev attempt: %d ms, "
                                                        + "Duration: %d ms, Code: %s, "
                                                        + "Message: %s",
                                                a.getDelay(), a.getDuration(),
                                                a.getErrorCode(),
                                                a.getErrorMessage()))
                                        .collect(Collectors.toList()), "n");

                        log.error(String.format("Record failed to put, attempts:n%s ",  errorList));

                }
                log.error("Exception during put", t);

            }

            @Override
            public void onSuccess(UserRecordResult userRecordResult)
            {

                long totalTime = userRecordResult.getAttempts().stream()
                        .mapToLong(a -> a.getDelay() + a.getDuration())
                        .sum();
                                 log.info(String.format(
                            "Succesfully put record,  "
                                    + " sequenceNumber=%s, "
                                    + "shardId=%s, took %d attempts, "
                                    + "totalling %s ms",
                            userRecordResult.getSequenceNumber(),
                            userRecordResult.getShardId(), userRecordResult.getAttempts().size(),
                            totalTime));

            }
        };

        String fileLocationToProcess = CentosUtils.getProperties().getProperty("filelocation");//Retrieves file location to process
        log.info("File Location is " + fileLocationToProcess);

        while(true)
        {
            List<String>filesToProcess =   getFilesToSend(new File(fileLocationToProcess));

            if(filesToProcess.size() != 0)
            {
                for(String file: filesToProcess)
                {
                    List<UserDAO> userList = null;
                    try
                    {
                        userList = getDataObjects(file); //gets the user objects from file into a list
                        log.info("Obtained user event list : " + userList.size());
                        log.info("About to delete completed file : " + file);
                        if(deleteReadFile(file))
                        {
                           log.info("file +  " + file + " "+ "deleted successfully");
                        }
                        else
                        {
                            log.error("File delete for " + file + " " + "failed");
                        }

                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    for(UserDAO user: userList)
                    {
                        try
                        {
                            ByteBuffer data = ByteBuffer.wrap(String.format(user.toString()).getBytes("UTF-8"));
                            ListenableFuture<UserRecordResult> f = producer.addUserRecord(streamName, randomPartitionKey(), data);
                            Thread.sleep(1);
                            //producer.getOutstandingRecordsCount()
                            Futures.addCallback(f, callback);

                        }
                        catch(Exception e)
                        {
                            log.error(e.getLocalizedMessage());
                            e.printStackTrace();
                        }
                        log.info(user.toString());
                    }

                }

            }


        }


    }

    public static String randomPartitionKey()
    {
        return new BigInteger(128, new Random()).toString(10);
    }

    /*
    * Deletes already read files, alternatively the files can be moved to a new location
    * Care should be taken not to generate the files in the same directory which the KPL is watching, it may cause ugly scenarios
     */
    private static boolean deleteReadFile(String  fileName)
    {
        //deleting file
        File file = new File(fileName);
        if(file.delete())
        {
            log.info(file.getName() + " has been deleted after reading into list");
            return true;
        }
        else
        {
            log.error("Unable to delete file "+ file.getName());

        }
        return false;
    }

    private static List<UserDAO> getDataObjects(String fileName) throws Exception
    {
        List<UserDAO> userObjectList= new ArrayList<UserDAO>();
        try
        {
            // read the json file
            FileReader reader = new FileReader(fileName);
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = (JSONArray)jsonParser.parse(reader);

            for(Object obj : jsonArray)
            {
                JSONObject jsonObject = (JSONObject) obj;
                //String id = (String)jsonObject.get("id");
                String userId = (String) jsonObject.get("userid");
                String fullName = (String) jsonObject.get("fullName");
                String gender = (String) jsonObject.get("gender");
                String relationshipStatus = (String) jsonObject.get("relationshipStatus");
                String activityTimestamp = (String) jsonObject.get("activityTimestamp");
                String activityType = (String) jsonObject.get("activityType");
                String activityMetadata = (String) jsonObject.get("ActivityMetadata");
                UserDAO user = new UserDAO(id++, userId, fullName, gender, relationshipStatus,activityTimestamp,activityType, activityMetadata );
                userObjectList.add(user);
            }

        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        catch (ParseException ex)
        {
            ex.printStackTrace();
        }

        log.info("Finished reading all objects from file");

        return userObjectList;
    }


    /*
    Retrieves all files to be processed
     */
    private static List<String> getFilesToSend(final File folder)
    {
        List<String> filesToSend = new ArrayList<String>();
        for (final File fileEntry : folder.listFiles())
        {
            if (fileEntry.isDirectory())
            {
                log.info("File is a directory.... skipping");
            }
            else
            {
                filesToSend.add(fileEntry.getAbsolutePath());
            }
        }

        return filesToSend;
    }

}
