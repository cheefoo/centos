**incomplete WIP**


##De-duplication and strict ordering Kinesis KCL example

This is an end-to-end [AWS Kinesis Streams](https://aws.amazon.com/kinesis/streams/) processing example using the [AWS Kinesis Producer Library (KPL)](http://docs.aws.amazon.com/streams/latest/dev/developing-producers-with-kpl.html) to send records to a kinesis stream, consume from the stream using the [AWS Kinesis Client Library (KCL)](http://docs.aws.amazon.com/streams/latest/dev/developing-consumers-with-kcl.html) and archive data to an [Amazon S3](http://docs.aws.amazon.com/AmazonS3/latest/dev/Welcome.html) bucket. Additionally, a second KCL consumer provides realtime data updates to a web front-end. 

The application consists of 5 components:

1. A script to generate data in the format required by the application(s)
2. A Kinesis producer application
3. A De-duplicating Dashboard KCL application
4. An S3 Archiving KCL application 
5. A Dashboard Job Scheduler 

##Architecture Diagrams:
![alt tag](https://github.com/cheefoo/centos/blob/master/Archie1.png)

###Architecture Diagram II:
![alt tag](https://github.com/cheefoo/centos/blob/master/Archie2.png)

###Requirements:
1. An Amazon Web Services [Account](https://aws.amazon.com/free/?sc_channel=PS&sc_campaign=acquisition_ZA&sc_publisher=google&sc_medium=cloud_computing_b&sc_content=aws_account_e&sc_detail=aws%20account&sc_category=cloud_computing&sc_segment=77706639422&sc_matchtype=e&sc_country=ZA&s_kwcid=AL!4422!3!77706639422!e!!g!!aws%20account&ef_id=V9u@TgAABMH86aOm:20161227051709:s)
2. AWS CLI Installed and configured
3. After following the steps in the **Getting Started** section, you will have set up the following resources:  
  3.1. An AWS kinesis Stream  
  3.2. Two IAM roles, Instance Profiles and [Policies](http://docs.aws.amazon.com/streams/latest/dev/controlling-access.html) required for the KCL and KPL instances  
  3.3. Two AWS EC2 Instances based on AmazonLinux with dependencies pre-installed  
  3.3. An RDS mysql database  
  3.4. An Amazon S3 bucket  
4. When the KCL is initiated, two DynamoDB tables are created  

###Setting up the environment:
1. Create a Kinesis Stream  
  ```
  aws kinesis create-stream --stream-name 12616-Stream --shard-count 2  
  ```
2. Create the Kinesis IAM roles required for EC2 Instances  (Please replace the account ids with your own account id)
  ```
  aws iam create-role \  
  --role-name 12616-KPLRole \  
  --assume-role-policy-document '  
  {  
      "Version": "2012-10-17",  
      "Statement": [{  
          "Sid": "",  
          "Effect": "Allow",  
          "Principal": {  
              "Service": "ec2.amazonaws.com"  
          },  
          "Action": "sts:AssumeRole"  
      }]  
  }'  

  aws iam create-role \  
  --role-name 12616-KCLRole \  
  --assume-role-policy-document '  
  {  
      "Version": "2012-10-17",  
      "Statement": [{  
          "Sid": "",  
          "Effect": "Allow",  
          "Principal": {  
              "Service": "ec2.amazonaws.com"  
          },  
          "Action": "sts:AssumeRole"  
      }]  
  }'  

  aws iam create-instance-profile --instance-profile-name 12616-KCLRole  

  aws iam create-instance-profile --instance-profile-name 12616-KPLRole  

  aws iam add-role-to-instance-profile --instance-profile-name 12616-KPLRole --role-name 12616-KPLRole  

  aws iam add-role-to-instance-profile --instance-profile-name 12616-KCLRole --role-name 12616-KCLRole  
  ```
3. Create the Kinesis IAM Policies  
  ```
  aws iam create-policy \  
  --policy-name 12616-KPLPolicy \  
  --policy-document '  
  {  
      "Version": "2012-10-17",  
      "Statement": [{  
          "Effect": "Allow",  
          "Action": ["kinesis:PutRecord"],  
          "Resource": ["arn:aws:kinesis:us-east-1:111122223333:stream/12616-Stream"]  
      }]  
  }'  

  aws iam create-policy \  
  --policy-name 12616-KCLPolicy \  
  --policy-document '  
  {  
      "Version": "2012-10-17",  
      "Statement": [{  
          "Effect": "Allow",  
          "Action": ["kinesis:Get*"],  
          "Resource": ["arn:aws:kinesis:us-east-1:111122223333:stream/12616-Stream"]  
      }, {  
          "Effect": "Allow",  
          "Action": ["kinesis:DescribeStream"],  
          "Resource": ["arn:aws:kinesis:us-east-1:111122223333:stream/12616-Stream"]  
      }, {  
          "Effect": "Allow",  
          "Action": ["kinesis:ListStreams"],  
          "Resource": ["*"]  
      }, {  
          "Effect": "Allow",  
          "Action": ["dynamodb:CreateTable", "dynamodb:DescribeTable", "dynamodb:Scan", "dynamodb:PutItem", "dynamodb:UpdateItem", "dynamodb:GetItem"],  
          "Resource": ["arn:aws:dynamodb:us-east-1:111122223333:table/Centos*"]  
      }, {  
          "Sid": "Stmt1482832527000",  
          "Effect": "Allow",  
          "Action": ["cloudwatch:PutMetricData"],  
          "Resource": ["*"]  
      }]  
  }'  
  ```
4. Attach the Policies to the Roles  
  ```
  aws iam attach-role-policy \  
  --policy-arn "arn:aws:iam::111122223333:policy/12616-KPLPolicy" \  
  --role-name 12616-KPLRole  

  aws iam attach-role-policy \  
  --policy-arn "arn:aws:iam::111122223333:policy/12616-KCLPolicy" \  
  --role-name 12616-KCLRole  
  ```
5. Create a Bootstrap script to automate the installation of the dependencies on newly launched instances  
  ```
  cat <<EOF > Bootstrap.sh  
  #!/bin/bash  
  sudo yum install -y java-1.8.0-* git gcc-c++ make  
  sudo yum remove -y java-1.7.0-*  
  curl --silent --location https://rpm.nodesource.com/setup_6.x | sudo bash -  
  sudo yum install -y nodejs 
  sudo yum install mysql -y
  sudo pip install faker  
  cd /home/ec2-user   
  wget http://mirrors.whoishostingthis.com/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip  
  unzip apache-maven-3.3.9-bin.zip  
  echo "export PATH=\$PATH:/home/ec2-user/apache-maven-3.3.9/bin" >> .bashrc  
  git clone https://github.com/cheefoo/centos.git  
  mkdir ./centos/logs  
  chown -R ec2-user ./centos  
  EOF  

  ```
6. Please note that image-id given in below command belongs to us-east-1, if you are launching in a different region please look up the image-id for that region. Take note of the returned "InstanceId" after launching each instance in order to create tags
  ``` 
  aws ec2 run-instances \  
  --image-id ami-9be6f38c \  
  --key-name sshkeypair \  
  --security-groups default \  
  --instance-type m3.large \  
  --iam-instance-profile Name="12616-KPLRole" \  
  --user-data file://Bootstrap.sh  

  aws ec2 create-tags --resources i-000d3b6d9fexample --tags Key=Name,Value="12616-KPLInstance"  

  aws ec2 run-instances \  
  --image-id ami-9be6f38c \  
  --key-name sshkeypair \  
  --security-groups default \  
  --instance-type m3.large \  
  --iam-instance-profile Name="12616-KCLRole" \  
  --user-data file://Bootstrap.sh  

  aws ec2 create-tags --resources i-0879e274caexample --tags Key=Name,Value="12616-KCLInstance"  
  ```
7. Create an RDS Instance and take note of the JDBC Endpoint, username and password  
  ```
  aws rds create-db-instance \  
  --db-instance-identifier RDSInstance12616 \  
  --db-name DB12616 \  
  --engine mysql \  
  --master-username groot \  
  --master-user-password ********** \  
  --db-instance-class db.t1.micro \  
  --allocated-storage 8  

  ```
8. Create an Amazon S3 bucket  
  ```
  aws s3 mb s3://12616S3Bucket  

  ```
9. Dont forget to modify the default security group to allow ssh access. SSH into the KCL Instance and edit the **~/home/ec2-user/centos/src/main/resources** file according to the resources created

| Key           | Default                                        | Description                                                                     |
| :------------ | :--------------------------------------------- | :------------------------------------------------------------------------------ |
| mysqldburl    | None                                           | The JDBC URL for the MySQL RDS Instance, e.g. jdbc:mysql://instance:3306/mydb   |
| mysqldbuser   | None                                           | Username for the MySQL Database                                                 |
| mysqldbpwd    | None                                           | Password for the MySQL Database                                                 |
| indexfile     | /home/ec2-user/centos/webapp/public/index.html | Dashboard index page                                                            |
| filelocation  | /home/ec2-user/centos/scripts/generatedData    | Input file location (json formatted)                                            |
| streamname    | None                                           | Name of the AWS Kinesis Stream                                                  |
| region        | us-west-2                                     | AWS Region of the Kinesis Stream                                                |
| s3bucket      | None                                           | S3 Bucket Name for archived data                                                |
| kcl_archiver_name      | CentosArchiver                        | KCL App name for the S3 Archiver consumer                                                |
| kcl_dashboard_name      | CentosDashboard                      | KCL App name for the dashboard consumer                                                 |

###Running the Example:
1. SSH into the KCL Instance and edit the **~/centos/src/main/resources/db.properties** file according to the resources created. 

1a. Login to the mysql db instance and create the user_events table by using the ddl user_events.sql located in ~/centos/scripts/user_events.sql

mysql --host=rdsinstance12616.cu74pzqocy8l.us-west-2.rds.amazonaws.com --user=groot --password=####### DB12616

1b. execute the ddl script

```mvn compile''' 
Start the Archiving Consumer from the **~/centos** directory  
  ```
  nohup bash -c \  
  "(mvn exec:java -Dexec.mainClass=com.tayo.centos.kcl1.ConsumerApp > ~/centos/logs/archiving_consumer.log) \  
   &> ~/centos/logs/archiving_consumer.log" &  

  ```
2. Start the dashboard consumer  
  ```
  nohup bash -c \  
  "(mvn exec:java -Dexec.mainClass=com.tayo.centos.kcl2.ConsumerApp2 > ~/centos/logs/dashboard_consumer.log) \  
  &> ~/centos/logs/dashboard_consumer.log" &  

  ```
3. SSH into the KPL Instance and edit the **~/centos/kpl_config.properties** file according to the resources created.  
4. Generate some sample data  
  ```
  cd ~/centos/scripts/  
  rm -rf ./generatedData  
  python generateJson.py 2 10  
  cd ..  

  ```
5. SSH into the KPL Instance and edit the **~/centos/src/main/resources/db.properties** file, add your location for the generated data. Modify ~/centos/kpl_config.properties appropriately.

Start the producer  
```mvn compile```
  ```
  nohup bash -c \  
  "(mvn exec:java -Dexec.mainClass=com.tayo.centos.ProducerOne > ~/centos/logs/producer.log) \  
   &> ~/centos/logs/producer.log" &  

  ```
  
6. From the KCL instance, Start the Job Scheduler 

  ```
  nohup bash -c \  
  "(mvn exec:java -Dexec.mainClass=ccom.tayo.centos.scheduler.DashboardMonitor  > ~/centos/logs/scheduler.log) \  
   &> ~/centos/logs/scheduler.log" &  

  ```
  
7. From the KCL instance,  Start the NodeJS Server  from the webapps directory
  ```
 node server.js

  ```

  
Note:   
* KPL reads region and stream name from "./kpl_config.properties"  
* KCL Apps reads region and stream name from ./db.properties


