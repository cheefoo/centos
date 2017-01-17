package com.tayo.centos.kcl1;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.tayo.centos.util.CentosUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Created by temitayo on 11/30/16.
 */
public class ConsumerApp
{

    private static final InitialPositionInStream INITIAL_POSITION_IN_STREAM = InitialPositionInStream.TRIM_HORIZON;
    private static AWSCredentialsProvider credentialsProvider;
    private static final Logger log = LoggerFactory.getLogger(ConsumerApp.class);



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
        String streamName = CentosUtils.getProperties().getProperty("streamname");
        String appName = CentosUtils.getProperties().getProperty("kcl_archiver_name");
        String region = CentosUtils.getProperties().getProperty("region");
        KinesisClientLibConfiguration kinesisClientLibConfiguration =
                new KinesisClientLibConfiguration(appName,
                        streamName,
                        credentialsProvider,
                        workerId);
        kinesisClientLibConfiguration.withInitialPositionInStream(INITIAL_POSITION_IN_STREAM).withRegionName(region);

        IRecordProcessorFactory recordProcessorFactory = new ConsumerOneRecordProcessorFactory();
        Worker worker = new Worker(recordProcessorFactory, kinesisClientLibConfiguration);

       log.info("Started KCL Worker process for Stream " +  streamName + " " + "with workerId " +  workerId);

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
    
}
