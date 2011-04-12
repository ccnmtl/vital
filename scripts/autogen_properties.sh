#!/bin/bash
#
# autogenerate the properties files based on the .in templates
#

# check for required arguments
if [ -z $1 ]
then
   echo "Usage: `basename $0` target"
   echo "-h for help"
   exit
fi

set -x


TARGET=$1
CONF_DIR=src/conf
ANT_DIR=codegen/ant
KODOS_SANDBOXES='\/usr\/local\/share\/sandboxes'

#defaults to using the internal tasty jar (jtasty.jar)
TASTY_URL=''

#to use an external tagging server:
#TASTY_URL='http://tasty.ccnmtl.columbia.edu'

#defaults to using an Oracle client
DATABASE_TYPE='oracle'
## other example alternatives:
#DATABASE_TYPE='postgres'
#DATABASE_TYPE='mysql'

# default base_url:
BASE_URL='http:\/\/localhost:8080'

#default tomcat_home is for kang 4080 tomcat
TOMCAT_HOME='\/usr\/local\/tomcat'

# default tasty_service is for all dev servers...
TASTY_SERVICENAME=vital3_dev

# default DBCP connection pool settings are for all dev servers...
# ensure that maxActive * number of dev sandboxes < 20 (our oracle session limit)
DBCP_MAXACTIVE=2
DBCP_MAXIDLE=1

#default database is dev:
ORACLE_DB_USERNAME=ccnmtl_vital3_dev

# are we generating for a CUIT machine? (default = no)
IS_CUIT=0

# are we using the second-level cache? (default is yes. prod may or may not use.)
USE_CACHE=1

# do we enable JGroups for cluster-safe caching? (default is yes)
USE_JGROUPS=1

# are we using the prod database, tasty server, and cache?
USE_PROD_DATA=0

# default log levels, that we use on development
LOG_LEVEL_ROOT="WARN"
LOG_LEVEL_VITAL="WARN"
LOG_LEVEL_SPRING="WARN"
LOG_LEVEL_VELOCITY="WARN"
LOG_LEVEL_HIBERNATE="WARN"
LOG_LEVEL_CACHE="WARN"
LOG_LEVEL_JGROUPS="WARN"

# default context name corresponds to the target name:
CONTEXT_NAME="${TARGET}\/vital3"

# default path to the PARENT DIRECTORY of the log directory:
PATH_TO_LOG_DIR="${KODOS_SANDBOXES}\/${TARGET}\/vital3"

# default "initial hosts" attribute for jgroups on development:
# NO SPACES, COMMA-SEPARATED
JGROUPS_INITIAL_HOSTS="kodos.ccnmtl.columbia.edu[7800]"

# Upload Video Host
VIDEO_UPLOAD_HOST="http://foo.ccnmtl.columbia.edu"
VIDEO_UPLOAD_KEY="hungryanders_wears_a_porkpie"

case "$TARGET" in 
    "dev" )     
	IS_CUIT=1; 
	USE_PROD_DATA=1
	BASE_URL='http:\/\/wwwappdev.cc.columbia.edu'
	#CONTEXT_NAME="/ccnmtl/vital3" # no trailing slash.
	PATH_TO_LOG_DIR="\/www\/apps\/tomcat4\/wwwappdev\/ccnmtl\/projects\/vital3"
	USE_JGROUPS=0
	VIDEO_UPLOAD_HOST="http://behemoth.ccnmtl.columbia.edu:8000"
	;;

    # "test" )
	#IS_CUIT=1; 
	#USE_PROD_DATA=1
	#BASE_URL='http:\/\/wwwapptest.cc.columbia.edu'
	#PATH_TO_LOG_DIR="\/www\/apps\/tomcat4\/wwwapptest\/ccnmtl\/projects\/vital3"
	#USE_JGROUPS=0
	#;;

    "prod" )
	IS_CUIT=1;
	USE_PROD_DATA=1
	BASE_URL='http:\/\/vital.ccnmtl.columbia.edu'
	PATH_TO_LOG_DIR="\/var\/log\/tcat\/ccnmtl\/vital3"
	JGROUPS_INITIAL_HOSTS="lingonberry.cc.columbia.edu[7800],huckleberry.cc.columbia.edu[7800]"
	VIDEO_UPLOAD_HOST="http://wardenclyffe.ccnmtl.columbia.edu"
	;;

    #test setup on Mac OS:
    "platypus" )
	IS_CUIT=0
	USE_PROD_DATA=0
	BASE_URL='http:\/\/platypus.ccnmtl.columbia.edu:9006'
	PATH_TO_LOG_DIR="\/Library\/Webserver\/Documents\/vital\/vital3"
	CONTEXT_NAME="/vital3"
	USE_JGROUPS=0
	TOMCAT_HOME='\/Library\/Tomcat/'
	CONTEXT_NAME="vital3"
	;;

    #dev setup on Mac OS:
    "sdreher_dev" )
	IS_CUIT=0
	USE_PROD_DATA=0
	BASE_URL='http:\/\/localhost:8080'
	PATH_TO_LOG_DIR="\/Users\/sdreher\/workspace\/vital3"
	CONTEXT_NAME="/vital3"
	USE_JGROUPS=0
	TOMCAT_HOME='\/usr\/local\/tomcat\/'
	CONTEXT_NAME="vital3" # no trailing slash
        DATABASE_TYPE="oracle"
	;;

    #dev setup on kodos
    "sdreher_kodos"  )
	IS_CUIT=0;
        USE_PROD_DATA=0
        USE_JGROUPS=0
	BASE_URL='http:\/\/kodos.ccnmtl.columbia.edu:4080\/sdreher'
        TOMCAT_HOME='\/usr\/local\/tomcat\/webapps\/'
        DATABASE_TYPE='oracle'
	PATH_TO_LOG_DIR="\/usr\/local\/share\/sandboxes\/sdreher\/vital3"
        CONTEXT_NAME="vital3" # no trailing slash.                 
	;;

    #Eddie's development environment
    "tiur_dev"  )
	IS_CUIT=0;
	USE_PROD_DATA=1
	BASE_URL='http:\/\/localhost:8080'
	TOMCAT_HOME='\/usr\/local\/tomcat\/webapps\/'
	DATABASE_TYPE='oracle'
	PATH_TO_LOG_DIR="\/usr\/local\/tomcat\/webapps\/vd"
	CONTEXT_NAME="vd" # no trailing slash.
	;;

    "common"  )
	PATH_TO_LOG_DIR="\/usr\/local\/share\/sandboxes\/common\/tomcat4_4080\/vital3"
	;;
