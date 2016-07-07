# CloudLab-IAAS-UniStuttgart-Part2-gRPCAWSWordpress
Java project of AWS-Wordpress gRPC API  

####WordPress gRPC  
This is the gRPC implementation for the automated deployment of WordPress using Puppet CM (Configuration Management) tool onto Amazon EC2 instance.  
This gRPC API automatically connects to the EC2 instance (details provided by the user) and deploys the WordPress App, deploys SQL database and then connects this deployed App to the database. All of this deployment makes use of the Puppet CM tool.

####WordPress proto

Description of proto file being used in the gRPC API implementation of the WordPress Service:  
Service Name: WordPressOps  
Method Name: deployApp, deployDB, connectAppToDB  
    Input to all the above method  
      
    credentials: The name of the pem file (key pair) of the EC2 instance which is required to SSH to it  
    bucketName: The name of the Amazon S3 bucket in which the above pem file is stored  
    username: Username of the Amazon EC2 instance in order to initiate session  
    publicIP: Public IP of the Amazon EC2 instance in order to initiate session

NOTE: Please maintain order of input to the method if you are manually invoking the API & naming convention

