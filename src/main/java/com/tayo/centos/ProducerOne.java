package com.tayo.centos;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.producer.*;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
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
    private static final String filePath = "/Users/temitayo/workspace/CentosProducer/MOCK_DATA.json";
    private static final String DELIM = ",";
    public static final String STREAM_NAME = "CentosStream";
    public static final String KPL_TMP_DIR = "/Users/temitayo/workspace/CentosProducer/bin";
    private static final Random RANDOM = new Random();
    private List<UserDAO> userList;

    public void setUserList(List<UserDAO> userList)
    {
        this.userList = userList;
    }

    public List<UserDAO> getUserList()
    {
        return userList;
    }

    public static KinesisProducer getKinesisProducer()
    {
        KinesisProducerConfiguration config = KinesisProducerConfiguration.fromPropertiesFile("default_config.properties");
        config.setCredentialsProvider(new DefaultAWSCredentialsProviderChain());
        config.setTempDirectory(KPL_TMP_DIR);
        return new KinesisProducer(config);
    }


    public static void main (String [] args)
    {
        //Read in user list from log file and set in
        List<UserDAO> userList = getDataObjects();
        ProducerOne one = new ProducerOne();
        one.setUserList(userList);

        KinesisProducer producer = getKinesisProducer();


        final List failedPutList = new ArrayList();
        final FutureCallback<UserRecordResult> callback = new FutureCallback<UserRecordResult>()
        {
            @Override
            public void onFailure(Throwable t) {

                if (t instanceof UserRecordFailedException)
                {
                   /* Attempt last = Iterables.getLast(
                            ((UserRecordFailedException) t).getResult().getAttempts());
                    log.error(String.format(
                            "Record failed to put - %s : %s : %s" ,
                            last.getErrorCode(), last.getErrorMessage(), ((UserRecordFailedException) t).getResult().getAttempts().size()));
                    //failedPutList.add(((UserRecordFailedException) t).getResult().getAttempts().size());*/
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
                /*log.info(String.format(
                        "Record Successfully put  - %s : %s",
                        result.getShardId(), result.getSequenceNumber()));;*/
                long totalTime = userRecordResult.getAttempts().stream()
                        .mapToLong(a -> a.getDelay() + a.getDuration())
                        .sum();
                // Only log with a small probability, otherwise it'll be very
                // spammy
                if (RANDOM.nextDouble() < 1e-5)
                {
                    log.info(String.format(
                            "Succesfully put record,  "
                                    + " sequenceNumber=%s, "
                                    + "shardId=%s, took %d attempts, "
                                    + "totalling %s ms",
                            userRecordResult.getSequenceNumber(),
                            userRecordResult.getShardId(), userRecordResult.getAttempts().size(),
                            totalTime));
                }
            }
        };

        while(true)
        {
            for(UserDAO user: userList)
            {
                try
                {
                    ByteBuffer data = ByteBuffer.wrap(String.format(user.toString()).getBytes("UTF-8"));
                    ListenableFuture<UserRecordResult> f = producer.addUserRecord(STREAM_NAME, randomPartitionKey(), data);
                     Thread.sleep(1);
                    //producer.getOutstandingRecordsCount()
                    Futures.addCallback(f, callback);

                }
                catch(Exception e)
                {
                    log.error(e.getLocalizedMessage());
                    e.printStackTrace();
                }


            }
            userList = one.getUserList();
        }


    }

    public static String randomPartitionKey()
    {
        return new BigInteger(128, new Random()).toString(10);
    }
    public static List<UserDAO> getDataObjects()
    {
        List<UserDAO> userObjectList= new ArrayList<UserDAO>();
        try
        {
            // read the json file
            FileReader reader = new FileReader(filePath);
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = (JSONArray)jsonParser.parse(reader);

            for(Object obj : jsonArray)
            {
                JSONObject jsonObject = (JSONObject) obj;
                String id = (String)jsonObject.get("id");
                String userId = (String) jsonObject.get("userId");
                String fullName = (String) jsonObject.get("fullName");
                String gender = (String) jsonObject.get("gender");
                String relationshipStatus = (String) jsonObject.get("relationshipStatus");
                String activityTimestamp = (String) jsonObject.get("activityTimestamp");
                String activityType = (String) jsonObject.get("activityType");
                String activityMetadata = (String) jsonObject.get("activityMetadata");
                UserDAO user = new UserDAO(id, userId, fullName, gender, relationshipStatus,activityTimestamp,activityType, activityMetadata );
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

}