esac


if [ $IS_CUIT = 1 ]; then
    #NOTE: this points the context at Tomcat 5:
    CONTEXT_NAME="ccnmtl\/vital3"
    
    LOG_LEVEL_ROOT="WARN"
    LOG_LEVEL_VITAL="DEBUG"
    LOG_LEVEL_SPRING="WARN"
    LOG_LEVEL_VELOCITY="WARN"
    LOG_LEVEL_HIBERNATE="WARN"
    LOG_LEVEL_CACHE="WARN"
    LOG_LEVEL_JGROUPS="WARN"
    #LOG_LEVEL_HIBERNATE="DEBUG"
    #LOG_LEVEL_CACHE="DEBUG"
    #LOG_LEVEL_JGROUPS="DEBUG"
    USE_CACHE=1
    
#  else - # non-cuit config (kodos, or local dev machines)
fi

if [ $USE_PROD_DATA = 1 ]; then
    DBCP_MAXACTIVE=17
    DBCP_MAXIDLE=9
    ORACLE_DB_USERNAME=ccnmtl_vital3_prod
    TASTY_SERVICENAME=vital3_prod
fi

# the JGROUPS var contains a single-line-comment delimeter by default,
# and changes to an empty space if USE_JGROUPS is true
JGROUPS="# "
if [ $USE_JGROUPS = 1 ]; then
    JGROUPS=
fi


if [ $USE_JGROUPS = 1 ]; then
    JGROUPS=
fi

#NOTE: for Oracle, 
#ORACLE_DB_USERNAME depends on whether $USE_PROD_DATA  is 1 or 0
case "$DATABASE_TYPE" in 
    "oracle" )     
        DATABASE_HIBERNATE_DIALECT='org.hibernate.dialect.Oracle9Dialect'
        DATABASE_DRIVER_CLASS_NAME='oracle.jdbc.OracleDriver'
        DATABASE_URL='jdbc:oracle:thin:\@chili\.cc\.columbia\.edu:1521:acisora1'
        DATABASE_USERNAME=${ORACLE_DB_USERNAME}
        DATABASE_PASSWORD='oracle_database_password'
	;;

    "postgres"  )
        DATABASE_HIBERNATE_DIALECT='org.hibernate.dialect.PostgreSQLDialect'
        DATABASE_DRIVER_CLASS_NAME='org.postgresql.Driver'
        DATABASE_URL='jdbc:postgresql://localhost/vital3'
        DATABASE_USERNAME='postgres_username'
        DATABASE_PASSWORD='postgres_passwd'
	;;

    "mysql"  )
        DATABASE_HIBERNATE_DIALECT='org.hibernate.dialect.MySQLInnoDBDialect'
        DATABASE_DRIVER_CLASS_NAME='com.mysql.jdbc.Driver'
        DATABASE_URL='jdbc:mysql://localhost/vital3?createDatabaseIfNotExist=false&amp;useUnicode=true&amp;characterEncoding=utf-8'
        DATABASE_USERNAME='msql_username'
        DATABASE_PASSWORD='mysql_passwd'
	;;
esac


# turn on debugging
set -x 

