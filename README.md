### centos
This is a Demo for a Kinesis KPL producer, an archiving KCL consumer, a Processing KCL consumer, a Job Scheduler and a node js web  application. The archiving consumer works by archiving all records to S3 while the processing consumer works by de-deuplicating the records and sending it to an RDS which is used to feed a nodejs dashboard.
#Pre-requisites: AWS EC2 instance, AWS RDS, AWS S3, AWS Kinesis
#1. Create two ec2 instances.
#2. ssh into first instance
#3. clone the repository
#4. start the consumers 
# mvn exec:java -Dexec.mainClass=com.tayo.centos.kcl1.ConsumerApp
# mvn exec:java -Dexec.mainClass=com.tayo.centos.kcl2.ConsumerApp2
#5. ssh into the second instance
#6. clone this repo
#7. start the producer
mvn exec:java -Dexec.mainClass=com.tayo.centos.ProducerOne
#8. ssh into instance 1 again
#9 start up the Scheduler
mvn exec:java -Dexec.mainClass=com.tayo.centos.scheduler.DashboardMonitor
#10. Start the nodejs server
sudo node server.js



