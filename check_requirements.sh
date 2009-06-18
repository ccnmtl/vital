#!/bin/bash

#set -x 
echo ''
echo 'Checking for basic Vital 3 requirements:'
echo ''

echo 'Checking for Tomcat.'
if [ -e `locate bin/catalina.sh` ]  ; then
    echo '---> Tomcat is installed.'
else
    echo '---> Tomcat does not appear to be installed. Please download it from http://tomcat.apache.org and install it.'
    exit;
fi

echo 'Checking for Subversion client.'
type svn --version  > /dev/null 2>&1
if [ $? -eq 0 ] ; then 
    echo '---> Subversion is installed.'
else
    echo '---> No subversion client. Please download it from http://subversion.tigris.org and install it.'
    exit;
fi

echo 'Checking for Maven version 1.0.2.'
type maven -v  2>/dev/null | grep 1.0.2  > /dev/null 2>&1
if [ $? -eq 0 ] ; then 
    echo '---> Maven 1.0.2 is installed.'
else
    echo '---> Maven is not installed. Please get it from http://archive.apache.org/dist/maven/binaries/maven-1.0.2.tar.gz , unpack it, and add its bin directory to your path.'
    exit;
fi

echo 'Checking for home directory.'
if [ -e $HOME ] ; then
    echo '--->' `whoami` ' has a home directory.'
else
    echo '--->' `whoami` 'does not have a home directory. Please create one.'
    exit;
fi

echo ''
echo 'You can proceed with svn co http://svn.ccnmtl.columbia.edu/vital3/trunk/ vital3'
echo ''
