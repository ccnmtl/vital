alter table ANNOTATIONS drop constraint FK47010F047A0F4E9E;
alter table ANNOTATIONS drop constraint FK47010F04491771CA;
alter table ANSWERS drop constraint FKF8494455EBCBF05;
alter table ANSWERS drop constraint FKF8494455C3F6856A;
alter table ASSIGNMENTS drop constraint FKE898E726B78970AA;
alter table ASSIGNMENT_MATERIAL_ASSOCS drop constraint FK23C7F7A4645FAC0A;
alter table ASSIGNMENT_MATERIAL_ASSOCS drop constraint FK23C7F7A4491771CA;
alter table ASSIGNMENT_RESPONSES drop constraint FK6F169540645FAC0A;
alter table ASSIGNMENT_RESPONSES drop constraint FK6F1695407A0F4E9E;
alter table COMMENTS drop constraint FKABDCDF4EBCBF05;
alter table COMMENTS drop constraint FKABDCDF459F0726A;
alter table COMMENTS drop constraint FKABDCDF47A0F4E9E;
alter table CUSTOM_FIELDS drop constraint FK8566DF47F856EB76;
alter table CUSTOM_FIELD_VALUES drop constraint FKABA1663534907427;
alter table CUSTOM_FIELD_VALUES drop constraint FKABA16635491771CA;
alter table MATERIALS drop constraint FKCEB1500CF856EB76;
alter table QUESTIONS drop constraint FK3BDD512D645FAC0A;
alter table QUESTION_MATERIAL_ASSOCS drop constraint FKF0DF5EBD491771CA;
alter table QUESTION_MATERIAL_ASSOCS drop constraint FKF0DF5EBDC3F6856A;
alter table TASTY_ITEMS drop constraint FK141602EC8DF4AFE6;
alter table TASTY_ITEMS drop constraint FK141602ECA9042904;
alter table TASTY_ITEM_TAGS drop constraint FK4312F31173F1FE90;
alter table TASTY_ITEM_TAGS drop constraint FK4312F311A24B5E75;
alter table TASTY_ITEM_TAGS drop constraint FK4312F31136904DB0;
alter table TASTY_ITEM_TAGS drop constraint FK4312F311558D87E4;
alter table TASTY_TAGS drop constraint FK74477F4DDB4B49FF;
alter table TASTY_TAGS drop constraint FK74477F4DA9042904;
alter table TASTY_USERS drop constraint FK14BEA93470CE42E;
alter table TASTY_USERS drop constraint FK14BEA934A9042904;
alter table TASTY_USER_ITEMS drop constraint FK6650E60AFADF2B0;
alter table TASTY_USER_ITEMS drop constraint FK6650E6036904DB0;
alter table TASTY_USER_ITEMS drop constraint FK6650E60918555F4;
alter table TASTY_USER_ITEMS drop constraint FK6650E60917FE53C;
alter table TASTY_USER_ITEM_TAGS drop constraint FK17472485AFADF2B0;
alter table TASTY_USER_ITEM_TAGS drop constraint FK1747248536904DB0;
alter table TASTY_USER_ITEM_TAGS drop constraint FK17472485558D87E4;
alter table TASTY_USER_TAGS drop constraint FKD6EF3559AFADF2B0;
alter table TASTY_USER_TAGS drop constraint FKD6EF355981C6CED8;
alter table TASTY_USER_TAGS drop constraint FKD6EF3559B0259F75;
alter table TASTY_USER_TAGS drop constraint FKD6EF3559558D87E4;
alter table UCM_COURSE_WORKSITE_AFFILS drop constraint FK27FF1C12F6DA3530;
alter table UCM_COURSE_WORKSITE_AFFILS drop constraint FK27FF1C12C0468FD0;
alter table UCM_PARTICIPANTS drop constraint FKF04F6860F6DA3530;
alter table UCM_PARTICIPANTS drop constraint FKF04F68609383750;
alter table UCM_WORKSITES drop constraint FK8D3744BBBD2267B0;
alter table UNITS drop constraint FK4D25F4FF856EB76;
alter table UNIT_MATERIAL_ASSOCS drop constraint FK6ECC085BB78970AA;
alter table UNIT_MATERIAL_ASSOCS drop constraint FK6ECC085B491771CA;
drop table ANNOTATIONS;
drop table ANSWERS;
drop table ASSIGNMENTS;
drop table ASSIGNMENT_MATERIAL_ASSOCS;
drop table ASSIGNMENT_RESPONSES;
drop table COMMENTS;
drop table CUSTOM_FIELDS;
drop table CUSTOM_FIELD_VALUES;
drop table MATERIALS;
drop table PARTICIPANTS;
drop table QUESTIONS;
drop table QUESTION_MATERIAL_ASSOCS;
drop table TASTY_ITEMS;
drop table TASTY_ITEM_TAGS;
drop table TASTY_SERVICES;
drop table TASTY_TAGS;
drop table TASTY_USERS;
drop table TASTY_USER_ITEMS;
drop table TASTY_USER_ITEM_TAGS;
drop table TASTY_USER_TAGS;
drop table UCM_COURSES;
drop table UCM_COURSE_WORKSITE_AFFILS;
drop table UCM_PARTICIPANTS;
drop table UCM_TERMS;
drop table UCM_USERS;
drop table UCM_WORKSITES;
drop table UNITS;
drop table UNIT_MATERIAL_ASSOCS;
drop table USERS;
drop table WORKSITES;
drop sequence hibernate_sequence;
create table ANNOTATIONS (ANNOTATION_ID int8 not null, MATERIAL_ID int8, PARTICIPANT_ID int8, CLIP_BEGIN varchar(255), CLIP_END varchar(255), DATE_MODIFIED timestamp, text text, title varchar(255), type varchar(255), primary key (ANNOTATION_ID));
create table ANSWERS (ANSWER_ID int8 not null, ASSIGNMENT_RESPONSE_ID int8, QUESTION_ID int8, STATUS int4, text0 text, text1 text, text2 text, text3 text, text4 text, text5 text, text6 text, text7 text, text8 text, text9 text, text10 text, text11 text, text12 text, text13 text, text14 text, text15 text, primary key (ANSWER_ID), unique (ASSIGNMENT_RESPONSE_ID, QUESTION_ID));
create table ASSIGNMENTS (ASSIGNMENT_ID int8 not null, UNIT_ID int8, CUSTOM_TYPE varchar(255), DATE_DUE timestamp, instructions text, ORDINAL_VALUE int4, title varchar(255), type varchar(255), primary key (ASSIGNMENT_ID));
create table ASSIGNMENT_MATERIAL_ASSOCS (ASSIGNMENT_MATERIAL_ASSOC_ID int8 not null, MATERIAL_ID int8, ASSIGNMENT_ID int8, primary key (ASSIGNMENT_MATERIAL_ASSOC_ID));
create table ASSIGNMENT_RESPONSES (ASSIGNMENT_RESPONSE_ID int8 not null, ASSIGNMENT_ID int8, PARTICIPANT_ID int8, DATE_SUBMITTED timestamp, STATUS int4, text0 text, text1 text, text2 text, text3 text, text4 text, text5 text, text6 text, text7 text, text8 text, text9 text, text10 text, text11 text, text12 text, text13 text, text14 text, text15 text, primary key (ASSIGNMENT_RESPONSE_ID), unique (ASSIGNMENT_ID, PARTICIPANT_ID));
create table COMMENTS (COMMENT_ID int8 not null, ANSWER_ID int8, ASSIGNMENT_RESPONSE_ID int8, PARTICIPANT_ID int8, DATE_MODIFIED timestamp, STATUS int4, text text, type varchar(255), primary key (COMMENT_ID));
create table CUSTOM_FIELDS (CUSTOM_FIELD_ID int8 not null, WORKSITE_ID int8, name varchar(255), ORDINAL_VALUE int4, visibility int4, primary key (CUSTOM_FIELD_ID));
create table CUSTOM_FIELD_VALUES (CUSTOM_FIELD_VALUE_ID int8 not null, CUSTOM_FIELD_ID int8, MATERIAL_ID int8, ORDINAL_VALUE int4, value varchar(255), primary key (CUSTOM_FIELD_VALUE_ID));
create table MATERIALS (MATERIAL_ID int8 not null, WORKSITE_ID int8, ACCESS_LEVEL int4, DATE_MODIFIED timestamp, text text, THUMB_URL varchar(255), title varchar(255), type varchar(255), url varchar(255), primary key (MATERIAL_ID));
create table PARTICIPANTS (PARTICIPANT_ID int8 not null, PARTICIPANT_ID_STRING varchar(255), ACCESS_LEVEL int4, primary key (PARTICIPANT_ID));
create table QUESTIONS (QUESTION_ID int8 not null, ASSIGNMENT_ID int8, ORDINAL_VALUE int4, text text, primary key (QUESTION_ID));
create table QUESTION_MATERIAL_ASSOCS (QUESTION_MATERIAL_ASSOC_ID int8 not null, QUESTION_ID int8, MATERIAL_ID int8, primary key (QUESTION_MATERIAL_ASSOC_ID));
create table TASTY_ITEMS (ITEM_ID int8 not null, name varchar(255), SERVICE_ID int8, primary key (ITEM_ID));
create table TASTY_ITEM_TAGS (ITEM_TAG_ID int8 not null, ITEM_ID int8, TAG_ID int8, primary key (ITEM_TAG_ID), unique (ITEM_ID, TAG_ID));
create table TASTY_SERVICES (SERVICE_ID int8 not null, name varchar(255), primary key (SERVICE_ID));
create table TASTY_TAGS (TAG_ID int8 not null, name varchar(255), SERVICE_ID int8, primary key (TAG_ID));
create table TASTY_USERS (USER_ID int8 not null, name varchar(255), SERVICE_ID int8, primary key (USER_ID));
create table TASTY_USER_ITEMS (USER_ITEM_ID int8 not null, USER_ID int8, ITEM_ID int8, primary key (USER_ITEM_ID), unique (USER_ID, ITEM_ID));
create table TASTY_USER_ITEM_TAGS (USER_ITEM_TAG_ID int8 not null, USER_ID int8, ITEM_ID int8, TAG_ID int8, primary key (USER_ITEM_TAG_ID), unique (USER_ID, ITEM_ID, TAG_ID));
create table TASTY_USER_TAGS (USER_TAG_ID int8 not null, USER_ID int8, TAG_ID int8, primary key (USER_TAG_ID), unique (USER_ID, TAG_ID));
create table UCM_COURSES (UCM_COURSE_ID int8 not null, COURSE_ID_STRING varchar(255), primary key (UCM_COURSE_ID));
create table UCM_COURSE_WORKSITE_AFFILS (UCM_COURSE_WORKSITE_AFFIL_ID int8 not null, UCM_COURSE_ID int8, UCM_WORKSITE_ID int8, primary key (UCM_COURSE_WORKSITE_AFFIL_ID));
create table UCM_PARTICIPANTS (UCM_PARTICIPANT_ID int8 not null, UCM_USER_ID int8, UCM_WORKSITE_ID int8, PARTICIPANT_ID_STRING varchar(255), primary key (UCM_PARTICIPANT_ID));
create table UCM_TERMS (UCM_TERM_ID int8 not null, END_DATE timestamp, name varchar(255), START_DATE timestamp, primary key (UCM_TERM_ID));
create table UCM_USERS (UCM_USER_ID int8 not null, USER_ID_STRING varchar(255), email varchar(255), FIRST_NAME varchar(255), LAST_NAME varchar(255), primary key (UCM_USER_ID));
create table UCM_WORKSITES (UCM_WORKSITE_ID int8 not null, UCM_TERM_ID int8, WORKSITE_ID_STRING varchar(255), title varchar(255), primary key (UCM_WORKSITE_ID));
create table UNITS (UNIT_ID int8 not null, WORKSITE_ID int8, title varchar(255), description text, START_DATE timestamp, END_DATE timestamp, VISIBILITY int4, primary key (UNIT_ID));
create table UNIT_MATERIAL_ASSOCS (UNIT_MATERIAL_ASSOC_ID int8 not null, MATERIAL_ID int8, UNIT_ID int8, primary key (UNIT_MATERIAL_ASSOC_ID));
create table USERS (USER_ID int8 not null, USER_ID_STRING varchar(255), AUTH_METHOD varchar(255), ACCESS_LEVEL int4, password varchar(255), primary key (USER_ID));
create table WORKSITES (WORKSITE_ID int8 not null, WORKSITE_ID_STRING varchar(255), announcement text, primary key (WORKSITE_ID));
alter table ANNOTATIONS add constraint FK47010F047A0F4E9E foreign key (PARTICIPANT_ID) references PARTICIPANTS;
alter table ANNOTATIONS add constraint FK47010F04491771CA foreign key (MATERIAL_ID) references MATERIALS;
alter table ANSWERS add constraint FKF8494455EBCBF05 foreign key (ASSIGNMENT_RESPONSE_ID) references ASSIGNMENT_RESPONSES;
alter table ANSWERS add constraint FKF8494455C3F6856A foreign key (QUESTION_ID) references QUESTIONS;
alter table ASSIGNMENTS add constraint FKE898E726B78970AA foreign key (UNIT_ID) references UNITS;
alter table ASSIGNMENT_MATERIAL_ASSOCS add constraint FK23C7F7A4645FAC0A foreign key (ASSIGNMENT_ID) references ASSIGNMENTS;
alter table ASSIGNMENT_MATERIAL_ASSOCS add constraint FK23C7F7A4491771CA foreign key (MATERIAL_ID) references MATERIALS;
alter table ASSIGNMENT_RESPONSES add constraint FK6F169540645FAC0A foreign key (ASSIGNMENT_ID) references ASSIGNMENTS;
alter table ASSIGNMENT_RESPONSES add constraint FK6F1695407A0F4E9E foreign key (PARTICIPANT_ID) references PARTICIPANTS;
alter table COMMENTS add constraint FKABDCDF4EBCBF05 foreign key (ASSIGNMENT_RESPONSE_ID) references ASSIGNMENT_RESPONSES;
alter table COMMENTS add constraint FKABDCDF459F0726A foreign key (ANSWER_ID) references ANSWERS;
alter table COMMENTS add constraint FKABDCDF47A0F4E9E foreign key (PARTICIPANT_ID) references PARTICIPANTS;
alter table CUSTOM_FIELDS add constraint FK8566DF47F856EB76 foreign key (WORKSITE_ID) references WORKSITES;
alter table CUSTOM_FIELD_VALUES add constraint FKABA1663534907427 foreign key (CUSTOM_FIELD_ID) references CUSTOM_FIELDS;
alter table CUSTOM_FIELD_VALUES add constraint FKABA16635491771CA foreign key (MATERIAL_ID) references MATERIALS;
alter table MATERIALS add constraint FKCEB1500CF856EB76 foreign key (WORKSITE_ID) references WORKSITES;
alter table QUESTIONS add constraint FK3BDD512D645FAC0A foreign key (ASSIGNMENT_ID) references ASSIGNMENTS;
alter table QUESTION_MATERIAL_ASSOCS add constraint FKF0DF5EBD491771CA foreign key (MATERIAL_ID) references MATERIALS;
alter table QUESTION_MATERIAL_ASSOCS add constraint FKF0DF5EBDC3F6856A foreign key (QUESTION_ID) references QUESTIONS;
alter table TASTY_ITEMS add constraint FK141602EC8DF4AFE6 foreign key (ITEM_ID) references TASTY_SERVICES;
alter table TASTY_ITEMS add constraint FK141602ECA9042904 foreign key (SERVICE_ID) references TASTY_SERVICES;
alter table TASTY_ITEM_TAGS add constraint FK4312F31173F1FE90 foreign key (ITEM_TAG_ID) references TASTY_TAGS;
alter table TASTY_ITEM_TAGS add constraint FK4312F311A24B5E75 foreign key (ITEM_TAG_ID) references TASTY_ITEMS;
alter table TASTY_ITEM_TAGS add constraint FK4312F31136904DB0 foreign key (ITEM_ID) references TASTY_ITEMS;
alter table TASTY_ITEM_TAGS add constraint FK4312F311558D87E4 foreign key (TAG_ID) references TASTY_TAGS;
alter table TASTY_TAGS add constraint FK74477F4DDB4B49FF foreign key (TAG_ID) references TASTY_SERVICES;
alter table TASTY_TAGS add constraint FK74477F4DA9042904 foreign key (SERVICE_ID) references TASTY_SERVICES;
alter table TASTY_USERS add constraint FK14BEA93470CE42E foreign key (USER_ID) references TASTY_SERVICES;
alter table TASTY_USERS add constraint FK14BEA934A9042904 foreign key (SERVICE_ID) references TASTY_SERVICES;
alter table TASTY_USER_ITEMS add constraint FK6650E60AFADF2B0 foreign key (USER_ID) references TASTY_USERS;
alter table TASTY_USER_ITEMS add constraint FK6650E6036904DB0 foreign key (ITEM_ID) references TASTY_ITEMS;
alter table TASTY_USER_ITEMS add constraint FK6650E60918555F4 foreign key (USER_ITEM_ID) references TASTY_USERS;
alter table TASTY_USER_ITEMS add constraint FK6650E60917FE53C foreign key (USER_ITEM_ID) references TASTY_ITEMS;
alter table TASTY_USER_ITEM_TAGS add constraint FK17472485AFADF2B0 foreign key (USER_ID) references TASTY_USERS;
alter table TASTY_USER_ITEM_TAGS add constraint FK1747248536904DB0 foreign key (ITEM_ID) references TASTY_ITEMS;
alter table TASTY_USER_ITEM_TAGS add constraint FK17472485558D87E4 foreign key (TAG_ID) references TASTY_TAGS;
alter table TASTY_USER_TAGS add constraint FKD6EF3559AFADF2B0 foreign key (USER_ID) references TASTY_USERS;
alter table TASTY_USER_TAGS add constraint FKD6EF355981C6CED8 foreign key (USER_TAG_ID) references TASTY_TAGS;
alter table TASTY_USER_TAGS add constraint FKD6EF3559B0259F75 foreign key (USER_TAG_ID) references TASTY_USERS;
alter table TASTY_USER_TAGS add constraint FKD6EF3559558D87E4 foreign key (TAG_ID) references TASTY_TAGS;
alter table UCM_COURSE_WORKSITE_AFFILS add constraint FK27FF1C12F6DA3530 foreign key (UCM_WORKSITE_ID) references UCM_WORKSITES;
alter table UCM_COURSE_WORKSITE_AFFILS add constraint FK27FF1C12C0468FD0 foreign key (UCM_COURSE_ID) references UCM_COURSES;
alter table UCM_PARTICIPANTS add constraint FKF04F6860F6DA3530 foreign key (UCM_WORKSITE_ID) references UCM_WORKSITES;
alter table UCM_PARTICIPANTS add constraint FKF04F68609383750 foreign key (UCM_USER_ID) references UCM_USERS;
alter table UCM_WORKSITES add constraint FK8D3744BBBD2267B0 foreign key (UCM_TERM_ID) references UCM_TERMS;
alter table UNITS add constraint FK4D25F4FF856EB76 foreign key (WORKSITE_ID) references WORKSITES;
alter table UNIT_MATERIAL_ASSOCS add constraint FK6ECC085BB78970AA foreign key (UNIT_ID) references UNITS;
alter table UNIT_MATERIAL_ASSOCS add constraint FK6ECC085B491771CA foreign key (MATERIAL_ID) references MATERIALS;
create sequence hibernate_sequence;
