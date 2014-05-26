Dependencies:
1. gdata package
* Download the gdata package from: http://code.google.com/p/gdata-java-client/downloads/list
* Extract it to a an arbitrary folder
* Install the following files into your maven repository
** gdata-youtube
*** mvn install:install-file -DgroupId=com.google.gdata -DartifactId=gdata-youtube -Dversion=2.0 -Dfile=gdata-youtube-2.0.jar -Dpackaging=jar -DgeneratePom=true
** gdata-client
*** mvn install:install-file -DgroupId=com.google.gdata -DartifactId=gdata-client -Dversion=1.0 -Dfile=gdata-client-1.0.jar -Dpackaging=jar -DgeneratePom=true
** gdata-core
*** mvn install:install-file -DgroupId=com.google.gdata -DartifactId=gdata-core -Dversion=1.0 -Dfile=gdata-core-1.0.jar -Dpackaging=jar -DgeneratePom=true
** gdata-media
*** mvn install:install-file -DgroupId=com.google.gdata -DartifactId=gdata-media -Dversion=1.0 -Dfile=gdata-media-1.0.jar -Dpackaging=jar -DgeneratePom=true

2. vCard4J
* Download and extract the vCard4J from http://sourceforge.net/projects/vcard4j/files/
* Extract it to a an arbitrary folder
* Install the following files into your maven repository   
** vcard4j
***mvn install:install-file -DgroupId=net.sf.vcard4j -DartifactId=vcard4j -Dversion=1.1.3 -Dfile=vcard4j-1_1_3.jar -Dpackaging=jar -DgeneratePom=true


Prepare your Tomcat
* Be sure that Java Mail is in your Tomcat lib directory ($CATALINA_HOME/common/lib/) if not you can obtain the file under the link below.
** http://repo1.maven.org/maven2/javax/mail/mail/1.4/mail-1.4.jar


Create an Eclipse project 
* mvn -Dwtpversion=1.5 eclipse:eclipse
* import as existing project