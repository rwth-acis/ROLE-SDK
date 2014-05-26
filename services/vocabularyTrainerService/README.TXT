How to set up the project in Eclipse:

1.	Get the code from SVN
	Use your favorite svn tool to check out the code from
	https://role-project.svn.sourceforge.net/svnroot/role-project/trunk/services/vocabularyTrainerService/
	to $PROJECT_HOME

2.	Create an eclipse project
	run $PROJECT_HOME/mvn -Dwtpversion=1.5 eclipse:eclipse
	
3.	Import the project to Eclipse via File->Import->Import Existing Project
	

How to deploy the web service:

1. 	Prepare your database
a.	Use MySQLAdministrator, the MySQL Workbench or MySQLDumb to create a database
	with the name "$VT_DATABASE" (default::vocabularytrainer)in your local MySQL database by using
	the provided SQL file createVTDB.sql
b.	Create a user $USER (default:vT) with $PASS (default:vT) and give her write permission on the database

2.	Adjust context.xml
	Edit the file /src/main/webapp/META-INF/context.xml regarding your variables. 
	You have to change the username, pass and url of the resource 
	
3.	Run the service
	Either run the service directly from your IDE or create a war file and put it 
	into your Tomcat.
	