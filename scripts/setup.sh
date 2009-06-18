#!/bin/bash
# Run this file after you check out vital3 from source control. It will set up a few things (see code).

if [ `env | grep 'CLASSPATH' | wc -l` -eq '1' ]
then
    echo "OK"
else
    echo "NOTE: You don't have the environment variable CLASSPATH set, so you won't be able to start the Tomcat server."
    echo "Try running the following (or similar) commands first:"
    echo ""   
    echo "export JAVA_HOME=/usr/lib/jvm/java-1.5.0-sun-1.5.0.15/"
    echo "export CLASSPATH=/usr/local/tomcat/common/lib/jsp-api.jar:/usr/local/tomcat/common/lib/servlet-api"
    exit;
fi

if [ `env | grep 'JAVA_HOME' | wc -l` -eq '1' ]
then
    echo "OK"
else
    echo "NOTE: You don't have the environment variable CLASSPATH set, so you won't be able to start the Tomcat server."
    echo "Try running the following (or similar) commands first:"
    echo ""   
    echo "export JAVA_HOME=/usr/lib/jvm/java-1.5.0-sun-1.5.0.15/"
    echo "export CLASSPATH=/usr/local/tomcat/common/lib/jsp-api.jar:/usr/local/tomcat/common/lib/servlet-api"
    exit;
fi




if [ ! -e "../logs" ] ;
then
    echo 'You are in the wrong directory or the "logs" directory is missing. You should invoke this script from the /scripts directory.'
    exit 1
fi

echo "running setup..."

echo "creating log files..."
cd ../logs
touch cache.log
touch everything.log
touch hibernate.log
touch spring.log
touch velocity.log
touch vital.log
echo "setting permissions on log files..."
chmod 777 *.log

echo "installing jars..."
cd ../archives/
./install_jars.sh

echo "done with setup! now run 'make' with the appropriate parameter (e.g. 'make emattes')"
