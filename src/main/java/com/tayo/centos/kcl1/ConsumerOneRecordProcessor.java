package com.tayo.centos.kcl1;


import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ThrottlingException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason;
import com.amazonaws.services.kinesis.model.Record;
import com.tayo.centos.ProducerOne;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by temitayo on 11/30/16.
 */
public class ConsumerOneRecordProcessor implements IRecordProcessor
{
    private static final Logger log = LoggerFactory.getLogger(ConsumerOneRecordProcessor.class);
    private String shardId;

    // Backoff and retry settings
    private static final long BACKOFF_TIME_IN_MILLIS = 3000L;
    private static final int NUM_RETRIES = 10;

    // Checkpoint about once a minute
    private static final long CHECKPOINT_INTERVAL_MILLIS = 60000L;
    private long nextCheckpointTimeInMillis;


    @Override
    public void initialize(String shardId)
    {
        log.info("Initializing record processor for shard :" + shardId);
        this.shardId = shardId;
    }

    @Override
    public void processRecords(List<Record> list, IRecordProcessorCheckpointer iRecordProcessorCheckpointer)
    {
        if(list.size() > 0)
        {
            log.info("Received record size is : " + list.size());
            S3ArchiverThread archiver = new S3ArchiverThread(list);
            Thread thread = new Thread(archiver);
            thread.start();

        }
        if (System.currentTimeMillis() > nextCheckpointTimeInMillis)
        {
            checkpoint(iRecordProcessorCheckpointer);
            nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
        }

    }

    @Override
    public void shutdown(IRecordProcessorCheckpointer iRecordProcessorCheckpointer, ShutdownReason shutdownReason)
    {
        log.info("Shutting down record processor for shard: " + shardId);
        // Important to checkpoint after reaching end of shard, so we can start processing data from child shards.
        if (shutdownReason == ShutdownReason.TERMINATE)
        {
            checkpoint(iRecordProcessorCheckpointer);
        }

    }

    private void checkpoint(IRecordProcessorCheckpointer checkpointer)
    {
        log.info("Checkpointing shard " + shardId);
        for (int i = 0; i < NUM_RETRIES; i++)
        {
            try
            {
                checkpointer.checkpoint();
                break;
            }
            catch (ShutdownException se)
            {
                // Ignore checkpoint if the processor instance has been shutdown (fail over).
                log.info("Caught shutdown exception, skipping checkpoint.", se);
                break;
            }
            catch (ThrottlingException e)
            {
                // Backoff and re-attempt checkpoint upon transient failures
                if (i >= (NUM_RETRIES - 1))
                {
                    log.error("Checkpoint failed after " + (i + 1) + "attempts.", e);
                    break;
                }
                else
                {
                    log.info("Transient issue when checkpointing - attempt " + (i + 1) + " of "
                            + NUM_RETRIES, e);
                }
            }
            catch (InvalidStateException e)
            {
                // This indicates an issue with the DynamoDB table (check for table, provisioned IOPS).
                log.error("Cannot save checkpoint to the DynamoDB table used by the Amazon Kinesis Client Library.", e);
                break;
            }
            try
            {
                Thread.sleep(BACKOFF_TIME_IN_MILLIS);
            }
            catch (InterruptedException e)
            {
                log.debug("Interrupted sleep", e);
            }
        }
    }
}
