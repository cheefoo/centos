##CENTOS

This is a Demo for a fictitious customer who intends to use Kinesis Producer Library (KPL) to send records to a kinesis stream, consume from the stream using the Kinesis Client Library (KCL) and archive to an Amazon S3 bucket. A second consumer using KCL to update a web application realtime. 

The applications consists of 4 components as follows:

1. A producer application
2. An Archiving Consumer application
3. A De-duplicating Dashboard consumer application
4. A Dashboard Job Scheduler
5. A script to generate data in the format required by the application(s)

#Requirements:
1. A kinesis Stream
2. Java 8
3. An RDS mysql database
4. S3 bucket
5. EC2 instance(s)
6. Kinesis IAM role to be attached to ec2 instances (http://docs.aws.amazon.com/streams/latest/dev/controlling-access.html)

#Getting Started
1. Clone the repository into your instance
2. Edit the db.properties file in resources folder and add your values for the respective properties
3. Edit the KPL default_config.properties and add your values (pay attention to the region in this config)
4. ssh into one of instance and start the Archiving Consumer --mvn exec:java -Dexec.mainClass=com.tayo.centos.kcl1.ConsumerApp
5. start the dashboard consumer mvn exec:java -Dexec.mainClass=com.tayo.centos.kcl2.ConsumerApp2
6. run the python script (script folder) to generate data -- python generateJson.py
6. start the producer -- mvn exec:java -Dexec.mainClass=com.tayo.centos.ProducerOne
7. start up the Job Scheduler -- mvn exec:java -Dexec.mainClass=com.tayo.centos.scheduler.DashboardMonitor
8. start the nodejs server (webapps folder) -- node server.js




