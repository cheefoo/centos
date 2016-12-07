package com.tayo.centos;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.producer.*;
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
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Created by temitayo on 11/3/16.
 */
public class ProducerOne
{
    private static final Logger log = LoggerFactory.getLogger(ProducerOne.class);
    private static final String DELIM = ",";
    public static final String STREAM_NAME = "centosstream";
    //public static final String KPL_TMP_DIR = "/Users/temitayo/workspace/centos";
    private static final Random RANDOM = new Random();
    private List<UserDAO> userList;
    //private final WatchService watcher;
    private static long id = 0;

    public void setUserList(List<UserDAO> userList)
    {
        this.userList = userList;
    }

    public List<UserDAO> getUserList()
    {
        return userList;
    }

    public static KinesisProducer getKinesisProducer() throws IOException
    {
    	String kpltempdir = getKPLTempDir();
        KinesisProducerConfiguration config = KinesisProducerConfiguration.fromPropertiesFile("default_config.properties");
        config.setCredentialsProvider(new DefaultAWSCredentialsProviderChain());
        config.setTempDirectory(kpltempdir);
        return new KinesisProducer(config);
    }


    public static void main (String [] args) throws IOException
    {
        //Read in user list from log file and set in
        //List<UserDAO> userList = getDataObjects();
        ProducerOne one = new ProducerOne();
        //one.setUserList(userList);
        String streamName = getStreamNameProps();
        KinesisProducer producer = getKinesisProducer();


        final List failedPutList = new ArrayList();
        final FutureCallback<UserRecordResult> callback = new FutureCallback<UserRecordResult>()
        {
            @Override
            public void onFailure(Throwable t) {

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
                // Only log with a small probability, otherwise it'll be very
                // spammy
               /* if (RANDOM.nextDouble() < 1e-5)
                {*/
                    log.info(String.format(
                            "Succesfully put record,  "
                                    + " sequenceNumber=%s, "
                                    + "shardId=%s, took %d attempts, "
                                    + "totalling %s ms",
                            userRecordResult.getSequenceNumber(),
                            userRecordResult.getShardId(), userRecordResult.getAttempts().size(),
                            totalTime));
               /* }*/
            }
        };
        String fileLocationToProcess = getFileLocationProps();
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
                        userList = getDataObjects(file);
                        log.info("Obtained user event list : " + userList.size());
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
        //deleting file
        File file = new File(fileName);
       /* if(file.delete())
        {
            log.info(file.getName() + " has been deleted after reading into list");
        }
        else
        {
            log.error("Unable to delete file "+ file.getName());
        }*/
        return userObjectList;
    }



    private static String getFileToProcess() throws IOException
    {
        Path directory = Paths.get(getFileLocationProps());
        Path child = null;
        WatchService watcher = FileSystems.getDefault().newWatchService();
        directory.register(watcher, ENTRY_CREATE);
        for(;;)
        {
            // wait for key to be signaled
            WatchKey key = null;
            try
            {
                key = watcher.take();
            }
            catch(InterruptedException x)
            {
                log.error(x.toString());
                System.exit(1);
            }
            for(WatchEvent<?> event: key.pollEvents())
            {
                WatchEvent.Kind kind = event.kind();

                if (kind == OVERFLOW)
                {
                    continue;
                }
                WatchEvent<Path> ev = (WatchEvent<Path>)event;
                Path filename = ev.context();
                //Verify that the new file is a text file.
                try
                {
                    child = directory.resolve(filename);
                    if (!Files.probeContentType(child).equals("text/plain"))
                    {
                        System.err.format("New file '%s' is not a plain text file.%n", filename);
                        continue;
                    }
                }
                catch (IOException x)
                {
                    log.error(x.toString());
                    continue;
                }

            }

            //Reset the key -- this step is critical if you want to receive
            //further watch events. If the key is no longer valid, the directory
            //is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid)
            {
                break;
            }

        }

        return child.toString();

    }

    public static List<String> getFilesToSend(final File folder)
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

    public static String getFileLocationProps() throws IOException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input =  classLoader.getResourceAsStream("db.properties");
        java.util.Properties prop = new Properties();
        prop.load(input);

        return prop.getProperty("filelocation");

    }

    public static String getStreamNameProps() throws IOException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("db.properties");
        java.util.Properties prop = new Properties();
        prop.load(input);

        return prop.getProperty("streamname");

    }
    
    public static String getKPLTempDir() throws IOException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input =  classLoader.getResourceAsStream("db.properties");
        java.util.Properties prop = new Properties();
        prop.load(input);

        return prop.getProperty("kpltempdir");

    }

}
