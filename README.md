**incomplete WIP**


##De-duplication and strict ordering Kinesis KCL example

This is an end-to-end [AWS Kinesis Streams](https://aws.amazon.com/kinesis/streams/) processing example using the [AWS Kinesis Producer Library (KPL)](http://docs.aws.amazon.com/streams/latest/dev/developing-producers-with-kpl.html) to send records to a kinesis stream, consume from the stream using the [AWS Kinesis Client Library (KCL)](http://docs.aws.amazon.com/streams/latest/dev/developing-consumers-with-kcl.html) and archive data to an [Amazon S3](http://docs.aws.amazon.com/AmazonS3/latest/dev/Welcome.html) bucket. Additionally, a second KCL consumer provides realtime data updates to a web front-end. 

The application consists of 5 components:

1. A producer application
2. An Archiving Consumer application
3. A De-duplicating Dashboard consumer application
4. A Dashboard Job Scheduler
5. A script to generate data in the format required by the application(s)

###Requirements:
1. An Amazon Web Services [Account](https://aws.amazon.com/free/?sc_channel=PS&sc_campaign=acquisition_ZA&sc_publisher=google&sc_medium=cloud_computing_b&sc_content=aws_account_e&sc_detail=aws%20account&sc_category=cloud_computing&sc_segment=77706639422&sc_matchtype=e&sc_country=ZA&s_kwcid=AL!4422!3!77706639422!e!!g!!aws%20account&ef_id=V9u@TgAABMH86aOm:20161227051709:s)
2. AWS CLI Installed and configured
3. After following the steps in the **Getting Started** section, you will have set up the following resources:  
  3.1. An AWS kinesis Stream  
  3.2. Two IAM roles, Instance Profiles and [Policies](http://docs.aws.amazon.com/streams/latest/dev/controlling-access.html) required for the KCL and KPL instances  
  3.3. Two AWS EC2 Instances based on AmazonLinux with dependencies pre-installed  
  3.3. An RDS mysql database  
  3.4. A Redshift database  
  3.5. An Amazon S3 bucket  
4. When the KCL is initiated, two DynamoDB tables are created  

###Setting up the environment:
1. Create a Kinesis Stream  
  ```
  aws kinesis create-stream --stream-name 12616-Stream --shard-count 2  
  ```
2. Create the Kinesis IAM roles required for EC2 Instances  
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
  sudo pip install faker  
  cd /home/ec2-user   
  wget http://mirrors.whoishostingthis.com/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip  
  unzip apache-maven-3.3.9-bin.zip  
  echo "export PATH=\$PATH:/home/ec2-user/apache-maven-3.3.9/bin" >> .bashrc  
  git clone https://github.com/leclue/centos.git  
  mkdir ./centos/logs  
  chown -R ec2-user ./centos  
  EOF  

  ```
6. Take note of the returned "InstanceId" after launching each instance in order to create tags
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
9. SSH into the KCL Instance and edit the **~/centos/target/classes/db.properties** file according to the resources created

| Key           | Default                                        | Description                                                                     |
| :------------ | :--------------------------------------------- | :------------------------------------------------------------------------------ |
| dburl         | None                                           | The JDBC URL for the redshift cluster, e.g. jdbc:redshift://cluster:5439/mydb   |
| dbuser        | None                                           | Username for the Redshift Database                                              |
| dbpwd         | None                                           | Password for the Redshift Database                                              |
| mysqldburl    | None                                           | The JDBC URL for the MySQL RDS Instance, e.g. jdbc:mysql://instance:3306/mydb   |
| mysqldbuser   | None                                           | Username for the MySQL Database                                                 |
| mysqldbpwd    | None                                           | Password for the MySQL Database                                                 |
| jsonfile      | /home/ec2-user/jsonfile.json                   | ???                                                                             |
| kpltempdir    | /home/ec2-user/centos                          | ???                                                                             |
| indexfile     | /home/ec2-user/centos/webapp/public/index.html | Dashboard index page                                                            |
| filelocation  | /home/ec2-user/centos/scripts/generatedData    | Input file location (json formatted)                                            |
| streamname    | None                                           | Name of the AWS Kinesis Stream                                                  |
| region        | us-east-1                                      | AWS Region of the Kinesis Stream                                                |
| s3bucket      | None                                           | S3 Bucket Name for archived data                                                |

###Running the Example:
1. Start the Archiving Consumer from the **~/centos** directory  
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
3. SSH into the KCL Instance and edit the **~/centos/target/classes/db.properties** file according to the resources created.  
4. Generate some sample data  
  ```
  cd ~/centos/scripts/  
  rm -rf ./generatedData  
  python generateJson.py 2 10  
  cd ..  

  ```
5. Start the producer  
  ```
  nohup bash -c \  
  "(mvn exec:java -Dexec.mainClass=com.tayo.centos.ProducerOne > ~/centos/logs/producer.log) \  
   &> ~/centos/logs/producer.log" &  

  ```
  

**todo**  
Start the Job Scheduler  
mvn exec:java -Dexec.mainClass=com.tayo.centos.scheduler.DashboardMonitor 
start the nodejs server (webapps folder) -- node server.js  
  
Note:   
* KPL reads region from "./default_config.properties"  
* Endless while loop in [KPL](https://github.com/leclue/centos/blob/master/src/main/java/com/tayo/centos/ProducerOne.java#L127) needs to be fixed.  


