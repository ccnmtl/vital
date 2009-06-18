#bash file for inserting users
set -x
COUNTER=20

while [  $COUNTER -gt 0 ]; do   
   echo "Please input the user's firstname, lastname, and userIdString accordingly....."
   read firstName
   read lastName
   read userIdString
   curl "http://kang.ccnmtl.columbia.edu:4090/jin/vital3/insertUsers.smvc?firstName="$firstName"&lastName="$lastName"&userIdString="$userIdString""
   echo "\n"
   let COUNTER=COUNTER-1     
done

