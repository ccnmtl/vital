#!/bin/sh

set -x 
echo 'copying oracle jar...'
mkdir -p ~/.maven/repository/oracle-jdbc14/jars
cp oracle-jdbc14-9.2.0.3.jar ~/.maven/repository/oracle-jdbc14/jars/

echo 'copying json jar...'
mkdir -p ~/.maven/repository/json/jars
cp org.json-0.jar ~/.maven/repository/json/jars/

echo 'copying jta jar...'
mkdir -p ~/.maven/repository/jta/jars
cp jta-1.0.1b.jar ~/.maven/repository/jta/jars/

echo 'copying jaf jar...'
mkdir -p ~/.maven/repository/jaf/jars
cp jaf-1.1.jar ~/.maven/repository/jaf/jars/

echo 'copying javaMail jar...'
mkdir -p ~/.maven/repository/javaMail/jars
cp javaMail-1.4.jar ~/.maven/repository/javaMail/jars/

echo 'copying jgroups jars...'
mkdir -p ~/.maven/repository/jgroups/jars
cp jgroups-all-2.4.0.jar ~/.maven/repository/jgroups/jars/
cp jgroups-concurrent-2.4.0.jar ~/.maven/repository/jgroups/jars/

echo 'copying jtasty jars...'
mkdir -p ~/.maven/repository/jtasty/jars
cp jtasty-0.0.8.jar ~/.maven/repository/jtasty/jars/

echo 'copying maven hibernate plugin jar...'
mkdir -p ~/.maven/repository/jtasty/jars
cp maven-hibernate-plugin-1.4.jar ~/.maven/repository/maven/plugins

echo 'done!'
