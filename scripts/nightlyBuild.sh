#!/bin/bash

#
# updates, rebuilds and reloads the comman sandbox
#

set -x 
COMMON_DIR=/usr/local/share/sandboxes/common
VITAL_DIR=$COMMON_DIR/tomcat4_4080/vital3
ANT=/usr/bin/ant

# not sure why -H on sudo isnt picking up these values in pushers .profile
export CVS_RSH=ssh 
export JAVA_HOME=`java-config -O`
export ANT_HOME=/usr/share/ant-core
export JDK_HOME=`java-config --jdk-home`
export CLASSPATH=.

cd $VITAL_DIR
# run me as pusher, from my home (so ssh keys work)
# sudo -H -u pusher $ANT rebuildAll

echo $JAVA_HOME
echo $ANT_HOME

# do svn update in shell script instead of ant task until svnant is more stable
sudo -H -u pusher svn update
sudo -H -u pusher make common
sudo -H -u pusher /usr/bin/maven -b war:webapp

# we no longer reload the tomcat context - restart instead
#sudo -H -u pusher $ANT -f codegen/ant/build.xml reload
/etc/init.d/tomcat4 restart
