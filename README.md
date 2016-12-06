### centos
This is a Demo for a Kinesis KPL producer, an archiving KCL consumer, a Processing KCL consumer, a Job Scheduler and a node js web  application*
#1. Create two ec2 instances.
#2. ssh into first instance
#3. clone the repository
#4. start the consumers 
mvn exec:java -Dexec.mainClass=com.tayo.centos.kcl1.ConsumerApp
mvn exec:java -Dexec.mainClass=com.tayo.centos.kcl2.ConsumerApp2
#5. ssh into the second instance
#6. clone this repo
#7. start the producer
mvn exec:java -Dexec.mainClass=com.tayo.centos.ProducerOne
#8. ssh into instance 1 again
start up the Scheduler
mvn exec:java -Dexec.mainClass=com.tayo.centos.scheduler.DashboardMonitor
#9. Start the nodejs server
sudo node server.js