echo "Generating spring.properties..."
perl -p -e \
    "s#<APP_BASE_URL>#${BASE_URL}/${CONTEXT_NAME}/#g;
     s#<TASTY_SERVICENAME>#${TASTY_SERVICENAME}#g;
     s#<TASTY_URL>#${TASTY_URL}#g;
     s#<DBCP_MAXACTIVE>#${DBCP_MAXACTIVE}#g;

     s#<DATABASE_HIBERNATE_DIALECT>#${DATABASE_HIBERNATE_DIALECT}#g;
     s#<DATABASE_DRIVER_CLASS_NAME>#${DATABASE_DRIVER_CLASS_NAME}#g;
     s#<DATABASE_URL>#${DATABASE_URL}#g;
     s#<DATABASE_USERNAME>#${DATABASE_USERNAME}#g;
     s#<DATABASE_PASSWORD>#${DATABASE_PASSWORD}#g;

     s#<DBCP_MAXIDLE>#${DBCP_MAXIDLE}#g;

     s#<VIDEO_UPLOAD_HOST>#${VIDEO_UPLOAD_HOST}#g;
     s#<VIDEO_UPLOAD_KEY>#${VIDEO_UPLOAD_KEY}#g;

     " \
     ${CONF_DIR}/spring.properties.in >  ${CONF_DIR}/spring.properties

echo "Generating log4j.properties...."
echo "Log is going to ${CONF_DIR}"
perl -p -e \
    "s#<PATH_TO_LOG_DIR>#${PATH_TO_LOG_DIR}#g;
     s#<LOG_LEVEL_ROOT>#${LOG_LEVEL_ROOT}#g;
     s#<LOG_LEVEL_VITAL>#${LOG_LEVEL_VITAL}#g;
     s#<LOG_LEVEL_SPRING>#${LOG_LEVEL_SPRING}#g;
     s#<LOG_LEVEL_VELOCITY>#${LOG_LEVEL_VELOCITY}#g;
     s#<LOG_LEVEL_HIBERNATE>#${LOG_LEVEL_HIBERNATE}#g;
     s#<LOG_LEVEL_CACHE>#${LOG_LEVEL_CACHE}#g;
     s#<LOG_LEVEL_JGROUPS>#${LOG_LEVEL_JGROUPS}#g;
     " \
     ${CONF_DIR}/log4j.properties.in >  ${CONF_DIR}/log4j.properties

cp ${CONF_DIR}/log4j.properties target/vital3/WEB-INF/classes/
cp ${CONF_DIR}/log4j.properties target/classes/

 
#### build.properties (we only use this on dev for reloading tomcat)
echo "Generating build.properties..."
perl -p -e \
    "s#<TOMCAT_HOME>#${TOMCAT_HOME}#g;
     s#<CONTEXT_NAME>#/${TARGET}/vital3#g;
     s#<BASE_URL>#${BASE_URL}#g;
     " \
     ${ANT_DIR}/build.properties.in >  ${ANT_DIR}/build.properties


#### oscache.properties
echo "Generating oscache.properties..."
perl -p -e \
     "s%<JGROUPS>%${JGROUPS}%g;
      s%<JGROUPS_INITIAL_HOSTS>%${JGROUPS_INITIAL_HOSTS}%g;
     " \
     ${CONF_DIR}/oscache.properties.in >  ${CONF_DIR}/oscache.properties

#### hibernate files
echo "Generating hibernate mapping files..."
if [ ! -e src/hibernate ]; then
    mkdir src/hibernate
fi

for x in `ls -1 src/hibernate.in`; do \
    cp src/hibernate.in/$x src/hibernate
    if [ $USE_CACHE = 0 ]; then
	## fyi: perl command line args: -i writes output back to the input file, -p executes code for each line of the input file
	perl -i -p -e \
	    's#<cache usage="nonstrict-read-write"/>##g;' \
	    src/hibernate/$x;
    fi
done

#### jtasty hibernate files
if [ ! -e src/hibernate/jtasty ]; then
    echo "Generating hibernate mapping files (Jtasty) ..."
    mkdir src/hibernate/jtasty
fi
for x in `ls -1 src/hibernate.in/jtasty`; do \
    cp src/hibernate.in/jtasty/$x src/hibernate/jtasty
    if [ $USE_CACHE = 0 ]; then
	## fyi: perl command line args: -i writes output back to the input file, -p executes code for each line of the input file
	perl -i -p -e \
	    's#<cache usage="nonstrict-read-write"/>##g;' \
	    src/hibernate/jtasty/$x;
    fi
done

#### hibernate.properties
echo "Generating hibernate.properties..."
perl -p -e \
    "s#<DATABASE_HIBERNATE_DIALECT>#${DATABASE_HIBERNATE_DIALECT}#g;
     s#<DATABASE_DRIVER_CLASS_NAME>#${DATABASE_DRIVER_CLASS_NAME}#g;
     s#<DATABASE_URL>#${DATABASE_URL}#g;
     s#<DATABASE_USERNAME>#${DATABASE_USERNAME}#g;
     s#<DATABASE_PASSWORD>#${DATABASE_PASSWORD}#g;
     " \
     src/hibernate.in/maven.hibernate.properties.in >  src/hibernate/maven.hibernate.properties
