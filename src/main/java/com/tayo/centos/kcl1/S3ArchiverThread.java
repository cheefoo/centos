package com.tayo.centos.kcl1;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.model.Record;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.tayo.centos.util.CentosUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by temitayo on 11/30/16.
 */
public class S3ArchiverThread implements Runnable
{
    private static String BUCKET_NAME = "temitayo-centos";
    private static final String PREFIX = "centos";
    private static final String fileName = "centos_consumer";
    private static final Logger log = LoggerFactory.getLogger(S3ArchiverThread.class);
    private List<Record> recordList;
    private static final CharsetDecoder DECODER = Charset.forName("UTF-8").newDecoder();
    AmazonS3Client s3Client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain()).withRegion(Region.getRegion(Regions.US_WEST_2));

    public S3ArchiverThread(List<Record> recordList)
    {
        this.recordList = recordList;
    }

    public List<Record> getRecordList()
    {
        return recordList;
    }



    public String writeRecordsToFile(List<Record> recordList)
    {

        //File file = new File(fileName+System.currentTimeMillis());
        Path path = Paths.get(fileName+System.currentTimeMillis());
        try(BufferedWriter writer = Files.newBufferedWriter(path))
        {

            for(Record record: recordList)
            {
                writer.write(DECODER.decode(record.getData()).toString());  //decodes and writes lines to file
            }

        }
        catch(Exception e)
        {
            log.error("error on writing to file for " + path.toString() + "Error : " +  e.toString());
        }


        return path.toString();
    }

    //This is a test
    public static void main(String[] args)
    {

            //File file = new File(fileName+System.currentTimeMillis());
            Path path = Paths.get("/Users/temitayo/Desktop/archive"+System.currentTimeMillis());
            try(BufferedWriter writer = Files.newBufferedWriter(path))
            {

                System.out.println("Writing now...");
                writer.write("This is Le Clue ");
                writer.write("Python\n");
                writer.write("Clojure\n");
                writer.write("Scala\n");
                writer.write("JavaScript\n");

            }
            catch(Exception e)
            {
                System.out.println(e.toString());
            }

        AmazonS3Client s3Client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());
        s3Client.setRegion(Region.getRegion(Regions.US_WEST_2));
        try
        {
            System.out.println("Uploading a new object to S3 from a file\n");
            File file = new File(path.toString());
            System.out.println("Filename is " + path.toString());
            s3Client.putObject(new PutObjectRequest(BUCKET_NAME, file.getName(), file));

        }
        catch(AmazonServiceException ase)
        {
            System.out.println("Uploading a new object to S3 from a file\n");
        }

    }

    private void uploadS3File(String path)
    {
    	
        try
        {
        	String bucketName = CentosUtils.getProperties().getProperty("s3bucket");
            System.out.println("Uploading a new object to S3 from a file\n");
            File file = new File(path.toString());
            log.info("Filename is " + path.toString());
            s3Client.putObject(new PutObjectRequest(bucketName, file.getName(), file));

        }
        catch(AmazonServiceException ase)
        {
            log.error("Failed uploading file to S3 : " + path);
        }
        catch(IOException ase)
        {
            log.error("Failed uploading file to S3 : " + ase.toString());
        }
    }

    @Override
    public void run()
    {
        String path = this.writeRecordsToFile(this.getRecordList());
        uploadS3File(path);

    }
}
