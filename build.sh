#                                                             
# build.sh - checks to see if project's properties files      
# and runs maven to build the project                         
#     

if [ ! -e "target/vital3" ] ;
then
    echo 'You are in the wrong directory or the "target/vital3" directory is missing! Run this script from the main vital3 directory.'
    exit 1
fi

if [ ! -e "src/conf/spring.properties" ] ;
then
    echo 'You are missing spring.properties. Did you run make?'
    exit 1
fi

if [ ! -e "src/conf/log4j.properties" ] ;
then
    echo 'You are missing spring.properties. Did you run make?'
    exit 1
fi

if [ ! -e "src/conf/oscache.properties" ] ;
then
    echo 'You are missing oscache.properties. Did you run make?'
    exit 1
fi

echo 'Building...'

maven -b war:webapp

exit 0