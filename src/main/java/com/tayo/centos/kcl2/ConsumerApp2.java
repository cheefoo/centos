package com.tayo.centos.kcl2;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.tayo.centos.ProducerOne;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by temitayo on 12/1/16.
 */
public class ConsumerApp2
{
    private static final String KCL_APP_NAME = "CentosDashboardConsumer";
    private static final InitialPositionInStream INITIAL_POSITION_IN_STREAM = InitialPositionInStream.TRIM_HORIZON;
    private static AWSCredentialsProvider credentialsProvider;
    private static final Logger log = LoggerFactory.getLogger(ConsumerApp2.class);
   

    private static void initialize()
    {
        java.security.Security.setProperty("networkaddress.cache.ttl", "60");
        credentialsProvider = new DefaultAWSCredentialsProviderChain();
        try
        {
            credentialsProvider.getCredentials();
        }
        catch(Exception e)
        {
            throw new AmazonClientException("Cannot find credentials");
        }

    }

    public static void main(String[] args) throws Exception
    {
        initialize();

        String workerId = InetAddress.getLocalHost().getCanonicalHostName() + ":" + UUID.randomUUID();
        String streamName = getStreamNameProps();
        KinesisClientLibConfiguration kinesisClientLibConfiguration =
                new KinesisClientLibConfiguration(KCL_APP_NAME,
                        streamName,
                        credentialsProvider,
                        workerId);
        kinesisClientLibConfiguration.withInitialPositionInStream(INITIAL_POSITION_IN_STREAM).withRegionName("us-west-2").withMaxRecords(20);

        IRecordProcessorFactory recordProcessorFactory = new ConsumerTwoRecordProcessorFactory();
        Worker worker = new Worker(recordProcessorFactory, kinesisClientLibConfiguration);

        log.info("Started KCL Worker process for Stream " +  ProducerOne.STREAM_NAME + " " + "with workerId " +  workerId);

        int exitCode = 0;
        try
        {
            worker.run();
        }
        catch (Throwable t)
        {
            System.err.println("Caught throwable while processing data.");
            t.printStackTrace();
            exitCode = 1;
        }
        System.exit(exitCode);
    }
    
    public static String getStreamNameProps() throws IOException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream input = classLoader.getResourceAsStream("db.properties");
        java.util.Properties prop = new Properties();
        prop.load(input);

        return prop.getProperty("streamname");

    }

}
